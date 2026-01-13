import request from './request'

/**
 * 首页看板数据
 */
export interface DashboardData {
  projectCount: number
  projects: Array<{
    id: number
    name: string
    code: string
    status: string
  }>
  pendingTaskCount: number
  pendingTasks: Array<{
    id: number
    title: string
    status: string
    priority: string
    projectId: number
    planEndDate: string
  }>
  todayHours: number
  weekHours: number
  monthHours: number
  totalHours: number
}

/**
 * 项目工时统计数据
 */
export interface ProjectWorkHourStatistics {
  projectId: number
  totalHours: number
  totalRecords: number
  userCount: number
  userHoursDistribution: Record<number, number>
  startDate?: string
  endDate?: string
}

/**
 * 用户工时统计数据
 */
export interface UserWorkHourStatistics {
  userId: number
  totalHours: number
  totalRecords: number
  projectCount: number
  projectHoursDistribution: Record<number, number>
  startDate?: string
  endDate?: string
}

/**
 * 部门工时统计数据
 */
export interface DepartmentWorkHourStatistics {
  departmentId: number
  totalHours: number
  totalRecords: number
  userCount: number
  userHoursDistribution: Record<number, number>
  startDate?: string
  endDate?: string
}

/**
 * 任务工时统计数据
 */
export interface TaskWorkHourStatistics {
  taskId: number
  totalHours: number
  estimateHours: number
  variance: number
  totalRecords: number
}

/**
 * 项目任务完成度统计
 */
export interface ProjectTaskCompletionStatistics {
  projectId: number
  totalTasks: number
  todoTasks: number
  inProgressTasks: number
  doneTasks: number
  completionRate: string
}

/**
 * 工时趋势统计
 */
export interface WorkHourTrendStatistics {
  projectId?: number
  userId?: number
  startDate: string
  endDate: string
  totalHours: number
  trendData: Array<{
    date: string
    hours: number
  }>
}

/**
 * 获取首页看板数据
 */
export function getDashboardData() {
  return request<DashboardData>({
    url: '/statistics/dashboard',
    method: 'get'
  })
}

/**
 * 获取项目工时统计
 */
export function getProjectWorkHourStatistics(
  projectId: number,
  startDate?: string,
  endDate?: string
) {
  return request<ProjectWorkHourStatistics>({
    url: `/statistics/project/${projectId}`,
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 获取用户工时统计
 */
export function getUserWorkHourStatistics(
  userId: number,
  startDate?: string,
  endDate?: string
) {
  return request<UserWorkHourStatistics>({
    url: `/statistics/user/${userId}`,
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 获取部门工时统计
 */
export function getDepartmentWorkHourStatistics(
  departmentId: number,
  startDate?: string,
  endDate?: string
) {
  return request<DepartmentWorkHourStatistics>({
    url: `/statistics/department/${departmentId}`,
    method: 'get',
    params: { startDate, endDate }
  })
}

/**
 * 获取任务工时统计
 */
export function getTaskWorkHourStatistics(taskId: number) {
  return request<TaskWorkHourStatistics>({
    url: `/statistics/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 获取项目任务完成度统计
 */
export function getProjectTaskCompletionStatistics(projectId: number) {
  return request<ProjectTaskCompletionStatistics>({
    url: `/statistics/project/${projectId}/completion`,
    method: 'get'
  })
}

/**
 * 获取工时趋势统计
 */
export function getWorkHourTrendStatistics(params: {
  projectId?: number
  userId?: number
  startDate: string
  endDate: string
}) {
  return request<WorkHourTrendStatistics>({
    url: '/statistics/trend',
    method: 'get',
    params
  })
}
