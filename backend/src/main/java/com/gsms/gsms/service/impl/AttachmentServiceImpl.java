package com.gsms.gsms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gsms.gsms.dto.attachment.AttachmentRenameReq;
import com.gsms.gsms.dto.attachment.AttachmentUploadReq;
import com.gsms.gsms.dto.attachment.AttachmentInfoResp;
import com.gsms.gsms.dto.task.TaskQueryReq;
import com.gsms.gsms.dto.task.TaskInfoResp;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.model.entity.Attachment;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.entity.ProjectMember;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.ProjectMemberRole;
import com.gsms.gsms.model.enums.TargetType;
import com.gsms.gsms.model.enums.errorcode.AttachmentErrorCode;
import com.gsms.gsms.repository.AttachmentMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.TaskMapper;
import com.gsms.gsms.service.AttachmentService;
import com.gsms.gsms.service.TaskService;
import com.gsms.gsms.service.storage.StorageService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 附件服务实现
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    // 单文件最大10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 支持在线预览的文件类型
    private static final Set<String> PREVIEWABLE_TYPES = new HashSet<>();

    static {
        // 图片
        PREVIEWABLE_TYPES.add("jpg");
        PREVIEWABLE_TYPES.add("jpeg");
        PREVIEWABLE_TYPES.add("png");
        PREVIEWABLE_TYPES.add("gif");
        PREVIEWABLE_TYPES.add("bmp");
        PREVIEWABLE_TYPES.add("webp");
        // 文本
        PREVIEWABLE_TYPES.add("txt");
        PREVIEWABLE_TYPES.add("pdf");
        // Office文档（仅支持查看，需要前端处理）
        PREVIEWABLE_TYPES.add("doc");
        PREVIEWABLE_TYPES.add("docx");
        PREVIEWABLE_TYPES.add("xls");
        PREVIEWABLE_TYPES.add("xlsx");
        PREVIEWABLE_TYPES.add("ppt");
        PREVIEWABLE_TYPES.add("pptx");
    }

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StorageService storageService;  // 使用 @Primary 标注的动态存储服务

    @Autowired
    private TaskService taskService;

    @Value("${attachment.storage.type:local}")
    private String storageType;  // 当前存储类型配置

    @Override
    @Transactional
    public AttachmentInfoResp upload(AttachmentUploadReq req, Long currentUserId) {
        MultipartFile file = req.getFile();

        // 1. 参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_FILE_EMPTY);
        }

        // 2. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_FILE_SIZE_EXCEEDED);
        }

        // 3. 验证关联对象
        validateTargetAccess(req.getTargetType(), req.getTargetId(), currentUserId, true);

        // 4. 上传文件到存储
        String relativePath = storageService.upload(file, null);

        // 5. 保存附件记录
        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setDisplayName(file.getOriginalFilename());
        attachment.setFilePath(relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
        attachment.setMimeType(file.getContentType());
        attachment.setStorageType(storageType);  // 使用配置的存储类型
        attachment.setTargetType(req.getTargetType());
        attachment.setTargetId(req.getTargetId());
        attachment.setUploaderId(currentUserId);

        // 获取当前用户名称
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser != null) {
            attachment.setUploaderName(currentUser.getUsername());
        }

        attachment.setCreateTime(LocalDateTime.now());
        attachment.setUpdateTime(LocalDateTime.now());
        attachment.setIsDeleted(0);

        attachmentMapper.insert(attachment);

        logger.info("用户 {} 上传附件成功: {}", currentUserId, file.getOriginalFilename());

        // 6. 返回附件信息
        return convertToInfoResp(attachment);
    }

    @Override
    public List<AttachmentInfoResp> listByTarget(String targetType, Long targetId, Long currentUserId) {
        // 验证访问权限
        validateTargetAccess(targetType, targetId, currentUserId, false);

        List<Attachment> attachments = attachmentMapper.selectByTarget(targetType, targetId);
        List<AttachmentInfoResp> result = new ArrayList<>();

        for (Attachment attachment : attachments) {
            result.add(convertToInfoResp(attachment));
        }

        return result;
    }

    @Override
    public List<AttachmentInfoResp> listByProject(Long projectId, Long currentUserId) {
        // 验证用户是否是项目成员
        List<Long> projectIds = projectMemberMapper.selectProjectIdsByUserId(currentUserId);
        if (!projectIds.contains(projectId)) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NO_PERMISSION);
        }

        List<AttachmentInfoResp> result = new ArrayList<>();

        // 1. 获取直接关联到项目的附件
        List<Attachment> projectAttachments = attachmentMapper.selectByTarget("project", projectId);
        for (Attachment attachment : projectAttachments) {
            result.add(convertToInfoResp(attachment));
        }

        // 2. 获取该项目下所有任务的附件
        // 首先获取项目的所有任务ID
        TaskQueryReq taskQuery = new TaskQueryReq();
        taskQuery.setProjectId(projectId);
        taskQuery.setPageNum(1);
        taskQuery.setPageSize(Integer.MAX_VALUE); // 获取所有任务
        PageResult<TaskInfoResp> taskPage = taskService.findAll(taskQuery);
        List<TaskInfoResp> tasks = taskPage.getData();

        // 查询这些任务的附件
        for (TaskInfoResp task : tasks) {
            List<Attachment> taskAttachments = attachmentMapper.selectByTarget("task", task.getId());
            for (Attachment attachment : taskAttachments) {
                result.add(convertToInfoResp(attachment));
            }
        }

        return result;
    }

    @Override
    public AttachmentInfoResp getDetail(Long id, Long currentUserId) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null || attachment.getIsDeleted() == 1) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 验证访问权限
        validateTargetAccess(attachment.getTargetType(), attachment.getTargetId(), currentUserId, false);

        return convertToInfoResp(attachment);
    }

    @Override
    public void download(Long id, Long currentUserId, HttpServletResponse response) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null || attachment.getIsDeleted() == 1) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 验证访问权限
        validateTargetAccess(attachment.getTargetType(), attachment.getTargetId(), currentUserId, false);

        // 设置响应头
        response.setContentType(attachment.getMimeType());
        response.setContentLengthLong(attachment.getFileSize());
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + attachment.getFileName() + "\"");

        // 写入文件流
        try (InputStream inputStream = storageService.getInputStream(attachment.getFilePath());
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

        } catch (IOException e) {
            logger.error("附件下载失败: {}", attachment.getFileName(), e);
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_DOWNLOAD_FAILED);
        }
    }

    @Override
    public void preview(Long id, Long currentUserId, HttpServletResponse response) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null || attachment.getIsDeleted() == 1) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 验证访问权限
        validateTargetAccess(attachment.getTargetType(), attachment.getTargetId(), currentUserId, false);

        // 检查是否可以预览
        if (!canPreview(attachment.getFileType())) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_TYPE_INVALID);
        }

        // 设置响应头（inline 表示在浏览器中预览）
        response.setContentType(attachment.getMimeType());
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + attachment.getFileName() + "\"");

        // 写入文件流
        try (InputStream inputStream = storageService.getInputStream(attachment.getFilePath());
             OutputStream outputStream = response.getOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

        } catch (IOException e) {
            logger.error("附件预览失败: {}", attachment.getFileName(), e);
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_DOWNLOAD_FAILED);
        }
    }

    @Override
    @Transactional
    public void rename(AttachmentRenameReq req, Long currentUserId) {
        Attachment attachment = attachmentMapper.selectById(req.getId());
        if (attachment == null || attachment.getIsDeleted() == 1) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 只有上传者本人可以重命名
        if (!attachment.getUploaderId().equals(currentUserId)) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NO_PERMISSION);
        }

        // 更新显示名称
        attachment.setDisplayName(req.getDisplayName());
        attachment.setUpdateTime(LocalDateTime.now());

        attachmentMapper.updateById(attachment);

        logger.info("用户 {} 重命名附件 {} 为 {}", currentUserId, attachment.getFileName(), req.getDisplayName());
    }

    @Override
    @Transactional
    public void delete(Long id, Long currentUserId) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null || attachment.getIsDeleted() == 1) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NOT_FOUND);
        }

        // 只有上传者本人可以删除
        if (!attachment.getUploaderId().equals(currentUserId)) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NO_PERMISSION);
        }

        // 逻辑删除
        attachmentMapper.logicalDelete(id);

        // 删除物理文件
        storageService.delete(attachment.getFilePath());

        logger.info("用户 {} 删除附件: {}", currentUserId, attachment.getFileName());
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids, Long currentUserId) {
        for (Long id : ids) {
            try {
                delete(id, currentUserId);
            } catch (BusinessException e) {
                logger.warn("批量删除附件失败: id={}, error={}", id, e.getMessage());
            }
        }
    }

    /**
     * 验证用户对关联对象的访问权限
     * @param targetType 关联对象类型
     * @param targetId 关联对象ID
     * @param userId 用户ID
     * @param needUploadPermission 是否需要上传权限（创建者或项目经理）
     */
    private void validateTargetAccess(String targetType, Long targetId, Long userId, boolean needUploadPermission) {
        TargetType type = TargetType.fromCode(targetType);

        switch (type) {
            case PROJECT:
                validateProjectAccess(targetId, userId, needUploadPermission);
                break;
            case TASK:
                validateTaskAccess(targetId, userId, needUploadPermission);
                break;
            case REQUIREMENT:
                // TODO: 需求模块暂未实现
                throw new BusinessException(AttachmentErrorCode.ATTACHMENT_TARGET_INVALID);
            default:
                throw new BusinessException(AttachmentErrorCode.ATTACHMENT_TARGET_INVALID);
        }
    }

    /**
     * 验证项目访问权限
     */
    private void validateProjectAccess(Long projectId, Long userId, boolean needUploadPermission) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_TARGET_INVALID);
        }

        // 检查用户是否是项目成员
        List<Long> projectIds = projectMemberMapper.selectProjectIdsByUserId(userId);
        if (!projectIds.contains(projectId)) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NO_PERMISSION);
        }

        // 如果需要上传权限，检查是否是创建者或项目经理
        if (needUploadPermission) {
            if (!canUploadToProject(project, userId)) {
                throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NO_PERMISSION);
            }
        }
    }

    /**
     * 验证任务访问权限
     */
    private void validateTaskAccess(Long taskId, Long userId, boolean needUploadPermission) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_TARGET_INVALID);
        }

        // 检查用户是否是项目成员
        List<Long> projectIds = projectMemberMapper.selectProjectIdsByUserId(userId);
        if (!projectIds.contains(task.getProjectId())) {
            throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NO_PERMISSION);
        }

        // 如果需要上传权限，检查是否是创建者或项目经理
        if (needUploadPermission) {
            if (!canUploadToTask(task, userId)) {
                throw new BusinessException(AttachmentErrorCode.ATTACHMENT_NO_PERMISSION);
            }
        }
    }

    /**
     * 检查用户是否可以上传到项目（创建者或项目经理）
     */
    private boolean canUploadToProject(Project project, Long userId) {
        // 检查是否是创建者
        if (project.getCreateUserId().equals(userId)) {
            return true;
        }

        // 检查是否是项目经理
        List<ProjectMember> members = projectMemberMapper.selectMembersByProjectId(project.getId());
        for (ProjectMember member : members) {
            if (member.getUserId().equals(userId) &&
                member.getRoleType().equals(ProjectMemberRole.PROJECT_MANAGER.getCode())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查用户是否可以上传到任务（任务创建者或项目经理）
     */
    private boolean canUploadToTask(Task task, Long userId) {
        // 检查是否是任务创建者
        if (task.getCreateUserId().equals(userId)) {
            return true;
        }

        // 检查是否是项目经理
        List<ProjectMember> members = projectMemberMapper.selectMembersByProjectId(task.getProjectId());
        for (ProjectMember member : members) {
            if (member.getUserId().equals(userId) &&
                member.getRoleType().equals(ProjectMemberRole.PROJECT_MANAGER.getCode())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断文件是否可以在线预览
     */
    private boolean canPreview(String fileType) {
        return PREVIEWABLE_TYPES.contains(fileType.toLowerCase());
    }

    /**
     * 转换为响应DTO
     */
    private AttachmentInfoResp convertToInfoResp(Attachment attachment) {
        AttachmentInfoResp resp = new AttachmentInfoResp();
        resp.setId(attachment.getId());
        resp.setFileName(attachment.getFileName());
        resp.setDisplayName(attachment.getDisplayName());
        resp.setFileSize(attachment.getFileSize());
        resp.setFileSizeFormatted(formatFileSize(attachment.getFileSize()));
        resp.setFileType(attachment.getFileType());
        resp.setMimeType(attachment.getMimeType());
        resp.setStorageType(attachment.getStorageType());
        resp.setTargetType(attachment.getTargetType());
        resp.setTargetId(attachment.getTargetId());
        resp.setUploaderId(attachment.getUploaderId());
        resp.setUploaderName(attachment.getUploaderName());
        // 使用存储服务生成 URL（支持本地存储和 RustFS）
        resp.setUrl(storageService.getUrl(attachment.getFilePath()));
        resp.setCanPreview(canPreview(attachment.getFileType()));
        resp.setCreateTime(attachment.getCreateTime());

        // 如果附件关联的是任务，查询任务信息
        if ("task".equals(attachment.getTargetType())) {
            Task task = taskMapper.selectById(attachment.getTargetId());
            if (task != null && task.getIsDeleted() == 0) {
                resp.setTaskId(task.getId());
                resp.setTaskNumber("#" + task.getId());
                resp.setTaskTitle(task.getTitle());
            }
        }

        return resp;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size <= 0) {
            return "0 B";
        }

        final String[] units = {"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
