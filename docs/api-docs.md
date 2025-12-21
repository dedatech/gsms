# API文档说明

## 概述

本系统使用Springdoc OpenAPI集成，自动生成RESTful API文档。Springdoc是Swagger的一个现代化替代品，支持OpenAPI 3规范，相比传统的Springfox更加轻量且性能更好。

## 访问API文档

启动应用程序后，可以通过以下URL访问API文档：

- Swagger UI界面：`http://localhost:8080/swagger-ui.html`
- API文档JSON格式：`http://localhost:8080/v3/api-docs`
- Redoc界面（可选）：`http://localhost:8080/swagger-ui/index.html`

## API文档结构

### 1. 接口分组

API文档按照业务功能分为以下几个分组：

1. **用户管理接口** - 用户相关的操作接口
2. **项目管理接口** - 项目相关的操作接口
3. **任务管理接口** - 任务相关的操作接口
4. **工时管理接口** - 工时记录相关的操作接口

### 2. 接口信息

每个接口包含以下信息：

- **接口名称** - 简洁的接口功能描述
- **接口说明** - 详细的接口功能说明
- **请求方法** - GET、POST、PUT、DELETE等HTTP方法
- **请求路径** - 接口的URL路径
- **请求参数** - 包括路径参数、查询参数、请求体参数等
- **响应格式** - 返回数据的结构和示例
- **状态码** - HTTP状态码和业务状态码说明

## 使用Swagger UI

### 1. 查看接口文档

在Swagger UI界面中，可以展开每个接口查看详细信息，包括：

- 接口描述和备注
- 参数说明和示例
- 响应模型和示例
- 错误码说明

### 2. 在线测试接口

Swagger UI提供了在线测试功能：

1. 点击接口旁的"Try it out"按钮
2. 填写必要的参数值
3. 点击"Execute"按钮发送请求
4. 查看响应结果和curl命令

### 3. 认证授权

对于需要认证的接口：

1. 点击右上角的"Authorize"按钮
2. 输入JWT Token：`Bearer {your_token}`
3. 点击"Authorize"完成授权
4. 之后所有受保护的接口都可以正常测试

## 文档维护

### 1. 添加新的API文档

在Controller类和方法上添加相应的注解：

```java
@Tag(name = "接口分组名称", description = "接口分组描述")
@Operation(summary = "接口名称", description = "接口详细说明")
@Parameter(description = "参数说明")
```

### 2. 更新文档

当接口发生变化时，只需更新相应的注解，文档会自动更新。

## 导出文档

可以通过以下方式导出API文档：

1. **JSON格式**：访问`http://localhost:8080/v3/api-docs`获取原始JSON数据
2. **YAML格式**：访问`http://localhost:8080/v3/api-docs.yaml`获取YAML格式数据
3. **HTML格式**：使用第三方工具将JSON转换为静态HTML文档
4. **PDF格式**：通过浏览器打印功能将Swagger UI页面保存为PDF

## 最佳实践

1. **保持同步**：确保代码注解与实际接口实现保持一致
2. **详细描述**：为每个接口和参数提供清晰的说明
3. **示例数据**：在实体类中提供合理的示例数据
4. **版本管理**：随着API演进，及时更新版本号和变更说明