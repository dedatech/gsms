package com.gsms.gsms.controller;

import com.gsms.gsms.dto.attachment.AttachmentRenameReq;
import com.gsms.gsms.dto.attachment.AttachmentUploadReq;
import com.gsms.gsms.dto.attachment.AttachmentInfoResp;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 附件管理控制器
 * @author MagicBook
 */
@RestController
@RequestMapping("/api/attachments")
@Tag(name = "附件管理", description = "附件上传、下载、预览等接口")
public class AttachmentController {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);

    @Autowired
    private AttachmentService attachmentService;

    @PostMapping("/upload")
    @Operation(summary = "上传附件", description = "上传附件到指定关联对象（项目/任务），只有创建者和项目经理可以上传")
    public Result<AttachmentInfoResp> upload(
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Long targetId,
            @RequestParam("file") MultipartFile file) {

        AttachmentUploadReq req = new AttachmentUploadReq();
        req.setTargetType(targetType);
        req.setTargetId(targetId);
        req.setFile(file);

        Long currentUserId = UserContext.getCurrentUserId();
        AttachmentInfoResp resp = attachmentService.upload(req, currentUserId);

        return Result.success(resp);
    }

    @GetMapping("/list")
    @Operation(summary = "获取附件列表", description = "根据关联对象类型和ID获取附件列表")
    public Result<List<AttachmentInfoResp>> listByTarget(
            @Parameter(description = "关联对象类型: project/task") @RequestParam String targetType,
            @Parameter(description = "关联对象ID") @RequestParam Long targetId) {

        Long currentUserId = UserContext.getCurrentUserId();
        List<AttachmentInfoResp> list = attachmentService.listByTarget(targetType, targetId, currentUserId);

        return Result.success(list);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "获取项目的所有附件", description = "获取指定项目的所有附件，包括项目附件和任务附件")
    public Result<List<AttachmentInfoResp>> listByProject(
            @Parameter(description = "项目ID") @PathVariable Long projectId) {

        Long currentUserId = UserContext.getCurrentUserId();
        List<AttachmentInfoResp> list = attachmentService.listByProject(projectId, currentUserId);

        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取附件详情", description = "根据附件ID获取详细信息")
    public Result<AttachmentInfoResp> getDetail(
            @Parameter(description = "附件ID") @PathVariable Long id) {

        Long currentUserId = UserContext.getCurrentUserId();
        AttachmentInfoResp resp = attachmentService.getDetail(id, currentUserId);

        return Result.success(resp);
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载附件", description = "下载指定附件")
    public void download(
            @Parameter(description = "附件ID") @PathVariable Long id,
            HttpServletResponse response) {

        Long currentUserId = UserContext.getCurrentUserId();
        attachmentService.download(id, currentUserId, response);
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "在线预览附件", description = "在线预览指定附件（支持图片、PDF、Office文档等）")
    public void preview(
            @Parameter(description = "附件ID") @PathVariable Long id,
            HttpServletResponse response) {

        Long currentUserId = UserContext.getCurrentUserId();
        attachmentService.preview(id, currentUserId, response);
    }

    @PutMapping("/rename")
    @Operation(summary = "重命名附件", description = "修改附件的显示名称（只有上传者可以修改）")
    public Result<Void> rename(@Valid @RequestBody AttachmentRenameReq req) {
        Long currentUserId = UserContext.getCurrentUserId();
        attachmentService.rename(req, currentUserId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除附件", description = "删除指定附件（只有上传者可以删除）")
    public Result<Void> delete(
            @Parameter(description = "附件ID") @PathVariable Long id) {

        Long currentUserId = UserContext.getCurrentUserId();
        attachmentService.delete(id, currentUserId);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除附件", description = "批量删除多个附件")
    public Result<Void> batchDelete(
            @Parameter(description = "附件ID列表") @RequestBody List<Long> ids) {

        Long currentUserId = UserContext.getCurrentUserId();
        attachmentService.batchDelete(ids, currentUserId);
        return Result.success();
    }
}
