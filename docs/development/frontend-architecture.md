# 前端架构文档

**文档版本：** 1.0
**创建时间：** 2026-01-08
**最后更新：** 2026-01-08

---

## 目录
1. [技术栈](#技术栈)
2. [项目结构](#项目结构)
3. [状态管理](#状态管理)
4. [认证管理](#认证管理)
5. [路由设计](#路由设计)
6. [API 请求](#api-请求)
7. [组件设计](#组件设计)
8. [样式规范](#样式规范)

---

## 技术栈

### 核心框架
- **Vue 3.4** - 渐进式 JavaScript 框架
- **Vite 5.0** - 下一代前端构建工具
- **TypeScript 5.3** - JavaScript 的超集

### UI 框架
- **Element Plus 2.5** - Vue 3 组件库
- **@element-plus/icons-vue** - Element Plus 图标库

### 状态管理
- **Pinia 2.1** - Vue 3 官方推荐的状态管理库

### 路由
- **Vue Router 4** - Vue 3 官方路由

### HTTP 客户端
- **Axios 1.6** - HTTP 请求库

---

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口
│   │   ├── request.ts   # Axios 实例和拦截器
│   │   ├── auth.ts      # 认证相关接口
│   │   ├── user.ts      # 用户相关接口
│   │   ├── project.ts   # 项目相关接口
│   │   ├── task.ts      # 任务相关接口
│   │   ├── iteration.ts # 迭代相关接口
│   │   └── workhour.ts  # 工时相关接口
│   ├── assets/          # 静态资源
│   ├── components/      # 公共组件
│   ├── router/          # 路由配置
│   │   └── index.ts
│   ├── stores/          # Pinia 状态管理
│   │   └── auth.ts      # 认证状态 Store
│   ├── types/           # TypeScript 类型定义
│   ├── views/           # 页面组件
│   │   ├── auth/        # 认证相关页面
│   │   │   ├── LoginView.vue
│   │   │   └── RegisterView.vue
│   │   ├── project/     # 项目相关页面
│   │   │   ├── ProjectList.vue
│   │   │   └── ProjectDetail.vue
│   │   ├── task/        # 任务相关页面
│   │   │   ├── TaskList.vue
│   │   │   └── TaskDetail.vue
│   │   ├── iteration/   # 迭代相关页面
│   │   │   ├── IterationList.vue
│   │   │   └── IterationDetail.vue
│   │   └── workhour/    # 工时相关页面
│   │       └── WorkHourList.vue
│   ├── App.vue          # 根组件
│   └── main.ts          # 应用入口
├── public/              # 公共静态资源
├── index.html           # HTML 模板
├── vite.config.ts       # Vite 配置
├── tsconfig.json        # TypeScript 配置
└── package.json         # 项目依赖
```

---

## 状态管理

### Pinia Store 架构

项目使用 Pinia 作为状态管理方案，相比 Vuex 3 具有以下优势：
- 更好的 TypeScript 支持
- 更简洁的 API
- 支持 Composition API
- 自动代码分割

### Auth Store - 认证状态管理

**文件位置：** `src/stores/auth.ts`

#### 核心功能

**1. 状态定义**
```typescript
interface JwtPayload {
  userId: number
  username?: string
  exp?: number
  iat?: number
}

// 状态
const token = ref<string>('')
const userId = ref<number>(0)
const username = ref<string>('')

// 计算属性
const isAuthenticated = computed(() => !!token.value)
const currentUser = computed(() => ({
  id: userId.value,
  username: username.value
}))
```

**2. JWT Token 解析**
```typescript
const parseToken = (tokenValue: string): JwtPayload | null => {
  try {
    // JWT 格式: header.payload.signature
    const parts = tokenValue.split('.')
    if (parts.length !== 3) {
      return null
    }

    // 解码 payload（Base64 URL 编码）
    const payload = parts[1]
      .replace(/-/g, '+')
      .replace(/_/g, '/')
    const decoded = atob(payload)
    return JSON.parse(decoded) as JwtPayload
  } catch (error) {
    return null
  }
}
```

**3. 认证信息管理**
```typescript
// 设置认证信息（登录时调用）
const setAuth = (tokenValue: string, usernameValue?: string) => {
  token.value = tokenValue

  // 自动解析 token 获取 userId
  const payload = parseToken(tokenValue)
  if (payload && payload.userId) {
    userId.value = payload.userId
  }

  // 设置用户名
  username.value = usernameValue || payload?.username || ''

  // 持久化到 localStorage
  localStorage.setItem('token', tokenValue)
  localStorage.setItem('userId', userId.value.toString())
  localStorage.setItem('username', username.value)
}

// 清除认证信息（退出登录时调用）
const clearAuth = () => {
  token.value = ''
  userId.value = 0
  username.value = ''

  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('username')
}

// 从 localStorage 恢复认证信息（应用启动时调用）
const restoreAuth = () => {
  const savedToken = localStorage.getItem('token')
  const savedUserId = localStorage.getItem('userId')
  const savedUsername = localStorage.getItem('username')

  if (savedToken) {
    token.value = savedToken
    userId.value = savedUserId ? parseInt(savedUserId) : 0
    username.value = savedUsername || ''

    // 验证 token 是否过期
    const payload = parseToken(savedToken)
    if (payload && payload.exp) {
      const now = Math.floor(Date.now() / 1000)
      if (payload.exp < now) {
        clearAuth() // Token 已过期，清除认证信息
      }
    }
  }
}
```

**4. 工具方法**
```typescript
// 获取当前用户ID
const getCurrentUserId = (): number => {
  return userId.value || 0
}
```

#### 使用示例

```typescript
// 在任何组件中使用
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 获取当前用户ID
const userId = authStore.getCurrentUserId()

// 检查是否已登录
if (authStore.isAuthenticated) {
  // 已登录
}

// 获取用户信息
const { id, username } = authStore.currentUser

// 登录
authStore.setAuth(token, username)

// 退出登录
authStore.clearAuth()
```

---

## 认证管理

### 认证流程

```
┌─────────────┐
│  用户登录   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  LoginView.vue  │
│  - 调用登录 API  │
│  - 获取 token   │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  authStore      │
│  - setAuth()    │
│  - 解析 JWT     │
│  - 保存到 store │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  localStorage   │
│  - 持久化存储   │
└─────────────────┘
```

### Axios 拦截器集成

**请求拦截器** - 自动添加 Token
```typescript
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    const token = authStore.token

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)
```

**响应拦截器** - 处理 401 错误
```typescript
service.interceptors.response.use(
  (response) => {
    // 处理成功响应
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.clearAuth() // 清除认证信息
      router.push('/login')  // 跳转到登录页
    }
    return Promise.reject(error)
  }
)
```

### 应用启动恢复认证

**文件：** `src/main.ts`
```typescript
const pinia = createPinia()

app.use(pinia)
app.use(router)

// 在应用挂载前恢复认证信息
const authStore = useAuthStore()
authStore.restoreAuth()

app.mount('#app')
```

---

## 路由设计

### 路由配置

**文件：** `src/router/index.ts`

```typescript
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      {
        path: 'projects',
        name: 'ProjectList',
        component: () => import('@/views/project/ProjectList.vue')
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetail.vue')
      },
      // ... 更多路由
    ]
  }
]
```

### 路由守卫（待实现）

建议添加路由守卫来保护需要认证的页面：

```typescript
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 需要认证的页面
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else {
    next()
  }
})
```

---

## API 请求

### Axios 配置

**文件：** `src/api/request.ts`

#### 基础配置
```typescript
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})
```

#### 响应拦截器

**PageResult 处理**
```typescript
// 自动转换分页响应格式
if (data.total !== undefined) {
  return {
    list: data.data,
    total: data.total,
    pageNum: data.pageNum,
    pageSize: data.pageSize
  }
}
```

**标准 Result 处理**
```typescript
if (code === 200) {
  return resultData
}
```

### API 模块设计

每个模块都有独立的 API 文件：

**示例：** `src/api/project.ts`
```typescript
import request from './request'

// 查询参数接口
export interface ProjectQuery {
  name?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

// 获取项目列表
export const getProjectList = (params: ProjectQuery) => {
  return request.get('/projects', { params })
}

// 创建项目请求
export interface ProjectCreateReq {
  name: string
  code: string
  description?: string
  managerId: number
  status: string
}

// 创建项目
export const createProject = (data: ProjectCreateReq) => {
  return request.post('/projects', data)
}

// 更新项目
export const updateProject = (data: ProjectUpdateReq) => {
  return request.put('/projects', data)
}

// 删除项目
export const deleteProject = (id: number) => {
  return request.delete(`/projects/${id}`)
}
```

---

## 组件设计

### 页面组件规范

**文件命名：**
- 列表页：`{Module}List.vue`（如 `ProjectList.vue`）
- 详情页：`{Module}Detail.vue`（如 `ProjectDetail.vue`）
- 表单页：`{Module}Form.vue`

**组件结构：**
```vue
<template>
  <!-- 页面模板 -->
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getProjectList } from '@/api/project'

// 使用组合式 API
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const projectList = ref<any[]>([])

// 生命周期
onMounted(() => {
  // 初始化逻辑
})

// 方法定义
const fetchData = async () => {
  // 数据获取逻辑
}
</script>

<style scoped>
/* 组件样式 */
</style>
```

### 公共组件（待扩展）

建议创建以下公共组件：
- `PageHeader.vue` - 页面头部
- `DataTable.vue` - 数据表格
- `SearchForm.vue` - 搜索表单
- `ConfirmDialog.vue` - 确认对话框

---

## 样式规范

### CSS 命名规范

使用 BEM (Block Element Modifier) 命名方法论：

```css
.project-list { }         /* Block */
.project-list__header { } /* Element */
.project-list--loading { } /* Modifier */
```

### 响应式设计

使用 Element Plus 的栅格系统：

```vue
<el-row :gutter="16">
  <el-col :xs="24" :sm="12" :md="8" :lg="6">
    <!-- 在小屏幕上占满整行，在大屏幕上占 1/4 -->
  </el-col>
</el-row>
```

---

## 最佳实践

### 1. 使用 Composition API

```typescript
// ✅ 推荐
<script setup lang="ts">
import { ref, computed } from 'vue'

const count = ref(0)
const doubled = computed(() => count.value * 2)
</script>

// ❌ 不推荐
<script>
export default {
  data() {
    return { count: 0 }
  }
}
</script>
```

### 2. 使用 TypeScript 类型

```typescript
// ✅ 推荐
interface User {
  id: number
  name: string
}

const user = ref<User | null>(null)

// ❌ 不推荐
const user = ref(null)
```

### 3. 统一使用 authStore

```typescript
// ✅ 推荐
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
const userId = authStore.getCurrentUserId()

// ❌ 不推荐
const userId = parseInt(localStorage.getItem('userId') || '0')
```

### 4. 错误处理

```typescript
try {
  const data = await apiCall()
  ElMessage.success('操作成功')
} catch (error) {
  console.error('操作失败:', error)
  ElMessage.error('操作失败，请稍后重试')
}
```

---

## 相关文档

- **前后端联调：** `docs/development/frontend-backend-setup.md`
- **API 文档：** `docs/api-docs.md`
- **项目状态：** `docs/PROJECT_STATUS_AND_PLAN.md`

---

**文档维护：** 本文档应随前端架构演进持续更新
