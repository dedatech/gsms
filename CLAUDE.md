# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

GSMS（工时管理系统）是一个面向研发团队的轻量级工时管理系统。这是一个基于 Spring Boot 的应用，采用标准三层架构结合DTO模式，具有清晰的分层结构。

**项目结构（Monorepo）：**

```
gsms/
├── backend/          # Spring Boot 后端（端口 8080）
├── frontend/         # Vue 3 前端（端口 3000）
├── docs/            # 项目文档
└── deployment/      # 部署配置
```

**技术栈：**

**后端：**

- Java 8 + Spring Boot 2.7.0
- MyBatis-Plus 3.5.3.1 + MySQL 8.0
- JWT 认证 (jjwt 0.9.1)
- Maven 构建管理
- SpringDoc OpenAPI 1.7.0 API文档
- Flyway 数据库版本管理
- PageHelper 分页插件
- Java 8 Time API (LocalDateTime, LocalDate)

**前端：**

- Vue 3.4 + TypeScript 5.3 + Vite 5.0
- Element Plus 2.5 UI 组件库
- Vue Router 4 + Pinia 2.1
- Axios 1.6 HTTP 客户端

## 常用命令

### 后端构建和运行

```bash
cd backend

# 构建项目
mvn clean install

# 运行应用（开发环境）
mvn spring-boot:run

# 使用指定配置文件运行
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 前端构建和运行

```bash
cd frontend

# 安装依赖
npm install

# 运行开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
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

### 分层结构（标准三层架构）

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
3. **Service** 实现业务逻辑，使用 `UserContext.getCurrentUserId()` 获取当前用户
4. **Repository**（MyBatis Mapper）处理数据库操作
5. **Model** 实体表示数据库表结构（位于 `model/entity/` 目录）
6. **Result<T>** 包装所有API响应为统一格式
7. **GlobalExceptionHandler** 统一异常处理，返回标准错误响应

### 架构模式

**标准三层架构 + DTO模式：**

- **Controller层**：处理HTTP请求和响应，参数校验
- **Service层**：实现业务逻辑和事务管理
- **Repository/Mapper层**：数据持久化操作
- **DTO模式**：Controller和Service之间使用DTO传输数据
- **Model层**（而非Domain）：实体类（entity）和枚举（enums），使用贫血模型模式
  - `model/entity/` - JPA实体（数据库模型）
  - `model/enums/` - 业务枚举
  - `model/errorcode/` - 错误码枚举
- 这种架构适合中小型项目，职责清晰，易于理解和维护

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

**前后端集成（CORS 配置）：**

- 后端 CORS 配置在 `WebConfig.java` 中，允许 `localhost:3000` 跨域访问
- 前端 Vite 代理配置在 `vite.config.ts` 中，将 `/api/*` 请求转发到 `http://localhost:8080/api/*`
- 开发时需要同时启动后端（8080）和前端（3000）
- JWT Token 存储在前端，通过 `Authorization: Bearer <token>` 请求头传递

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

### 日期时间处理

- **使用 Java 8 Time API**：所有日期字段使用 `LocalDateTime`（日期+时间）或 `LocalDate`（仅日期）
- **Jackson 配置**（`JacksonConfig.java`）：
  - 禁用 ISO 8601 格式输出
  - `LocalDateTime` 序列化为 `yyyy-MM-dd HH:mm:ss`
  - 时区设置为 `Asia/Shanghai`
- **枚举类型序列化**：所有业务枚举使用 `@JsonValue` 注解 `toString()` 方法，返回枚举名称（如 `NORMAL`）
  - 数据库存储：使用 `@EnumValue` 标记的 `code` 字段（Integer）
  - JSON 序列化：返回枚举的 `name()`（String）

### 数据库设计原则

项目采用严格的表分类和审计策略（详见 `docs/DATABASE_OPTIMIZATION.md`）：

**1. 核心业务表（9个）** - 完整审计字段：

- 表：`sys_user`, `sys_department`, `role`, `permission`, `gsms_project`, `gsms_iteration`, `gsms_task`, `gsms_work_hour`, `gsms_project_member`
- 审计字段：`create_user_id`, `create_time`, `update_user_id`, `update_time`, `is_deleted`
- 注意：`gsms_project` 和 `gsms_iteration` 的 `create_user_id` 通过关联表间接获取

**2. 关联表（4个）** - 简化审计字段：

- 表：`gsms_project_member`, `role_permission`, `user_role`, `department_user`
- 仅需：`update_time`, `update_user_id`, `is_deleted`
- 原则：关联表专注于表达关系，不需要创建人信息

**3. 枚举表（0个）** - 使用 Java 枚举：

- 所有状态/类型字段使用 Java 枚举 + MyBatis-Plus 枚举处理器
- 不在数据库中维护枚举表

**设计原则**：

- 数据表专注于核心职责
- 关联表用于表达关系，审计由专门日志系统完成
- 外键约束确保数据完整性
- 性能优化索引（查询常用字段）

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

1. **目录结构差异**：代码使用 `model/` 而非 `domain/`
   
   - 实体位于：`model/entity/`
   - 枚举位于：`model/enums/`
   - 错误码位于：`model/errorcode/`

2. **Service方法签名已变更：** 许多Service现在接受DTO而非Entity
   
   - 旧版：`createUser(User user)`
   - 新版：`create(UserCreateReq req)`

3. **缺少审计字段：** 忘记设置`createUserId`/`updateUserId`会导致SQL错误
   
   - 核心业务表需要设置：`createUserId` 和 `updateUserId`
   - 关联表（如 `project_member`）只需要 `updateUserId`

4. **权限检查：** Controller通过`AuthService`检查权限，因此测试必须：
   
   - 创建具有适当成员关系的测试项目
   - 使用具有相应权限的用户
   - 权限检查在存在性检查之前失败时，期望403（而非404）

5. **查询参数绑定：** 测试带查询参数的GET请求时，使用`.param("key", "value")`而非`.queryParam()`

6. **JWT Token必需：** 除`/api/users/login`和`/hello`外的所有端点都需要`Authorization: Bearer <token>`请求头

7. **PageHelper使用：** 必须在执行查询**之前**调用`PageHelper.startPage()`，且只对之后执行的**第一个**查询生效

8. **枚举序列化差异**：
   
   - GET 请求枚举参数支持两种形式：数字码（`?status=1`）或枚举名（`?status=NOT_STARTED`）
   - JSON 响应中枚举输出为字符串（枚举名），如 `"status": "NORMAL"`
   - 数据库存储为整数（code），如 `status = 1`

9. **日期类型迁移**：项目已从 `java.util.Date` 迁移到 Java 8 Time API
   
   - 新代码应使用 `LocalDateTime` 或 `LocalDate`
   - 测试中使用 `LocalDateTime.now()` 而非 `new Date()`
   - Jackson 自动格式化为 `yyyy-MM-dd HH:mm:ss`

10. **Debug 模式死锁问题**：已升级 ClassGraph 到 4.8.172 修复
    
    - 详见：`docs/DEBUG_MODE_HANG_ISSUE.md`
    - 如果遇到 Spring Boot Debug 模式启动卡住，检查 ClassGraph 版本

## 参考文档

- **缓存技术决策**：`docs/caching-technical-decisions.md` - 缓存方案对比（ConcurrentHashMap vs Caffeine vs Redis）、Spring 单例原理
- **数据库优化**：`docs/DATABASE_OPTIMIZATION.md` - 表分类、审计字段、外键约束设计
- **调试指南**：`docs/DEBUG_PROCESS_WALKTHROUGH.md` - 远程调试、断点、变量查看
- **前后端联调**：`docs/development/frontend-backend-setup.md` - CORS、代理、认证配置
- **API 文档**：`docs/api-docs.md` - REST API 接口文档
- **重构原则**：`docs/refactoring-principles.md` - 代码重构最佳实践

## 文件命名规范

- 实体：`User.java`、`Project.java`、`WorkHour.java`
- DTO：`UserCreateReq.java`、`UserInfoResp.java`、`UserQueryReq.java`
- Controller：`UserController.java`
- Service：`UserService.java`（接口）、`UserServiceImpl.java`（实现）
- Mapper：`UserMapper.java`（接口）、`UserMapper.xml`（SQL）
- 测试：`UserControllerTest.java`、`UserServiceTest.java`

## 开发工作流

### 新增功能的标准步骤

1. **数据库迁移**：在 `src/main/resources/db/migration/` 创建新的 Flyway 脚本
2. **创建实体类**：在 `model/entity/` 创建实体，添加枚举到 `model/enums/`
3. **创建 Mapper**：创建 `Mapper.java` 接口和 `mapper/Mapper.xml` SQL 映射
4. **创建 DTO**：在 `dto/` 下创建包，定义 CreateReq、UpdateReq、InfoResp、QueryReq
5. **创建 Converter**：在 `dto/` 下创建 converter 包，实现 Entity ↔ DTO 转换
6. **实现 Service**：创建 Service 接口和 Impl，包含业务逻辑和权限检查
7. **实现 Controller**：创建 REST 端点，使用 `@Valid` 校验，调用 Service
8. **编写测试**：创建 ControllerTest、ServiceTest，覆盖成功和失败场景
9. **更新文档**：如需要，更新 API 文档（Swagger 自动生成）

### 代码审查要点

- [ ] 审计字段是否正确设置（createUserId/updateUserId）
- [ ] 权限检查是否完善（使用 AuthService）
- [ ] DTO 参数校验是否完整（@Valid、@NotNull 等）
- [ ] 异常处理是否恰当（BusinessException、错误码）
- [ ] 测试覆盖率是否足够（成功/失败场景）
- [ ] 枚举序列化是否正确（@JsonValue、@EnumValue）
- [ ] 日志输出是否合理（INFO、DEBUG、ERROR）

---

## 前端开发规范

基于项目实践总结的前端开发规范，涵盖布局、样式、组件设计等最佳实践。

### 前端项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口模块
│   │   ├── request.ts   # Axios 实例和拦截器
│   │   └── *.ts         # 各业务模块 API
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia 状态管理
│   │   └── auth.ts      # 认证状态 Store
│   ├── views/           # 页面组件
│   │   ├── auth/        # 认证相关页面
│   │   ├── project/     # 项目相关页面
│   │   ├── task/        # 任务相关页面
│   │   ├── iteration/   # 迭代相关页面
│   │   └── workhour/    # 工时相关页面
│   ├── App.vue          # 根组件
│   └── main.ts          # 应用入口
├── public/              # 公共静态资源
├── index.html           # HTML 模板
├── vite.config.ts       # Vite 配置
└── package.json         # 项目依赖
```

### 状态管理规范

#### 使用 Pinia Store

**必须使用 auth store 管理认证状态：**

```typescript
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// ✅ 正确：从 store 获取用户ID
const userId = authStore.getCurrentUserId()

// ❌ 错误：直接从 localStorage 获取
const userId = parseInt(localStorage.getItem('userId') || '0')
```

**认证信息管理：**

```typescript
// 登录时设置认证信息
authStore.setAuth(token, username)

// 退出登录时清除认证信息
authStore.clearAuth()

// 检查是否已登录
if (authStore.isAuthenticated) {
  // 已登录逻辑
}

// 获取当前用户信息
const { id, username } = authStore.currentUser
```

**Store 定义规范：**

```typescript
// ✅ 推荐：使用 Composition API 风格
export const useAuthStore = defineStore('auth', () => {
  // 状态定义
  const token = ref<string>('')
  const userId = ref<number>(0)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)

  // 操作方法
  const setAuth = (tokenValue: string, usernameValue?: string) => {
    token.value = tokenValue
    // ...
  }

  return {
    token,
    userId,
    isAuthenticated,
    setAuth
  }
})
```

### 页面组件设计规范

#### 组件文件结构

**标准模板：**

```vue
<template>
  <div class="{module-list}">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">{模块标题}</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建{模块}
        </el-button>
      </div>
      <div class="header-right">
        <!-- 搜索、筛选、视图切换 -->
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="{view-mode}-view">
      <!-- 表格/看板/列表 -->
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination />
    </div>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" title="{标题}">
      <el-form :model="formData" :rules="formRules">
        <!-- 表单字段 -->
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getModuleList } from '@/api/module'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()

// 数据定义
const list = ref<any[]>([])
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  name: '',
  status: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10
})

// 对话框
const dialogVisible = ref(false)
const formData = reactive({...})
const formRules: FormRules = {...}

// 生命周期
onMounted(() => {
  fetchData()
})

// 方法定义
const fetchData = async () => {
  try {
    const res = await getModuleList(searchForm)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

const handleCreate = () => {
  dialogTitle.value = '新建{模块}'
  resetForm()
  dialogVisible.value = true
}

// ...
</script>

<style scoped>
.{module-list} {
  min-height: calc(100vh - 160px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 主内容样式 */
.{view-mode}-view {
  margin-bottom: 24px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}
</style>
```

### 看板视图设计规范

#### 看板布局结构

**标准看板模板（基于项目列表实践）：**

```vue
<template>
  <div class="kanban-view">
    <el-row :gutter="16">
      <el-col v-for="status in statuses" :key="status.value" :xs="24" :sm="12" :md="6">
        <div
          class="kanban-column"
          :data-status="status.value"
          @dragover.prevent="handleDragOver"
          @dragleave="handleDragLeave"
          @drop="handleDrop"
        >
          <!-- 列头 -->
          <div class="column-header">
            <div class="column-title">
              <div class="status-dot" :style="{ backgroundColor: status.color }"></div>
              <span>{{ status.label }}</span>
              <span class="count">{{ getItemsByStatus(status.value).length }}</span>
            </div>
          </div>

          <!-- 列体 -->
          <div class="column-body">
            <div
              v-for="item in getItemsByStatus(status.value)"
              :key="item.id"
              class="item-card"
              draggable="true"
              @dragstart="handleDragStart(item, $event)"
              @click="handleView(item)"
            >
              <!-- 卡片内容 -->
            </div>
            <el-empty v-if="getItemsByStatus(status.value).length === 0" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
// 状态配置
const statuses = [
  { label: '状态1', value: 'STATUS_1', color: '#d9d9d9' },
  { label: '状态2', value: 'STATUS_2', color: '#1890ff' },
  { label: '状态3', value: 'STATUS_3', color: '#52c41a' }
]

// 使用 computed 优化性能
const status1Items = computed(() => list.value.filter(item => item.status === 'STATUS_1'))
const status2Items = computed(() => list.value.filter(item => item.status === 'STATUS_2'))
const status3Items = computed(() => list.value.filter(item => item.status === 'STATUS_3'))

// 辅助函数
const getItemsByStatus = (status: string) => {
  const map: Record<string, any[]> = {
    'STATUS_1': status1Items.value,
    'STATUS_2': status2Items.value,
    'STATUS_3': status3Items.value
  }
  return map[status] || []
}

// 拖拽处理
const draggedItem = ref<any>(null)

const handleDragStart = (item: any, event: DragEvent) => {
  draggedItem.value = item
  if (event.dataTransfer) {
    event.dataTransfer.setData('text/plain', item.id.toString())
    event.dataTransfer.effectAllowed = 'move'
  }
  ;(event.target as HTMLElement).classList.add('dragging')
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
  ;(event.currentTarget as HTMLElement).classList.add('drag-over')
}

const handleDragLeave = (event: DragEvent) => {
  ;(event.currentTarget as HTMLElement).classList.remove('drag-over')
}

const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  const targetStatus = (event.currentTarget as HTMLElement).getAttribute('data-status')

  // 移除拖拽样式
  const draggingElements = document.querySelectorAll('.dragging')
  draggingElements.forEach(el => el.classList.remove('dragging'))

  const dragOverElements = document.querySelectorAll('.drag-over')
  dragOverElements.forEach(el => el.classList.remove('drag-over'))

  if (draggedItem.value && targetStatus && draggedItem.value.status !== targetStatus) {
    // 更新状态
    await updateItem({
      id: draggedItem.value.id,
      status: targetStatus
    })
    ElMessage.success('状态已更新')
    fetchData()
  }

  draggedItem.value = null
}
</script>

<style scoped>
.kanban-view {
  margin-bottom: 24px;
}

.kanban-column {
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.kanban-column.drag-over {
  background: #e6f7ff;
  box-shadow: 0 0 0 2px #1890ff inset;
}

.column-header {
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #333;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.count {
  margin-left: auto;
  font-size: 12px;
  color: #8c8c8c;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.column-body {
  padding: 16px;
  min-height: 400px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.item-card {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.item-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.item-card.dragging {
  opacity: 0.5;
  cursor: move;
}
</style>
```

### 视图模式切换规范

**看板/列表切换按钮：**

```vue
<el-button-group>
  <el-button
    :type="viewMode === 'kanban' ? 'primary' : ''"
    @click="viewMode = 'kanban'"
  >
    <el-icon><Grid /></el-icon>
    看板
  </el-button>
  <el-button
    :type="viewMode === 'table' ? 'primary' : ''"
    @click="viewMode = 'table'"
  >
    <el-icon><List /></el-icon>
    列表
  </el-button>
</el-button-group>

<!-- 分页只在列表视图显示 -->
<div v-if="total > 0 && viewMode === 'table'" class="pagination">
  <el-pagination />
</div>
```

**状态响应式显示：**

```vue
<!-- 看板视图 -->
<div v-if="viewMode === 'kanban'" class="kanban-view">
  <!-- 看板内容 -->
</div>

<!-- 列表视图 -->
<div v-else class="table-view">
  <el-table :data="list">
    <!-- 表格列 -->
  </el-table>
</div>
```

### 表单设计规范

#### 表单布局

**标准表单结构：**

```vue
<el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
  <!-- 单行输入 -->
  <el-form-item label="项目名称" prop="name">
    <el-input v-model="formData.name" placeholder="请输入项目名称" />
  </el-form-item>

  <!-- 多行文本 -->
  <el-form-item label="项目描述" prop="description">
    <el-input
      v-model="formData.description"
      type="textarea"
      :rows="3"
      placeholder="请输入项目描述"
    />
  </el-form-item>

  <!-- 下拉选择 -->
  <el-form-item label="项目经理" prop="managerId">
    <el-select v-model="formData.managerId" placeholder="请选择项目经理" style="width: 100%">
      <el-option
        v-for="user in userList"
        :key="user.id"
        :label="user.nickname"
        :value="user.id"
      />
    </el-select>
  </el-form-item>

  <!-- 单选框组 -->
  <el-form-item label="项目状态" prop="status">
    <el-radio-group v-model="formData.status">
      <el-radio label="NOT_STARTED">未开始</el-radio>
      <el-radio label="IN_PROGRESS">进行中</el-radio>
      <el-radio label="ARCHIVED">已归档</el-radio>
    </el-radio-group>
  </el-form-item>

  <!-- 日期选择 -->
  <el-form-item label="计划开始" prop="planStartDate">
    <el-date-picker
      v-model="formData.planStartDate"
      type="date"
      placeholder="选择日期"
      style="width: 100%"
      value-format="YYYY-MM-DD"
    />
  </el-form-item>
</el-form>
```

#### 表单验证规则

**标准验证规则定义：**

```typescript
const formRules: FormRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '只能包含大写字母、数字和下划线', trigger: 'blur' }
  ],
  managerId: [
    { required: true, message: '请选择项目经理', trigger: 'change' }
  ]
}

const formRef = ref<FormInstance>()

// 提交验证
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      // 提交逻辑
    }
  })
}
```

### 卡片组件设计规范

**标准卡片模板：**

```vue
<div class="item-card" @click="handleView(item)">
  <!-- 卡片头部 -->
  <div class="card-header">
    <div class="card-icon" :style="{ backgroundColor: getStatusColor(item.status) }">
      <el-icon :size="20"><FolderOpened /></el-icon>
    </div>
    <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, item)">
      <el-icon class="more-icon" @click.stop><MoreFilled /></el-icon>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="view" :icon="View">查看</el-dropdown-item>
          <el-dropdown-item command="edit" :icon="Edit">编辑</el-dropdown-item>
          <el-dropdown-item command="delete" :icon="Delete" divided>删除</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>

  <!-- 卡片主体 -->
  <div class="card-body">
    <h3 class="item-title">{{ item.name }}</h3>
    <p class="item-code">{{ item.code }}</p>
    <p class="item-description">{{ item.description || '暂无描述' }}</p>
  </div>

  <!-- 卡片底部 -->
  <div class="card-footer">
    <div class="item-meta">
      <el-tag :type="getStatusType(item.status)" size="small">
        {{ getStatusText(item.status) }}
      </el-tag>
      <span class="meta-item">
        <el-icon><User /></el-icon>
        {{ getManagerName(item.managerId) || '未设置' }}
      </span>
    </div>
    <div class="item-time">
      <span>{{ formatDate(item.createTime) }}</span>
    </div>
  </div>
</div>
```

**卡片样式规范：**

```css
.item-card {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.item-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.more-icon {
  font-size: 18px;
  color: #8c8c8c;
  cursor: pointer;
  transition: color 0.3s;
}

.more-icon:hover {
  color: #333;
}

.item-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-code {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #8c8c8c;
}

.item-description {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #595959;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #595959;
}

.item-time {
  font-size: 12px;
  color: #8c8c8c;
}
```

### 响应式设计规范

#### 栅格系统使用

**响应式列宽：**

```vue
<el-row :gutter="16">
  <!-- 小屏全宽，中屏半宽，大屏1/3宽 -->
  <el-col :xs="24" :sm="12" :md="8" :lg="6">
    <!-- 内容 -->
  </el-col>
</el-row>
```

**断点说明：**

- `xs`: <768px（小屏）
- `sm`: ≥768px（中屏）
- `md`: ≥992px（大屏）
- `lg`: ≥1200px（超大屏）
- `xl`: ≥1920px（特大屏）

#### 响应式隐藏

```vue
<!-- 在小屏幕隐藏 -->
<div class="hidden-xs-only">
  <!-- 内容 -->
</div>

<!-- 在大屏幕显示 -->
<div class="hidden-md-and-down">
  <!-- 内容 -->
</div>
```

### 样式命名规范

#### BEM 命名方法论

**Block（块）**

- 独立的有意义的实体
- 小写字母，单词用连字符连接
- 示例：`.project-list`, `.kanban-view`

**Element（元素）**

- 块的一部分，没有独立含义
- 用连字符连接
- 示例：`.project-list__header`, `.kanban-column`

**Modifier（修饰符）**

- 块或元素的变体
- 用连字符连接
- 示例：`.project-card--dragging`, `.kanban-column.drag-over`

**示例：**

```css
/* Block */
.project-list {}

/* Element */
.project-list__header {}

/* Modifier */
.project-card--dragging {
  opacity: 0.5;
  cursor: move;
}
```

#### 工具类规范

**间距工具类：**

```css
.mb-16 { margin-bottom: 16px; }
.mr-16 { margin-right: 16px; }
.p-16 { padding: 16px; }
```

**文本工具类：**

```css
.text-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-center { text-align: center; }
.text-right { text-align: right; }
```

**显示工具类：**

```css
.hidden { display: none; }
.flex { display: flex; }
.flex-center { display: flex; align-items: center; justify-content: center; }
```

### 颜色规范

#### 状态颜色

**项目状态：**

- 未开始：`#d9d9d9`（灰色）
- 进行中：`#1890ff`（蓝色）
- 已暂停：`#faad14`（橙色）
- 已归档：`#`8c8c8c（深灰）

**任务状态：**

- 待办：`#d9d9d9`（灰色）
- 进行中：`#1890ff`（蓝色）
- 已完成：`#52c41a`（绿色）

**优先级：**

- 低：`#`（灰色，info）
- 中：``（默认，无颜色）
- 高：`#faad14`（橙色，warning）

#### Element Plus Tag 类型

```typescript
const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'primary',
    'SUSPENDED': 'warning',
    'ARCHIVED': ''
  }
  return types[status] || 'info'
}
```

### 图标使用规范

**图标引入：**

```typescript
import {
  Plus,
  Search,
  Grid,
  List,
  FolderOpened,
  User,
  MoreFilled,
  Edit,
  Delete,
  View
} from '@element-plus/icons-vue'
```

**图标使用：**

```vue
<!-- 按钮图标 -->
<el-button :icon="Plus">新建</el-button>

<!-- 前缀图标 -->
<el-input :prefix-icon="Search" />

<!-- 独立图标 -->
<el-icon :size="20"><FolderOpened /></el-icon>
```

### 动画与过渡

**卡片悬停效果：**

```css
.item-card {
  transition: all 0.3s;
}

.item-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
```

**拖拽动画：**

```css
.item-card {
  transition: all 0.3s;
}

.item-card.dragging {
  opacity: 0.5;
  cursor: move;
}

.kanban-column {
  transition: all 0.3s;
}

.kanban-column.drag-over {
  background: #e6f7ff;
  box-shadow: 0 0 0 2px #1890ff inset;
}
```

**淡入淡出：**

```vue
<transition name="fade">
  <div v-if="show">内容</div>
</transition>

<style>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
```

### 代码注释规范

**组件注释：**

```vue
<template>
  <!-- 页面头部 -->
  <div class="page-header">
    <!-- 搜索区域 -->
    <div class="search-area">
    </div>
  </div>

  <!-- 主内容区域 -->
  <div class="main-content">
    <!-- 表格/看板 -->
  </div>
</template>

<script setup lang="ts">
// 获取用户列表
const fetchUsers = async () => {
  // ...
}

// 处理搜索
const handleSearch = () => {
  // ...
}
</script>
```

**关键逻辑注释：**

```typescript
// 解析 JWT Token 获取 userId
const payload = parseToken(token)
const userId = payload?.userId || 0

// 使用 computed 优化性能，按状态过滤项目
const notStartedProjects = computed(() =>
  projectList.value.filter(project => project.status === 'NOT_STARTED')
)
```

### 性能优化规范

#### 使用 computed 优化

```typescript
// ✅ 推荐：使用 computed 缓存计算结果
const notStartedProjects = computed(() =>
  projectList.value.filter(project => project.status === 'NOT_STARTED')
)

// ❌ 避免：在方法中重复计算
const getNotStartedProjects = () => {
  return projectList.value.filter(project => project.status === 'NOT_STARTED')
}
```

#### 列表数据优化

```typescript
// 使用 computed 缓存过滤结果
const statusGroups = {
  NOT_STARTED: computed(() => list.value.filter(item => item.status === 'NOT_STARTED')),
  IN_PROGRESS: computed(() => list.value.filter(item => item.status === 'IN_PROGRESS')),
  ARCHIVED: computed(() => list.value.filter(item => item.status === 'ARCHIVED'))
}

// 辅助函数
const getItemsByStatus = (status: string) => {
  return statusGroups[status]?.value || []
}
```

#### 分页加载优化

```typescript
// 只在列表视图启用分页
const showPagination = computed(() => {
  return viewMode.value === 'table' && total.value > 0
})

// 看板视图加载全部数据
const fetchProjects = async () => {
  if (viewMode.value === 'kanban') {
    // 看板视图：加载所有数据
    searchForm.pageSize = 1000
  } else {
    // 列表视图：使用分页
    searchForm.pageSize = 10
  }
  // ...
}
```

### 前端最佳实践总结

#### 1. 组件设计

**✅ DO（推荐）：**

- 使用 Composition API (`<script setup>`)
- 使用 TypeScript 类型定义
- 组件文件命名：`{Module}List.vue`, `{Module}Detail.vue`
- 单一职责原则，每个组件只负责一个功能模块
- Props 和 Emits 明确定义
- 使用 ref 和 reactive 管理响应式状态

**❌ DON'T（避免）：**

- 组件过大时，拆分为多个子组件
- 硬编码数据和配置
- 在组件中直接访问 localStorage（使用 auth store）
- 混用 Options API 和 Composition API

#### 2. 状态管理

**✅ DO（推荐）：**

- 使用 Pinia Store 管理全局状态（认证、用户信息等）
- 使用 local state 管理组件私有状态
- 认证信息统一从 auth store 获取
- 使用 computed 缓存计算结果

**❌ DON'T（避免）：**

- 在组件中直接操作 localStorage
- 在多个组件中重复获取相同数据
- 将大量状态存储在全局 store（只在必要时使用）

#### 3. 样式编写

**✅ DO（推荐）：**

- 使用 `<style scoped>` 限制样式作用域
- 遵循 BEM 命名规范
- 使用 CSS 变量定义颜色和尺寸
- 响应式设计考虑（断点、布局）
- 过渡动画使用 Vue transition

**❌ DON'T（避免）：**

- 内联样式（style="..."）
- !important 声明（优先级问题）
- 硬编码颜色和尺寸
- 过度嵌套选择器（性能问题）

#### 4. API 调用

**✅ DO（推荐）：**

- 每个 API 模块定义 TypeScript 接口
- 统一的错误处理（try-catch + ElMessage）
- 使用 async/await 处理异步
- 请求前验证权限和数据
- loading 状态管理

**❌ DON'T（避免）：**

- 在组件中直接调用 axios（使用 API 封装）
- 重复的 API 调用
- 忽略错误处理
- 忘记设置 loading 状态

#### 5. 路由设计

**✅ DO（推荐）：**

- 路由懒加载（动态 import）
- 路由元信息定义（title, requiresAuth 等）
- 编程式导航（router.push）
- 路由参数校验

**❌ DON'T（避免）：**

- 硬编码路径
- 重复定义路由
- 忘记处理 404 路由

---

## 前端技术决策

**详细的架构和设计决策：**

- **前端架构文档**：`docs/development/frontend-architecture.md`
- **前后端联调**：`docs/development/frontend-backend-setup.md`
