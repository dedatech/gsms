import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

/**
 * 设置权限路由守卫
 * @param router Vue Router 实例
 */
export const setupPermissionGuard = (router: Router) => {
  router.beforeEach(async (to, from, next) => {
    try {
      const authStore = useAuthStore()

      // 不需要认证的页面直接放行
      if (!to.meta.requiresAuth) {
        return next()
      }

      // 需要认证的页面
      if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        return next({ name: 'Login', query: { redirect: to.fullPath } })
      }

      // 已登录用户访问登录页，跳转到首页
      if (to.name === 'Login' && authStore.isAuthenticated) {
        return next({ name: 'Dashboard' })
      }

      // 检查路由权限（安全地检查）
      if (to.meta.permissions) {
        try {
          const permissions = to.meta.permissions as string[]
          const hasPermission = authStore.hasAnyPermission(permissions)

          if (!hasPermission) {
            console.warn('没有权限访问该页面:', to.path, '需要的权限:', permissions)
            return next({ name: 'Dashboard' })
          }
        } catch (permError) {
          console.error('权限检查失败:', permError)
          // 权限检查失败时，允许继续访问（避免阻塞用户）
        }
      }

      next()
    } catch (error) {
      console.error('路由守卫执行错误:', error)
      // 发生错误时，仍然允许导航继续（避免卡住用户）
      next()
    }
  })

  // 路由错误处理
  router.onError((error) => {
    console.error('路由错误:', error)
    // 可以在这里添加错误上报逻辑
  })
}

/**
 * 权限路由配置（系统管理相关）
 * 这些路由需要权限才能访问
 */
export const permissionRoutes = [
  {
    path: 'system',
    name: 'System',
    redirect: '/system/users',
    meta: {
      title: '系统管理',
      requiresAuth: true,
      permissions: ['USER_VIEW', 'ROLE_VIEW', 'PERMISSION_VIEW']
    }
  },
  {
    path: 'users',
    name: 'Users',
    component: () => import('@/views/system/UserList.vue'),
    meta: {
      title: '用户管理',
      requiresAuth: true,
      permissions: ['USER_VIEW']
    }
  },
  {
    path: 'roles',
    name: 'Roles',
    component: () => import('@/views/system/RoleList.vue'),
    meta: {
      title: '角色管理',
      requiresAuth: true,
      permissions: ['ROLE_VIEW']
    }
  },
  {
    path: 'permissions',
    name: 'Permissions',
    component: () => import('@/views/system/PermissionList.vue'),
    meta: {
      title: '权限管理',
      requiresAuth: true,
      permissions: ['PERMISSION_VIEW']
    }
  }
]
