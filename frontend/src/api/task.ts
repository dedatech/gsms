import request from './request'

// 任务信息
export interface TaskInfo {
  id: number
  title: string
  description?: string
  projectId: number
  projectName?: string
  iterationId?: number
  iterationName?: string
  parentId?: number
  assigneeId?: number
  assigneeName?: string
  type?: string  // 任务类型：TASK, REQUIREMENT, BUG
  status: string
  priority: string
  planStartDate?: string
  planEndDate?: string
  actualStartDate?: string
  actualEndDate?: string
  estimateHours?: number
  createTime?: string
  updateTime?: string
  subtasks?: TaskInfo[]
  // 缺陷特有字段
  severity?: string  // 严重程度：TRIVIAL, MINOR, MAJOR, CRITICAL, BLOCKER
  reproductionSteps?: string  // 复现步骤
  attachments?: string[]  // 附件列表
}

// 任务查询参数
export interface TaskQuery {
  projectId?: number
  assigneeId?: number
  iterationId?: number
  status?: number
  pageNum?: number
  pageSize?: number
}

// 获取任务列表
export const getTaskList = (params: TaskQuery) => {
  return request.get('/tasks/search', { params })
}

// 根据项目ID获取任务列表（返回扁平列表）
export const getTasksByProjectId = (projectId: number, pageNum?: number, pageSize?: number) => {
  return request.get('/tasks/search', {
    params: {
      projectId,
      pageNum: pageNum || 1,
      pageSize: pageSize || 10
    }
  })
}

// 创建任务
export interface TaskCreateReq {
  title: string
  description?: string
  projectId: number
  iterationId?: number
  parentId?: number
  assigneeId?: number
  type?: string  // 任务类型：TASK, REQUIREMENT, BUG
  priority?: string  // 优先级：LOW, MEDIUM, HIGH
  status?: string  // 状态：TODO, IN_PROGRESS, DONE
  planStartDate?: string
  planEndDate?: string
  // 缺陷特有字段
  severity?: string  // 严重程度
  reproductionSteps?: string  // 复现步骤
  estimateHours?: number  // 预估工时
}

export const createTask = (data: TaskCreateReq) => {
  return request.post('/tasks', data)
}

// 更新任务
export interface TaskUpdateReq {
  id: number
  title?: string
  description?: string
  projectId?: number
  iterationId?: number
  parentId?: number
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

// 更新任务迭代ID（轻量级接口，用于拖拽和移动，支持设置为null）
export const updateTaskIterationId = (taskId: number, iterationId?: number) => {
  const params: Record<string, any> = {}
  if (iterationId !== undefined) {
    params.iterationId = iterationId
  }
  return request.put(`/tasks/${taskId}/iteration`, null, { params })
}

// 删除任务
export const deleteTask = (id: number) => {
  return request.delete(`/tasks/${id}`)
}

// 获取任务详情
export const getTaskDetail = (id: number) => {
  return request.get(`/tasks/${id}`)
}

// 获取子任务列表
export const getSubtasks = (parentId: number) => {
  return request.get(`/tasks/${parentId}/subtasks`)
}

// 需求统计信息
export interface RequirementStatsResp {
  id: number
  title: string
  type: string
  priority: string
  assigneeId?: number
  assigneeName?: string
  estimateHours?: number
  subtaskCount: number
  completedSubtasks: number
  todoSubtasks: number
  inProgressSubtasks: number
  testingSubtasks: number
  reopenedSubtasks: number
}

// 看板表格数据
export interface KanbanTableData {
  rows: {
    requirement: RequirementStatsResp
    todoTasks: TaskInfo[]
    inProgressTasks: TaskInfo[]
    testingTasks: TaskInfo[]
    doneTasks: TaskInfo[]
    reopenedTasks: TaskInfo[]
    closedTasks: TaskInfo[]
  }[]
  totalTodoTasks: number
  totalInProgressTasks: number
  totalTestingTasks: number
  totalDoneTasks: number
  totalReopenedTasks: number
  totalClosedTasks: number
}

// 获取看板表格数据
export const getKanbanTableData = (params: {
  projectId: number
  iterationId?: number
  assigneeId?: number
  priority?: string
}) => {
  return request.get<KanbanTableData>('/tasks/kanban-table', { params })
}
