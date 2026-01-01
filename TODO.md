# 待办事项

## API 文档优化

### 1. Swagger 中日期时间类型的默认值显示
- [ ] 研究如何在 Swagger/OpenAPI 中为 Date 类型字段设置默认示例值
- [ ] 当前问题：Swagger UI 中日期字段没有显示默认值示例
- [ ] 可能方案：
  - 使用 `@Schema(example = "2024-01-01T00:00:00")` 注解
  - 配置全局的日期格式化
  - 自定义 `ApiModelProperty` 或 `Schema` 注解处理器

### 2. 枚举类型在 API JSON 中的表示方式
- [ ] 研究枚举类型在 API 请求/响应中的最佳实践
- [ ] 当前问题：
  - GET 请求查询参数：`/api/projects?status=1` vs `?status=NOT_STARTED`
  - 已实现 String → Enum 转换器，但测试仍有问题（testGetProjectsByStatus 失败）
- [ ] 需要评估的方案：
  - **方案1：使用 String 类型**
    - 优点：简单、灵活、前后端解耦
    - 缺点：类型不安全、需要手动验证
  - **方案2：使用 Integer 类型**
    - 优点：节省空间、数据库友好
    - 缺点：语义不明、可读性差
  - **方案3：使用 Enum 类型（当前）**
    - 优点：类型安全、自文档化
    - 缺点：需要转换器、前后端耦合
- [ ] 参考行业标准：RESTful API 设计规范、其他项目的实践

## 相关文件
- `src/main/java/com/gsms/gsms/infra/converter/StringToEnumConverterFactory.java` - String 到枚举的转换器
- `src/main/java/com/gsms/gsms/dto/project/ProjectQueryReq.java` - 查询请求 DTO
- `src/test/java/com/gsms/gsms/controller/ProjectControllerTest.java` - 控制器测试
