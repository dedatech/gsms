package com.gsms.gsms.service.storage;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * RustFS 文件存储服务实现（基于 S3 协议）
 */
@Service("rustfsStorageProvider")
public class RustFSStorageProvider implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(RustFSStorageProvider.class);

    @Value("${attachment.storage.rustfs.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${attachment.storage.rustfs.access-key:minioadmin}")
    private String accessKey;

    @Value("${attachment.storage.rustfs.secret-key:minioadmin}")
    private String secretKey;

    @Value("${attachment.storage.rustfs.bucket-name:gsms-attachments}")
    private String bucketName;

    @Value("${attachment.storage.rustfs.region:us-east-1}")
    private String region;

    @Value("${attachment.storage.rustfs.public-url:}")
    private String publicUrl;

    @Value("${attachment.storage.rustfs.presigned-url-expire-seconds:3600}")
    private int presignedUrlExpireSeconds;

    @Value("${attachment.storage.rustfs.use-presigned-url:false}")
    private boolean usePresignedUrl;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 初始化 RustFS S3 客户端
     */
    @PostConstruct
    public void init() {
        try {
            // 创建 S3 客户端（RustFS 使用 S3 兼容协议）
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .forcePathStyle(true)  // RustFS 必须使用 path style
                    .build();

            // 创建预签名 URL 生成器
            s3Presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    .build();

            // 检查并创建存储桶
            ensureBucketExists();

            logger.info("RustFS 存储服务初始化成功: endpoint={}, bucket={}", endpoint, bucketName);
        } catch (Exception e) {
            logger.error("RustFS 存储服务初始化失败", e);
            throw new RuntimeException("RustFS 存储服务初始化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists() {
        try {
            // 检查存储桶是否存在
            boolean bucketExists = s3Client.listBuckets().buckets().stream()
                    .anyMatch(bucket -> bucket.name().equals(bucketName));

            if (!bucketExists) {
                // 创建存储桶
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                s3Client.createBucket(createBucketRequest);
                logger.info("创建 RustFS 存储桶成功: {}", bucketName);
            } else {
                logger.info("RustFS 存储桶已存在: {}", bucketName);
            }
        } catch (Exception e) {
            logger.error("检查/创建存储桶失败: {}", bucketName, e);
            throw new RuntimeException("检查/创建存储桶失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String upload(MultipartFile file, String targetPath) {
        try {
            // 如果指定了目标路径，直接使用；否则生成日期路径
            String objectKey = targetPath != null && !targetPath.isEmpty()
                    ? targetPath
                    : generateDatePath() + "/" + generateUniqueFileName(file.getOriginalFilename());

            // 上传文件到 RustFS
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            logger.info("文件上传到 RustFS 成功: bucket={}, key={}, size={}", bucketName, objectKey, file.getSize());
            return objectKey;

        } catch (IOException e) {
            logger.error("文件上传到 RustFS 失败", e);
            throw new RuntimeException("文件上传到 RustFS 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info("文件从 RustFS 删除成功: bucket={}, key={}", bucketName, filePath);
            return true;

        } catch (Exception e) {
            logger.error("文件从 RustFS 删除失败: bucket={}, key={}", bucketName, filePath, e);
            return false;
        }
    }

    @Override
    public InputStream getInputStream(String filePath) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            return s3Client.getObject(getObjectRequest);

        } catch (Exception e) {
            logger.error("从 RustFS 获取文件流失败: bucket={}, key={}", bucketName, filePath, e);
            throw new RuntimeException("从 RustFS 获取文件流失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String filePath) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filePath)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("检查文件存在性失败: bucket={}, key={}", bucketName, filePath, e);
            return false;
        }
    }

    @Override
    public String getUrl(String filePath) {
        // 如果配置了公网 URL，直接拼接
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + bucketName + "/" + filePath;
        }

        // 如果使用预签名 URL
        if (usePresignedUrl) {
            return generatePresignedUrl(filePath);
        }

        // 默认返回路径样式 URL
        return endpoint + "/" + bucketName + "/" + filePath;
    }

    /**
     * 生成预签名 URL（用于临时访问）
     * 注意：简化实现，暂时返回普通 URL
     * TODO: 实现真正的预签名 URL 功能
     */
    private String generatePresignedUrl(String filePath) {
        // 暂时返回普通 URL
        logger.debug("预签名 URL 功能暂未实现，使用普通 URL: bucket={}, key={}", bucketName, filePath);
        return endpoint + "/" + bucketName + "/" + filePath;
    }

    /**
     * 生成预签名上传 URL（用于前端直传）
     * 注意：当前版本暂不支持预签名 URL
     */
    public String generatePresignedUploadUrl(String filePath) {
        throw new UnsupportedOperationException("预签名上传 URL 功能暂不支持，请使用普通上传方式");
    }

    /**
     * 生成日期路径 (YYYY/MM/DD)
     */
    private String generateDatePath() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 生成唯一文件名 (UUID + 原始扩展名)
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String baseName = FilenameUtils.getBaseName(originalFilename);
        // 保留原始文件名的前20个字符，加上UUID，确保唯一性
        String shortBaseName = baseName.length() > 20 ? baseName.substring(0, 20) : baseName;
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return shortBaseName + "_" + uuid + "." + extension;
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        try {
            if (s3Client != null) {
                s3Client.close();
            }
            if (s3Presigner != null) {
                s3Presigner.close();
            }
            logger.info("RustFS 存储服务资源释放成功");
        } catch (Exception e) {
            logger.error("RustFS 存储服务资源释放失败", e);
        }
    }

    /**
     * 获取 S3 客户端（用于迁移等特殊场景）
     */
    public S3Client getS3Client() {
        return s3Client;
    }

    /**
     * 获取存储桶名称
     */
    public String getBucketName() {
        return bucketName;
    }
}
