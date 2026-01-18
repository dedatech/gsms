/**
 * 项目上下文状态管理
 * 管理当前选中的项目、迭代、视图模式等状态
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getProjectList, getProjectDetail } from '@/api/project'
import type { ProjectInfoResp } from '@/api/project'

/**
 * 迭代信息
 */
export interface IterationInfo {
  id: number
  name: string
  status: string
  startDate?: string
  endDate?: string
}

/**
 * 项目状态
 */
export interface ProjectState {
  currentIterationId: number | null
  viewMode: 'overview' | 'kanban' | 'list'
  filters: Record<string, any>
}

export const useProjectStore = defineStore('project', () => {
  // ========== 状态定义 ==========

  /**
   * 当前选中的项目
   */
  const currentProject = ref<ProjectInfoResp | null>(null)

  /**
   * 项目列表（用于切换）
   */
  const projectList = ref<ProjectInfoResp[]>([])

  /**
   * 最近访问的项目ID列表
   */
  const recentProjects = ref<number[]>([])

  /**
   * 当前选中的迭代
   */
  const currentIteration = ref<IterationInfo | null>(null)

  /**
   * 当前视图模式
   */
  const currentViewMode = ref<'overview' | 'kanban' | 'list'>('overview')

  /**
   * 项目状态映射（为每个项目保存独立状态）
   */
  const projectStates = new Map<number, ProjectState>()

  // ========== 计算属性 ==========

  /**
   * 是否为大型项目
   */
  const isLargeScale = computed(() =>
    currentProject.value?.projectType === 'LARGE_SCALE'
  )

  /**
   * 当前项目ID
   */
  const currentProjectId = computed(() =>
    currentProject.value?.id
  )

  /**
   * 最近访问的项目列表（按访问时间倒序）
   */
  const recentProjectList = computed(() => {
    return recentProjects.value
      .map(id => projectList.value.find(p => p.id === id))
      .filter((p): p is ProjectInfoResp => p !== undefined)
  })

  // ========== Actions ==========

  /**
   * 设置当前项目
   */
  const setCurrentProject = async (projectId: number) => {
    try {
      const project = await getProjectDetail(projectId)
      currentProject.value = project
      addToRecent(projectId)

      // 恢复该项目的状态
      const state = getProjectState(projectId)
      currentViewMode.value = state.viewMode

      return project
    } catch (error) {
      console.error('设置项目失败:', error)
      throw error
    }
  }

  /**
   * 添加到最近访问列表
   */
  const addToRecent = (projectId: number) => {
    const idx = recentProjects.value.indexOf(projectId)
    if (idx > -1) {
      recentProjects.value.splice(idx, 1)
    }
    recentProjects.value.unshift(projectId)
    if (recentProjects.value.length > 5) {
      recentProjects.value.pop()
    }
    saveRecentToStorage()
  }

  /**
   * 获取项目列表
   */
  const fetchProjectList = async () => {
    try {
      const res = await getProjectList({
        pageNum: 1,
        pageSize: 100
      })
      projectList.value = res.list || []
      return projectList.value
    } catch (error) {
      console.error('获取项目列表失败:', error)
      throw error
    }
  }

  /**
   * 设置当前迭代
   */
  const setCurrentIteration = (iteration: IterationInfo | null) => {
    currentIteration.value = iteration
    if (currentProjectId.value) {
      const state = getProjectState(currentProjectId.value)
      state.currentIterationId = iteration?.id || null
    }
  }

  /**
   * 设置视图模式
   */
  const setViewMode = (mode: 'overview' | 'kanban' | 'list') => {
    currentViewMode.value = mode
    if (currentProjectId.value) {
      const state = getProjectState(currentProjectId.value)
      state.viewMode = mode
    }
  }

  /**
   * 获取项目的状态
   */
  const getProjectState = (projectId: number): ProjectState => {
    if (!projectStates.has(projectId)) {
      projectStates.set(projectId, {
        currentIterationId: null,
        viewMode: 'overview',
        filters: {}
      })
    }
    return projectStates.get(projectId)!
  }

  /**
   * 设置筛选条件
   */
  const setFilters = (filters: Record<string, any>) => {
    if (currentProjectId.value) {
      const state = getProjectState(currentProjectId.value)
      state.filters = { ...state.filters, ...filters }
    }
  }

  /**
   * 清除筛选条件
   */
  const clearFilters = () => {
    if (currentProjectId.value) {
      const state = getProjectState(currentProjectId.value)
      state.filters = {}
    }
  }

  /**
   * 重置项目状态
   */
  const resetProjectState = () => {
    currentProject.value = null
    currentIteration.value = null
    currentViewMode.value = 'overview'
  }

  /**
   * 从 localStorage 加载最近访问的项目
   */
  const loadRecentFromStorage = () => {
    const stored = localStorage.getItem('recentProjects')
    if (stored) {
      try {
        recentProjects.value = JSON.parse(stored)
      } catch (error) {
        console.error('加载最近项目失败:', error)
        recentProjects.value = []
      }
    }
  }

  /**
   * 保存最近访问的项目到 localStorage
   */
  const saveRecentToStorage = () => {
    localStorage.setItem('recentProjects', JSON.stringify(recentProjects.value))
  }

  // ========== 初始化 ==========

  // 页面加载时恢复最近访问的项目
  loadRecentFromStorage()

  return {
    // 状态
    currentProject,
    projectList,
    recentProjects,
    recentProjectList,
    currentIteration,
    currentViewMode,

    // 计算属性
    isLargeScale,
    currentProjectId,

    // Actions
    setCurrentProject,
    fetchProjectList,
    setCurrentIteration,
    setViewMode,
    setFilters,
    clearFilters,
    resetProjectState,
    loadRecentFromStorage
  }
})
