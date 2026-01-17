/**
 * 任务分组相关类型定义
 */

import type { TaskInfo } from '@/api/task'
import type { IterationInfo } from '@/api/iteration'

/**
 * 任务分组类型
 */
export type TaskGroupType = 'iteration' | 'parent_task' | 'orphan'

/**
 * 任务分组接口
 */
export interface TaskGroup {
  id: string // 'iter-1' | 'parent-2' | 'orphan'
  type: TaskGroupType
  name: string
  dateRange?: string // 迭代的时间范围
  status?: string
  taskCount: number
  completedCount: number
  progress: number // 0-100
  tasks: TaskInfo[]
  subGroups?: TaskGroup[] // 用于父任务的嵌套
  expanded: boolean // 是否展开
}

/**
 * 判断迭代是否应该默认展开
 * 展开原则：
 * 1. 当且仅有一个迭代时，默认展开
 * 2. 按迭代的时间范围判断：当前时间在迭代时间范围内则展开
 * 3. 如果当前时间在多个迭代中，按迭代的创建时间排序（只展开最新的）
 *
 * @param iteration 当前迭代
 * @param allIterations 所有迭代列表
 * @returns 是否展开
 */
export function shouldExpandIteration(
  iteration: IterationInfo,
  allIterations: IterationInfo[]
): boolean {
  // 规则1: 当且仅有一个迭代时，默认展开
  if (allIterations.length === 1) {
    return true
  }

  const now = new Date()
  const iterStartDate = iteration.planStartDate ? new Date(iteration.planStartDate) : null
  const iterEndDate = iteration.planEndDate ? new Date(iteration.planEndDate) : null

  // 规则2: 判断当前时间是否在迭代时间范围内
  const isInTimeRange =
    (!iterStartDate || now >= iterStartDate) &&
    (!iterEndDate || now <= iterEndDate)

  if (!isInTimeRange) {
    return false
  }

  // 规则3: 如果当前时间在多个迭代中，只展开最新的（按创建时间）
  const activeIterations = allIterations.filter(iter => {
    const startDate = iter.planStartDate ? new Date(iter.planStartDate) : null
    const endDate = iter.planEndDate ? new Date(iter.planEndDate) : null
    return (!startDate || now >= startDate) && (!endDate || now <= endDate)
  })

  // 按创建时间排序，取最新的
  activeIterations.sort((a, b) => {
    const timeA = a.createTime ? new Date(a.createTime).getTime() : 0
    const timeB = b.createTime ? new Date(b.createTime).getTime() : 0
    return timeB - timeA // 降序，最新的在前
  })

  // 只展开最新的活跃迭代
  return activeIterations.length > 0 && activeIterations[0].id === iteration.id
}

/**
 * 将任务列表按迭代分组
 * @param tasks 任务列表
 * @param iterations 迭代列表
 * @returns 分组后的任务数组
 */
export function groupTasksByIteration(
  tasks: TaskInfo[],
  iterations: IterationInfo[]
): TaskGroup[] {
  const groups: TaskGroup[] = []

  // 1. 按迭代分组
  iterations.forEach(iter => {
    const iterTasks = tasks.filter(t => t.iterationId === iter.id)
    const completedCount = iterTasks.filter(t => t.status === 'DONE').length

    // 区分父任务和孤立任务
    const parentTasks = iterTasks.filter(t => hasSubtasks(t.id, tasks))
    const orphanTasks = iterTasks.filter(t => !hasSubtasks(t.id, tasks))

    // 构建父任务子分组
    const subGroups = parentTasks.map(parent => buildParentTaskGroup(parent, tasks))

    groups.push({
      id: `iter-${iter.id}`,
      type: 'iteration',
      name: iter.name,
      dateRange: `${iter.planStartDate || ''} ~ ${iter.planEndDate || ''}`,
      status: iter.status,
      taskCount: iterTasks.length,
      completedCount,
      progress: iterTasks.length > 0 ? Math.round((completedCount / iterTasks.length) * 100) : 0,
      tasks: orphanTasks,
      subGroups,
      expanded: shouldExpandIteration(iter, iterations) // 使用智能展开判断
    })
  })

  // 2. 未分配迭代任务（只在无迭代时显示）
  const orphanTasks = tasks.filter(t => !t.iterationId)
  if (orphanTasks.length > 0 && iterations.length === 0) {
    groups.push({
      id: 'orphan',
      type: 'orphan',
      name: '未分配迭代',
      taskCount: orphanTasks.length,
      completedCount: orphanTasks.filter(t => t.status === 'DONE').length,
      progress: 0,
      tasks: orphanTasks,
      expanded: false
    })
  }

  return groups
}

/**
 * 构建父任务分组
 */
function buildParentTaskGroup(parentTask: TaskInfo, allTasks: TaskInfo[]): TaskGroup {
  const subtasks = allTasks.filter(t => t.parentId === parentTask.id)
  const completedCount = subtasks.filter(t => t.status === 'DONE').length

  return {
    id: `parent-${parentTask.id}`,
    type: 'parent_task',
    name: parentTask.title,
    status: parentTask.status,
    taskCount: subtasks.length,
    completedCount,
    progress: subtasks.length > 0 ? Math.round((completedCount / subtasks.length) * 100) : 0,
    tasks: subtasks,
    expanded: parentTask.status !== 'DONE' // 未完成默认展开
  }
}

/**
 * 检查任务是否有子任务
 */
function hasSubtasks(taskId: number, allTasks: TaskInfo[]): boolean {
  return allTasks.some(t => t.parentId === taskId)
}
