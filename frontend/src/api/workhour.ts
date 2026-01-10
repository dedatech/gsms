import request from './request'
import type { PageResult, ApiResponse } from '@/types'

// 工时查询参数
export interface WorkHourQuery {
  userId?: number
  projectId?: number
  taskId?: number
  startDate?: string
  endDate?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

// 工时信息响应
export interface WorkHourInfo {
  id: number
  userId: number
  projectId: number
  taskId?: number
  workDate: string
  hours: number
  content: string
  status: string
  createUserId?: number
  updateUserId?: number
  createTime?: string
  updateTime?: string
  // 关联信息（非数据库字段）
  userName?: string
  projectName?: string
  taskName?: string
}

// 创建工时请求
export interface WorkHourCreateReq {
  projectId: number
  taskId?: number
  workDate: string
  hours: number
  content: string
  status?: string
}

// 更新工时请求
export interface WorkHourUpdateReq {
  id: number
  projectId: number
  taskId?: number
  workDate: string
  hours: number
  content: string
  status?: string
}

// API函数（带类型注解）
export const getWorkHourList = (data: WorkHourQuery): Promise<ApiResponse<PageResult<WorkHourInfo>>> => {
  return request.post('/work-hours/query', data)
}

export const getWorkHourDetail = (id: number): Promise<ApiResponse<WorkHourInfo>> => {
  return request.get(`/work-hours/${id}`)
}

export const createWorkHour = (data: WorkHourCreateReq): Promise<ApiResponse<WorkHourInfo>> => {
  return request.post('/work-hours', data)
}

export const updateWorkHour = (data: WorkHourUpdateReq): Promise<ApiResponse<WorkHourInfo>> => {
  return request.put('/work-hours', data)
}

export const deleteWorkHour = (id: number): Promise<ApiResponse<string>> => {
  return request.delete(`/work-hours/${id}`)
}

export const getWorkHoursByUserId = (userId: number): Promise<ApiResponse<WorkHourInfo[]>> => {
  return request.get(`/work-hours/user/${userId}`)
}

export const getWorkHoursByProjectId = (projectId: number): Promise<ApiResponse<WorkHourInfo[]>> => {
  return request.get(`/work-hours/project/${projectId}`)
}

export const getUserWorkHourStatistics = (
  userId: number,
  startDate?: string,
  endDate?: string
): Promise<ApiResponse<{ totalHours: number }>> => {
  return request.get(`/statistics/user/${userId}`, {
    params: { startDate, endDate }
  })
}

export const getProjectWorkHourStatistics = (
  projectId: number,
  startDate?: string,
  endDate?: string
): Promise<ApiResponse<{ totalHours: number }>> => {
  return request.get(`/statistics/project/${projectId}`, {
    params: { startDate, endDate }
  })
}

export const getWorkHourTrend = (params: {
  projectId?: number
  userId?: number
  startDate: string
  endDate: string
}): Promise<ApiResponse<unknown[]>> => {
  return request.get('/statistics/trend', { params })
}
