import request from './request'

// 任务查询参数
export interface TaskQuery {
  projectId?: number
  assigneeId?: number
  status?: number
  pageNum?: number
  pageSize?: number
}

// 获取任务列表
export const getTaskList = (params: TaskQuery) => {
  return request.get('/tasks/search', { params })
}

// 创建任务
export interface TaskCreateReq {
  title: string
  description?: string
  projectId: number
  iterationId?: number
  assigneeId?: number
  priority?: number
  planStartDate?: string
  planEndDate?: string
}

export const createTask = (data: TaskCreateReq) => {
  return request.post('/tasks', data)
}

// 更新任务
export interface TaskUpdateReq {
  id: number
  title?: string
  description?: string
  assigneeId?: number
  priority?: number
  status?: number
  planStartDate?: string
  planEndDate?: string
  actualStartDate?: string
  actualEndDate?: string
}

export const updateTask = (data: TaskUpdateReq) => {
  return request.put('/tasks', data)
}

// 更新任务状态（轻量级接口，用于拖拽和快捷状态变更）
export interface TaskStatusUpdateReq {
  id: number
  status: string
  actualStartDate?: string
  actualEndDate?: string
}

export const updateTaskStatus = (data: TaskStatusUpdateReq) => {
  return request.put('/tasks/status', data)
}

// 删除任务
export const deleteTask = (id: number) => {
  return request.delete(`/tasks/${id}`)
}

// 获取任务详情
export const getTaskDetail = (id: number) => {
  return request.get(`/tasks/${id}`)
}
