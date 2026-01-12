import request from './request'

// 获取用户列表
export interface UserQuery {
  username?: string
  nickname?: string
  departmentId?: number
  status?: number
  pageNum?: number
  pageSize?: number
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email?: string
  phone?: string
  departmentId?: number
  departmentName?: string
  status: string // 'NORMAL' | 'DISABLED'
  createTime?: string
  roles?: RoleInfo[]
}

export interface RoleInfo {
  id: number
  name: string
  code: string
  description?: string
}

export const getUserList = (params: UserQuery) => {
  return request.get('/users', { params })
}

// 获取所有用户（用于下拉选择，不分页）
export const getAllUsers = () => {
  return request.get('/users', { params: { pageNum: 1, pageSize: 1000 } })
}

/**
 * 根据ID查询用户角色列表
 */
export const getUserRoles = (userId: number) => {
  return request.get<number[]>(`/users/${userId}/roles`)
}

/**
 * 为用户分配角色
 */
export const assignUserRoles = (userId: number, roleIds: number[]) => {
  return request.post(`/users/${userId}/roles`, { userId, roleIds })
}

/**
 * 移除用户角色
 */
export const removeUserRole = (userId: number, roleId: number) => {
  return request.delete(`/users/${userId}/roles/${roleId}`)
}

/**
 * 获取用户权限编码列表
 */
export const getUserPermissions = (userId: number) => {
  return request.get<string[]>(`/users/${userId}/permissions`)
}

/**
 * 更新用户
 */
export const updateUser = (data: {
  id: number
  nickname?: string
  email?: string
  phone?: string
  departmentId?: number
  status?: string // 'NORMAL' | 'DISABLED'
}) => {
  return request.put('/users', data)
}

/**
 * 删除用户
 */
export const deleteUser = (id: number) => {
  return request.delete(`/users/${id}`)
}
