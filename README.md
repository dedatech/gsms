# 工时管理系统 (GSMS)

## 项目简介

工时管理系统（GSMS - Gantt System Management System）是一个专注于研发团队使用的轻量级工时管理工具。系统旨在实现"员工填报便捷、管理统计清晰"，在保证核心体验流畅的前提下，摒弃复杂冗余的功能。

## 功能模块

1. **项目管理**：项目创建、编辑、成员管理、状态跟踪
2. **任务管理**：任务创建、分配、进度跟踪、工时关联
3. **我的工时**：工时填报、日历视图、个人统计
4. **数据统计**：多维度数据分析、图表展示、报表导出
5. **系统设置**：用户管理、权限控制、基础数据维护

## 技术栈

- 后端：Java + Spring Boot + MyBatis
- 数据库：MySQL
- 安全认证：JWT Token
- 构建工具：Maven

## 项目结构

```
gsms/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/gsms/gsms/
│   │   │       ├── GsmsApplication.java         # 启动类
│   │   │       ├── common/                      # 通用类
│   │   │       ├── config/                      # 配置类
│   │   │       ├── controller/                  # 控制器层
│   │   │       ├── entity/                      # 实体类
│   │   │       ├── mapper/                      # 数据访问层
│   │   │       ├── service/                     # 业务逻辑层
│   │   │       └── utils/                       # 工具类
│   │   └── resources/
│   │       ├── mapper/                          # MyBatis映射文件
│   │       └── application.yml                  # 配置文件
│   └── test/                                    # 测试代码
├── sql/                                         # 数据库脚本
└── pom.xml                                      # Maven配置文件
```

## 数据库设计

数据库设计文件位于 `sql/gsms_ddl.sql`，包含以下主要表：

- 用户表 (user)
- 部门表 (department)
- 项目表 (project)
- 项目成员表 (project_member)
- 迭代表 (iteration)
- 任务表 (task)
- 工时记录表 (work_hour)
- 角色表 (role)
- 用户角色关联表 (user_role)
- 权限表 (permission)
- 角色权限关联表 (role_permission)
- 操作日志表 (operation_log)

## 快速开始

1. 确保已安装 Java 8+ 和 Maven
2. 创建 MySQL 数据库并执行 `sql/gsms_ddl.sql` 脚本
3. 修改 `src/main/resources/application.yml` 中的数据库连接配置
4. 在项目根目录执行 `mvn spring-boot:run` 启动应用
5. 访问 `http://localhost:8080/hello` 验证应用是否正常运行

## API接口

### 公共接口
- GET `/hello` - 测试接口
- POST `/api/users/login` - 用户登录

### 受保护接口（需要JWT Token）
- GET `/api/hello` - 受保护的测试接口
- GET `/api/users` - 获取所有用户
- GET `/api/users/{id}` - 根据ID获取用户
- POST `/api/users` - 创建用户
- PUT `/api/users` - 更新用户
- DELETE `/api/users/{id}` - 删除用户

### API文档
系统集成了Springdoc OpenAPI，可以自动生成和展示API文档。

访问地址：
- Swagger UI界面：`http://localhost:8080/swagger-ui.html`
- API文档JSON格式：`http://localhost:8080/v3/api-docs`
- Redoc界面（可选）：`http://localhost:8080/swagger-ui/index.html`

通过Swagger UI界面，您可以：
1. 查看所有API接口的详细说明
2. 直接在线测试API接口
3. 查看请求参数和响应格式
4. 获取JWT Token并用于受保护接口的测试

## 开发规范

1. 使用RESTful风格的API设计
2. 统一的返回结果格式：`{"code": 200, "message": "success", "data": {}}`
3. 使用JWT Token进行身份认证
4. 遵循MyBatis最佳实践编写SQL映射
5. 使用SLF4J进行日志记录，按业务重要性选择合适的日志级别

## 日志配置

项目使用Logback作为日志框架，配置文件位于`src/main/resources/logback-spring.xml`。

### 日志级别
- DEBUG: 详细调试信息，仅在开发环境启用
- INFO: 一般信息，记录关键业务流程
- WARN: 警告信息，不影响系统运行但需要注意
- ERROR: 错误信息，系统发生错误需要关注

### 日志输出
- 控制台输出: 开发环境实时查看日志
- 文件输出: 按日志级别分别记录到不同文件
  - info.log: 一般信息日志
  - warn.log: 警告信息日志
  - error.log: 错误信息日志

日志文件默认存储在项目根目录的`logs`文件夹中。

## 许可证

本项目仅供学习和参考使用。