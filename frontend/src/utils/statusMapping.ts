/**
 * 状态映射工具类
 * 统一管理项目中所有状态的映射逻辑（类型、文本、颜色等）
 */

// ==================== 类型定义 ====================

/** 状态映射信息 */
export interface StatusMapping {
  /** Element Plus 标签类型 */
  type: '' | 'success' | 'warning' | 'info' | 'primary' | 'danger'
  /** 显示文本 */
  text: string
  /** 主题色（可选） */
  color?: string
}

/** 状态映射配置类型 */
type StatusMapConfig = Record<string, StatusMapping>

// ==================== 项目状态映射 ====================

/**
 * 项目状态映射配置
 */
export const PROJECT_STATUS_MAP: StatusMapConfig = {
  NOT_STARTED: {
    type: 'info',
    text: '未开始',
    color: '#d9d9d9'
  },
  IN_PROGRESS: {
    type: 'primary',
    text: '进行中',
    color: '#1890ff'
  },
  SUSPENDED: {
    type: 'warning',
    text: '已暂停',
    color: '#faad14'
  },
  ARCHIVED: {
    type: '',
    text: '已归档',
    color: '#8c8c8c'
  }
}

/**
 * 获取项目状态映射信息
 * @param status - 状态值（如 'IN_PROGRESS'）
 * @returns 状态映射信息，找不到则返回默认值
 */
export const getProjectStatusInfo = (status: string): StatusMapping => {
  return PROJECT_STATUS_MAP[status] || { type: 'info', text: '未知', color: '#d9d9d9' }
}

/**
 * 获取项目状态列表（用于下拉选择等）
 * @returns 项目状态选项数组
 */
export const getProjectStatusOptions = () => {
  return Object.entries(PROJECT_STATUS_MAP).map(([value, info]) => ({
    value,
    label: info.text,
    color: info.color
  }))
}

// ==================== 任务状态映射 ====================

/**
 * 任务状态映射配置
 */
export const TASK_STATUS_MAP: StatusMapConfig = {
  TODO: {
    type: 'info',
    text: '待办',
    color: '#d9d9d9'
  },
  IN_PROGRESS: {
    type: 'primary',
    text: '进行中',
    color: '#1890ff'
  },
  COMPLETED: {
    type: 'success',
    text: '已完成',
    color: '#52c41a'
  },
  CANCELLED: {
    type: 'danger',
    text: '已取消',
    color: '#ff4d4f'
  }
}

/**
 * 获取任务状态映射信息
 * @param status - 状态值（如 'IN_PROGRESS'）
 * @returns 状态映射信息
 */
export const getTaskStatusInfo = (status: string): StatusMapping => {
  return TASK_STATUS_MAP[status] || { type: 'info', text: '未知', color: '#d9d9d9' }
}

/**
 * 获取任务状态列表
 * @returns 任务状态选项数组
 */
export const getTaskStatusOptions = () => {
  return Object.entries(TASK_STATUS_MAP).map(([value, info]) => ({
    value,
    label: info.text,
    color: info.color
  }))
}

// ==================== 任务优先级映射 ====================

/**
 * 任务优先级映射配置
 */
export const TASK_PRIORITY_MAP: StatusMapConfig = {
  LOW: {
    type: 'info',
    text: '低',
    color: '#13c2c2'
  },
  MEDIUM: {
    type: '',
    text: '中',
    color: '#1890ff'
  },
  HIGH: {
    type: 'warning',
    text: '高',
    color: '#faad14'
  }
}

/**
 * 获取任务优先级映射信息
 * @param priority - 优先级值（如 'HIGH'）
 * @returns 优先级映射信息
 */
export const getTaskPriorityInfo = (priority: string): StatusMapping => {
  return TASK_PRIORITY_MAP[priority] || { type: 'info', text: '未知', color: '#d9d9d9' }
}

/**
 * 获取任务优先级列表
 * @returns 优先级选项数组
 */
export const getTaskPriorityOptions = () => {
  return Object.entries(TASK_PRIORITY_MAP).map(([value, info]) => ({
    value,
    label: info.text,
    color: info.color
  }))
}

// ==================== 迭代状态映射 ====================

/**
 * 迭代状态映射配置
 */
export const ITERATION_STATUS_MAP: StatusMapConfig = {
  NOT_STARTED: {
    type: 'info',
    text: '未开始',
    color: '#d9d9d9'
  },
  IN_PROGRESS: {
    type: 'primary',
    text: '进行中',
    color: '#1890ff'
  },
  COMPLETED: {
    type: 'success',
    text: '已完成',
    color: '#52c41a'
  }
}

/**
 * 获取迭代状态映射信息
 * @param status - 状态值（如 'IN_PROGRESS'）
 * @returns 状态映射信息
 */
export const getIterationStatusInfo = (status: string): StatusMapping => {
  return ITERATION_STATUS_MAP[status] || { type: 'info', text: '未知', color: '#d9d9d9' }
}

/**
 * 获取迭代状态列表
 * @returns 迭代状态选项数组
 */
export const getIterationStatusOptions = () => {
  return Object.entries(ITERATION_STATUS_MAP).map(([value, info]) => ({
    value,
    label: info.text,
    color: info.color
  }))
}

// ==================== 工时状态映射 ====================

/**
 * 工时状态映射配置
 */
export const WORK_HOUR_STATUS_MAP: StatusMapConfig = {
  SAVED: {
    type: 'info',
    text: '已保存',
    color: '#d9d9d9'
  },
  SUBMITTED: {
    type: 'warning',
    text: '已提交',
    color: '#faad14'
  },
  CONFIRMED: {
    type: 'success',
    text: '已确认',
    color: '#52c41a'
  }
}

/**
 * 获取工时状态映射信息
 * @param status - 状态值（如 'SUBMITTED'）
 * @returns 状态映射信息
 */
export const getWorkHourStatusInfo = (status: string): StatusMapping => {
  return WORK_HOUR_STATUS_MAP[status] || { type: 'info', text: '未知', color: '#d9d9d9' }
}

// ==================== 通用辅助函数 ====================

/**
 * 获取状态类型（用于 el-tag 的 type 属性）
 * @param status - 状态值
 * @param statusMap - 状态映射配置
 * @returns Element Plus 标签类型
 */
export const getStatusType = (
  status: string,
  statusMap: StatusMapConfig
): StatusMapping['type'] => {
  return statusMap[status]?.type ?? 'info'
}

/**
 * 获取状态文本
 * @param status - 状态值
 * @param statusMap - 状态映射配置
 * @returns 状态显示文本
 */
export const getStatusText = (
  status: string,
  statusMap: StatusMapConfig
): string => {
  return statusMap[status]?.text ?? '未知'
}

/**
 * 获取状态颜色
 * @param status - 状态值
 * @param statusMap - 状态映射配置
 * @returns 状态主题色
 */
export const getStatusColor = (
  status: string,
  statusMap: StatusMapConfig
): string => {
  return statusMap[status]?.color ?? '#d9d9d9'
}
