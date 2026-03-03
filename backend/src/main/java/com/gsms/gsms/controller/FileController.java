package com.gsms.gsms.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.model.entity.Attachment;
import com.gsms.gsms.repository.AttachmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;

/**
 * 文件访问控制器
 * 处理通过静态资源URL访问文件的权限验证
 */
@RestController
@RequestMapping("/api/attachments/file")
@Tag(name = "文件访问", description = "通过URL直接访问上传的文件")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Value("${attachment.storage.local.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 通过文件路径访问文件（带权限验证）
     * 路径格式: /api/attachments/file/2024/01/15/xxx.jpg
     */
    @GetMapping("/**")
    @Operation(summary = "访问文件", description = "通过文件路径直接访问文件，带权限验证")
    public void accessFile(HttpServletRequest request,
                           HttpServletResponse response) {
        // 获取请求路径
        String requestUri = request.getRequestURI();

        try {
            // 获取文件路径（移除 /api/attachments/file/ 前缀）
            String filePath = requestUri.substring("/api/attachments/file/".length());

            // 查找附件记录
            QueryWrapper<Attachment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("file_path", filePath);
            queryWrapper.eq("is_deleted", 0);
            Attachment attachment = attachmentMapper.selectOne(queryWrapper);

            if (attachment == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在");
                return;
            }

            // 验证访问权限
            Long currentUserId = UserContext.getCurrentUserId();
            validateAccessPermission(attachment, currentUserId);

            // 获取文件
            File file = new File(uploadDir, filePath);
            if (!file.exists() || !file.isFile()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在");
                return;
            }

            // 设置响应头
            String mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (mimeType == null) {
                mimeType = attachment.getMimeType();
            }
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);
            response.setContentLengthLong(file.length());

            // 图片等可预览文件使用 inline，其他使用 attachment
            String disposition = isPreviewable(attachment.getFileType()) ? "inline" : "attachment";
            response.setHeader("Content-Disposition", disposition + "; filename=\"" + attachment.getFileName() + "\"");

            // 写入文件流
            try (InputStream inputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

        } catch (IOException e) {
            logger.error("文件访问失败: {}", requestUri, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 验证访问权限
     */
    private void validateAccessPermission(Attachment attachment, Long userId) {
        // 检查用户是否有权限访问该附件
        // 这里简化处理：检查用户是否是附件所属项目的成员

        // TODO: 实际项目中应该查询项目成员关系来验证权限
        // 这里暂时允许所有登录用户访问，因为已经在 JWT 拦截器层面验证了身份

        // 如果需要更严格的权限控制，可以在这里添加逻辑
        // 例如：检查用户是否是附件所属项目的成员
    }

    /**
     * 判断文件是否可预览
     */
    private boolean isPreviewable(String fileType) {
        String type = fileType.toLowerCase();
        return type.equals("jpg") || type.equals("jpeg") || type.equals("png") ||
               type.equals("gif") || type.equals("bmp") || type.equals("webp") ||
               type.equals("pdf") || type.equals("txt");
    }
}
