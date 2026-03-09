# AI 拆分接口测试说明

## 测试结果

### 后端 API 测试 ✅
- 测试时间：76 秒
- 结果：成功
- 拆分子任务数：10 个
- 总预估工时：16.0 人天

### 修复的超时配置

#### 后端配置（application.yml）
```yaml
deepseek:
  api:
    timeout: 180000  # 3 分钟（之前是 60 秒）
```

#### 前端配置（src/api/ai.ts）
```typescript
export const breakdownRequirement = (data: RequirementBreakdownReq) => {
  const config: AxiosRequestConfig = {
    timeout: 120000 // 2 分钟
  }
  return request.post('/ai/breakdown-requirement', data, config)
}
```

## 如何测试

1. **确认后端服务已重启并加载新配置**
   - 查看后端日志，确认配置已加载
   - 或者手动重启后端服务

2. **刷新前端页面**
   - 按 Ctrl + F5 强制刷新浏览器
   - 或者清除浏览器缓存后刷新

3. **测试步骤**
   - 进入 项目详情 → 迭代页签
   - 找到任意根需求（一级任务）
   - 点击"AI拆分"按钮
   - 填写需求信息：
     ```
     需求描述：开发一个用户登录功能
     项目类型：Web 应用
     团队规模：3
     期望时间：14
     ```
   - 点击"开始 AI 拆分"
   - 耐心等待 1-2 分钟
   - 查看拆分结果

## 预期时间

- 简单需求：30-60 秒
- 复杂需求：60-120 秒
- 超时设置：前端 120 秒，后端 180 秒

## 如果仍然超时

1. 检查网络连接
2. 查看 DeepSeek API 状态
3. 检查后端日志是否有错误
