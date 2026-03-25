# RustFS 附件迁移指南

## 概述

本文档描述如何将现有本地存储的附件迁移到 RustFS 对象存储系统。

## 迁移前准备

### 1. 确认 RustFS 服务已搭建

确保您的 RustFS 服务已正常运行并可以访问：

```bash
# 测试 RustFS 服务连接
curl http://your-rustfs-server:9000
```

### 2. 获取 RustFS 连接信息

准备以下配置信息：

- **endpoint**: RustFS 服务地址（如：`http://192.168.1.100:9000`）
- **access-key**: 访问密钥 ID
- **secret-key**: 访问密钥
- **bucket-name**: 存储桶名称（建议：`gsms-attachments`）
- **region**: 区域（随意填写，RustFS 不验证）

### 3. 在 RustFS 中创建存储桶

可以通过 RustFS 管理界面或 API 创建存储桶：

```bash
# 使用 mc (MinIO Client) 创建存储桶
mc alias set myrustfs http://your-rustfs-server:9000 access-key secret-key
mc mb myrustfs/gsms-attachments
```

## 迁移步骤

### 第一步：更新配置

编辑 `application.yml`，更新 RustFS 配置：

```yaml
attachment:
  storage:
    type: rustfs  # 修改为 rustfs
    rustfs:
      endpoint: http://your-rustfs-server:9000  # 修改为实际地址
      access-key: your-access-key  # 修改为实际密钥
      secret-key: your-secret-key  # 修改为实际密钥
      bucket-name: gsms-attachments
      region: us-east-1
      public-url: http://your-rustfs-server:9000  # 可选：公网访问地址
      presigned-url-expire-seconds: 3600
      use-presigned-url: false  # 是否使用预签名 URL
```

### 第二步：添加 Maven 依赖

已在 `pom.xml` 中添加 AWS S3 SDK 依赖：

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.25.27</version>
</dependency>
```

### 第三步：编译并启动应用

```bash
# 编译项目
mvn clean compile

# 启动应用
mvn spring-boot:run
```

启动日志应显示：

```
RustFS 存储服务初始化成功: endpoint=http://your-rustfs-server:9000, bucket=gsms-attachments
```

### 第四步：执行数据迁移

**⚠️ 重要提示：**
- 建议在非业务高峰期执行迁移
- 迁移前请备份数据库和本地附件文件
- 建议先在测试环境验证迁移流程

**通过 API 执行迁移：**

```bash
# 获取管理员 Token
TOKEN=$(curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123"}' | jq -r '.data.token')

# 执行迁移
curl -X POST http://localhost:8080/api/admin/storage-migration/migrate-to-rustfs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 150,
    "successCount": 148,
    "failureCount": 1,
    "skippedCount": 1,
    "failures": [
      {
        "id": 123,
        "fileName": "example.pdf",
        "errorMessage": "连接超时"
      }
    ]
  }
}
```

### 第五步：验证迁移结果

**检查 RustFS 存储桶中的文件：**

```bash
# 使用 mc 客户端查看
mc ls myrustfs/gsms-attachments
```

**通过数据库验证：**

```sql
-- 检查附件存储类型分布
SELECT storage_type, COUNT(*) as count
FROM gsms_attachment
GROUP BY storage_type;

-- 应该看到所有记录的 storage_type 为 'rustfs'
```

**通过应用验证：**

1. 登录系统
2. 进入项目详情页
3. 上传新附件，确认可以正常上传和下载
4. 查看历史附件，确认可以正常访问

### 第六步：清理工作

**迁移成功后，可以执行以下清理：**

1. **删除迁移控制器**（可选，建议禁用而非删除）：

```java
// 在 StorageMigrationController 上添加注解禁用
// @Deprecated
// @Api(hidden = true)
```

2. **备份并删除本地附件文件**（谨慎操作）：

```bash
# 先压缩备份
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz uploads/

# 确认备份无误后，删除本地文件（谨慎）
# rm -rf uploads/
```

3. **更新文档**：将迁移过程记录到项目文档中

## 回滚方案

如果迁移过程中出现问题，可以按以下步骤回滚：

### 方案一：快速回滚（配置回滚）

```yaml
# 修改 application.yml
attachment:
  storage:
    type: local  # 改回 local
```

重启应用即可回滚到本地存储。

### 方案二：数据回滚（数据库回滚）

如果需要恢复数据库中的 `storage_type` 字段：

```sql
-- 将所有附件改回本地存储
UPDATE gsms_attachment
SET storage_type = 'local'
WHERE storage_type = 'rustfs';
```

## 性能优化建议

### 1. 使用前端直传

对于大文件上传，建议使用 RustFS 的预签名 URL 功能，让前端直接上传到 RustFS：

```java
// 后端生成预签名上传 URL
String uploadUrl = rustfsStorageProvider.generatePresignedUploadUrl(filePath);

// 前端使用此 URL 直接上传
```

### 2. 启用 CDN

如果 RustFS 支持公网访问，可以配置 CDN 加速：

```yaml
attachment:
  storage:
    rustfs:
      public-url: https://cdn.yourdomain.com
```

### 3. 分片上传

对于超大文件（>100MB），建议使用 RustFS 的分片上传功能：

```java
// 使用 MultipartUpload API
CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
    .bucket(bucketName)
    .key(objectKey)
    .build();

String uploadId = s3Client.createMultipartUpload(createMultipartUploadRequest).uploadId();
// ... 上传分片 ...
```

## 常见问题

### Q1: 迁移过程中连接失败

**原因**：RustFS 服务不可访问或网络问题

**解决方案**：
1. 检查 RustFS 服务是否正常运行
2. 检查防火墙规则
3. 检查 endpoint 配置是否正确

### Q2: 部分文件迁移失败

**原因**：本地文件已损坏或不存在

**解决方案**：
1. 查看迁移失败日志
2. 从备份恢复本地文件
3. 重新执行迁移

### Q3: 迁移后文件无法访问

**原因**：RustFS 权限配置问题或 URL 配置错误

**解决方案**：
1. 检查 RustFS 存储桶的访问策略
2. 确认 `public-url` 配置正确
3. 如果使用预签名 URL，确认过期时间设置合理

### Q4: 迁移速度慢

**原因**：网络带宽限制或单线程上传

**解决方案**：
1. 使用并发上传（修改迁移服务）
2. 在网络条件好的时段执行迁移
3. 考虑使用 RustFS 的导入工具直接从服务器导入

## 安全建议

1. **密钥管理**：
   - 不要将 `access-key` 和 `secret-key` 提交到代码仓库
   - 使用环境变量或配置中心管理敏感信息
   - 定期轮换密钥

2. **访问控制**：
   - 为 RustFS 创建专用的访问密钥
   - 限制存储桶的公开访问权限
   - 定期审计访问日志

3. **数据加密**：
   - 敏感文件建议在客户端加密后再上传
   - 启用 HTTPS 传输加密

## 后续维护

1. **监控**：
   - 监控 RustFS 存储空间使用情况
   - 监控上传/下载成功率
   - 设置告警阈值

2. **备份**：
   - 定期备份 RustFS 中的数据
   - 验证备份的完整性
   - 制定恢复演练计划

3. **优化**：
   - 定期清理无用的附件文件
   - 优化文件存储结构
   - 评估是否需要生命周期策略

## 技术支持

如遇到问题，请参考以下资源：

- RustFS 官方文档：https://docs.rustfs.cn/
- AWS S3 SDK 文档：https://docs.aws.amazon.com/sdk-for-java/
- 项目 Issue 跟踪：联系技术支持团队
