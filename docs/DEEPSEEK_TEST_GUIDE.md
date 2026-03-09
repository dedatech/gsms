# DeepSeek API 测试指南

## 应用已成功启动 ✅

后端服务已在 `http://localhost:8080` 成功启动，DeepSeek API 集成已完成！

## 测试方式

### 方式一：使用 Swagger UI 测试（推荐）

1. **访问 Swagger 文档**
   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **登录获取 Token**
   - 找到 `用户管理 (User)` -> `POST /api/users/login`
   - 点击 "Try it out"
   - 输入测试账号密码：
     - `admin` / `Admin123` （管理员）
     - `zhangsan03` / `Admin123` （普通用户）
   - **注意**：登录时需要先调用 `/api/users/captcha` 获取验证码
   - 复制返回的 `token` 值

3. **配置认证**
   - 点击 Swagger UI 页面顶部的 "Authorize" 按钮
   - 在弹出框中输入：`Bearer <你的token>`
   - 点击 "Authorize"

4. **测试需求拆分功能**
   - 找到 `AI 功能` -> `POST /api/ai/breakdown-requirement`
   - 点击 "Try it out"
   - 输入测试数据：
     ```json
     {
       "requirement": "开发一个用户登录功能，包括用户名密码登录、记住密码、找回密码功能",
       "projectType": "Web应用",
       "teamSize": 3,
       "expectedDays": 14
     }
     ```
   - 点击 "Execute"
   - 查看返回的 AI 分析结果

### 方式二：使用 Postman/cURL 测试

**步骤 1：获取验证码**
```bash
curl -X GET http://localhost:8080/api/users/captcha
```

**步骤 2：登录获取 Token**
```bash
# 将 <captcha_uuid> 和 <captcha_code> 替换为实际值
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123",
    "uuid": "<captcha_uuid>",
    "captcha": "<captcha_code>"
  }'
```

**步骤 3：调用需求拆分 API**
```bash
# 将 <your_token> 替换为步骤2返回的token
curl -X POST http://localhost:8080/api/ai/breakdown-requirement \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -d '{
    "requirement": "开发一个用户登录功能，包括用户名密码登录、记住密码、找回密码功能",
    "projectType": "Web应用",
    "teamSize": 3,
    "expectedDays": 14
  }'
```

### 方式三：使用浏览器控制台测试

1. **打开浏览器控制台**（F12）

2. **获取验证码并登录**
```javascript
// 获取验证码
fetch('http://localhost:8080/api/users/captcha')
  .then(res => res.json())
  .then(data => {
    console.log('验证码UUID:', data.data.uuid);
    console.log('验证码图片URL:', data.data.image);

    // 手动输入验证码后登录
    // fetch('http://localhost:8080/api/users/login', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({
    //     username: 'admin',
    //     password: 'Admin123',
    //     uuid: data.data.uuid,
    //     captcha: '<手动输入验证码>'
    //   })
    // }).then(res => res.json())
    // .then(loginData => {
    //   console.log('Token:', loginData.data.token);
    // });
  });
```

3. **测试需求拆分功能**
```javascript
// 替换 <your_token> 为实际的 token
fetch('http://localhost:8080/api/ai/breakdown-requirement', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer <your_token>'
  },
  body: JSON.stringify({
    requirement: '开发一个用户登录功能，包括用户名密码登录、记住密码、找回密码功能',
    projectType: 'Web应用',
    teamSize: 3,
    expectedDays: 14
  })
})
.then(res => res.json())
.then(data => {
  console.log('需求拆分结果:', data);
});
```

## 预期返回结果

成功的响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": "开发一个完整的用户登录系统，包含基础登录功能及用户账号管理功能",
    "subTasks": [
      {
        "sequence": 1,
        "title": "需求分析与设计",
        "description": "分析登录功能需求，设计登录流程和用户交互",
        "estimatedDays": 1.0,
        "taskType": "需求分析",
        "priority": "高",
        "dependsOn": null,
        "notes": "需要与产品经理确认详细需求"
      },
      {
        "sequence": 2,
        "title": "数据库设计",
        "description": "设计用户表、登录日志表等数据库结构",
        "estimatedDays": 1.5,
        "taskType": "数据库设计",
        "priority": "高",
        "dependsOn": 1,
        "notes": "需要考虑密码加密存储"
      }
    ],
    "totalEstimatedDays": 10.0,
    "suggestedTeamSize": 3,
    "suggestedIterationDays": 14,
    "risks": [
      "密码安全需要特别注意",
      "找回密码功能需要邮件服务支持"
    ],
    "suggestions": [
      "建议使用BCrypt进行密码加密",
      "建议实现登录失败次数限制"
    ],
    "notes": "整体开发周期约2周，建议分两个迭代完成"
  }
}
```

## 测试其他需求

你可以尝试不同的需求描述：

```json
{
  "requirement": "开发一个电商网站的商品管理功能，包括商品列表、商品详情、商品搜索、商品分类管理",
  "projectType": "电商平台",
  "teamSize": 5
}
```

```json
{
  "requirement": "开发一个在线聊天应用，支持一对一聊天、群聊、消息推送、文件传输",
  "projectType": "即时通讯",
  "teamSize": 4
}
```

## 故障排查

### 1. API Key 无效
```
错误：DeepSeek API 调用失败: HTTP 401
解决：检查 application-dev.yml 中的 API Key 是否正确
```

### 2. 网络连接失败
```
错误：DeepSeek API 调用失败: timeout
解决：
- 检查网络连接
- 确认能访问 https://api.deepseek.com
- 增加 application.yml 中的 timeout 配置
```

### 3. AI 响应解析失败
```
错误：需求拆分失败: AI 响应解析失败
解决：查看后端日志，检查 DeepSeek API 返回的原始响应
```

### 4. Token 过期
```
错误：{"code":1401,"message":"令牌已过期"}
解决：重新登录获取新的 Token
```

## 查看日志

后端日志会输出详细的调用信息：
```bash
# 查看实时日志
tail -f E:/codes/gsms/backend/logs/gsms.log | grep -i deepseek
```

日志示例：
```
2026-03-09 14:22:01.567 [INFO] 开始调用 DeepSeek API 拆分需求
2026-03-09 14:22:05.123 [INFO] 需求拆分完成，耗时: 3556ms
2026-03-09 14:22:05.124 [INFO] 需求拆分成功，共拆分 5 个子任务，总预估 12.0 人天
```

## 配置验证

确认 DeepSeek API 配置已正确加载：

```bash
# 检查配置文件
cat E:/codes/gsms/backend/src/main/resources/application-dev.yml | grep -A 10 "deepseek"
```

应该显示：
```yaml
deepseek:
  api:
    key: sk-cafb7c4bbc884fd9bcb0dd2fcda0f2ea
    url: https://api.deepseek.com/v1/chat/completions
    model: deepseek-chat
    timeout: 60000
    max-retries: 3
```

## 性能参考

- **首次调用**：通常需要 5-10 秒（冷启动）
- **后续调用**：通常需要 3-5 秒
- **Token 消耗**：大约 500-2000 tokens/次（取决于需求复杂度）

## 下一步

功能测试通过后，你可以：

1. **前端集成**：在前端页面中调用此 API
2. **优化提示词**：根据业务需求调整 AI 提示词
3. **添加缓存**：对相似需求的拆分结果进行缓存
4. **扩展功能**：基于此框架添加更多 AI 功能

## 技术支持

如有问题，请查看：
- [完整使用文档](./DEEPSEEK_INTEGRATION.md)
- [Swagger API 文档](http://localhost:8080/swagger-ui.html)
- 后端日志：`E:/codes/gsms/backend/logs/gsms.log`
