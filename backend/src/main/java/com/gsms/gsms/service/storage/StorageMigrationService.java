package com.gsms.gsms.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gsms.gsms.model.entity.Attachment;
import com.gsms.gsms.repository.AttachmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 存储迁移服务：将本地存储的附件迁移到 RustFS
 */
@Service
public class StorageMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(StorageMigrationService.class);

    @Autowired
    private AttachmentMapper attachmentMapper;

    @Autowired
    private RustFSStorageProvider rustfsStorageProvider;

    @Value("${attachment.storage.local.upload-dir:./uploads}")
    private String localUploadDir;

    /**
     * 迁移所有本地附件到 RustFS
     *
     * @return 迁移结果统计
     */
    public MigrationResult migrateAllToRustFS() {
        MigrationResult result = new MigrationResult();

        try {
            // 获取所有本地存储的附件
            List<Attachment> localAttachments = attachmentMapper.selectList(
                    Wrappers.<Attachment>lambdaQuery()
                            .eq(Attachment::getStorageType, "local")
            );

            logger.info("开始迁移附件到 RustFS，共 {} 个文件", localAttachments.size());
            result.setTotalCount(localAttachments.size());

            Path uploadPath = Paths.get(localUploadDir).toAbsolutePath();

            for (Attachment attachment : localAttachments) {
                try {
                    // 检查本地文件是否存在
                    Path localFilePath = uploadPath.resolve(attachment.getFilePath());
                    if (!Files.exists(localFilePath)) {
                        logger.warn("本地文件不存在，跳过: {}", localFilePath);
                        result.addSkipped();
                        continue;
                    }

                    // 读取本地文件
                    byte[] fileBytes = Files.readAllBytes(localFilePath);

                    // 上传到 RustFS
                    S3Client s3Client = rustfsStorageProvider.getS3Client();
                    String bucketName = rustfsStorageProvider.getBucketName();

                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(attachment.getFilePath())
                            .contentType(attachment.getMimeType())
                            .contentLength((long) fileBytes.length)
                            .build();

                    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

                    // 更新附件记录的存储类型
                    attachment.setStorageType("rustfs");
                    attachment.setUpdateTime(LocalDateTime.now());
                    attachmentMapper.updateById(attachment);

                    logger.info("附件迁移成功: id={}, file={}", attachment.getId(), attachment.getFileName());
                    result.addSuccess();

                } catch (Exception e) {
                    logger.error("附件迁移失败: id={}, file={}", attachment.getId(), attachment.getFileName(), e);
                    result.addFailure(attachment.getId(), attachment.getFileName(), e.getMessage());
                }
            }

            logger.info("附件迁移完成: 成功={}, 失败={}, 跳过={}",
                    result.getSuccessCount(), result.getFailureCount(), result.getSkippedCount());

        } catch (Exception e) {
            logger.error("附件迁移过程中发生异常", e);
            throw new RuntimeException("附件迁移失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 迁移结果统计
     */
    public static class MigrationResult {
        private int totalCount;
        private int successCount;
        private int failureCount;
        private int skippedCount;
        private List<FailureItem> failures;

        public MigrationResult() {
            this.totalCount = 0;
            this.successCount = 0;
            this.failureCount = 0;
            this.skippedCount = 0;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public void addSuccess() {
            this.successCount++;
        }

        public void addFailure(Long id, String fileName, String errorMessage) {
            this.failureCount++;
            if (this.failures != null) {
                this.failures.add(new FailureItem(id, fileName, errorMessage));
            }
        }

        public void addSkipped() {
            this.skippedCount++;
        }

        // Getters
        public int getTotalCount() { return totalCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public int getSkippedCount() { return skippedCount; }
        public List<FailureItem> getFailures() { return failures; }

        /**
         * 失败项详情
         */
        public static class FailureItem {
            private Long id;
            private String fileName;
            private String errorMessage;

            public FailureItem(Long id, String fileName, String errorMessage) {
                this.id = id;
                this.fileName = fileName;
                this.errorMessage = errorMessage;
            }

            // Getters
            public Long getId() { return id; }
            public String getFileName() { return fileName; }
            public String getErrorMessage() { return errorMessage; }
        }
    }
}
