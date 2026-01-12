# 待办事项

## 用户、角色、权限管理（RBAC）- 已完成 ✅

### ✅ 已完成的功能（2026-01-12）

**后端 API：**
- [x] 用户管理 - CRUD、角色分配、启用/禁用
- [x] 角色管理 - CRUD、权限分配
- [x] 权限管理 - CRUD、权限查询
- [x] 用户-角色关联 API
- [x] 角色-权限关联 API
- [x] 用户注册功能 - 默认禁用状态，需管理员审核

**前端页面：**
- [x] 用户管理页面（UserList.vue）- 完整 CRUD + 角色分配 + 启用/禁用按钮
- [x] 角色管理页面（RoleList.vue）- 权限分配对话框
- [x] 权限管理页面（PermissionList.vue）- 权限列表和查看使用情况
- [x] 注册页面（RegisterView.vue）- 注册成功提示"请联系管理员审核"

**权限控制体系：**
- [x] v-permission 指令 - 按钮级权限控制
- [x] 路由守卫 - 路由级权限控制
- [x] AuthStore 扩展 - 权限和角色状态管理
- [x] 登录时检查用户状态 - 禁用用户无法登录

**系统管理菜单：**
- [x] 侧边栏添加"系统管理"菜单（Operation 图标）
- [x] 包含：用户管理、角色管理、权限管理三个子菜单

### 📋 待完成的功能

**高级功能：**
- [ ] 操作日志记录（OperationLog）
  - 记录角色创建/更新/删除
  - 记录权限创建/更新/删除
  - 记录用户角色分配/移除
  - 记录角色权限分配/移除
  - 操作日志查询页面

- [ ] 权限模板功能（RoleTemplate）
  - 预置角色模板（系统管理员、项目经理、普通员工）
  - 快速基于模板创建角色
  - 模板管理页面

**数据级权限控制：**
- [ ] 部门级数据权限过滤
- [ ] 项目级数据权限过滤
- [ ] 自定义数据权限规则

**性能优化：**
- [ ] 权限缓存优化（前端 + 后端）
- [ ] 批量权限检查接口
- [ ] 表格虚拟滚动（大数据量）

---

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
