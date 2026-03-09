# DeepSeek API 集成使用说明

## 概述

本项目已集成 DeepSeek API，提供智能需求拆分和工时估算功能。用户可以输入需求描述，AI 会自动将需求拆分为可执行的子任务，并为每个子任务预估人天。

## 功能特性

1. **需求拆分**：将复杂需求自动拆分为具体的子任务
2. **工时估算**：为每个子任务预估人天（单位：人天）
3. **任务分类**：自动识别任务类型（前端开发、后端开发、测试等）
4. **优先级设置**：为每个子任务设置优先级（高、中、低）
5. **依赖关系**：识别子任务之间的依赖关系
6. **风险评估**：识别潜在风险
7. **技术建议**：提供技术实现建议

## 配置说明

### 1. 配置 DeepSeek API Key

在启动应用前，需要设置 `DEEPSEEK_API_KEY` 环境变量：

**Windows:**
```bash
set DEEPSEEK_API_KEY=your_api_key_here
```

**Linux/Mac:**
```bash
export DEEPSEEK_API_KEY=your_api_key_here
```

**Maven 启动时设置:**
```bash
cd backend
DEEPSEEK_API_KEY=your_api_key_here mvn spring-boot:run
```

**IDE 启动时设置:**
在 IDE 的运行配置中添加环境变量：`DEEPSEEK_API_KEY=your_api_key_here`

### 2. 获取 API Key

1. 访问 DeepSeek 官网：https://platform.deepseek.com/
2. 注册账号并登录
3. 在控制台中创建 API Key
4. 复制 API Key 并配置到环境变量中

## API 使用说明

### 接口地址

```
POST /api/ai/breakdown-requirement
```

### 请求参数

```json
{
  "requirement": "开发一个用户登录功能，包括用户名密码登录、记住密码、找回密码功能",
  "projectType": "Web应用",
  "teamSize": 3,
  "expectedDays": 14
}
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| requirement | String | 是 | 需求描述，最多5000字符 |
| projectType | String | 否 | 项目类型（如：Web应用、移动应用等） |
| teamSize | Integer | 否 | 团队规模（人数） |
| expectedDays | Integer | 否 | 期望完成时间（天） |

### 响应示例

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
      },
      {
        "sequence": 3,
        "title": "后端登录接口开发",
        "description": "开发登录验证、Token生成、密码找回等接口",
        "estimatedDays": 3.0,
        "taskType": "后端开发",
        "priority": "高",
        "dependsOn": 2,
        "notes": "使用JWT进行身份验证"
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

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| summary | String | 需求概述 |
| subTasks | Array | 子任务列表 |
| sequence | Integer | 子任务序号 |
| title | String | 子任务标题 |
| description | String | 子任务详细描述 |
| estimatedDays | Double | 预估人天 |
| taskType | String | 任务类型 |
| priority | String | 优先级（高/中/低） |
| dependsOn | Integer | 依赖的任务序号 |
| notes | String | 备注信息 |
| totalEstimatedDays | Double | 总预估人天 |
| suggestedTeamSize | Integer | 建议团队规模 |
| suggestedIterationDays | Integer | 建议迭代周期（天） |
| risks | Array<String> | 风险列表 |
| suggestions | Array<String> | 技术建议列表 |
| notes | String | 其他备注 |

### 其他接口

**检查 AI 服务状态**
```
GET /api/ai/status
```

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

## Swagger 文档

启动应用后，访问 Swagger UI 查看完整的 API 文档：
```
http://localhost:8080/swagger-ui.html
```

## 使用示例

### cURL 示例

```bash
curl -X POST http://localhost:8080/api/ai/breakdown-requirement \
  -H "Content-Type: application/json" \
  -d '{
    "requirement": "开发一个用户登录功能，包括用户名密码登录、记住密码、找回密码功能",
    "projectType": "Web应用",
    "teamSize": 3
  }'
```

### JavaScript 示例

```javascript
async function breakdownRequirement(requirement) {
  const response = await fetch('http://localhost:8080/api/ai/breakdown-requirement', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      requirement: requirement,
      projectType: 'Web应用',
      teamSize: 3
    })
  });

  const result = await response.json();
  return result.data;
}

// 使用示例
const result = await breakdownRequirement(
  '开发一个用户登录功能，包括用户名密码登录、记住密码、找回密码功能'
);
console.log('总预估人天:', result.totalEstimatedDays);
console.log('子任务列表:', result.subTasks);
```

## 错误处理

### 常见错误

**1. API Key 未配置**
```
错误信息：DeepSeek API 调用失败: HTTP 401
解决方法：检查 DEEPSEEK_API_KEY 环境变量是否正确设置
```

**2. 需求描述为空**
```
错误信息：需求描述不能为空
解决方法：确保 request 字段不为空且长度不超过5000字符
```

**3. AI 响应解析失败**
```
错误信息：需求拆分失败: AI 响应解析失败
解决方法：检查 API 响应格式，可能需要联系技术支持
```

**4. 网络超时**
```
错误信息：DeepSeek API 调用失败: timeout
解决方法：检查网络连接，或增加超时时间配置
```

## 配置参数

在 `application.yml` 中可以调整以下配置：

```yaml
deepseek:
  api:
    key: ${DEEPSEEK_API_KEY:}  # API Key（从环境变量读取）
    url: https://api.deepseek.com/v1/chat/completions  # API 地址
    model: deepseek-chat  # 使用的模型
    timeout: 60000  # 请求超时时间（毫秒）
    max-retries: 3  # 最大重试次数
```

## 工时估算标准

AI 按照以下标准进行工时估算：

- **简单任务**：0.5-1 人天
- **中等任务**：1-3 人天
- **复杂任务**：3-5 人天
- **非常复杂任务**：5+ 人天

## 任务类型

系统支持的任务类型包括：

- 需求分析
- 系统设计
- 前端开发
- 后端开发
- 数据库设计
- 接口开发
- 测试
- 部署
- 其他

## 注意事项

1. **API Key 安全**：不要将 API Key 提交到代码仓库，使用环境变量管理
2. **配额限制**：DeepSeek API 有调用配额限制，合理使用避免超限
3. **响应时间**：AI 分析需要一定时间，通常在 3-10 秒之间
4. **结果仅供参考**：AI 预估的工时仅供参考，实际工时可能因团队和技术栈而异
5. **网络要求**：需要能够访问 DeepSeek API（https://api.deepseek.com）

## 技术实现

- **HTTP 客户端**：OkHttp 4.12.0
- **JSON 处理**：Jackson
- **AI 模型**：DeepSeek Chat
- **重试机制**：自动重试 3 次，间隔 1 秒

## 后续扩展

可以基于此功能扩展更多 AI 能力：

1. 代码审查辅助
2. 技术方案生成
3. 测试用例生成
4. 文档自动生成
5. 代码重构建议

## 支持

如有问题，请联系技术支持或查看项目文档。
