import request from './request'

// 迭代查询参数
export interface IterationQuery {
  projectId?: number
  status?: string
  pageNum?: number
  pageSize?: number
}

// 迭代信息响应
export interface IterationInfo {
  id: number
  projectId: number
  name: string
  description?: string
  status: string
  planStartDate?: string
  planEndDate?: string
  actualStartDate?: string
  actualEndDate?: string
  createUserId?: number
  createUserName?: string
  updateUserId?: number
  updateUserName?: string
  createTime?: string
  updateTime?: string
}

// 创建迭代请求
export interface IterationCreateReq {
  projectId: number
  name: string
  description?: string
  status?: string
  planStartDate?: string
  planEndDate?: string
}

// 更新迭代请求
export interface IterationUpdateReq {
  id: number
  name?: string
  description?: string
  status?: string
  planStartDate?: string
  planEndDate?: string
  actualStartDate?: string
  actualEndDate?: string
}

// 分页查询迭代列表
export const getIterationList = (data: IterationQuery) => {
  return request.post('/iterations/query', data)
}

// 获取迭代详情
export const getIterationDetail = (id: number) => {
  return request.get(`/iterations/${id}`)
}

// 创建迭代
export const createIteration = (data: IterationCreateReq) => {
  return request.post('/iterations', data)
}

// 更新迭代
export const updateIteration = (data: IterationUpdateReq) => {
  return request.put('/iterations', data)
}

// 删除迭代
export const deleteIteration = (id: number) => {
  return request.delete(`/iterations/${id}`)
}
