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
  status: number
  createTime?: string
}

export const getUserList = (params: UserQuery) => {
  return request.get('/users', { params })
}

// 获取所有用户（用于下拉选择，不分页）
export const getAllUsers = () => {
  return request.get('/users', { params: { pageNum: 1, pageSize: 1000 } })
}
