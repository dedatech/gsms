import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupPermissionGuard } from './permission'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', requiresAuth: true },
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('@/views/project/ProjectList.vue'),
        meta: { title: '项目管理', requiresAuth: true },
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/project/ProjectDetail.vue'),
        meta: { title: '项目详情', requiresAuth: true },
      },
      {
        path: 'projects/:id/gantt',
        name: 'ProjectGantt',
        component: () => import('@/views/project/ProjectGanttView.vue'),
        meta: { title: '项目甘特图', requiresAuth: true },
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/task/TaskList.vue'),
        meta: { title: '任务中心', requiresAuth: true },
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/TaskDetail.vue'),
        meta: { title: '任务详情', requiresAuth: true },
      },
      {
        path: 'projects/:projectId/iterations/:iterationId',
        name: 'IterationDetail',
        component: () => import('@/views/iteration/IterationDetail.vue'),
        meta: { title: '迭代详情', requiresAuth: true },
      },
      {
        path: 'workhours/calendar',
        name: 'WorkHourCalendar',
        component: () => import('@/views/workhour/WorkHourCalendar.vue'),
        meta: { title: '工时日历', requiresAuth: true },
      },
      {
        path: 'workhours/list',
        name: 'WorkHourList',
        component: () => import('@/views/workhour/WorkHourList.vue'),
        meta: { title: '工时列表', requiresAuth: true },
      },
      {
        path: 'system/users',
        name: 'Users',
        component: () => import('@/views/system/UserList.vue'),
        meta: { title: '用户管理', requiresAuth: true },
      },
      {
        path: 'system/roles',
        name: 'Roles',
        component: () => import('@/views/system/RoleList.vue'),
        meta: { title: '角色管理', requiresAuth: true },
      },
      {
        path: 'system/permissions',
        name: 'Permissions',
        component: () => import('@/views/system/PermissionList.vue'),
        meta: { title: '权限管理', requiresAuth: true },
      },
      {
        path: 'system/operation-logs',
        name: 'OperationLogs',
        component: () => import('@/views/system/OperationLogList.vue'),
        meta: { title: '操作日志', requiresAuth: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '404', requiresAuth: false },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 设置权限守卫
setupPermissionGuard(router)

// 设置页面标题
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - GSMS` : 'GSMS 工时管理系统'
})

export default router
