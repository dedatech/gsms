// 通用类型定义

// API 响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页请求
export interface PageQuery {
  pageNum?: number
  pageSize?: number
}

// 分页响应
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

// 项目状态
export enum ProjectStatus {
  NOT_STARTED = 1,
  IN_PROGRESS = 2,
  SUSPENDED = 3,
  ARCHIVED = 4,
}

// 任务状态
export enum TaskStatus {
  TODO = 1,
  IN_PROGRESS = 2,
  COMPLETED = 3,
  CANCELLED = 4,
}

// 任务优先级
export enum TaskPriority {
  LOW = 1,
  MEDIUM = 2,
  HIGH = 3,
}

// 迭代状态
export enum IterationStatus {
  NOT_STARTED = 1,
  IN_PROGRESS = 2,
  COMPLETED = 3,
}
