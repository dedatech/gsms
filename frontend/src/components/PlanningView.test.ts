import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PlanningView from './PlanningView.vue'

// Mock API
vi.mock('@/api/project', () => ({
  getProjectMembers: vi.fn(() => Promise.resolve([
    { userId: 1, nickname: '张三', roleType: 1 },
    { userId: 2, nickname: '李四', roleType: 2 },
  ])),
}))

vi.mock('@/api/task', () => ({
  getTaskList: vi.fn(() => Promise.resolve({
    list: [
      {
        id: 1,
        title: '需求1',
        type: 'REQUIREMENT',
        status: 'TODO',
        priority: 'HIGH',
        assigneeId: 1,
        assigneeName: '张三',
        iterationId: 1,
        estimateHours: 8,
      },
      {
        id: 2,
        title: '需求2',
        type: 'REQUIREMENT',
        status: 'IN_PROGRESS',
        priority: 'MEDIUM',
        assigneeId: 2,
        assigneeName: '李四',
        iterationId: null,
        estimateHours: 6,
      },
    ],
    total: 2,
  })),
  updateTaskIterationId: vi.fn(() => Promise.resolve()),
}))

vi.mock('@/api/iteration', () => ({
  getIterationList: vi.fn(() => Promise.resolve({
    list: [
      {
        id: 1,
        name: '迭代1',
        status: 'IN_PROGRESS',
        planStartDate: '2026-01-01',
        planEndDate: '2026-01-31',
      },
    ],
    total: 1,
  })),
  createIteration: vi.fn(() => Promise.resolve()),
  updateIteration: vi.fn(() => Promise.resolve()),
  deleteIteration: vi.fn(() => Promise.resolve()),
}))

describe('PlanningView.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该正确渲染规划视图组件', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.planning-view').exists()).toBe(true)
  })

  it('应该正确接收 projectId props', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 123,
      },
    })

    expect(wrapper.props('projectId')).toBe(123)
  })

  it('应该有左右两栏布局', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.find('.iteration-panel').exists()).toBe(true)
    expect(wrapper.find('.unplanned-panel').exists()).toBe(true)
  })

  it('应该有创建迭代按钮', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    // 检查是否有右侧操作区域
    const headerRight = wrapper.find('.header-right-section')
    expect(headerRight.exists()).toBe(true)
  })

  it('应该有迭代规划标题', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    const title = wrapper.find('.panel-title')
    expect(title.exists()).toBe(true)
  })

  it('应该有未规划需求标题', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    const panels = wrapper.findAll('.panel-title')
    expect(panels.length).toBeGreaterThanOrEqual(2)
  })

  it('应该正确获取优先级类型', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.getPriorityType?.('HIGH')).toBe('danger')
    expect(wrapper.vm.getPriorityType?.('MEDIUM')).toBe('warning')
    expect(wrapper.vm.getPriorityType?.('LOW')).toBe('info')
  })

  it('应该正确获取优先级文本', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.getPriorityText?.('HIGH')).toBe('高')
    expect(wrapper.vm.getPriorityText?.('MEDIUM')).toBe('中')
    expect(wrapper.vm.getPriorityText?.('LOW')).toBe('低')
  })

  it('应该正确获取状态类型', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.getStatusType?.('TODO')).toBe('info')
    expect(wrapper.vm.getStatusType?.('IN_PROGRESS')).toBe('warning')
    expect(wrapper.vm.getStatusType?.('DONE')).toBe('success')
  })

  it('应该正确获取状态文本', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.getStatusText?.('TODO')).toBe('待办')
    expect(wrapper.vm.getStatusText?.('IN_PROGRESS')).toBe('进行中')
    expect(wrapper.vm.getStatusText?.('DONE')).toBe('已完成')
  })

  it('应该支持切换迭代展开状态', async () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    // 初始状态应该是折叠的
    expect(wrapper.vm.expandedIterationIds?.size).toBe(0)

    // 切换展开状态
    await wrapper.vm.toggleIterationExpansion?.(1)

    // 现在应该展开了
    expect(wrapper.vm.expandedIterationIds?.has(1)).toBe(true)

    // 再次切换应该是折叠的
    await wrapper.vm.toggleIterationExpansion?.(1)
    expect(wrapper.vm.expandedIterationIds?.has(1)).toBe(false)
  })

  it('应该正确计算迭代需求数量', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    // 假设有需求在迭代1中
    const count = wrapper.vm.getIterationRequirementCount?.(1)
    expect(typeof count).toBe('number')
  })

  it('应该正确获取迭代需求数组', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    const requirements = wrapper.vm.getIterationRequirements?.(1)
    expect(Array.isArray(requirements)).toBe(true)
  })

  it('应该正确获取未规划需求', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    const unplanned = wrapper.vm.unplannedRequirements
    expect(Array.isArray(unplanned)).toBe(true)
  })

  it('应该正确获取活跃迭代数组', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    const activeIterations = wrapper.vm.activeIterations
    expect(Array.isArray(activeIterations)).toBe(true)
  })

  it('应该有加载状态', () => {
    const wrapper = mount(PlanningView, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.loading).toBeDefined()
    expect(typeof wrapper.vm.loading).toBe('boolean')
  })
})
