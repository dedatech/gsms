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
│   │   │       ├── controller/                  # API控制器层
│   │   │       ├── domain/                      # 领域层
│   │   │       │   ├── entity/                  # 实体类
│   │   │       │   └── enums/                   # 枚举类
│   │   │       ├── dto/                         # 数据传输对象层
│   │   │       │   ├── converter/               # 对象转换器
│   │   │       │   ├── project/                 # 项目相关DTO
│   │   │       │   ├── task/                    # 任务相关DTO
│   │   │       │   ├── user/                    # 用户相关DTO
│   │   │       │   └── workhour/                # 工时相关DTO
│   │   │       ├── infra/                       # 基础设施层
│   │   │       │   ├── common/                  # 通用返回结果
│   │   │       │   ├── config/                  # 配置类
│   │   │       │   ├── exception/               # 异常类
│   │   │       │   └── utils/                   # 工具类
│   │   │       ├── repository/                  # 数据访问层（原mapper）
│   │   │       └── service/                     # 业务逻辑层
│   │   │           └── impl/                    # 服务实现类
│   │   └── resources/
│   │       ├── mapper/                          # MyBatis映射文件
│   │       └── application.yml                  # 配置文件
│   └── test/                                    # 测试代码
├── sql/                                         # 数据库脚本
└── pom.xml                                      # Maven配置文件
```

### 架构分层说明

项目采用标准三层架构结合DTO模式，具有清晰的分层结构：

```
┌─────────────────────────────────────┐
│         Controller (API层)          │  ← 接收HTTP请求，参数校验
├─────────────────────────────────────┤
│      DTO (数据传输对象 + 转换器)      │  ← API层与业务层的数据传输
├─────────────────────────────────────┤
│        Service (业务逻辑层)          │  ← 核心业务逻辑处理
├─────────────────────────────────────┤
│      Domain (领域模型：实体+枚举)     │  ← 数据库实体和业务枚举
├─────────────────────────────────────┤
│      Repository (数据访问层)         │  ← 数据持久化操作
├─────────────────────────────────────┤
│    Infra (基础设施：配置+工具+异常)   │  ← 技术基础设施支撑
└─────────────────────────────────────┘
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

## 项目架构评估与建设规划

### 当前架构评分：**85/100** ⭐⭐⭐⭐

#### 评分维度

| 维度 | 得分 | 评价 |
|------|------|------|
| **分层架构** | 18/20 | ⭐⭐⭐⭐⭐ 三层架构+DTO模式+分层清晰 |
| **代码规范** | 16/20 | ⭐⭐⭐⭐ DTO模式+枚举+参数校验 |
| **工程化** | 14/20 | ⭐⭐⭐⭐ API文档+日志+多环境配置 |
| **可维护性** | 16/20 | ⭐⭐⭐⭐ 包结构清晰+职责单一 |
| **技术选型** | 11/20 | ⭐⭐⭐ Java 8 + Spring Boot 2.7 偏旧 |

#### 核心优势 ✅

1. **架构设计优秀**
   - 标准三层架构：Controller → Service → Repository
   - DTO模式应用完善：API层和持久层分离
   - 职责清晰：每层职责明确，依赖关系合理
   - 基础设施分离：infra包含config、utils、exception

2. **代码质量高**
   - 统一返回结构：`Result<T>`封装
   - 统一异常处理：`GlobalExceptionHandler`
   - 类型安全：状态字段使用枚举而非魔法值
   - 参数校验：JSR-303注解（`@NotBlank`、`@NotNull`）

3. **工程实践规范**
   - API文档：Springdoc OpenAPI集成
   - 日志管理：Logback配置完善
   - 多环境配置：dev/prod分离
   - 版本控制：Git + `.gitignore`

#### 改进方向 ⚠️

**短期优化（1-2周）- 目标：90分**

1. **升级技术栈** (+3分)
   ```xml
   <java.version>17</java.version>
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.2.0</version>
   </parent>
   ```
   - Java 17：性能提升、Records、Pattern Matching等新特性
   - Spring Boot 3.x：更好的性能和安全性

2. **添加缓存支持** (+2分)
   ```xml
   <!-- Redis缓存 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-redis</artifactId>
   </dependency>
   <!-- Caffeine本地缓存 -->
   <dependency>
       <groupId>com.github.ben-manes.caffeine</groupId>
       <artifactId>caffeine</artifactId>
   </dependency>
   ```
   - 减少数据库压力
   - 提升系统响应速度

3. **增强监控能力** (+2分)
   ```xml
   <!-- Spring Boot Actuator -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   <!-- Micrometer Prometheus -->
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-prometheus</artifactId>
   </dependency>
   ```
   - 健康检查、性能指标
   - Prometheus + Grafana可视化

**中期优化（1-2月）- 目标：95分**

4. **链路追踪**
   - 集成 SkyWalking Agent
   - 日志添加 TraceId
   - 问题快速定位

5. **消息队列**
   - RabbitMQ/Kafka
   - 异步任务处理
   - 工时审批通知解耦

6. **容器化部署**
   ```dockerfile
   FROM eclipse-temurin:17-jre-alpine
   COPY target/*.jar app.jar
   ENTRYPOINT ["java", "-jar", "/app.jar"]
   ```
   - Docker镜像
   - Docker Compose本地开发
   - K8s生产部署

**长期优化（3-6月）- 目标：100分**

7. **微服务改造**（按需）
   ```
   gsms-auth-service      # 认证服务
   gsms-project-service   # 项目服务
   gsms-task-service      # 任务服务
   gsms-workhour-service  # 工时服务
   gsms-gateway           # API网关
   ```

8. **完善安全机制**
   - OAuth 2.0 / OIDC
   - 接口加密签名
   - 防重放攻击

9. **性能优化**
   - 读写分离
   - 分库分表（ShardingSphere）
   - ElasticSearch全文检索

#### 适用场景评估

| 场景 | 评分 | 说明 |
|------|------|------|
| **个人学习项目** | 95/100 | ⭐⭐⭐⭐⭐ 架构设计非常优秀 |
| **小型团队（<10人）** | 85/100 | ⭐⭐⭐⭐ 功能完备，可直接使用 |
| **中型企业（10-50人）** | 75/100 | ⭐⭐⭐⭐ 需添加缓存、监控 |
| **大型企业（>50人）** | 60/100 | ⭐⭐⭐ 需微服务改造 |
| **互联网高并发** | 50/100 | ⭐⭐⭐ 需全面升级架构 |

#### 总结

> **当前项目在小型项目和学习场景下是一个优秀的架构设计（95分）。**
> 
> **在生产环境和商业项目中，需要补齐缓存、监控、消息队列等基础设施。**
> 
> **整体来说，这是一个架构设计规范、代码质量高的项目，技术栈需要现代化升级。**

**优先级建议：**
- 🔴 **高优先级**：升级 Java 17 + Spring Boot 3.x
- 🟡 **中优先级**：添加 Redis 缓存 + Actuator 监控
- 🟢 **低优先级**：微服务改造（按业务需求）

---

## 许可证

本项目仅供学习和参考使用。