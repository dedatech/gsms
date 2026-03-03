package com.gsms.gsms.service;

import com.gsms.gsms.dto.attachment.AttachmentRenameReq;
import com.gsms.gsms.dto.attachment.AttachmentUploadReq;
import com.gsms.gsms.dto.attachment.AttachmentInfoResp;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 附件服务接口
 */
public interface AttachmentService {

    /**
     * 上传附件
     * @param req 上传请求
     * @param currentUserId 当前用户ID
     * @return 附件信息
     */
    AttachmentInfoResp upload(AttachmentUploadReq req, Long currentUserId);

    /**
     * 获取附件列表（根据关联对象）
     * @param targetType 关联对象类型
     * @param targetId 关联对象ID
     * @param currentUserId 当前用户ID
     * @return 附件列表
     */
    List<AttachmentInfoResp> listByTarget(String targetType, Long targetId, Long currentUserId);

    /**
     * 获取项目的所有附件（包括项目附件和任务附件）
     * @param projectId 项目ID
     * @param currentUserId 当前用户ID
     * @return 附件列表
     */
    List<AttachmentInfoResp> listByProject(Long projectId, Long currentUserId);

    /**
     * 获取附件详情
     * @param id 附件ID
     * @param currentUserId 当前用户ID
     * @return 附件信息
     */
    AttachmentInfoResp getDetail(Long id, Long currentUserId);

    /**
     * 下载附件
     * @param id 附件ID
     * @param currentUserId 当前用户ID
     * @param response HTTP响应
     */
    void download(Long id, Long currentUserId, HttpServletResponse response);

    /**
     * 在线预览附件
     * @param id 附件ID
     * @param currentUserId 当前用户ID
     * @param response HTTP响应
     */
    void preview(Long id, Long currentUserId, HttpServletResponse response);

    /**
     * 重命名附件
     * @param req 重命名请求
     * @param currentUserId 当前用户ID
     */
    void rename(AttachmentRenameReq req, Long currentUserId);

    /**
     * 删除附件
     * @param id 附件ID
     * @param currentUserId 当前用户ID
     */
    void delete(Long id, Long currentUserId);

    /**
     * 批量删除附件
     * @param ids 附件ID列表
     * @param currentUserId 当前用户ID
     */
    void batchDelete(List<Long> ids, Long currentUserId);
}
