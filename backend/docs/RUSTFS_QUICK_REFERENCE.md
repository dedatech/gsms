# RustFS 迁移快速参考

## 一、核心文件清单

### 新增文件

| 文件路径 | 说明 |
|---------|------|
| `pom.xml` | 添加 AWS S3 SDK 依赖 |
| `src/main/java/com/gsms/gsms/service/storage/RustFSStorageProvider.java` | RustFS 存储服务实现 |
| `src/main/java/com/gsms/gsms/service/storage/StorageMigrationService.java` | 数据迁移服务 |
| `src/main/java/com/gsms/gsms/controller/StorageMigrationController.java` | 迁移 API 控制器 |
| `src/main/java/com/gsms/gsms/infra/config/StorageConfig.java` | 更新存储配置类 |
| `src/test/java/com/gsms/gsms/service/storage/RustFSStorageProviderTest.java` | RustFS 单元测试 |
| `src/main/resources/db/migration/V2.2__verify_rustfs_migration.sql` | 迁移验证脚本 |
| `docs/RUSTFS_MIGRATION_GUIDE.md` | 详细迁移指南 |

### 修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `application.yml` | 添加 RustFS 配置项 |
| `pom.xml` | 添加 AWS S3 SDK 依赖 |

## 二、配置要点

### application.yml 配置

```yaml
attachment:
  storage:
    type: rustfs  # 关键：设置为 rustfs
    rustfs:
      endpoint: http://localhost:9000  # RustFS 服务地址
      access-key: minioadmin           # 访问密钥 ID
      secret-key: minioadmin           # 访问密钥
      bucket-name: gsms-attachments    # 存储桶名称
      region: us-east-1                # 区域（随意）
      public-url:                      # 可选：公网访问地址
      presigned-url-expire-seconds: 3600
      use-presigned-url: false         # 是否使用预签名 URL
```

### 环境变量配置（推荐）

```bash
export RUSTFS_ENDPOINT=http://localhost:9000
export RUSTFS_ACCESS_KEY=minioadmin
export RUSTFS_SECRET_KEY=minioadmin
export RUSTFS_BUCKET_NAME=gsms-attachments
```

## 三、迁移流程

### 1. 准备阶段

```bash
# 1. 确保 RustFS 服务运行
curl http://localhost:9000

# 2. 创建存储桶
mc mb myrustfs/gsms-attachments

# 3. 备份数据库和附件
mysqldump gsms > gsms_backup_$(date +%Y%m%d).sql
tar -czf uploads_backup_$(date +%Y%m%d).tar.gz uploads/
```

### 2. 代码部署

```bash
# 1. 拉取最新代码
git pull origin fix-权限修复

# 2. 编译项目
mvn clean compile

# 3. 启动应用
mvn spring-boot:run
```

### 3. 执行迁移

```bash
# 获取 Token
TOKEN=$(curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin123"}' | jq -r '.data.token')

# 执行迁移
curl -X POST http://localhost:8080/api/admin/storage-migration/migrate-to-rustfs \
  -H "Authorization: Bearer $TOKEN"
```

### 4. 验证结果

```sql
-- 检查存储类型分布
SELECT storage_type, COUNT(*) FROM gsms_attachment GROUP BY storage_type;

-- 应该全部为 'rustfs'
```

## 四、API 端点

### 迁移 API

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/admin/storage-migration/migrate-to-rustfs` | POST | 执行迁移 |

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 150,
    "successCount": 148,
    "failureCount": 1,
    "skippedCount": 1
  }
}
```

## 五、回滚方案

### 快速回滚

```yaml
# 修改 application.yml
attachment:
  storage:
    type: local  # 改回 local
```

```bash
# 重启应用
mvn spring-boot:run
```

### 数据库回滚

```sql
-- 将所有附件改回本地存储
UPDATE gsms_attachment
SET storage_type = 'local'
WHERE storage_type = 'rustfs';
```

## 六、测试验证

### 单元测试

```bash
# 运行 RustFS 测试
mvn test -Dtest=RustFSStorageProviderTest
```

### 功能测试

1. 登录系统
2. 上传新附件
3. 下载历史附件
4. 删除附件

## 七、常见问题

### 问题 1：连接失败

```
RustFS 存储服务初始化失败
```

**解决方案**：
1. 检查 RustFS 服务是否运行
2. 检查 endpoint 配置
3. 检查网络连接

### 问题 2：迁移失败

```
附件迁移失败: 连接超时
```

**解决方案**：
1. 检查网络带宽
2. 分批迁移（修改代码）
3. 增加超时时间

### 问题 3：文件无法访问

```
404 Not Found
```

**解决方案**：
1. 检查 public-url 配置
2. 检查 RustFS 访问策略
3. 确认文件已上传成功

## 八、性能优化

### 前端直传

```java
// 后端生成预签名 URL
String uploadUrl = rustfsStorageProvider.generatePresignedUploadUrl(filePath);

// 前端直接上传到 RustFS
```

### CDN 加速

```yaml
attachment:
  storage:
    rustfs:
      public-url: https://cdn.yourdomain.com
```

### 并发迁移

修改 `StorageMigrationService` 使用线程池并发上传。

## 九、安全建议

1. **密钥管理**
   - 使用环境变量存储密钥
   - 不要提交到代码仓库
   - 定期轮换密钥

2. **访问控制**
   - 限制存储桶公开访问
   - 使用 IAM 策略
   - 启用访问日志

3. **数据加密**
   - 启用 HTTPS 传输
   - 敏感文件客户端加密

## 十、监控指标

| 指标 | 说明 | 告警阈值 |
|------|------|---------|
| 存储空间使用率 | 已用空间/总空间 | > 80% |
| 上传成功率 | 成功上传数/总上传数 | < 95% |
| 下载成功率 | 成功下载数/总下载数 | < 95% |
| 平均响应时间 | 平均请求响应时间 | > 1000ms |

## 十一、联系支持

- 技术文档：`docs/RUSTFS_MIGRATION_GUIDE.md`
- Issue 跟踪：项目 GitHub Issues
- 技术支持：联系技术团队

---

**最后更新**：2026-03-25
**版本**：v1.0.0
