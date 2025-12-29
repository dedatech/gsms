# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供在此代码库中工作的指导。

## 项目概述

GSMS（工时管理系统）是一个面向研发团队的轻量级工时管理系统。这是一个基于 Spring Boot 的应用，采用 DDD（领域驱动设计）原则，具有清晰的分层架构。

**技术栈：**
- Java 8 + Spring Boot 2.7.0
- MyBatis-Plus 3.5.3.1 + MySQL 8.0
- JWT 认证 (jjwt 0.9.1)
- Maven 构建管理
- SpringDoc OpenAPI API文档

## 常用命令

### 构建和运行
```bash
# 构建项目
mvn clean install

# 运行应用（开发环境）
mvn spring-boot:run

# 使用指定配置文件运行
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 测试
```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserControllerTest

# 运行指定测试方法
mvn test -Dtest=UserControllerTest#testGetUserById

# 运行所有Controller测试
mvn test -Dtest=*ControllerTest
```

### 数据库
```bash
# Flyway数据库迁移（启动时自动执行）
# 手动执行迁移：
mvn flyway:migrate

# 查看Flyway状态
mvn flyway:info
```

## 架构设计

### 分层结构（DDD思想）

```
controller/         # REST API层 - 处理HTTP请求、参数校验
├── dto/           # 数据传输对象（独立包）
├── service/       # 业务逻辑层
│   └── impl/      # 服务实现类
├── domain/        # 领域层
│   ├── entity/    # JPA实体（数据库模型）
│   └── enums/     # 业务枚举（包括错误码）
├── repository/    # 数据访问层（MyBatis Mapper接口）
└── infra/         # 基础设施层
    ├── common/    # 通用组件（Result、PageResult）
    ├── config/    # Spring配置类
    ├── exception/ # 自定义异常
    └── utils/     # 工具类
```

**数据流：**
1. **Controller** 接收HTTP请求，使用 `@Valid` 校验，调用Service
2. **DTO** 对象在Controller和Service层之间传输数据
3. **Service** 实现业务逻辑，使用 `UserContext` 获取当前用户
4. **Repository**（MyBatis Mapper）处理数据库操作
5. **Domain** 实体表示数据库表结构
6. **Result<T>** 包装所有API响应为统一格式

### 关键设计模式

**DTO模式：**
- **CreateReq** DTO用于创建实体（排除`id`、`createTime`等字段）
- **UpdateReq** DTO用于更新（包含`id`，字段可选）
- **InfoResp** DTO用于API响应（排除敏感字段）
- **QueryReq** DTO用于搜索/过滤和分页（继承`BasePageQuery`）
- `dto/*/converter/`中的转换器处理 Entity ↔ DTO 映射

**Service接口模式：**
- 所有服务在`service/`中有接口，在`service/impl/`中有实现
- 方法返回Entity（内部使用）或DTO（API响应）
- **重要：** Service方法现在使用DTO参数（如`create(UserCreateReq)`）而非Entity

**认证和授权：**
- 通过`JwtInterceptor`实现基于JWT Token的无状态认证
- `UserContext.getCurrentUserId()`获取已认证用户ID
- `AuthService`提供权限检查：
  - `checkProjectAccess(userId, projectId)` - 项目成员检查
  - `checkTaskAccess(userId, taskId)` - 任务可见性检查
  - `canViewAllProjects(userId)` - 全局项目查看权限
- 基于角色/权限表的访问控制

**分页：**
- MyBatis查询使用`PageHelper.startPage(pageNum, pageSize)`
- `PageResult<T>`包装分页结果及元数据
- 请求DTO继承`BasePageQuery`（默认：pageNum=1, pageSize=10）

## 关键实现细节

### 用户上下文管理
始终使用`executeWithUserContext`包装需要用户上下文的操作：
```java
executeWithUserContext(userId, () -> {
    // 需要 UserContext.getCurrentUserId() 才能正常工作的代码
    return result;
});
```
这在测试的`@BeforeEach`设置中创建测试数据时特别重要。

### 实体审计字段
大多数实体具有以下审计字段（自动管理）：
- `createTime` / `updateTime` - 时间戳
- `createUserId` / `updateUserId` - 用户追踪
- `isDeleted` - 软删除标志（0=有效，1=已删除）

**重要：** 始终在Service的create方法中设置`createUserId`和`updateUserId`：
```java
workHour.setCreateUserId(currentUserId);
workHour.setUpdateUserId(currentUserId);
```

### 错误处理
- 自定义异常继承`BusinessException`
- 错误码定义在`domain/enums/errorcode/`包中
- `GlobalExceptionHandler`捕获所有异常并返回标准`Result`格式
- 常见错误码：`UNAUTHORIZED(1401)`、`FORBIDDEN(1403)`、`NOT_FOUND`系列

### 参数校验
- 在DTO上使用JSR-303注解：`@NotBlank`、`@NotNull`、`@Email`等
- Controller方法在请求体参数上使用`@Valid`或`@Validated`
- Service层的自定义校验规则抛出`BusinessException`

### MyBatis Mapper XML
位于`src/main/resources/mapper/`。
- 使用`<resultMap>`进行复杂实体映射
- 使用`<sql id="selectAllFields">`避免重复列列表
- 使用`typeHandler="com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler"`处理枚举类型

## 测试策略

### 测试结构
```
src/test/java/com/gsms/gsms/
├── config/
│   └── TestMyBatisConfig.java       # MyBatis测试配置
├── controller/
│   ├── BaseControllerTest.java      # Controller测试基类
│   └── *ControllerTest.java         # 各Controller的集成测试
├── service/
│   └── *ServiceTest.java            # Service层测试
└── repository/
    └── *MapperTest.java             # Mapper层测试
```

### Controller集成测试
- 继承`BaseControllerTest`（使用`@SpringBootTest`和真实Service、MySQL测试数据库）
- 在`@BeforeEach`中创建测试用户并使用`JwtUtil`生成JWT Token
- 使用`executeWithUserContext()`创建测试数据以设置UserContext
- 使用`objectMapper.writeValueAsString()`将DTO序列化为JSON
- 测试成功和失败场景（401、403、404）

### Service层测试
- 对依赖项使用`@MockBean`（其他Service、Mapper）
- 测试业务逻辑和错误处理
- 验证与Mapper的交互

### 单元测试最佳实践
- 使用MySQL测试数据库（gsms_test）进行集成测试
- 必要时在`@AfterEach`中清理测试数据
- 避免硬编码ID - 使用创建的测试数据的ID
- 使用描述性测试方法名：`test{方法名}_{场景}_{预期结果}`

## API文档

Swagger UI地址：`http://localhost:8080/swagger-ui.html`

**常用端点：**
- POST `/api/users/login` - 获取JWT Token（设置到`Authorization: Bearer <token>`请求头）
- GET `/api/users` - 用户列表（分页）
- GET `/api/projects` - 项目列表（需要项目成员权限）
- GET `/api/tasks/search` - 按条件搜索任务
- POST `/api/work-hours` - 创建工时记录

## 配置文件

- `application.yml` - 主配置（数据库、MyBatis、PageHelper、日志）
- `application-dev.yml` - 开发环境配置覆盖
- `application-prod.yml` - 生产环境配置覆盖
- `logback-spring.xml` - 日志配置（输出到`logs/`目录）
- `src/main/resources/mapper/`中的Mapper XML - MyBatis SQL映射

## 常见陷阱

1. **Service方法签名已变更：** 许多Service现在接受DTO而非Entity
   - 旧版：`createUser(User user)`
   - 新版：`create(UserCreateReq req)`

2. **缺少审计字段：** 忘记设置`createUserId`/`updateUserId`会导致SQL错误

3. **权限检查：** Controller通过`AuthService`检查权限，因此测试必须：
   - 创建具有适当成员关系的测试项目
   - 使用具有相应权限的用户
   - 权限检查在存在性检查之前失败时，期望403（而非404）

4. **查询参数绑定：** 测试带查询参数的GET请求时，使用`.param("key", "value")`而非`.queryParam()`

5. **JWT Token必需：** 除`/api/users/login`和`/hello`外的所有端点都需要`Authorization: Bearer <token>`请求头

6. **PageHelper使用：** 必须在执行查询**之前**调用`PageHelper.startPage()`，且只对之后执行的**第一个**查询生效

## 文件命名规范

- 实体：`User.java`、`Project.java`、`WorkHour.java`
- DTO：`UserCreateReq.java`、`UserInfoResp.java`、`UserQueryReq.java`
- Controller：`UserController.java`
- Service：`UserService.java`（接口）、`UserServiceImpl.java`（实现）
- Mapper：`UserMapper.java`（接口）、`UserMapper.xml`（SQL）
- 测试：`UserControllerTest.java`、`UserServiceTest.java`
