# RustFS 快速参考卡片

## 🎯 一键配置（已完成）

### ✅ 当前状态

您的应用**已配置为使用 RustFS**，所有附件操作都通过对象存储。

```yaml
# application.yml
attachment:
  storage:
    type: rustfs  # ✅ 当前使用 RustFS
```

---

## 🚀 启动验证

### 1. 启动应用

```bash
cd /e/codes/gsms/backend
mvn spring-boot:run
```

### 2. 确认日志

看到以下日志表示成功：

```
✅ RustFS 存储服务初始化成功: endpoint=http://49.235.153.206:31210, bucket=teammanagement
✅ RustFS 存储桶已存在: teammanagement
```

### 3. 测试上传

1. 登录系统（`http://localhost:3000`）
2. 进入项目详情
3. 上传附件
4. 验证数据库：`storage_type = "rustfs"`

---

## 📋 配置说明

### 核心配置（application.yml）

```yaml
attachment:
  storage:
    type: rustfs  # 关键配置：rustfs
    rustfs:
      endpoint: http://49.235.153.206:31210  # RustFS 服务地址
      access-key: eyYXRWlsmHD1nco3T45I        # 访问密钥 ID
      secret-key: ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7  # 访问密钥
      bucket-name: teammanagement             # 存储桶名称
      region: us-east-1                       # 区域（随意）
```

### 切换存储类型

**切换到 RustFS**：
```yaml
attachment.storage.type: rustfs
```

**切换到本地存储**：
```yaml
attachment.storage.type: local
```

修改后**重启应用**即可。

---

## 🔄 工作流程

### 上传附件

```
前端 → Controller → Service → RustFSStorageProvider → RustFS Server
                                              ↓
                              返回路径: 2026/03/25/uuid.pdf
                                              ↓
                              保存到数据库: storage_type = "rustfs"
```

**关键代码**：
```java
// AttachmentServiceImpl.java
String relativePath = storageService.upload(file, null);  // ✅ 使用 RustFS
attachment.setStorageType(storageType);  // ✅ "rustfs"
```

### 下载附件

```
前端 → Controller → Service → RustFSStorageProvider → RustFS Server
                                              ↓
                              返回文件流 → 写入响应 → 下载到客户端
```

**关键代码**：
```java
InputStream inputStream = storageService.getInputStream(filePath);  // ✅ 从 RustFS 读取
```

---

## ✅ 验证清单

### 自动验证（代码层面）

- [x] `StorageConfig.java` - 根据 `type` 动态选择存储服务
- [x] `AttachmentServiceImpl.java` - 使用动态存储服务（无 `@Qualifier`）
- [x] `attachment.setStorageType(storageType)` - 动态读取配置

### 手动验证（运行时）

- [ ] 应用启动成功
- [ ] RustFS 初始化日志正常
- [ ] 上传测试文件成功
- [ ] 数据库 `storage_type = "rustfs"`
- [ ] RustFS 存储桶中有文件
- [ ] 下载文件内容正确

---

## 🔧 故障排查

### 问题：RustFS 初始化失败

**检查**：
```bash
curl http://49.235.153.206:31210  # RustFS 服务是否运行
```

**解决**：启动 RustFS 服务或检查网络连接

### 问题：上传失败

**检查**：
```bash
# 验证密钥
mc alias set myrustfs http://49.235.153.206:31210 eyYXRWlsmHD1nco3T45I ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7
mc ls myrustfs/teammanagement
```

**解决**：验证 `access-key` 和 `secret-key` 是否正确

### 问题：下载 404

**检查**：
```sql
SELECT file_path, storage_type FROM gsms_attachment WHERE id = 1;
```

**检查 RustFS**：
```bash
mc ls myrustfs/teammanagement/2026/03/25/
```

**解决**：确保文件路径和存储类型一致

---

## 📊 监控指标

### 关键指标

| 指标 | 命令 | 告警阈值 |
|------|------|---------|
| 存储空间 | `mc du myrustfs/teammanagement` | > 80% |
| 文件数量 | `mc ls myrustfs/teammanagement --recursive \| wc -l` | - |
| 上传统计 | `SELECT COUNT(*) FROM gsms_attachment WHERE storage_type='rustfs'` | - |

### 数据库查询

```sql
-- 查看存储类型分布
SELECT storage_type, COUNT(*) FROM gsms_attachment GROUP BY storage_type;

-- 查看最新上传的文件
SELECT id, file_name, storage_type, create_time
FROM gsms_attachment
ORDER BY create_time DESC
LIMIT 10;
```

---

## 🔄 回滚方案

### 快速回滚到本地存储

**步骤 1**：修改配置
```yaml
attachment.storage.type: local
```

**步骤 2**：重启应用
```bash
mvn spring-boot:run
```

**步骤 3**：验证
```
文件上传目录初始化成功: /path/to/uploads
```

---

## 📚 相关文档

- **完整配置指南**：`docs/RUSTFS_CONFIGURATION_GUIDE.md`
- **迁移指南**：`docs/RUSTFS_MIGRATION_GUIDE.md`
- **审查报告**：`docs/ATTACHMENT_STORAGE_AUDIT.md`
- **快速参考**：`docs/RUSTFS_QUICK_REFERENCE.md`

---

## 💡 最佳实践

### 1. 使用环境变量

```bash
export RUSTFS_ENDPOINT=http://49.235.153.206:31210
export RUSTFS_ACCESS_KEY=eyYXRWlsmHD1nco3T45I
export RUSTFS_SECRET_KEY=ZDi2BU8KQYGSLXeau5cynIHkprVPCxjfmhMd1sN7
```

### 2. 定期备份

```bash
mc mirror myrustfs/teammanagement ./backup/teammanagement
```

### 3. 监控空间

```bash
mc du myrustfs/teammanagement
```

### 4. 清理旧文件

```bash
# 删除 90 天前的文件
find ./uploads -type f -mtime +90 -delete
```

---

## ✅ 总结

### 当前配置

✅ **存储类型**：RustFS
✅ **服务地址**：http://49.235.153.206:31210
✅ **存储桶**：teammanagement
✅ **代码修复**：已完成

### 下一步

1. **启动应用**：`mvn spring-boot:run`
2. **查看日志**：确认 RustFS 初始化成功
3. **测试上传**：验证文件存储到 RustFS
4. **验证数据库**：确认 `storage_type = "rustfs"`

---

**文档版本**：v1.0.0
**最后更新**：2026-03-25
**配置状态**：✅ 已完成
