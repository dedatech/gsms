import request from './request'

// 权限查询接口
export interface PermissionQuery {
  name?: string
  code?: string
  pageNum?: number
  pageSize?: number
}

// 权限信息接口
export interface PermissionInfo {
  id: number
  name: string
  code: string
  description?: string
  createTime: string
  updateTime: string
  roleIds?: number[]
}

// 创建权限请求
export interface PermissionCreateReq {
  name: string
  code: string
  description?: string
}

// 更新权限请求
export interface PermissionUpdateReq {
  id: number
  name?: string
  code?: string
  description?: string
}

/**
 * 根据ID查询权限
 */
export const getPermissionById = (id: number) => {
  return request.get<PermissionInfo>(`/permissions/${id}`)
}

/**
 * 分页查询权限列表
 */
export const getPermissionList = (params: PermissionQuery) => {
  return request.get('/permissions', { params })
}

/**
 * 获取所有权限（不分页，用于角色分配）
 */
export const getAllPermissions = () => {
  return request.get<PermissionInfo[]>('/permissions/all')
}

/**
 * 创建权限
 */
export const createPermission = (data: PermissionCreateReq) => {
  return request.post('/permissions', data)
}

/**
 * 更新权限
 */
export const updatePermission = (data: PermissionUpdateReq) => {
  return request.put('/permissions', data)
}

/**
 * 删除权限
 */
export const deletePermission = (id: number) => {
  return request.delete(`/permissions/${id}`)
}
