import request from './request'

// 操作日志查询接口
export interface OperationLogQuery {
  username?: string
  module?: string
  operationType?: string
  status?: string
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
}

// 操作日志信息
export interface OperationLogInfo {
  id: number
  userId: number
  username: string
  operationType: string
  module: string
  operationContent: string
  ipAddress: string
  status: string
  errorMessage?: string
  operationTime: string
}

// 分页响应
export interface OperationLogPageResult {
  list: OperationLogInfo[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 分页查询操作日志
 */
export const getOperationLogList = (params: OperationLogQuery) => {
  return request.get<OperationLogPageResult>('/operation-logs', { params })
}

/**
 * 根据ID查询操作日志详情
 */
export const getOperationLogById = (id: number) => {
  return request.get<OperationLogInfo>(`/operation-logs/${id}`)
}
