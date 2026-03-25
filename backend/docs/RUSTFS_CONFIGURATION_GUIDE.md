# RustFS 附件存储配置指南

## 📋 目录

1. [快速开始](#快速开始)
2. [配置说明](#配置说明)
3. [验证步骤](#验证步骤)
4. [工作流程](#工作流程)
5. [故障排查](#故障排查)
6. [回滚方案](#回滚方案)
7. [最佳实践](#最佳实践)

---

## 🚀 快速开始

### 当前配置状态

您的应用**已配置为使用 RustFS**，配置文件位于 `application.yml`：

```yaml
attachment:
  storage:
    type: rustfs  # ✅ 当前使用 RustFS
    rustfs:
      endpoint: http://49.235.153.206:31210
      access-key: eyYXRWlsmHD1nco3T45I
      secret-key: ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7
      bucket-name: teammanagement
      region: us-east-1
      public-url:  # 可选：公网访问地址
      presigned-url-expire-seconds: 3600
      use-presigned-url: false
```

### 确认 RustFS 正常工作

**步骤 1：启动应用**

```bash
cd /e/codes/gsms/backend
mvn spring-boot:run
```

**步骤 2：查看启动日志**

找到以下日志表示 RustFS 初始化成功：

```
RustFS 存储服务初始化成功: endpoint=http://49.235.153.206:31210, bucket=teammanagement
RustFS 存储桶已存在: teammanagement
```

如果看到错误，请参考[故障排查](#故障排查)部分。

**步骤 3：测试上传**

1. 登录系统（`http://localhost:3000`）
2. 进入项目详情页
3. 上传一个测试文件
4. 检查 RustFS 存储桶中是否有文件：

```bash
# 使用 mc 客户端检查（需要安装 mc）
mc alias set myrustfs http://49.235.153.206:31210 eyYXRWlsmHD1nco3T45I ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7
mc ls myrustfs/teammanagement

# 或通过 RustFS 管理界面查看
```

**步骤 4：验证数据库记录**

```sql
-- 检查最新上传的附件的存储类型
SELECT
    id,
    file_name,
    storage_type,
    file_path,
    create_time
FROM gsms_attachment
ORDER BY create_time DESC
LIMIT 5;

-- 确认 storage_type 为 'rustfs'
```

---

## ⚙️ 配置说明

### application.yml 完整配置

```yaml
# 附件存储配置
attachment:
  storage:
    # 存储类型：local（本地）或 rustfs（对象存储）
    type: rustfs

    # 本地存储配置（type: local 时使用）
    local:
      upload-dir: ./uploads
      url-prefix: /api/attachments/file

    # RustFS 对象存储配置（type: rustfs 时使用）
    rustfs:
      # RustFS 服务地址
      endpoint: http://49.235.153.206:31210

      # 访问凭证
      access-key: eyYXRWlsmHD1nco3T45I
      secret-key: ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7

      # 存储桶名称
      bucket-name: teammanagement

      # 区域（RustFS 不验证，可随意填写）
      region: us-east-1

      # 公网访问地址（可选）
      # 如果配置，附件 URL 将使用此地址
      # 示例：https://cdn.yourdomain.com 或 http://49.235.153.206:31210
      public-url:

      # 预签名 URL 过期时间（秒）
      presigned-url-expire-seconds: 3600

      # 是否使用预签名 URL
      # true：生成临时访问链接（需要签名）
      # false：生成永久访问链接
      use-presigned-url: false
```

### 环境变量配置（推荐）

为了安全，建议使用环境变量存储敏感信息：

```bash
# 创建环境变量文件 .env 或 export
export RUSTFS_ENDPOINT=http://49.235.153.206:31210
export RUSTFS_ACCESS_KEY=eyYXRWlsmHD1nco3T45I
export RUSTFS_SECRET_KEY=ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7
export RUSTFS_BUCKET_NAME=teammanagement

# 启动应用
mvn spring-boot:run
```

然后在 `application.yml` 中引用：

```yaml
attachment:
  storage:
    rustfs:
      endpoint: ${RUSTFS_ENDPOINT}
      access-key: ${RUSTFS_ACCESS_KEY}
      secret-key: ${RUSTFS_SECRET_KEY}
      bucket-name: ${RUSTFS_BUCKET_NAME}
```

---

## ✅ 验证步骤

### 1. 验证存储服务选择

查看 `StorageConfig.java` 确认逻辑正确：

```java
@Bean
@Primary
public StorageService storageService(
        LocalStorageProvider localStorageProvider,
        RustFSStorageProvider rustfsStorageProvider) {

    switch (storageType.toLowerCase()) {
        case "rustfs":
            return rustfsStorageProvider;  // ✅ 返回 RustFS
        case "local":
        default:
            return localStorageProvider;
    }
}
```

### 2. 验证附件服务使用

查看 `AttachmentServiceImpl.java` 确认使用动态存储服务：

```java
@Autowired
private StorageService storageService;  // ✅ 没有 @Qualifier，使用 @Primary

// 上传时
String relativePath = storageService.upload(file, null);  // ✅ 使用 RustFS 上传

// 保存时
attachment.setStorageType(storageType);  // ✅ 动态读取配置（"rustfs"）

// URL 生成
resp.setUrl(storageService.getUrl(attachment.getFilePath()));  // ✅ 使用 RustFS URL
```

### 3. 端到端测试

#### 测试上传

```bash
# 1. 获取 Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123"}' | jq -r '.data.token')

# 2. 上传文件
curl -X POST http://localhost:8080/api/attachments/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "targetType=project" \
  -F "targetId=1" \
  -F "file=@test.pdf"

# 3. 查看响应
# 应返回：
# {
#   "code": 200,
#   "data": {
#     "storageType": "rustfs",  # ✅ 确认为 rustfs
#     "url": "http://49.235.153.206:31210/teammanagement/..."
#   }
# }
```

#### 测试下载

```bash
# 获取附件 ID 后下载
curl -X GET http://localhost:8080/api/attachments/1/download \
  -H "Authorization: Bearer $TOKEN" \
  -o downloaded_file.pdf

# 验证文件
md5 downloaded_file.pdf test.pdf  # 应该相同
```

#### 测试删除

```bash
curl -X DELETE http://localhost:8080/api/attachments/1 \
  -H "Authorization: Bearer $TOKEN"

# 验证 RustFS 中文件已删除
mc ls myrustfs/teammanagement  # 文件应该不存在
```

---

## 🔄 工作流程

### 上传流程

```
┌─────────┐     上传请求      ┌──────────────┐
│ 前端    │ ────────────────> │ Controller   │
└─────────┘                  └──────────────┘
                                    │
                                    ▼
                           ┌──────────────┐
                           │ Attachment   │
                           │ ServiceImpl  │
                           └──────────────┘
                                    │
                                    ▼
                           ┌──────────────┐
                           │ RustFS       │
                           │ Storage      │
                           │ Provider     │
                           └──────────────┘
                                    │
                                    ▼
                           ┌──────────────┐
                           │ RustFS       │
                           │ Server       │
                           │ (S3 API)     │
                           └──────────────┘
                                    │
                                    ▼
                           保存到对象存储
```

**代码调用链**：
1. `AttachmentController.upload()` → 接收请求
2. `AttachmentServiceImpl.upload()` → 业务逻辑
3. `RustFSStorageProvider.upload()` → 上传到 RustFS
4. 返回文件路径：`2026/03/25/uuid.pdf`
5. 数据库保存：`storage_type = "rustfs"`

### 下载流程

```
┌─────────┐     下载请求      ┌──────────────┐
│ 前端    │ ────────────────> │ Controller   │
└─────────┘                  └──────────────┘
                                    │
                                    ▼
                           ┌──────────────┐
                           │ Attachment   │
                           │ ServiceImpl  │
                           └──────────────┘
                                    │
                                    ▼
                           ┌──────────────┐
                           │ RustFS       │
                           │ Storage      │
                           │ Provider     │
                           └──────────────┘
                                    │
                                    ▼
                           ┌──────────────┐
                           │ RustFS       │
                           │ Server       │
                           └──────────────┘
                                    │
                                    ▼
                           返回文件流
```

---

## 🔍 故障排查

### 问题 1：RustFS 初始化失败

**错误日志**：
```
RustFS 存储服务初始化失败: Connection refused
```

**解决方案**：

1. **检查 RustFS 服务是否运行**
   ```bash
   curl http://49.235.153.206:31210
   ```

2. **检查网络连接**
   ```bash
   telnet 49.235.153.206 31210
   ```

3. **验证配置**
   ```bash
   # 检查 endpoint 是否正确
   echo $RUSTFS_ENDPOINT
   ```

### 问题 2：存储桶不存在

**错误日志**：
```
检查/创建存储桶失败: NoSuchBucket
```

**解决方案**：

1. **手动创建存储桶**
   ```bash
   mc mb myrustfs/teammanagement
   ```

2. **或通过 RustFS 管理界面创建**

### 问题 3：上传失败

**错误日志**：
```
文件上传到 RustFS 失败: Access Denied
```

**解决方案**：

1. **验证访问密钥**
   ```bash
   mc admin user info myrustfs eyYXRWlsmHD1nco3T45I
   ```

2. **检查权限策略**
   ```bash
   mc admin policy list myrustfs
   ```

3. **重新生成密钥**（如果需要）

### 问题 4：下载 404

**现象**：上传成功，但下载时文件不存在

**解决方案**：

1. **检查数据库中的文件路径**
   ```sql
   SELECT file_path FROM gsms_attachment WHERE id = 1;
   ```

2. **检查 RustFS 中的文件**
   ```bash
   mc ls myrustfs/teammanagement/2026/03/25/
   ```

3. **验证路径是否一致**

### 问题 5：URL 无法访问

**现象**：附件 URL 返回 403 或 404

**解决方案**：

1. **检查 public-url 配置**
   ```yaml
   attachment:
     storage:
       rustfs:
         public-url: http://49.235.153.206:31210  # 确保可访问
   ```

2. **检查 RustFS 访问策略**
   ```bash
   mc admin policy get myrustfs download
   ```

3. **设置存储桶为公开读取**（如果需要）
   ```bash
   mc anonymous set download myrustfs/teammanagement
   ```

---

## 🔄 回滚方案

### 快速回滚到本地存储

**步骤 1：修改配置**

```yaml
# application.yml
attachment:
  storage:
    type: local  # 改回 local
```

**步骤 2：重启应用**

```bash
# 停止应用
Ctrl+C

# 重新启动
mvn spring-boot:run
```

**步骤 3：验证日志**

```
文件上传目录初始化成功: /path/to/uploads
```

### 数据库记录处理

如果需要修改已有附件的存储类型：

```sql
-- 将所有附件改回本地存储（仅用于回滚）
UPDATE gsms_attachment
SET storage_type = 'local'
WHERE storage_type = 'rustfs';
```

**注意**：这仅修改数据库记录，不会迁移文件。如果需要从 RustFS 迁移回本地，需要使用迁移服务。

---

## 🎯 最佳实践

### 1. 使用环境变量管理密钥

**推荐做法**：
```bash
# .env 文件（不提交到 Git）
export RUSTFS_ACCESS_KEY=your_key
export RUSTFS_SECRET_KEY=your_secret
```

**不推荐做法**：
```yaml
# ❌ 直接写在配置文件中
secret-key: ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7
```

### 2. 定期备份 RustFS 数据

```bash
# 备份存储桶
mc mirror myrustfs/teammanagement ./backup/teammanagement

# 定时备份（cron）
0 2 * * * mc mirror myrustfs/teammanagement ./backup/teammanagement-$(date +\%Y\%m\%d)
```

### 3. 监控存储空间

```bash
# 查看存储桶大小
mc du myrustfs/teammanagement

# 设置告警（如果空间不足）
```

### 4. 设置生命周期策略

```bash
# 自动删除 90 天前的文件
mc ilm add --expiry-days 90 myrustfs/teammanagement
```

### 5. 使用 CDN 加速

如果 RustFS 支持公网访问，配置 CDN：

```yaml
attachment:
  storage:
    rustfs:
      public-url: https://cdn.yourdomain.com  # 使用 CDN 域名
```

### 6. 前端直传（大文件优化）

对于大文件上传，考虑使用预签名 URL 让前端直传到 RustFS：

```java
// 后端生成预签名 URL
String uploadUrl = rustfsStorageProvider.generatePresignedUploadUrl(filePath);

// 前端使用此 URL 直接上传到 RustFS
```

这样可以减轻后端服务器压力。

### 7. 权限控制

**生产环境建议**：
- ✅ 使用专用的 RustFS 访问密钥
- ✅ 限制密钥权限（只允许读写特定存储桶）
- ✅ 定期轮换密钥
- ✅ 启用访问日志审计

```bash
# 创建只读密钥（用于下载）
mc admin user add myrustfs readonly readonly_password

# 创建读写密钥（用于上传）
mc admin user add myrustfs readwrite readwrite_password
```

---

## 📊 监控和维护

### 关键指标

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| 存储空间使用率 | 已用空间/总空间 | > 80% |
| 上传成功率 | 成功上传数/总上传数 | < 95% |
| 下载成功率 | 成功下载数/总下载数 | < 95% |
| 平均响应时间 | 平均请求响应时间 | > 1000ms |
| 错误日志数量 | 每小时错误日志数 | > 10 |

### 日志监控

```bash
# 实时查看 RustFS 相关日志
mvn spring-boot:run | grep -i rustfs

# 查看上传失败日志
mvn spring-boot:run | grep -i "上传.*失败"
```

### 数据库监控

```sql
-- 查看附件存储类型分布
SELECT
    storage_type,
    COUNT(*) as count,
    SUM(file_size) / 1024 / 1024 as total_size_mb
FROM gsms_attachment
WHERE is_deleted = 0
GROUP BY storage_type;

-- 查看每日上传统计
SELECT
    DATE(create_time) as date,
    COUNT(*) as count,
    SUM(file_size) / 1024 / 1024 as size_mb
FROM gsms_attachment
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY DATE(create_time)
ORDER BY date DESC;
```

---

## 📞 技术支持

### 相关文档

- **RustFS 官方文档**：https://docs.rustfs.cn/
- **AWS S3 SDK 文档**：https://docs.aws.amazon.com/sdk-for-java/
- **迁移指南**：`docs/RUSTFS_MIGRATION_GUIDE.md`
- **快速参考**：`docs/RUSTFS_QUICK_REFERENCE.md`
- **审查报告**：`docs/ATTACHMENT_STORAGE_AUDIT.md`

### 常见问题

**Q: 如何切换存储类型？**
A: 修改 `application.yml` 中的 `attachment.storage.type`，重启应用即可。

**Q: 如何迁移历史附件？**
A: 调用迁移 API：`POST /api/admin/storage-migration/migrate-to-rustfs`

**Q: RustFS 和本地存储可以共存吗？**
A: 可以。数据库中的 `storage_type` 字段记录每个附件的存储位置，系统会自动选择正确的存储服务读取文件。

**Q: 如何清理迁移后的本地文件？**
A:
```bash
# 1. 确认迁移成功
SELECT storage_type, COUNT(*) FROM gsms_attachment GROUP BY storage_type;

# 2. 备份本地文件
tar -czf uploads_backup.tar.gz uploads/

# 3. 删除本地文件（谨慎操作）
rm -rf uploads/
```

---

## ✅ 检查清单

### 部署前检查

- [ ] RustFS 服务正常运行
- [ ] 存储桶已创建
- [ ] 访问密钥已配置
- [ ] 网络连接正常
- [ ] `application.yml` 配置正确
- [ ] 环境变量已设置（如使用）

### 部署后验证

- [ ] 应用启动成功
- [ ] RustFS 初始化日志正常
- [ ] 测试上传成功
- [ ] 测试下载成功
- [ ] 测试删除成功
- [ ] 数据库记录正确（`storage_type = "rustfs"`）
- [ ] RustFS 中文件存在

### 定期维护

- [ ] 每月检查存储空间使用情况
- [ ] 每月备份 RustFS 数据
- [ ] 每季度轮换访问密钥
- [ ] 每季度审查访问日志
- [ ] 每年检查和更新依赖版本

---

**文档版本**：v1.0.0
**最后更新**：2026-03-25
**维护者**：开发团队
