import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/login',
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
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/task/TaskList.vue'),
        meta: { title: '任务管理', requiresAuth: true },
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/TaskDetail.vue'),
        meta: { title: '任务详情', requiresAuth: true },
      },
      {
        path: 'iterations',
        name: 'Iterations',
        component: () => import('@/views/iteration/IterationList.vue'),
        meta: { title: '迭代管理', requiresAuth: true },
      },
      {
        path: 'workhours',
        name: 'WorkHours',
        component: () => import('@/views/workhour/WorkHourList.vue'),
        meta: { title: '工时管理', requiresAuth: true },
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

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    // 需要认证但没有 token，跳转到登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && token) {
    // 已登录用户访问登录页，跳转到首页
    next({ name: 'Projects' })
  } else {
    next()
  }

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - GSMS` : 'GSMS 工时管理系统'
})

export default router
