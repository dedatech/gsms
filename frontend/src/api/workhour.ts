import request from './request'

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

// 根据条件查询工时记录
export const getWorkHourList = (data: WorkHourQuery) => {
  return request.post('/work-hours/query', data)
}

// 获取工时详情
export const getWorkHourDetail = (id: number) => {
  return request.get(`/work-hours/${id}`)
}

// 创建工时记录
export const createWorkHour = (data: WorkHourCreateReq) => {
  return request.post('/work-hours', data)
}

// 更新工时记录
export const updateWorkHour = (data: WorkHourUpdateReq) => {
  return request.put('/work-hours', data)
}

// 删除工时记录
export const deleteWorkHour = (id: number) => {
  return request.delete(`/work-hours/${id}`)
}

// 根据用户ID查询工时记录
export const getWorkHoursByUserId = (userId: number) => {
  return request.get(`/work-hours/user/${userId}`)
}

// 根据项目ID查询工时记录
export const getWorkHoursByProjectId = (projectId: number) => {
  return request.get(`/work-hours/project/${projectId}`)
}

// 获取用户工时统计
export const getUserWorkHourStatistics = (userId: number, startDate?: string, endDate?: string) => {
  return request.get(`/statistics/user/${userId}`, {
    params: { startDate, endDate }
  })
}

// 获取项目工时统计
export const getProjectWorkHourStatistics = (projectId: number, startDate?: string, endDate?: string) => {
  return request.get(`/statistics/project/${projectId}`, {
    params: { startDate, endDate }
  })
}

// 获取工时趋势统计
export const getWorkHourTrend = (params: {
  projectId?: number
  userId?: number
  startDate: string
  endDate: string
}) => {
  return request.get('/statistics/trend', { params })
}
