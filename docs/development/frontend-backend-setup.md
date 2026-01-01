# GSMS 前后端联调指南

## 开发环境配置

### 后端配置（Spring Boot）

**端口**: 8080

**CORS 配置**已在 `WebConfig.java` 中配置，允许前端跨域访问。

### 前端配置（Vite）

**端口**: 3000

**代理配置** (`vite.config.ts`):
```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

## 启动步骤

### 1. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

### 2. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:3000 启动

### 3. 访问应用

打开浏览器访问 http://localhost:3000

## API 调用说明

### 开发环境
- 前端通过 Vite 代理访问后端
- 前端请求 `/api/*` → Vite 转发到 `http://localhost:8080/api/*`
- 无需配置 CORS

### 生产环境
- 需要配置 Nginx 反向代理
- 后端和前端部署在同一服务器上

## 常见问题

### 1. CORS 错误

**问题**: 前端请求报错 `No 'Access-Control-Allow-Origin' header`

**解决**:
- 确保后端 `WebConfig.java` 中 CORS 配置正确
- 检查前端代理配置

### 2. 401 Unauthorized

**问题**: 请求返回 401 错误

**解决**:
- 检查是否已登录
- localStorage 中的 token 是否有效
- 检查后端 JWT 拦截器配置

### 3. 端口冲突

**问题**: 8080 或 3000 端口被占用

**解决**:
- 修改 `application.yml` 中的端口配置
- 修改 `vite.config.ts` 中的端口配置

## 调试技巧

### 后端调试

1. 使用 IDEA 的 Debug 模式启动后端
2. 在代码中添加断点
3. 通过 Postman 或 Swagger UI 测试 API

### 前端调试

1. 使用浏览器开发者工具 (F12)
2. 查看 Console 和 Network 面板
3. 使用 Vue DevTools 浏览组件状态

### 联调调试

1. 先测试后端 API 是否正常（Swagger UI）
2. 使用浏览器 Network 面板查看请求和响应
3. 检查请求头中的 Authorization token
4. 查看后端日志输出
