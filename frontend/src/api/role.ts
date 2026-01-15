import request from './request'

// 角色查询接口
export interface RoleQuery {
  name?: string
  code?: string
  roleType?: string
  pageNum?: number
  pageSize?: number
}

// 角色信息接口
export interface RoleInfo {
  id: number
  name: string
  code: string
  description?: string
  roleType: string
  createTime: string
  updateTime: string
  permissionIds?: number[]
}

// 创建角色请求
export interface RoleCreateReq {
  name: string
  code: string
  description?: string
  roleType: string
}

// 更新角色请求
export interface RoleUpdateReq {
  id: number
  name?: string
  code?: string
  description?: string
  roleType?: string
}

// 角色权限分配请求
export interface RolePermissionAssignReq {
  roleId: number
  permissionIds: number[]
}

/**
 * 根据ID查询角色
 */
export const getRoleById = (id: number) => {
  return request.get<RoleInfo>(`/roles/${id}`)
}

/**
 * 分页查询角色列表
 */
export const getRoleList = (params: RoleQuery) => {
  return request.get('/roles', { params })
}

/**
 * 创建角色
 */
export const createRole = (data: RoleCreateReq) => {
  return request.post('/roles', data)
}

/**
 * 更新角色
 */
export const updateRole = (data: RoleUpdateReq) => {
  return request.put('/roles', data)
}

/**
 * 删除角色
 */
export const deleteRole = (id: number) => {
  return request.delete(`/roles/${id}`)
}

/**
 * 查询角色的权限ID列表
 */
export const getRolePermissions = (roleId: number) => {
  return request.get<number[]>(`/roles/${roleId}/permissions`)
}

/**
 * 为角色分配权限
 */
export const assignPermissions = (roleId: number, permissionIds: number[]) => {
  return request.post(`/roles/${roleId}/permissions`, { roleId, permissionIds })
}

/**
 * 移除角色权限
 */
export const removePermission = (roleId: number, permissionId: number) => {
  return request.delete(`/roles/${roleId}/permissions/${permissionId}`)
}

/**
 * 查询拥有该角色的用户列表
 */
export const getRoleUsers = (roleId: number) => {
  return request.get<number[]>(`/roles/${roleId}/users`)
}
