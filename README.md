# TeamMaster - 统领工时管理平台

<div align="center">

![Java](https://img.shields.io/badge/Java-8-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

**统领团队每一刻** - 面向研发团队的轻量级工时管理平台

[在线文档](#) · [快速开始](#快速开始) · [API 文档](#api-文档) · [贡献指南](#贡献指南)

</div>

---

## 📋 目录

- [项目背景](#项目背景)
- [核心功能](#核心功能)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [快速开始](#快速开始)
- [开发指南](#开发指南)
- [Roadmap](#roadmap)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 项目背景

### 为什么需要 TeamMaster？

在研发团队的日常管理中，我们遇到了这些痛点：

- ❌ **工时统计困难** - 无法准确统计项目、任务的实际工时投入
- ❌ **进度跟踪不透明** - 项目进度依赖人工汇报，数据不及时
- ❌ **资源分配不合理** - 缺乏数据支撑，难以优化人员配置
- ❌ **现有工具过于复杂** - JIRA、Trello 等工具功能过多，学习成本高

### 我们的解决方案

**TeamMaster** 专注于研发团队的工时管理核心需求：

✅ **简单易用** - 员工3分钟即可完成工时填报
✅ **数据驱动** - 多维度统计分析，辅助管理决策
✅ **轻量高效** - 核心功能聚焦，拒绝冗余
✅ **开源免费** - 团队可自主部署，数据完全掌控

### 🤖 AI 辅助开发

本项目积极探索**AI 驱动的软件研发新范式**，采用 **Vibe Coding** 模式进行迭代开发：

> **Vibe Coding** - 在保持对代码整体掌控的前提下，将编码工作委托给 AI，开发者专注于架构设计、业务逻辑和代码审查。

**我们的实践**：
- 🚀 **提升开发效率** - AI 辅助代码生成，开发速度提升 3-5 倍
- 🎯 **聚焦核心价值** - 开发者从"编码者"转变为"架构师"和"审查者"
- 🔄 **快速迭代** - 借助 AI 快速实现功能，持续验证和优化
- 📚 **知识沉淀** - 通过与 AI 的交互，记录技术决策和最佳实践

**为什么选择 AI 辅助开发？**

传统软件开发模式正在被 AI 重新定义：
- **代码生成**: AI 可以快速生成样板代码、CRUD 操作、测试用例
- **代码审查**: AI 可以发现潜在 bug、性能问题、安全漏洞
- **文档编写**: AI 可以自动生成 API 文档、注释、README
- **问题诊断**: AI 可以快速定位错误、提供解决方案

我们相信，**未来的软件开发 = 人类创意 + AI 效率**。

---

## 核心功能

### 1️⃣ 项目管理
- 📁 项目创建与配置
- 👥 项目成员管理与角色分配
- 📊 项目状态跟踪（未开始/进行中/已挂起/已归档）
- 📅 项目计划时间与实际时间对比

### 2️⃣ 迭代管理
- 🔄 Sprint 迭代创建与规划
- ⏱️ 迭代周期管理
- 📈 迭代进度跟踪

### 3️⃣ 任务管理
- ✅ 任务创建与分配
- 🎯 优先级管理（高/中/低）
- 📝 任务状态跟踪（待办/进行中/已完成/已取消）
- 🔗 任务与工时关联

### 4️⃣ 工时管理
- ⏰ 工时填报与记录
- 📅 日历视图展示
- 📊 个人工时统计
- 📈 项目工时分析

### 5️⃣ 用户与权限
- 👤 用户管理（注册/登录/信息维护）
- 🏢 组织架构管理（部门/角色）
- 🔐 基于角色的访问控制（RBAC）
- 📝 操作日志记录

---

## 技术栈

### 后端框架
- **Java 8** - 稳定的企业级开发语言
- **Spring Boot 2.7.0** - 快速应用开发框架
- **SpringDoc OpenAPI** - 自动生成 API 文档
- **Maven** - 项目构建与依赖管理

### 数据层
- **MyBatis-Plus 3.5.3.1** - 强大的 ORM 框架
  - 代码生成器
  - 分页插件
  - 枚举自动处理
- **MySQL 8.0** - 可靠的关系型数据库
- **Flyway** - 数据库版本管理工具

### 安全认证
- **JWT (jjwt 0.9.1)** - 无状态认证
- **自定义拦截器** - 请求鉴权
- **RBAC** - 基于角色的权限控制

### 前端框架
- **Vue 3.4** - 渐进式 JavaScript 框架（Composition API）
- **TypeScript 5.3** - 类型安全的 JavaScript 超集
- **Vite 5.0** - 下一代前端构建工具
- **Element Plus 2.5** - 基于 Vue 3 的组件库
- **Vue Router 4** - 官方路由管理器
- **Pinia 2.1** - Vue 3 官方状态管理
- **Axios 1.6** - HTTP 客户端

### 基础设施
- **Logback** - 日志框架
- **Jackson** - JSON 序列化
- **Jakarta Validation** - 参数校验

---

## 🤖 AI 开发工具

本项目使用以下 AI 工具辅助开发，践行 Vibe Coding 理念：

### 核心工具

| 工具 | 用途 | 特点 |
|------|------|------|
| **Claude Code + GLM 4.7** | 主要编码助手 | • 智能代码生成与重构<br>• 架构设计与技术选型<br>• 代码审查与问题诊断<br>• 测试用例生成 |
| **Qoder** | IDE 插件 | • 实时代码补全<br>• 快捷键触发优化<br>• 多语言支持<br>• 本地化体验 |
| **通义灵码** | 阿里云助手 | • 中文代码注释<br>• 技术文档生成<br>• 最佳实践建议<br>• 问题排查辅助 |

### 应用场景

#### 1. 代码生成
```
# Claude Code 快速生成 CRUD 代码
输入：创建用户管理的 Controller、Service、Mapper
输出：完整的三层架构代码 + 参数校验 + 异常处理
```

#### 2. 代码重构
```
# Qoder 智能重构
选中代码 → Alt+Enter → 选择重构建议
- 提取方法
- 优化循环
- 引入设计模式
```

#### 3. 测试编写
```
# 通义灵码生成测试用例
输入：为 ProjectController 生成集成测试
输出：@BeforeEach 设置 + 多场景测试 + 断言验证
```

#### 4. 文档生成
```
# AI 生成项目文档
输入：项目结构和技术栈
输出：完整的 README.md + API 文档 + 注释
```

### 实践经验

**✅ 推荐做法**：
- 保持对代码的**整体把控**，理解每一行代码的作用
- 将**重复性工作**交给 AI（CRUD、测试、文档）
- 仔细**审查 AI 生成的代码**，确保符合项目规范
- 通过**对话式交互**优化代码质量

**⚠️ 注意事项**：
- AI 生成的代码需要**人工审查**和**充分测试**
- 复杂业务逻辑需要**人类设计**，AI 负责实现
- 数据库设计、架构决策需要**人类主导**
- 安全问题、性能优化需要**重点关注**

### 开发效率提升

| 任务 | 传统开发 | AI 辅助开发 | 效率提升 |
|------|---------|------------|---------|
| CRUD 代码 | 2 小时 | 30 分钟 | **4x** |
| 测试用例 | 1 小时 | 15 分钟 | **4x** |
| 文档编写 | 1 小时 | 10 分钟 | **6x** |
| Bug 修复 | 30 分钟 | 10 分钟 | **3x** |
| 代码重构 | 2 小时 | 40 分钟 | **3x** |

**整体开发效率提升：3-5 倍**

### 学习与探索

本项目将持续探索 AI 辅助开发的最佳实践：
- 📝 记录 AI 辅助开发的典型案例
- 🔬 对比不同 AI 工具的效果
- 💡 总结 Vibe Coding 的开发模式
- 🎓 分享 AI 时代的软件开发经验

**欢迎关注本项目，一起探索软件开发的未来！**

---

## 系统架构

### 分层架构

项目采用**标准三层架构 + DTO 模式**：

```
┌─────────────────────────────────────┐
│      Controller (API 控制层)         │  ← HTTP 请求处理、参数校验
├─────────────────────────────────────┤
│         DTO (数据传输对象)            │  ← Controller ↔ Service 数据传输
├─────────────────────────────────────┤
│       Service (业务逻辑层)           │  ← 核心业务逻辑、事务管理
├─────────────────────────────────────┤
│    Domain (领域模型：实体+枚举)       │  ← 数据库模型、业务枚举
├─────────────────────────────────────┤
│   Repository (MyBatis Mapper)        │  ← 数据持久化、SQL 执行
├─────────────────────────────────────┤
│  Infra (基础设施：配置+工具+异常)     │  ← 技术支撑、通用组件
└─────────────────────────────────────┘
```

### 项目结构

```
gsms/
├── backend/               # 后端服务（Spring Boot）
│   ├── src/main/java/com/gsms/gsms/
│   │   ├── controller/         # REST API 层
│   │   ├── service/           # 业务逻辑层
│   │   │   └── impl/          # 服务实现
│   │   ├── repository/        # MyBatis Mapper 接口
│   │   ├── domain/            # 领域层
│   │   │   ├── entity/        # JPA 实体
│   │   │   └── enums/         # 业务枚举
│   │   ├── dto/               # 数据传输对象
│   │   │   ├── project/       # 项目相关 DTO
│   │   │   ├── task/          # 任务相关 DTO
│   │   │   ├── user/          # 用户相关 DTO
│   │   │   └── workhour/      # 工时相关 DTO
│   │   └── infra/             # 基础设施层
│   │       ├── common/        # 通用组件（Result、PageResult）
│   │       ├── config/        # Spring 配置
│   │       ├── exception/     # 自定义异常
│   │       ├── converter/     # 类型转换器
│   │       └── utils/         # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/            # MyBatis XML 映射
│   │   ├── db/migration/      # Flyway 数据库迁移脚本
│   │   ├── application.yml    # 主配置文件
│   │   └── logback-spring.xml # 日志配置
│   └── src/test/              # 测试代码
├── frontend/              # 前端应用（Vue 3）
│   ├── src/
│   │   ├── api/               # API 调用封装
│   │   ├── assets/            # 静态资源
│   │   ├── components/        # 通用组件
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # 状态管理（Pinia）
│   │   ├── types/             # TypeScript 类型
│   │   ├── utils/             # 工具函数
│   │   ├── views/             # 页面视图
│   │   ├── App.vue
│   │   └── main.ts
│   ├── public/                # 公共资源
│   ├── vite.config.ts         # Vite 配置
│   ├── package.json           # 依赖管理
│   └── tsconfig.json          # TypeScript 配置
├── docs/                 # 项目文档
│   ├── development/           # 开发指南
│   └── api/                   # API 文档
└── deployment/           # 部署配置
    └── docker/                # Docker 配置
```

### 数据库设计

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `sys_user` | 用户表 | id, username, password, email, department_id |
| `sys_department` | 部门表 | id, name, parent_id |
| `gsms_project` | 项目表 | id, name, code, manager_id, status |
| `gsms_project_member` | 项目成员表 | project_id, user_id, role_type |
| `gsms_iteration` | 迭代表 | id, project_id, name, status, plan_start_date |
| `gsms_task` | 任务表 | id, project_id, iteration_id, title, status, priority |
| `gsms_work_hour` | 工时记录表 | id, task_id, user_id, work_date, hours |
| `sys_role` | 角色表 | id, name, description |
| `sys_permission` | 权限表 | id, name, resource, action |

---

## 快速开始

### 环境要求

**后端**:
- **JDK** 8+
- **Maven** 3.6+
- **MySQL** 8.0+

**前端**:
- **Node.js** 18+
- **npm** 9+ 或 **pnpm** 8+

### 安装步骤

1. **克隆仓库**
```bash
git clone https://github.com/your-username/gsms.git
cd gsms
```

2. **创建数据库**
```sql
CREATE DATABASE gsms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **启动后端服务**

编辑 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gsms?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

启动后端：
```bash
cd backend
mvn spring-boot:run
```

后端将在 http://localhost:8080 启动

4. **启动前端服务**

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:3000 启动

5. **访问应用**

- **前端应用**: http://localhost:3000
- **后端 API**: http://localhost:8080
- **Swagger 文档**: http://localhost:8080/swagger-ui.html
- **健康检查**: http://localhost:8080/actuator/health

### 快速体验

**方式1: 通过前端界面**

1. 打开浏览器访问 http://localhost:3000
2. 注册用户或使用默认账号登录
3. 开始使用系统

**方式2: 通过 API**

```bash
# 1. 注册用户
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123","email":"test@example.com"}'

# 2. 登录获取 Token
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# 3. 使用 Token 访问受保护接口
curl -X GET http://localhost:8080/api/projects \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 开发指南

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=ProjectControllerTest

# 运行 Controller 测试
mvn test -Dtest=*ControllerTest
```

### 数据库迁移

项目使用 **Flyway** 进行数据库版本管理：

```bash
# 查看迁移状态
mvn flyway:info

# 执行迁移（启动应用时自动执行）
mvn flyway:migrate

# 修复失败的迁移
mvn flyway:repair
```

迁移脚本位于：`src/main/resources/db/migration/`

### 代码规范

#### 分层职责

- **Controller**: 处理 HTTP 请求、参数校验、调用 Service
- **Service**: 实现业务逻辑、事务管理
- **Repository**: 数据持久化操作
- **DTO**: Controller 和 Service 之间的数据传输

#### 命名规范

- **实体类**: `User.java`、`Project.java`
- **DTO**: `UserCreateReq.java`、`UserInfoResp.java`
- **Service**: `UserService.java`（接口）、`UserServiceImpl.java`（实现）
- **Mapper**: `UserMapper.java`（接口）、`UserMapper.xml`（SQL）
- **测试**: `UserControllerTest.java`、`UserServiceTest.java`

#### 提交规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/)：

- `feat:` - 新功能
- `fix:` - 修复 bug
- `docs:` - 文档更新
- `style:` - 代码格式
- `refactor:` - 代码重构
- `test:` - 测试相关
- `chore:` - 构建/工具链

### API 文档

启动应用后访问 Swagger UI：
- 地址：http://localhost:8080/swagger-ui.html
- 功能：查看 API、在线测试、获取 JWT Token

### Docker 部署

使用 Docker Compose 快速部署整个系统：

```bash
# 启动所有服务（MySQL、后端、前端）
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

详细部署指南请查看：[deployment/README.md](deployment/README.md)

### 前后端联调

项目采用 **Monorepo** 结构，前后端代码在同一仓库中。

**开发环境配置**：

1. **后端 CORS 配置**
   - 已在 `backend/src/main/java/com/gsms/gsms/infra/config/WebConfig.java` 中配置
   - 允许前端跨域访问（localhost:3000）

2. **前端代理配置**
   - 在 `frontend/vite.config.ts` 中配置 Vite 代理
   - 前端请求 `/api/*` → Vite 转发到 `http://localhost:8080/api/*`

**开发流程**：

```bash
# 终端1: 启动后端
cd backend
mvn spring-boot:run

# 终端2: 启动前端
cd frontend
npm run dev
```

**联调调试技巧**：

1. 先使用 Swagger UI 测试后端 API 是否正常
2. 使用浏览器开发者工具（F12）查看 Network 面板
3. 检查请求头中的 Authorization token
4. 查看后端日志输出

详细的前后端联调指南请查看：[docs/development/frontend-backend-setup.md](docs/development/frontend-backend-setup.md)

---

## Roadmap

### ✅ 已完成 (v1.0)

- [x] 用户认证与授权（JWT）
- [x] 项目管理（CRUD）
- [x] 任务管理（CRUD）
- [x] 迭代管理（CRUD）
- [x] 工时记录（CRUD）
- [x] 部门管理
- [x] 基于角色的权限控制（RBAC）
- [x] 操作日志记录

### 🚧 进行中 (v1.1)

- [ ] 数据统计与报表
  - [ ] 个人工时统计
  - [ ] 项目工时分析
  - [ ] 图表可视化
- [ ] 前端界面（Vue 3）
  - [ ] 用户管理界面
  - [ ] 项目管理界面
  - [ ] 工时填报界面

### 📅 计划中 (v1.2)

- [ ] 消息通知
  - [ ] 任务分配通知
  - [ ] 工时审批提醒
  - [ ] 邮件通知
- [ ] 文件管理
  - [ ] 附件上传
  - [ ] 文档关联
- [ ] 移动端适配
  - [ ] 响应式设计
  - [ ] 移动端 API

### 🎯 未来愿景 (v2.0)

- [ ] 缓存优化（Redis）
- [ ] 消息队列（RabbitMQ）
- [ ] 链路追踪（SkyWalking）
- [ ] 容器化部署（Docker + K8s）
- [ ] 微服务改造

查看详细的 TODO 列表：[TODO.md](TODO.md)

---

## 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. **Fork 本仓库**
2. **创建特性分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'feat: Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **提交 Pull Request**

### 开发规范

- 遵循现有代码风格
- 添加单元测试（新功能）
- 更新相关文档
- 确保 CI/CD 通过

### 报告 Bug

请在 [Issues](https://github.com/your-username/gsms/issues) 中报告 Bug，并包含：
- Bug 描述
- 复现步骤
- 预期行为
- 实际行为
- 环境信息（OS、Java 版本等）

### 功能建议

欢迎在 [Issues](https://github.com/your-username/gsms/issues) 中提出功能建议！

---

## 常见问题

### 1. Flyway 迁移失败？

**问题**: 启动时报错 `Found non-empty schema but no schema history table`

**解决**:
```
-- 方案1: 清空数据库重新初始化（开发环境）
DROP DATABASE gsms;
CREATE DATABASE gsms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
-- 方案2: 执行 Flyway repair
```bash
mvn flyway:repair
mvn flyway:migrate
```

### 2. JWT Token 过期？

Token 默认有效期为 24 小时，过期后需要重新登录。

可在 `application.yml` 中配置：
```yaml
jwt:
  expiration: 86400000  # 24小时（毫秒）
```

### 3. 枚举类型参数报错？

GET 请求的枚举参数支持两种形式：
- 数字码：`/api/projects?status=1`
- 枚举名：`/api/projects?status=NOT_STARTED`

---

## 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis-Plus](https://baomidou.com/)
- [Flyway](https://flywaydb.org/)
- [SpringDoc](https://springdoc.org/)

---

## 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star**

Made with ❤️ by GSMS Team

</div>
