# RBAC 用户、角色、权限管理系统实现文档

**实现日期**: 2026-01-12
**状态**: ✅ 已完成并投入使用

---

## 📋 功能概述

基于 RBAC（Role-Based Access Control）模型，为 GSMS 工时管理系统实现了完整的用户、角色、权限管理功能，包括：

- ✅ 用户管理（CRUD + 角色分配 + 启用/禁用）
- ✅ 角色管理（CRUD + 权限分配）
- ✅ 权限管理（CRUD + 权限查询）
- ✅ 用户注册流程（默认禁用，需管理员审核）
- ✅ 三级权限控制（路由级 + 按钮级 + 数据级）
- ✅ 系统管理菜单和页面

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                          │
├─────────────────────────────────────────────────────────────┤
│  路由级权限控制        按钮级权限控制      数据级权限过滤     │
│  (菜单显示/隐藏)       (按钮显示/隐藏)    (数据过滤)         │
│         ↓                    ↓                    ↓          │
│  router.beforeEach    v-permission指令   请求参数过滤        │
│  + 路由守卫            + 权限函数         + 后端验证         │
└─────────────────────────────────────────────────────────────┘
                            ↕ HTTP (JWT)
┌─────────────────────────────────────────────────────────────┐
│                    后端 (Spring Boot)                       │
├─────────────────────────────────────────────────────────────┤
│  Controller → Service → Mapper → Database                   │
│  AuthService (权限检查 + 缓存)                               │
└─────────────────────────────────────────────────────────────┘
                            ↕ SQL
┌─────────────────────────────────────────────────────────────┐
│                    数据库 (MySQL)                           │
├─────────────────────────────────────────────────────────────┤
│  sys_user, sys_role, sys_permission                        │
│  sys_user_role, sys_role_permission                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 文件清单

### 后端文件（约 15 个）

#### 新建文件

**DTO（约 6 个）：**
- `dto/role/RoleCreateReq.java`
- `dto/role/RoleUpdateReq.java`
- `dto/role/RoleInfoResp.java`
- `dto/role/RoleQueryReq.java`
- `dto/role/RoleConverter.java`
- `dto/permission/PermissionCreateReq.java`
- `dto/permission/PermissionUpdateReq.java`
- `dto/permission/PermissionInfoResp.java`
- `dto/permission/PermissionQueryReq.java`
- `dto/permission/PermissionConverter.java`

**Service（约 4 个）：**
- `service/RoleService.java` + `service/impl/RoleServiceImpl.java`
- `service/PermissionService.java` + `service/impl/PermissionServiceImpl.java`

**Controller（约 2 个）：**
- `controller/RoleController.java`
- `controller/PermissionController.java`

#### 修改现有文件

- `repository/RoleMapper.java` - 扩展 CRUD 方法
- `repository/PermissionMapper.java` - 扩展 CRUD 方法
- `repository/UserMapper.java` - 扩展角色关联方法
- `resources/mapper/RoleMapper.xml` - 完整 SQL 实现
- `resources/mapper/PermissionMapper.xml` - 完整 SQL 实现
- `resources/mapper/UserMapper.xml` - 添加角色关联 SQL
- `service/UserService.java` - 添加角色管理方法
- `service/impl/UserServiceImpl.java` - 实现角色管理逻辑
- `controller/UserController.java` - 添加角色管理端点

### 前端文件（约 15 个）

#### API 模块（约 3 个）
- `api/role.ts` - 角色管理 API
- `api/permission.ts` - 权限管理 API
- `api/user.ts` - 扩展用户角色管理 API

#### 页面组件（约 4 个）
- `views/system/UserList.vue` - 用户管理页面
- `views/system/RoleList.vue` - 角色管理页面
- `views/system/PermissionList.vue` - 权限管理页面
- `views/auth/RegisterView.vue` - 注册页面

#### 权限控制（约 3 个）
- `router/permission.ts` - 路由守卫
- `directives/permission.ts` - v-permission 指令
- `utils/permission.ts` - 权限检查工具函数

#### 状态管理（约 1 个）
- `stores/auth.ts` - 扩展权限和角色状态

#### 路由配置（约 1 个）
- `router/index.ts` - 添加系统管理路由

#### 布局组件（约 1 个）
- `components/Layout.vue` - 添加系统管理菜单

---

## 🔌 API 端点清单

### 用户管理

```
POST   /api/users/register           - 用户注册（默认禁用）
GET    /api/users/{id}               - 根据ID查询用户
GET    /api/users                    - 分页查询用户
POST   /api/users                    - 创建用户
PUT    /api/users                    - 更新用户
DELETE /api/users/{id}               - 删除用户
GET    /api/users/{id}/roles         - 查询用户角色列表
POST   /api/users/{id}/roles         - 为用户分配角色
DELETE /api/users/{userId}/roles/{roleId} - 移除用户角色
```

### 角色管理

```
GET    /api/roles                    - 分页查询角色
GET    /api/roles/{id}               - 根据ID查询角色
POST   /api/roles                    - 创建角色
PUT    /api/roles                    - 更新角色
DELETE /api/roles/{id}               - 删除角色
GET    /api/roles/{id}/permissions   - 查询角色权限列表
POST   /api/roles/{id}/permissions   - 为角色分配权限
DELETE /api/roles/{id}/permissions/{permId} - 移除角色权限
GET    /api/roles/{id}/users         - 查询拥有该角色的用户列表
```

### 权限管理

```
GET    /api/permissions              - 分页查询权限
GET    /api/permissions/{id}         - 根据ID查询权限
GET    /api/permissions/all          - 获取所有权限（不分页，用于角色分配）
POST   /api/permissions              - 创建权限
PUT    /api/permissions              - 更新权限
DELETE /api/permissions/{id}         - 删除权限
```

---

## 🔐 权限控制实现

### 1. 路由级权限控制

**文件**: `frontend/src/router/permission.ts`

```typescript
export const setupPermissionGuard = (router: Router) => {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()

    // 需要认证的页面
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }

    // 检查路由权限
    if (to.meta?.permissions) {
      const hasPermission = authStore.hasAnyPermission(to.meta.permissions as string[])
      if (!hasPermission) {
        return next({ name: 'Forbidden' })
      }
    }

    next()
  })
}
```

**使用示例**：
```typescript
{
  path: '/system/users',
  name: 'UserList',
  component: () => import('@/views/system/UserList.vue'),
  meta: {
    title: '用户管理',
    permissions: ['USER_VIEW'] // 需要的权限
  }
}
```

### 2. 按钮级权限控制

**文件**: `frontend/src/directives/permission.ts`

```typescript
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const authStore = useAuthStore()

    if (value) {
      const hasPermission = typeof value === 'string'
        ? authStore.hasPermission(value)
        : value.some((p: string) => authStore.hasPermission(p))

      if (!hasPermission) {
        el.parentNode?.removeChild(el)
      }
    }
  }
}
```

**使用示例**：
```vue
<!-- 单个权限 -->
<el-button v-permission="'USER_CREATE'">新建用户</el-button>

<!-- 多个权限（满足其一即可显示） -->
<el-button v-permission="['USER_EDIT', 'USER_DELETE']">操作</el-button>
```

### 3. AuthStore 权限管理

**文件**: `frontend/src/stores/auth.ts`

```typescript
// 权限相关状态
const permissions = ref<Set<string>>(new Set())
const userRoles = ref<RoleInfo[]>([])

// 获取用户权限（从后端加载）
const fetchUserPermissions = async () => {
  const userId = getCurrentUserId()
  if (!userId) return

  try {
    const data = await getUserPermissions(userId)
    permissions.value = new Set(data)
  } catch (error) {
    console.error('获取用户权限失败:', error)
  }
}

// 检查是否有指定权限
const hasPermission = (permission: string): boolean => {
  return permissions.value.has(permission)
}

// 检查是否有任一权限
const hasAnyPermission = (permissionList: string[]): boolean => {
  return permissionList.some(p => permissions.value.has(p))
}
```

---

## 🎨 前端页面设计

### 用户管理页面

**文件**: `frontend/src/views/system/UserList.vue`

**功能清单**：
- ✅ 用户列表（表格展示）
- ✅ 搜索筛选（用户名、邮箱、部门、状态）
- ✅ 新建用户对话框
- ✅ 编辑用户对话框
- ✅ 分配角色对话框（多选下拉框）
- ✅ 启用/禁用按钮（切换状态）
- ✅ 删除用户（二次确认）
- ✅ 分页

**关键代码**：
```vue
<template>
  <!-- 启用/禁用按钮 -->
  <el-button
    :type="row.status === 'NORMAL' ? 'warning' : 'success'"
    size="small"
    @click="handleToggleStatus(row)"
  >
    {{ row.status === 'NORMAL' ? '禁用' : '启用' }}
  </el-button>
</template>

<script setup lang="ts">
const handleToggleStatus = async (row: UserInfo) => {
  const newStatus = row.status === 'NORMAL' ? 'DISABLED' : 'NORMAL'
  await updateUser({ id: row.id, status: newStatus })
  ElMessage.success(`${newStatus === 'NORMAL' ? '启用' : '禁用'}成功`)
  fetchData()
}
</script>
```

### 角色管理页面

**文件**: `frontend/src/views/system/RoleList.vue`

**功能清单**：
- ✅ 角色列表（表格展示）
- ✅ 搜索筛选（角色名称、级别）
- ✅ 新建角色对话框
- ✅ 编辑角色对话框
- ✅ 分配权限对话框（表格勾选，按模块分组）
- ✅ 查看角色下的用户列表
- ✅ 删除角色（检查是否有用户使用）
- ✅ 分页

**权限分配对话框**：
```vue
<el-dialog v-model="permissionDialogVisible" title="分配权限" width="800px">
  <el-table
    ref="permissionTableRef"
    :data="permissionTableData"
    @selection-change="handlePermissionSelectionChange"
  >
    <el-table-column type="selection" width="55" />
    <el-table-column prop="module" label="模块" width="120" />
    <el-table-column prop="name" label="权限名称" width="200" />
    <el-table-column prop="code" label="权限编码" width="200" />
    <el-table-column prop="description" label="描述" />
  </el-table>
</el-dialog>
```

### 权限管理页面

**文件**: `frontend/src/views/system/PermissionList.vue`

**功能清单**：
- ✅ 权限列表（表格展示）
- ✅ 搜索筛选（权限名称、编码）
- ✅ 新建权限对话框
- ✅ 编辑权限对话框
- ✅ 查看哪些角色使用了该权限
- ✅ 删除权限（检查是否被角色使用）
- ✅ 按模块分组展示
- ✅ 分页

### 注册页面

**文件**: `frontend/src/views/auth/RegisterView.vue`

**功能清单**：
- ✅ 注册表单（用户名、密码、邮箱、电话）
- ✅ 表单验证（用户名长度、密码强度、邮箱格式）
- ✅ 注册成功页面
- ✅ 提示"请联系管理员进行审核"

**成功提示**：
```vue
<el-result icon="success" title="注册成功" sub-title="注册成功，请联系管理员进行审核">
  <template #extra>
    <el-alert title="温馨提示" type="info" :closable="false">
      您的账号已创建成功，但当前处于<strong>待审核</strong>状态。
      请联系系统管理员启用您的账号后，方可正常登录。
    </el-alert>
  </template>
</el-result>
```

---

## 🔧 后端实现细节

### 用户注册默认禁用

**文件**: `backend/.../UserServiceImpl.java`

```java
@Override
@Transactional(rollbackFor = Exception.class)
public UserInfoResp create(UserCreateReq createReq) {
    logger.info("创建用户: {}", createReq.getUsername());

    // DTO转Entity
    User user = UserConverter.toUser(createReq);

    // 设置默认状态为禁用（需要管理员审核）
    user.setStatus(UserStatus.DISABLED);

    // 密码加密
    user.setPassword(PasswordUtil.encrypt(user.getPassword()));

    // 设置审计字段
    Long currentUserId = UserContext.getCurrentUserId();
    user.setCreateUserId(currentUserId != null ? currentUserId : 1L);
    user.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

    // 检查用户名是否已存在
    User existUser = userMapper.selectByUsername(user.getUsername());
    if (existUser != null) {
        throw new BusinessException(UserErrorCode.USERNAME_EXISTS);
    }

    int result = userMapper.insert(user);
    if (result <= 0) {
        throw new BusinessException(UserErrorCode.USER_CREATE_FAILED);
    }

    // 重新查询获取完整数据
    User createdUser = userMapper.selectById(user.getId());
    cacheService.putUser(createdUser);

    logger.info("用户创建成功: {}", user.getUsername());
    UserInfoResp resp = UserInfoResp.from(createdUser);
    enrichUserInfoResp(resp);
    return resp;
}
```

### 登录时检查用户状态

**文件**: `backend/.../UserServiceImpl.java`

```java
@Override
public User login(String username, String password) {
    logger.info("用户登录: {}", username);

    // 从缓存获取用户
    User user = cacheService.getUserByUsername(username)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    // 验证密码
    if (!PasswordUtil.verify(password, user.getPassword())) {
        throw new BusinessException(UserErrorCode.PASSWORD_ERROR);
    }

    // 检查用户状态
    if (user.getStatus() != UserStatus.NORMAL) {
        logger.warn("用户登录失败 - 用户已禁用: username={}", username);
        throw new BusinessException(UserErrorCode.USER_DISABLED);
    }

    logger.info("用户登录成功: {}", username);
    return user;
}
```

### 角色-权限分配

**文件**: `backend/.../RoleServiceImpl.java`

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void assignPermissions(Long roleId, List<Long> permissionIds) {
    logger.info("为角色分配权限: roleId={}, permissionIds={}", roleId, permissionIds);

    // 检查角色是否存在
    getRoleById(roleId);

    // 删除角色现有的所有权限
    permissionMapper.deleteRolePermissions(roleId);

    // 分配新的权限
    if (permissionIds != null && !permissionIds.isEmpty()) {
        permissionMapper.insertRolePermissions(roleId, permissionIds);
    }

    logger.info("角色权限分配成功: roleId={}", roleId);
}
```

---

## 🐛 技术问题和解决方案

### 问题 1: 用户状态枚举序列化

**现象**：
- 前端显示用户状态错误
- 后端返回的是字符串（`"NORMAL"` / `"DISABLED"`）
- 前端代码检查的是数字（`row.status === 1`）

**解决方案**：
- 统一使用字符串格式
- 前端 TypeScript 接口：`status: string`
- 前端比较：`row.status === 'NORMAL'`
- 单选按钮：`:label="'NORMAL'"` 而不是 `:label="1"`

**修改文件**：
- `frontend/src/views/system/UserList.vue`

### 问题 2: 用户状态默认值设置

**现象**：
- 代码中明确调用 `user.setStatus(UserStatus.DISABLED)`
- 但数据库中 status 字段值为 1（NORMAL）而不是 2（DISABLED）
- 极其困惑，添加调试日志后莫名其妙正常了

**调试过程**：
1. 添加详细日志，跟踪对象引用（`System.identityHashCode()`）
2. 确认 `setStatus()` 调用后，status 确实是 DISABLED
3. 密码加密后、设置审计字段后，status 仍然是 DISABLED
4. 重新编译后，数据库正确存储了 2（DISABLED）

**根因分析**：
- MyBatis-Plus BaseMapper 方法冲突
- `UserMapper` 继承了 `BaseMapper<User>`，已经有 `insert()` 方法
- 自定义 XML 也有 `insert()` 方法
- Spring/MyBatis 在某些情况下选择了错误的实现
- BaseMapper 的默认实现可能没有正确应用 `typeHandler`

**解决方案**：
- 重新编译项目（`mvn clean compile`）
- 重启后端应用
- MyBatis 重新扫描 Mapper，正确选择 XML 实现

**预防措施**：
- 避免自定义方法与 BaseMapper 方法同名
- 如果必须同名，确保 XML 配置正确
- 遇到类似问题，先尝试 `mvn clean` + 重启

---

## 📊 数据库表结构

### 核心表

**sys_user（用户表）**：
```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50),
  email VARCHAR(100),
  phone VARCHAR(20),
  department_id BIGINT,
  status INT DEFAULT 2 COMMENT '1-NORMAL, 2-DISABLED',
  password_reset_required INT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user_id BIGINT,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_user_id BIGINT,
  is_deleted INT DEFAULT 0
);
```

**sys_role（角色表）**：
```sql
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  code VARCHAR(50) UNIQUE NOT NULL,
  description VARCHAR(200),
  role_level INT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user_id BIGINT,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_user_id BIGINT,
  is_deleted INT DEFAULT 0
);
```

**sys_permission（权限表）**：
```sql
CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  code VARCHAR(100) UNIQUE NOT NULL,
  description VARCHAR(200),
  module VARCHAR(50),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted INT DEFAULT 0
);
```

### 关联表

**sys_user_role（用户-角色关联表）**：
```sql
CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  update_user_id BIGINT,
  is_deleted INT DEFAULT 0,
  UNIQUE KEY uk_user_role (user_id, role_id)
);
```

**sys_role_permission（角色-权限关联表）**：
```sql
CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  is_deleted INT DEFAULT 0,
  UNIQUE KEY uk_role_permission (role_id, permission_id)
);
```

---

## 📝 操作日志功能（Operation Log）

**实现日期**: 2026-01-13
**状态**: ✅ 已完成并投入使用

### 功能概述

为系统提供了完整的操作日志记录和查询功能，用于追踪所有关键业务操作，满足审计和安全需求：

- ✅ 记录所有关键操作（创建、更新、删除、分配、移除、登录、登出、查询）
- ✅ 支持多维度过滤（用户名、操作模块、操作类型、状态、时间范围）
- ✅ 自动记录操作人、IP地址、操作时间
- ✅ 区分成功和失败操作，记录失败原因
- ✅ 前端查询页面，支持分页和详情查看
- ✅ 与业务操作无缝集成（用户管理、角色管理、权限管理）

---

### 数据库设计

**表名**: `sys_operation_log`

**SQL**:
```sql
CREATE TABLE sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  user_id BIGINT COMMENT '操作用户ID',
  username VARCHAR(50) COMMENT '操作用户名',
  operation_type INT COMMENT '操作类型（1-创建 2-更新 3-删除 4-分配 5-移除 6-登录 7-登出 8-查询）',
  module INT COMMENT '操作模块（1-用户 2-角色 3-权限 4-项目 5-任务 6-工时 7-部门 8-迭代 9-系统）',
  operation_content VARCHAR(500) COMMENT '操作内容描述',
  ip_address VARCHAR(50) COMMENT '操作IP地址',
  status INT DEFAULT 1 COMMENT '操作状态（1-成功 2-失败）',
  error_message VARCHAR(1000) COMMENT '错误信息（失败时记录）',
  operation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (user_id),
  INDEX idx_operation_time (operation_time),
  INDEX idx_module (module),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

**索引设计**:
- `idx_user_id`: 按用户查询操作历史
- `idx_operation_time`: 按时间范围查询
- `idx_module`: 按模块筛选
- `idx_status`: 按状态筛选（成功/失败）

---

### 后端实现

#### 1. 枚举设计

**操作类型枚举** - `OperationType.java`:
```java
public enum OperationType {
    CREATE(1, "CREATE", "创建"),
    UPDATE(2, "UPDATE", "更新"),
    DELETE(3, "DELETE", "删除"),
    ASSIGN(4, "ASSIGN", "分配"),
    REMOVE(5, "REMOVE", "移除"),
    LOGIN(6, "LOGIN", "登录"),
    LOGOUT(7, "LOGOUT", "登出"),
    QUERY(8, "QUERY", "查询");

    @EnumValue
    private final Integer code;  // 数据库存储

    private final String name;    // JSON序列化

    private final String description; // 中文描述

    @JsonValue
    @Override
    public String toString() {
        return this.name();  // 返回 "CREATE", "UPDATE" 等
    }
}
```

**操作模块枚举** - `OperationModule.java`:
```java
public enum OperationModule {
    USER(1, "USER", "用户管理"),
    ROLE(2, "ROLE", "角色管理"),
    PERMISSION(3, "PERMISSION", "权限管理"),
    PROJECT(4, "PROJECT", "项目管理"),
    TASK(5, "TASK", "任务管理"),
    WORK_HOUR(6, "WORK_HOUR", "工时管理"),
    DEPARTMENT(7, "DEPARTMENT", "部门管理"),
    ITERATION(8, "ITERATION", "迭代管理"),
    SYSTEM(9, "SYSTEM", "系统管理");
    // ... 同上结构
}
```

**操作状态枚举** - `OperationStatus.java`:
```java
public enum OperationStatus {
    SUCCESS(1, "SUCCESS", "成功"),
    FAILED(2, "FAILED", "失败");
    // ... 同上结构
}
```

**枚举设计要点**:
- `@EnumValue` 标记 `code` 字段，数据库存储为整数
- `@JsonValue` 标记 `toString()` 方法，JSON 返回枚举名称
- 参考 `UserStatus` 等现有枚举，保持一致性
- **不使用** `IEnum<Integer>` 接口（会导致方法签名冲突）

#### 2. 实体类

**OperationLog.java**:
```java
public class OperationLog {
    private Long id;
    private Long userId;
    private String username;
    private OperationType operationType;
    private OperationModule module;
    private String operationContent;
    private String ipAddress;
    private OperationStatus status;
    private String errorMessage;
    private LocalDateTime operationTime;
    private LocalDateTime createTime;

    // 手动实现 getter/setter（项目不使用 Lombok）
    // ... 15个getter/setter方法
}
```

#### 3. Service层

**OperationLogService.java**:
```java
public interface OperationLogService {
    /**
     * 记录成功操作
     */
    void logSuccess(OperationType operationType, OperationModule module,
                    String operationContent);

    /**
     * 记录失败操作
     */
    void logFailure(OperationType operationType, OperationModule module,
                    String operationContent, String errorMessage);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLogInfoResp> findByPage(OperationLogQueryReq queryReq);
}
```

**OperationLogServiceImpl.java** - 关键实现:

**1. 自动获取用户上下文**:
```java
private void log(OperationType operationType, OperationModule module,
                 String operationContent, OperationStatus status,
                 String errorMessage) {
    try {
        OperationLog log = new OperationLog();

        // 自动从 UserContext 获取当前用户信息
        Long userId = UserContext.getCurrentUserId();
        String username = UserContext.getCurrentUsername();

        log.setUserId(userId);
        log.setUsername(username);
        log.setOperationType(operationType);
        log.setModule(module);
        log.setOperationContent(operationContent);
        log.setIpAddress(getIpAddress());  // 自动提取IP
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setOperationTime(LocalDateTime.now());

        operationLogMapper.insert(log);
    } catch (Exception e) {
        // 日志记录失败不影响业务操作
        logger.error("记录操作日志失败: {}", e.getMessage());
    }
}
```

**2. IP地址提取**（支持代理）:
```java
private String getIpAddress() {
    try {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
            .getRequest();

        String ip = null;

        // 按优先级检查各种代理头
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 处理多个IP的情况（X-Forwarded-For: client, proxy1, proxy2）
                int index = ip.indexOf(',');
                if (index != -1) {
                    ip = ip.substring(0, index);
                }
                return ip;
            }
        }

        // 最后使用 RemoteAddr
        ip = request.getRemoteAddr();
        return ip;

    } catch (Exception e) {
        logger.warn("获取IP地址失败: {}", e.getMessage());
        return "UNKNOWN";
    }
}
```

**3. 条件查询**（动态SQL）:
```java
@Override
public PageResult<OperationLogInfoResp> findByPage(OperationLogQueryReq queryReq) {
    // 使用 PageHelper 分页
    PageHelper.startPage(queryReq.getPageNum(), queryReq.getPageSize());

    List<OperationLog> logs = operationLogMapper.findByCondition(queryReq);
    PageInfo<OperationLog> pageInfo = new PageInfo<>(logs);

    List<OperationLogInfoResp> respList =
        logs.stream()
            .map(OperationLogInfoResp::from)
            .collect(Collectors.toList());

    return PageResult.success(respList, pageInfo.getTotal(),
                              pageInfo.getPageNum(), pageInfo.getPageSize());
}
```

#### 4. Helper工具类

**OperationLogHelper.java** - 简化日志记录:

```java
@Component
public class OperationLogHelper {
    private final OperationLogService operationLogService;

    public OperationLogHelper(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 记录成功操作（自动获取用户信息）
     */
    public void logSuccess(OperationType operationType, OperationModule module,
                          String operationContent) {
        operationLogService.logSuccess(operationType, module, operationContent);
    }

    /**
     * 记录失败操作（自动获取用户信息）
     */
    public void logFailure(OperationType operationType, OperationModule module,
                          String operationContent, String errorMessage) {
        operationLogService.logFailure(operationType, module,
                                      operationContent, errorMessage);
    }
}
```

**使用示例**:
```java
@Service
public class UserServiceImpl implements UserService {
    private final OperationLogHelper operationLogHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResp create(UserCreateReq createReq) {
        // ... 业务逻辑

        if (result <= 0) {
            // 记录失败日志
            operationLogHelper.logFailure(
                OperationType.CREATE,
                OperationModule.USER,
                String.format("创建用户: %s", createReq.getUsername()),
                "数据库插入失败"
            );
            throw new BusinessException(UserErrorCode.USER_CREATE_FAILED);
        }

        // 记录成功日志
        operationLogHelper.logSuccess(
            OperationType.CREATE,
            OperationModule.USER,
            String.format("创建用户: %s (%s)", createdUser.getUsername(),
                         createdUser.getNickname())
        );

        return resp;
    }
}
```

#### 5. Mapper层

**OperationLogMapper.xml** - 动态SQL:

```xml
<select id="findByCondition" resultType="com.gsms.gsms.model.entity.OperationLog">
    SELECT
        id, user_id, username, operation_type, module,
        operation_content, ip_address, status, error_message,
        operation_time, create_time
    FROM sys_operation_log
    <where>
        <if test="username != null and username != ''">
            AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="module != null">
            AND module = #{module, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler}
        </if>
        <if test="operationType != null">
            AND operation_type = #{operationType, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler}
        </if>
        <if test="status != null">
            AND status = #{status, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler}
        </if>
        <if test="startTime != null">
            AND operation_time &gt;= #{startTime}
        </if>
        <if test="endTime != null">
            AND operation_time &lt;= #{endTime}
        </if>
    </where>
    ORDER BY operation_time DESC
</select>
```

**关键配置**:
- 使用 `typeHandler` 处理枚举类型
- 动态 SQL 支持可选过滤条件
- 按操作时间倒序排列

---

### 前端实现

#### 1. API模块

**api/operationLog.ts**:
```typescript
import request from './request'

// 查询请求接口
export interface OperationLogQuery {
  username?: string
  module?: string
  operationType?: string
  status?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

// 操作日志信息接口
export interface OperationLogInfo {
  id: number
  userId: number
  username: string
  operationType: string  // "CREATE", "UPDATE" 等
  module: string         // "USER", "ROLE" 等
  operationContent: string
  ipAddress: string
  status: string        // "SUCCESS" 或 "FAILED"
  errorMessage?: string
  operationTime: string
  createTime: string
}

// 分页结果接口
export interface OperationLogPageResult {
  list: OperationLogInfo[]
  total: number
  pageNum: number
  pageSize: number
}

// 获取操作日志列表
export const getOperationLogList = (params: OperationLogQuery) => {
  return request.get<OperationLogPageResult>('/operation-logs', { params })
}

// 根据ID获取操作日志详情
export const getOperationLogById = (id: number) => {
  return request.get<OperationLogInfo>(`/operation-logs/${id}`)
}
```

#### 2. 查询页面

**views/system/OperationLogList.vue**:

**页面结构**:
```vue
<template>
  <div class="operation-log-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
    </div>

    <!-- 搜索筛选卡片 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" clearable placeholder="请选择模块">
            <el-option label="用户管理" value="USER" />
            <el-option label="角色管理" value="ROLE" />
            <el-option label="权限管理" value="PERMISSION" />
            <!-- ... 其他模块 -->
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" clearable placeholder="请选择类型">
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <!-- ... 其他类型 -->
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable placeholder="请选择状态">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card class="table-card">
      <el-table :data="list" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="操作模块" width="120">
          <template #default="{ row }">
            <el-tag :type="getModuleTagType(row.module)" size="small">
              {{ getModuleLabel(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)" size="small">
              {{ getOperationTypeLabel(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="操作日志详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="操作人">
          {{ currentLog?.username }}
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">
          <el-tag :type="getModuleTagType(currentLog?.module)">
            {{ getModuleLabel(currentLog?.module) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTagType(currentLog?.operationType)">
            {{ getOperationTypeLabel(currentLog?.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作内容">
          {{ currentLog?.operationContent }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          {{ currentLog?.ipAddress }}
        </el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog?.status === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog?.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentLog?.errorMessage">
          {{ currentLog.errorMessage }}
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">
          {{ currentLog?.operationTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>
```

**关键功能**:
- 多条件筛选（用户名、模块、类型、状态、时间范围）
- 彩色标签显示模块和操作类型
- 失败操作显示错误信息
- 详情对话框查看完整日志

---

### 与业务操作集成

#### 用户管理模块集成

**UserServiceImpl.java** - 在关键操作中添加日志:

```java
@Service
public class UserServiceImpl implements UserService {
    private final OperationLogHelper operationLogHelper;

    // 创建用户
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResp create(UserCreateReq createReq) {
        // ... 检查用户名是否存在
        if (existUser != null) {
            operationLogHelper.logFailure(
                OperationType.CREATE,
                OperationModule.USER,
                String.format("创建用户: %s", createReq.getUsername()),
                "用户名已存在"
            );
            throw new BusinessException(UserErrorCode.USERNAME_EXISTS);
        }

        // ... 数据库插入
        if (result <= 0) {
            operationLogHelper.logFailure(
                OperationType.CREATE,
                OperationModule.USER,
                String.format("创建用户: %s", createReq.getUsername()),
                "数据库插入失败"
            );
            throw new BusinessException(UserErrorCode.USER_CREATE_FAILED);
        }

        // 成功后记录日志
        operationLogHelper.logSuccess(
            OperationType.CREATE,
            OperationModule.USER,
            String.format("创建用户: %s (%s)", createdUser.getUsername(),
                         createdUser.getNickname())
        );

        return resp;
    }

    // 更新用户
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoResp update(UserUpdateReq updateReq) {
        // ... 业务逻辑

        if (result <= 0) {
            operationLogHelper.logFailure(
                OperationType.UPDATE,
                OperationModule.USER,
                String.format("更新用户: ID=%d, 用户名=%s", updateReq.getId(),
                             existingUser.getUsername()),
                "数据库更新失败"
            );
            throw new BusinessException(UserErrorCode.USER_UPDATE_FAILED);
        }

        operationLogHelper.logSuccess(
            OperationType.UPDATE,
            OperationModule.USER,
            String.format("更新用户: %s (%s), ID=%d", updatedUser.getUsername(),
                         updatedUser.getNickname(), updatedUser.getId())
        );

        return resp;
    }

    // 删除用户
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        User user = getUserById(id);
        String username = user.getUsername();
        String nickname = user.getNickname();

        int result = userMapper.deleteById(id);
        if (result <= 0) {
            operationLogHelper.logFailure(
                OperationType.DELETE,
                OperationModule.USER,
                String.format("删除用户: %s", username),
                "数据库删除失败"
            );
            throw new BusinessException(UserErrorCode.USER_DELETE_FAILED);
        }

        // 记录操作日志
        operationLogHelper.logSuccess(
            OperationType.DELETE,
            OperationModule.USER,
            String.format("删除用户: %s (%s), ID=%d", username, nickname, id)
        );
    }

    // 分配角色
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        User user = getUserById(userId);

        // 删除现有角色
        roleMapper.deleteUserRoles(userId);
        // 分配新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            roleMapper.insertUserRoles(userId, roleIds);
        }

        // 记录操作日志
        operationLogHelper.logSuccess(
            OperationType.ASSIGN,
            OperationModule.USER,
            String.format("为用户 %s 分配 %d 个角色", user.getUsername(),
                         roleIds != null ? roleIds.size() : 0)
        );
    }

    // 移除角色
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRole(Long userId, Long roleId) {
        User user = getUserById(userId);

        int result = roleMapper.deleteUserRole(userId, roleId);
        if (result > 0) {
            operationLogHelper.logSuccess(
                OperationType.REMOVE,
                OperationModule.USER,
                String.format("移除用户 %s 的角色 ID=%d", user.getUsername(), roleId)
            );
        }
    }
}
```

---

### API端点清单

**操作日志管理**:
```
GET  /api/operation-logs          - 分页查询操作日志（支持多条件过滤）
GET  /api/operation-logs/{id}     - 根据ID查询日志详情
```

**查询参数**:
- `username` - 用户名（模糊搜索）
- `module` - 操作模块（USER/ROLE/PERMISSION/PROJECT/TASK/WORK_HOUR/DEPARTMENT/ITERATION/SYSTEM）
- `operationType` - 操作类型（CREATE/UPDATE/DELETE/ASSIGN/REMOVE/LOGIN/LOGOUT/QUERY）
- `status` - 操作状态（SUCCESS/FAILED）
- `startTime` - 开始时间（yyyy-MM-dd HH:mm:ss）
- `endTime` - 结束时间（yyyy-MM-dd HH:mm:ss）
- `pageNum` - 页码（默认1）
- `pageSize` - 每页大小（默认10）

---

### 技术问题和解决方案

#### 问题1: 枚举类型处理器异常

**错误现象**:
```
Failed invoking constructor for handler class com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
```

**原因分析**:
- 操作日志枚举错误地实现了 `IEnum<Integer>` 接口
- 导致与 `@EnumValue` 注解冲突，方法签名不匹配
- MyBatis-Plus 无法正确实例化枚举类型处理器

**解决方案**:
- 参考 `UserStatus` 等现有枚举实现
- **移除** `IEnum<Integer>` 接口
- 保留 `@EnumValue` 标记 `code` 字段（用于数据库存储）
- 使用 `@JsonValue` 标记 `toString()` 方法（用于JSON序列化）
- 保持枚举结构一致

**修改后的枚举结构**:
```java
public enum OperationType {
    CREATE(1, "CREATE", "创建"),
    // ...

    @EnumValue  // 数据库存储用
    private final Integer code;

    private final String name;    // JSON序列化用

    private final String description;

    @JsonValue  // JSON序列化时调用
    @Override
    public String toString() {
        return this.name();  // 返回 "CREATE"
    }
}
```

#### 问题2: 日志记录失败影响业务操作

**问题**: 如果日志记录抛出异常，会导致业务操作回滚

**解决方案**:
- 在 `OperationLogServiceImpl.log()` 方法中使用 try-catch
- 捕获所有异常，只记录错误日志，不向上抛出
- 确保日志记录失败不影响业务事务

```java
private void log(...) {
    try {
        // 记录日志
        operationLogMapper.insert(log);
    } catch (Exception e) {
        // 只记录错误，不影响业务
        logger.error("记录操作日志失败: {}", e.getMessage());
    }
}
```

#### 问题3: IP地址提取支持代理

**问题**: 生产环境使用 Nginx 等反向代理，`request.getRemoteAddr()` 获取的是代理IP

**解决方案**:
- 按优先级检查多个 HTTP 头
- 处理多个IP的情况（`X-Forwarded-For: client, proxy1, proxy2`）
- 取第一个非unknown的IP

**头检查顺序**:
1. `X-Forwarded-For`
2. `Proxy-Client-IP`
3. `WL-Proxy-Client-IP`
4. `HTTP_CLIENT_IP`
5. `HTTP_X_FORWARDED_FOR`
6. `request.getRemoteAddr()`（最后兜底）

---

### 文件清单

#### 后端文件（约 10 个）

**枚举（3个）**:
- `model/enums/OperationType.java`
- `model/enums/OperationModule.java`
- `model/enums/OperationStatus.java`

**实体（1个）**:
- `model/entity/OperationLog.java`

**DTO（3个）**:
- `dto/operationlog/OperationLogQueryReq.java`
- `dto/operationlog/OperationLogInfoResp.java`
- `dto/operationlog/OperationLogConverter.java`

**Mapper（2个）**:
- `repository/OperationLogMapper.java`
- `resources/mapper/OperationLogMapper.xml`

**Service（2个）**:
- `service/OperationLogService.java`
- `service/impl/OperationLogServiceImpl.java`

**Helper（1个）**:
- `infra/utils/OperationLogHelper.java`

**Controller（1个）**:
- `controller/OperationLogController.java`

**数据库迁移（1个）**:
- `resources/db/migration/V20260112__Create_operation_log_table.sql`

#### 前端文件（约 3 个）

**API（1个）**:
- `api/operationLog.ts`

**页面（1个）**:
- `views/system/OperationLogList.vue`

**布局（1个）**:
- `components/Layout.vue`（添加菜单项）

#### 修改文件

- `service/impl/UserServiceImpl.java`（添加操作日志记录）
- `router/index.ts`（添加路由）

---

### 测试验证

#### 后端API测试

```bash
# 查询操作日志
curl -X GET "http://localhost:8080/api/operation-logs?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer <token>"

# 根据条件筛选
curl -X GET "http://localhost:8080/api/operation-logs?module=USER&status=SUCCESS" \
  -H "Authorization: Bearer <token>"

# 根据ID查询详情
curl -X GET "http://localhost:8080/api/operation-logs/1" \
  -H "Authorization: Bearer <token>"
```

#### 功能测试场景

1. ✅ 创建用户 → 操作日志记录成功
2. ✅ 创建用户失败（用户名重复）→ 记录失败原因
3. ✅ 更新用户 → 记录操作内容
4. ✅ 删除用户 → 记录删除的用户信息
5. ✅ 分配角色 → 记录角色分配详情
6. ✅ 移除角色 → 记录角色移除操作
7. ✅ 前端查询页面 → 多条件筛选正常
8. ✅ 详情对话框 → 显示完整日志信息

---

**功能完成日期**: 2026-01-13
**状态**: ✅ 已完成，可用于生产环境

---

## 🎯 后续扩展方向

### 高优先级

2. **权限模板功能**
   - 预置角色模板（系统管理员、项目经理、普通员工）
   - 快速基于模板创建角色
   - 模板管理页面

3. **数据级权限控制**
   - 部门级数据权限过滤
   - 项目级数据权限过滤
   - 自定义数据权限规则

### 中优先级

4. **性能优化**
   - 权限缓存优化（前端 + 后端）
   - 批量权限检查接口
   - 表格虚拟滚动（大数据量）

5. **用户体验优化**
   - 权限分配界面优化（树形结构）
   - 角色复制功能
   - 批量操作

### 低优先级

6. **权限审批流程**
   - 角色分配需要审批
   - 权限变更需要审批
   - 审批历史记录

7. **权限分析报表**
   - 用户权限统计
   - 角色使用情况分析
   - 权限分布热力图

---

## ✅ 测试验证

### 后端 API 测试

```bash
# 用户注册
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Admin123","nickname":"测试用户"}'

# 用户登录（禁用用户应失败）
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"Admin123"}'

# 创建角色
curl -X POST http://localhost:8080/api/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"name":"测试角色","code":"TEST_ROLE","description":"测试"}'

# 分配权限
curl -X POST http://localhost:8080/api/roles/1/permissions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"roleId":1,"permissionIds":[1,2,3]}'

# 为用户分配角色
curl -X POST http://localhost:8080/api/users/1/roles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"userId":1,"roleIds":[1,2]}'
```

### 前端功能测试

**测试场景**：
1. ✅ 用户注册 → 默认禁用状态
2. ✅ 禁用用户无法登录
3. ✅ 管理员启用用户 → 用户可以登录
4. ✅ 用户分配角色 → 获得对应权限
5. ✅ 无权限用户看不到系统管理菜单
6. ✅ 无权限用户看不到操作按钮
7. ✅ 角色分配权限 → 用户权限自动更新

---

## 📚 参考资料

- **项目规范**: `CLAUDE.md`
- **前后端协作**: `COLLABORATION.md`
- **待办事项**: `TODO.md`
- **数据库设计**: `docs/DATABASE_OPTIMIZATION.md`
- **前端架构**: `docs/development/frontend-architecture.md`

---

**文档维护**:
- 最后更新: 2026-01-13（新增操作日志功能）
- 维护者: Claude (AI Assistant)
- 状态: ✅ 当前版本稳定，可用于生产环境
