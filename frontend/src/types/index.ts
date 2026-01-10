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

// ==================== 通用错误类型 ====================

/** API 错误响应 */
export interface ApiError {
  code: number
  message: string
  data?: unknown
}

/** 未知错误类型 */
export type UnknownError = Error | { message: string } | string | null

// ==================== 项目类型 ====================

/** 项目信息（基础） */
export interface ProjectInfo {
  id: number
  name: string
  code: string
  description?: string
  status: string
  managerId?: number
  startDate?: string
  endDate?: string
  createUserId?: number
  updateUserId?: number
  createTime?: string
  updateTime?: string
  // 关联信息（非数据库字段）
  managerName?: string
  createUserName?: string
  updateUserName?: string
}

// ==================== 任务类型 ====================

/** 任务信息（基础） */
export interface TaskInfo {
  id: number
  projectId: number
  title: string
  description?: string
  status: string
  priority: string
  type?: string
  assigneeId?: number
  iterationId?: number
  planStartDate?: string
  planEndDate?: string
  actualStartDate?: string
  actualEndDate?: string
  createUserId?: number
  updateUserId?: number
  createTime?: string
  updateTime?: string
  // 关联信息（非数据库字段）
  projectName?: string
  assigneeName?: string
  createUserName?: string
  updateUserName?: string
}

// ==================== 迭代类型 ====================

/** 迭代信息（基础） */
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
  updateUserId?: number
  createTime?: string
  updateTime?: string
  // 关联信息（非数据库字段）
  projectName?: string
  createUserName?: string
  updateUserName?: string
}

// ==================== 工时类型 ====================

/** 工时统计信息 */
export interface WorkHourStatistics {
  totalHours: number
  userId?: number
  startDate?: string
  endDate?: string
}

// ==================== 用户类型 ====================

/** 用户登录信息 */
export interface LoginRequest {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  userInfo: {
    id: number
    username: string
    nickname?: string
  }
}

