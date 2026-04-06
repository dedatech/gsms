import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import WorkHourStats from './WorkHourStats.vue'

// Mock API
vi.mock('@/api/project', () => ({
  getProjectMembers: vi.fn(() => Promise.resolve([
    { userId: 1, nickname: '张三', roleType: 1 },
    { userId: 2, nickname: '李四', roleType: 2 },
  ])),
}))

vi.mock('@/api/task', () => ({
  getWorkHourList: vi.fn(() => Promise.resolve({
    list: [
      {
        id: 1,
        userId: 1,
        userName: '张三',
        taskId: 1,
        taskName: '任务1',
        workDate: '2026-01-01',
        hours: 8,
        content: '完成需求分析',
      },
      {
        id: 2,
        userId: 2,
        userName: '李四',
        taskId: 2,
        taskName: '任务2',
        workDate: '2026-01-01',
        hours: 6,
        content: '完成前端开发',
      },
      {
        id: 3,
        userId: 1,
        userName: '张三',
        taskId: 1,
        taskName: '任务1',
        workDate: '2026-01-02',
        hours: 7,
        content: '完成系统设计',
      },
    ],
    total: 3,
  })),
}))

describe('WorkHourStats.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该正确渲染工时统计组件', () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.workhour-stats').exists()).toBe(true)
  })

  it('应该正确接收 projectId props', () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 123,
      },
    })

    expect(wrapper.props('projectId')).toBe(123)
  })

  it('应该计算总工时', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.vm.summary.totalHours).toBe(21) // 8 + 6 + 7
  })

  it('应该计算活跃成员数', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.vm.summary.activeMembers).toBe(2) // 张三和李四
  })

  it('应该计算工作日数', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.vm.summary.workDays).toBe(2) // 2026-01-01 和 2026-01-02
  })

  it('应该计算人均工时', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    const avgHours = Math.round(21 / 2) // 21总工时 / 2成员
    expect(wrapper.vm.summary.avgHours).toBe(avgHours)
  })

  it('应该正确计算成员贡献度排行', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    const ranking = wrapper.vm.memberRanking
    expect(ranking.length).toBe(2)
    expect(ranking[0].userId).toBe(1) // 张三应该排第一
    expect(ranking[0].totalHours).toBe(15) // 8 + 7
    expect(ranking[0].taskCount).toBe(1) // 只有一个任务
    expect(ranking[0].workDays).toBe(2) // 工作了2天
  })

  it('应该正确计算每日趋势', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    const trend = wrapper.vm.dailyTrend
    expect(trend.length).toBe(2)
    expect(trend[0].date).toBe('2026-01-01')
    expect(trend[0].hours).toBe(14) // 8 + 6
    expect(trend[1].date).toBe('2026-01-02')
    expect(trend[1].hours).toBe(7)
  })

  it('应该正确格式化趋势日期', () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    expect(wrapper.vm.formatTrendDate('2026-01-15')).toBe('1/15')
    expect(wrapper.vm.formatTrendDate('2026-12-31')).toBe('12/31')
  })

  it('应该正确计算最大工时', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.vm.maxHours).toBe(15) // 张三的15小时
  })

  it('应该正确计算最大每日工时', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    await wrapper.vm.$nextTick()
    await new Promise(resolve => setTimeout(resolve, 100))

    expect(wrapper.vm.maxDailyHours).toBe(14) // 1月1日的14小时
  })

  it('筛选器变更时应该重置页码', async () => {
    const wrapper = mount(WorkHourStats, {
      props: {
        projectId: 1,
      },
    })

    wrapper.vm.pageNum = 5

    await wrapper.vm.handleFilterChange()

    expect(wrapper.vm.pageNum).toBe(1)
  })
})
