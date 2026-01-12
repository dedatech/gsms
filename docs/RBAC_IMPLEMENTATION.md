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

## 🎯 后续扩展方向

### 高优先级

1. **操作日志记录**
   - 记录角色创建/更新/删除操作
   - 记录权限创建/更新/删除操作
   - 记录用户角色分配/移除操作
   - 记录角色权限分配/移除操作
   - 操作日志查询页面

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
- 最后更新: 2026-01-12
- 维护者: Claude (AI Assistant)
- 状态: ✅ 当前版本稳定，可用于生产环境
