package com.gsms.gsms.service.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

/**
 * 存储服务接口
 */
public interface StorageService {

    /**
     * 上传文件
     * @param file 文件
     * @param targetPath 目标路径（相对路径）
     * @return 存储的文件路径
     */
    String upload(MultipartFile file, String targetPath);

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean delete(String filePath);

    /**
     * 获取文件输入流
     * @param filePath 文件路径
     * @return 文件输入流
     */
    InputStream getInputStream(String filePath);

    /**
     * 判断文件是否存在
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean exists(String filePath);

    /**
     * 获取文件访问URL
     * @param filePath 文件路径
     * @return 访问URL
     */
    String getUrl(String filePath);
}
