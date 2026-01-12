import { useAuthStore } from '@/stores/auth'

/**
 * 检查是否有权限
 * @param permission 权限码或权限码数组
 * @returns 是否有权限
 */
export const hasPermission = (permission: string | string[]): boolean => {
  const authStore = useAuthStore()

  if (Array.isArray(permission)) {
    return authStore.hasAnyPermission(permission)
  }

  return authStore.hasPermission(permission)
}

/**
 * 检查是否有任一权限（用于v-if）
 * @param permissions 权限码数组
 * @returns 是否有任一权限
 */
export const checkAnyPermission = (permissions: string[]): boolean => {
  return hasPermission(permissions)
}

/**
 * 检查是否有所有权限
 * @param permissions 权限码数组
 * @returns 是否有所有权限
 */
export const checkAllPermissions = (permissions: string[]): boolean => {
  const authStore = useAuthStore()
  return permissions.every(p => authStore.hasPermission(p))
}

/**
 * 从权限码中提取模块名
 * @param code 权限码，如 "PROJECT_VIEW_ALL"
 * @returns 模块名，如 "项目管理"
 */
export const getModuleFromCode = (code: string): string => {
  if (code.startsWith('PROJECT_')) return '项目管理'
  if (code.startsWith('TASK_')) return '任务管理'
  if (code.startsWith('WORKHOUR_')) return '工时管理'
  if (code.startsWith('ITERATION_')) return '迭代管理'
  if (code.startsWith('USER_')) return '用户管理'
  if (code.startsWith('ROLE_')) return '角色管理'
  if (code.startsWith('PERMISSION_')) return '权限管理'
  return '其他'
}

/**
 * 从权限码中提取操作名
 * @param code 权限码，如 "PROJECT_VIEW_ALL"
 * @returns 操作名，如 "查看所有"
 */
export const getActionFromCode = (code: string): string => {
  const parts = code.split('_')
  const action = parts[parts.length - 1]

  const actionMap: Record<string, string> = {
    VIEW: '查看',
    VIEW_ALL: '查看所有',
    CREATE: '创建',
    EDIT: '编辑',
    UPDATE: '更新',
    DELETE: '删除',
    ASSIGN: '分配',
    EXPORT: '导出',
    IMPORT: '导入',
    APPROVE: '审批',
    SUBMIT: '提交'
  }

  return actionMap[action] || action
}
