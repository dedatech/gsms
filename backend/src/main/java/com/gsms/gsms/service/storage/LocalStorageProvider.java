package com.gsms.gsms.service.storage;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地文件存储服务实现
 */
@Service("localStorageProvider")
public class LocalStorageProvider implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageProvider.class);

    @Value("${attachment.storage.local.upload-dir:./uploads}")
    private String uploadDirConfig;

    @Value("${attachment.storage.local.url-prefix:/api/attachments/file}")
    private String urlPrefix;

    private Path uploadDirPath;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 初始化上传目录
     */
    @PostConstruct
    public void init() {
        // 将相对路径转换为绝对路径
        Path configuredPath = Paths.get(uploadDirConfig);
        if (configuredPath.isAbsolute()) {
            uploadDirPath = configuredPath;
        } else {
            // 相对路径：基于当前工作目录
            uploadDirPath = Paths.get(System.getProperty("user.dir")).resolve(uploadDirConfig).normalize();
        }

        // 创建上传目录
        try {
            Files.createDirectories(uploadDirPath);
            logger.info("文件上传目录初始化成功: {}", uploadDirPath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("创建上传目录失败: {}", uploadDirPath.toAbsolutePath(), e);
            throw new RuntimeException("创建上传目录失败", e);
        }
    }

    @Override
    public String upload(MultipartFile file, String targetPath) {
        try {
            // 如果指定了目标路径，直接使用；否则生成日期路径
            String relativePath = targetPath != null && !targetPath.isEmpty()
                ? targetPath
                : generateDatePath() + "/" + generateUniqueFileName(file.getOriginalFilename());

            Path fullPath = uploadDirPath.resolve(relativePath).normalize();

            // 确保父目录存在
            Files.createDirectories(fullPath.getParent());

            // 保存文件
            file.transferTo(fullPath.toFile());

            logger.info("文件上传成功: {}", fullPath);
            return relativePath;

        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String filePath) {
        try {
            Path fullPath = uploadDirPath.resolve(filePath).normalize();
            File file = fullPath.toFile();

            if (file.exists()) {
                boolean deleted = file.delete();
                logger.info("文件删除: {}, 结果: {}", fullPath, deleted ? "成功" : "失败");
                return deleted;
            }

            logger.warn("文件不存在: {}", fullPath);
            return false;

        } catch (Exception e) {
            logger.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }

    @Override
    public InputStream getInputStream(String filePath) {
        try {
            Path fullPath = uploadDirPath.resolve(filePath).normalize();
            return new FileInputStream(fullPath.toFile());
        } catch (IOException e) {
            logger.error("获取文件流失败: {}", filePath, e);
            throw new RuntimeException("获取文件流失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String filePath) {
        Path fullPath = uploadDirPath.resolve(filePath).normalize();
        return fullPath.toFile().exists();
    }

    @Override
    public String getUrl(String filePath) {
        return "/" + urlPrefix + "/" + filePath;
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
}
