package com.gsms.gsms.dto.attachment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 附件信息响应
 */
@Schema(description = "附件信息响应")
public class AttachmentInfoResp {

    @Schema(description = "附件ID")
    private Long id;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "显示名称")
    private String displayName;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "格式化文件大小")
    private String fileSizeFormatted;

    @Schema(description = "文件类型/扩展名")
    private String fileType;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "存储类型")
    private String storageType;

    @Schema(description = "关联对象类型")
    private String targetType;

    @Schema(description = "关联对象ID")
    private Long targetId;

    @Schema(description = "上传者ID")
    private Long uploaderId;

    @Schema(description = "上传者名称")
    private String uploaderName;

    @Schema(description = "访问URL")
    private String url;

    @Schema(description = "是否可以在线预览")
    private Boolean canPreview;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "关联任务ID")
    private Long taskId;

    @Schema(description = "关联任务编号")
    private String taskNumber;

    @Schema(description = "关联任务标题")
    private String taskTitle;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeFormatted() {
        return fileSizeFormatted;
    }

    public void setFileSizeFormatted(String fileSizeFormatted) {
        this.fileSizeFormatted = fileSizeFormatted;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getCanPreview() {
        return canPreview;
    }

    public void setCanPreview(Boolean canPreview) {
        this.canPreview = canPreview;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }
}
