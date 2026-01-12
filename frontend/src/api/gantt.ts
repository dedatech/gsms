import request from './request'

// 甘特图任务节点
export interface GanttTask {
  id: number           // 业务ID（原始数据库ID）
  ganttId: number      // 甘特图节点ID（用于dhtmlx-gantt渲染）
  text: string
  type: string
  startDate?: string
  endDate?: string
  duration?: number
  progress?: number
  parent?: number | null
  owner?: string
  ownerId?: number
  ownerAvatar?: string
  status?: string
  priority?: string
  color?: string
  actualStartDate?: string
  actualEndDate?: string
  critical?: boolean
  slack?: number
  subtasks?: GanttTask[]
}

// 甘特图任务依赖关系
export interface GanttLink {
  id: number
  source: number
  target: number
  type: string
  lag?: number
}

// 甘特图数据响应
export interface GanttDataResp {
  data: GanttTask[]
  links: GanttLink[]
}

// 任务时间更新请求
export interface TaskDateUpdateReq {
  planStartDate: string
  planEndDate: string
}

// 任务层级更新请求
export interface TaskParentUpdateReq {
  parentId: number | null
}

// 任务依赖关系创建请求
export interface TaskLinkCreateReq {
  source: number
  target: number
  type?: number
  lag?: number
}

/**
 * 获取项目甘特图数据
 * @param projectId 项目ID
 * @param startDate 开始日期（可选）
 * @param endDate 结束日期（可选）
 */
export const getProjectGanttData = (
  projectId: number,
  startDate?: string,
  endDate?: string
) => {
  return request.get<GanttDataResp>(`/gantt/project/${projectId}`, {
    params: { startDate, endDate }
  })
}

/**
 * 更新任务时间（拖拽后）
 * @param taskId 任务ID
 * @param data 任务时间更新请求
 */
export const updateTaskDates = (taskId: number, data: TaskDateUpdateReq) => {
  return request.put(`/gantt/task/${taskId}/dates`, data)
}

/**
 * 更新任务层级（拖拽改变父任务）
 * @param taskId 任务ID
 * @param data 任务层级更新请求
 */
export const updateTaskParent = (taskId: number, data: TaskParentUpdateReq) => {
  return request.put(`/gantt/task/${taskId}/parent`, data)
}

/**
 * 创建任务依赖关系
 * @param data 任务依赖关系创建请求
 */
export const createTaskLink = (data: TaskLinkCreateReq) => {
  return request.post('/gantt/link', data)
}

/**
 * 删除任务依赖关系
 * @param linkId 依赖关系ID
 */
export const deleteTaskLink = (linkId: number) => {
  return request.delete(`/gantt/link/${linkId}`)
}
