import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ProjectOverview from './ProjectOverview.vue'

// Mock API
vi.mock('@/api/project', () => ({
  getProjectDetail: vi.fn(() => Promise.resolve({
    id: 1,
    name: '测试项目',
    code: 'TEST-001',
    description: '这是一个测试项目',
    status: 'IN_PROGRESS',
    createTime: '2026-01-01',
  })),
  getProjectMembers: vi.fn(() => Promise.resolve([
    { userId: 1, nickname: '张三', roleType: 1 },
    { userId: 2, nickname: '李四', roleType: 2 },
  ])),
}))

vi.mock('@/api/task', () => ({
  getTasksByProjectId: vi.fn(() => Promise.resolve({
    list: [
      { id: 1, title: '任务1', status: 'DONE', estimateHours: 8, planEndDate: '2026-12-31' },
      { id: 2, title: '任务2', status: 'TODO', estimateHours: 4, planEndDate: '2026-12-31' },
      { id: 3, title: '任务3', status: 'IN_PROGRESS', estimateHours: 6, planEndDate: '2026-12-31' },
    ],
    total: 3,
  })),
}))

describe('ProjectOverview.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该正确渲染项目概览组件', () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.project-overview').exists()).toBe(true)
  })

  it('应该在加载时显示骨架屏', async () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    // 等待组件挂载
    await wrapper.vm.$nextTick()

    // 检查是否显示骨架屏（初始状态）
    expect(wrapper.find('.loading-container').exists()).toBe(true)
  })

  it('应该正确接收 projectId props', () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 123,
      },
    })

    expect(wrapper.props('projectId')).toBe(123)
  })

  it('应该计算关键指标', async () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    // 等待数据加载完成
    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    // 检查 metrics 是否被正确计算
    const metrics = wrapper.vm.metrics
    expect(metrics.totalTasks).toBe(3)
    expect(metrics.completedTasks).toBe(1)
    expect(metrics.todoTasks).toBe(1)
    expect(metrics.inProgressTasks).toBe(1)
  })

  it('应该计算完成率', async () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    const metrics = wrapper.vm.metrics
    const expectedRate = Math.round((1 / 3) * 100)
    expect(metrics.completionRate).toBe(expectedRate)
  })

  it('应该正确获取项目经理名称', async () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.vm.managerName).toBe('张三')
  })

  it('应该正确获取项目状态类型', () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.getStatusType('IN_PROGRESS')).toBe('primary')
    expect(wrapper.vm.getStatusType('NOT_STARTED')).toBe('info')
    expect(wrapper.vm.getStatusType('SUSPENDED')).toBe('warning')
    expect(wrapper.vm.getStatusType('ARCHIVED')).toBe('success')
  })

  it('应该正确获取项目状态文本', () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.getStatusText('IN_PROGRESS')).toBe('进行中')
    expect(wrapper.vm.getStatusText('NOT_STARTED')).toBe('未开始')
    expect(wrapper.vm.getStatusText('SUSPENDED')).toBe('已暂停')
    expect(wrapper.vm.getStatusText('ARCHIVED')).toBe('已归档')
  })

  it('应该正确获取角色文本', () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.getRoleText(1)).toBe('管理员')
    expect(wrapper.vm.getRoleText(2)).toBe('成员')
    expect(wrapper.vm.getRoleText(3)).toBe('访客')
  })

  it('应该正确格式化日期', () => {
    const wrapper = mount(ProjectOverview, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.formatDate('2026-01-15')).toBe('2026-01-15')
    expect(wrapper.vm.formatDate('')).toBe('-')
    expect(wrapper.vm.formatDate(undefined)).toBe('-')
  })
})
