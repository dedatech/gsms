import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserPermissions, getUserRoles } from '@/api/user'

/**
 * JWT Token Payload 接口
 */
interface JwtPayload {
  userId: number
  username?: string
  exp?: number
  iat?: number
}

/**
 * 角色信息接口
 */
export interface RoleInfo {
  id: number
  name: string
  code: string
  description?: string
}

/**
 * 认证状态管理 Store
 */
export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>('')
  const userId = ref<number>(0)
  const username = ref<string>('')
  const permissions = ref<Set<string>>(new Set())
  const userRoles = ref<RoleInfo[]>([])

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const currentUser = computed(() => ({
    id: userId.value,
    username: username.value
  }))

  /**
   * 从 JWT Token 中解析用户信息
   */
  const parseToken = (tokenValue: string): JwtPayload | null => {
    try {
      // JWT 格式: header.payload.signature
      const parts = tokenValue.split('.')
      if (parts.length !== 3) {
        console.error('无效的 JWT token 格式')
        return null
      }

      // 解码 payload（Base64 URL 编码）
      const payload = parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/')
      const decoded = atob(payload)
      return JSON.parse(decoded) as JwtPayload
    } catch (error) {
      console.error('解析 JWT token 失败:', error)
      return null
    }
  }

  /**
   * 设置认证信息
   */
  const setAuth = (tokenValue: string, usernameValue?: string) => {
    token.value = tokenValue

    // 解析 token 获取 userId
    const payload = parseToken(tokenValue)
    if (payload && payload.userId) {
      userId.value = payload.userId
      console.log('从 token 解析的 userId:', payload.userId)
    }

    // 设置用户名
    if (usernameValue) {
      username.value = usernameValue
    } else if (payload && payload.username) {
      username.value = payload.username
    }

    // 持久化到 localStorage
    localStorage.setItem('token', tokenValue)
    localStorage.setItem('userId', userId.value.toString())
    if (username.value) {
      localStorage.setItem('username', username.value)
    }
  }

  /**
   * 清除认证信息
   */
  const clearAuth = () => {
    token.value = ''
    userId.value = 0
    username.value = ''
    permissions.value.clear()
    userRoles.value = []

    // 清除 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  /**
   * 从 localStorage 恢复认证信息
   */
  const restoreAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserId = localStorage.getItem('userId')
    const savedUsername = localStorage.getItem('username')

    if (savedToken) {
      token.value = savedToken
      userId.value = savedUserId ? parseInt(savedUserId) : 0
      username.value = savedUsername || ''

      // 验证 token 是否有效
      const payload = parseToken(savedToken)
      if (payload && payload.exp) {
        const now = Math.floor(Date.now() / 1000)
        if (payload.exp < now) {
          console.log('Token 已过期，清除认证信息')
          clearAuth()
        }
      }
    }
  }

  /**
   * 获取当前用户ID
   */
  const getCurrentUserId = (): number => {
    return userId.value || 0
  }

  /**
   * 获取Token
   */
  const getToken = (): string => {
    return token.value
  }

  /**
   * 获取用户权限（从后端加载）
   */
  const fetchUserPermissions = async () => {
    const currentUserId = getCurrentUserId()
    if (!currentUserId) {
      console.warn('未登录，无法获取用户权限')
      return
    }

    try {
      const permissionCodes = await getUserPermissions(currentUserId)
      permissions.value = new Set(permissionCodes)
      console.log('用户权限加载成功:', permissions.value)
    } catch (error) {
      console.error('获取用户权限失败:', error)
      permissions.value.clear()
    }
  }

  /**
   * 获取用户角色（从后端加载）
   */
  const fetchUserRoles = async () => {
    const currentUserId = getCurrentUserId()
    if (!currentUserId) {
      console.warn('未登录，无法获取用户角色')
      return
    }

    try {
      const roleIds = await getUserRoles(currentUserId)
      // TODO: 后续可以根据roleIds获取角色详情
      console.log('用户角色加载成功:', roleIds)
    } catch (error) {
      console.error('获取用户角色失败:', error)
      userRoles.value = []
    }
  }

  /**
   * 检查是否有指定权限
   */
  const hasPermission = (permission: string): boolean => {
    return permissions.value.has(permission)
  }

  /**
   * 检查是否有任一权限
   */
  const hasAnyPermission = (permissionList: string[]): boolean => {
    return permissionList.some(p => permissions.value.has(p))
  }

  /**
   * 刷新权限和角色信息
   */
  const refreshAuth = async () => {
    await Promise.all([
      fetchUserPermissions(),
      fetchUserRoles()
    ])
  }

  return {
    // 状态
    token,
    userId,
    username,
    permissions,
    userRoles,
    // 计算属性
    isAuthenticated,
    currentUser,
    // 方法
    setAuth,
    clearAuth,
    restoreAuth,
    getCurrentUserId,
    getToken,
    parseToken,
    fetchUserPermissions,
    fetchUserRoles,
    hasPermission,
    hasAnyPermission,
    refreshAuth
  }
})
