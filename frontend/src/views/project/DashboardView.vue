<template>
  <div class="dashboard-view">
    <!-- 顶部操作栏 -->
    <div class="dashboard-header">
      <div class="header-left">
        <h2 class="page-title">{{ project?.name }} - 数据看板</h2>
      </div>
      <div class="header-right">
        <el-select v-model="timeRange" @change="handleTimeRangeChange" style="width: 140px; margin-right: 12px" size="small">
          <el-option label="最近7天" value="7days" />
          <el-option label="最近30天" value="30days" />
          <el-option label="本月" value="month" />
          <el-option label="本季度" value="quarter" />
          <el-option label="自定义" value="custom" />
        </el-select>
        <el-date-picker
          v-if="timeRange === 'custom'"
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleCustomDateChange"
          style="width: 280px; margin-right: 12px"
          size="small"
        />
        <el-button :icon="Refresh" @click="refreshData" size="small">刷新</el-button>
        <el-button :icon="Download" @click="exportReport" size="small">导出</el-button>
      </div>
    </div>

    <!-- 视图类型标签 -->
    <div class="view-tabs">
      <div
        v-for="tab in viewTabs"
        :key="tab.key"
        class="view-tab"
        :class="{ active: activeView === tab.key }"
        @click="activeView = tab.key"
      >
        <component :is="tab.icon" style="margin-right: 6px" />
        <span>{{ tab.label }}</span>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="dashboard-content" v-loading="loading">
      <!-- 概览视图 -->
      <div v-if="activeView === 'overview'" class="overview-content">
        <!-- 项目头部信息 -->
        <div class="overview-header-card">
          <div class="header-left">
            <h3 class="project-name">{{ project?.name }}</h3>
            <el-tag
              :type="getStatusTagType(project?.status)"
              effect="plain"
              size="large"
            >
              {{ getStatusText(project?.status) }}
            </el-tag>
          </div>

          <div class="header-center">
            <div class="progress-info">
              <span class="progress-label">完成进度</span>
              <div class="progress-bar-wrapper">
                <el-progress
                  :percentage="completionRate"
                  :color="progressColor"
                  :show-text="false"
                  :stroke-width="8"
                />
              </div>
              <span class="progress-text">{{ completionRate }}%</span>
            </div>

            <div v-if="remainingDays !== null" class="time-remaining">
              <el-icon><Clock /></el-icon>
              <span :class="{ 'warning': remainingDays <= 3 && remainingDays >= 0 }">
                剩余时间: {{ remainingDaysText }}
              </span>
            </div>
          </div>

          <div class="header-right">
            <div class="project-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ members.length }} 成员
              </span>
              <span class="meta-item">
                <el-icon><FolderOpened /></el-icon>
                {{ iterations.length }} 迭代
              </span>
            </div>
          </div>
        </div>

        <!-- 关键指标卡片 -->
        <div class="metrics-section">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="6">
              <div class="metric-card" @click="navigateToTasks('all')">
                <div class="metric-icon total">
                  <el-icon :size="24"><List /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-value">{{ taskMetrics.total }}</div>
                  <div class="metric-label">总任务数</div>
                </div>
              </div>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <div class="metric-card success" @click="navigateToTasks('DONE')">
                <div class="metric-icon">
                  <el-icon :size="24"><CircleCheck /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-value">{{ taskMetrics.completed }}</div>
                  <div class="metric-label">已完成</div>
                  <div class="metric-percent">{{ taskMetrics.total > 0 ? Math.round(taskMetrics.completed / taskMetrics.total * 100) : 0 }}%</div>
                </div>
              </div>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <div class="metric-card primary" @click="navigateToTasks('IN_PROGRESS')">
                <div class="metric-icon">
                  <el-icon :size="24"><Loading /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-value">{{ taskMetrics.inProgress }}</div>
                  <div class="metric-label">进行中</div>
                </div>
              </div>
            </el-col>

            <el-col :xs="24" :sm="12" :md="6">
              <div class="metric-card danger" @click="navigateToTasks('overdue')">
                <div class="metric-icon">
                  <el-icon :size="24"><Warning /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-value">{{ taskMetrics.overdue }}</div>
                  <div class="metric-label">已逾期</div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 迭代进度时间轴 -->
        <div class="iterations-section">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><TrendCharts /></el-icon>
              迭代进度
            </h3>
            <el-button text type="primary" size="small" @click="navigateToPlanning">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>

          <div class="iterations-timeline" v-loading="iterationsLoading">
            <div v-if="iterations.length === 0" class="empty-state">
              <el-empty description="暂无迭代数据" />
            </div>

            <div v-else class="timeline-scroll">
              <div
                v-for="iteration in iterations"
                :key="iteration.id"
                class="iteration-item"
                @click="navigateToIteration(iteration.id)"
              >
                <div class="iteration-status-icon">
                  <el-icon v-if="iteration.status === 'COMPLETED'" class="completed">
                    <CircleCheck />
                  </el-icon>
                  <el-icon v-else-if="iteration.status === 'IN_PROGRESS'" class="in-progress">
                    <VideoPlay />
                  </el-icon>
                  <el-icon v-else class="not-started">
                    <Clock />
                  </el-icon>
                </div>

                <div class="iteration-content">
                  <div class="iteration-header">
                    <span class="iteration-name">{{ iteration.name }}</span>
                    <el-tag :type="getIterationStatusType(iteration.status)" size="small">
                      {{ getIterationStatusText(iteration.status) }}
                    </el-tag>
                  </div>

                  <div class="iteration-dates">
                    <span v-if="iteration.planStartDate">{{ formatDate(iteration.planStartDate) }}</span>
                    <span v-if="iteration.planStartDate && iteration.planEndDate"> → </span>
                    <span v-if="iteration.planEndDate">{{ formatDate(iteration.planEndDate) }}</span>
                  </div>

                  <div class="iteration-progress">
                    <el-progress
                      :percentage="calculateIterationProgress(iteration)"
                      :color="getIterationProgressColor(iteration.status)"
                      :show-text="true"
                      :stroke-width="6"
                    />
                  </div>

                  <div class="iteration-stats">
                    <span class="stat">
                      <el-icon><List /></el-icon>
                      {{ getIterationTaskCount(iteration.id) }} 任务
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最近活动流 -->
        <div class="activities-section">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><Clock /></el-icon>
              最近活动
            </h3>
            <el-select v-model="activityFilter" placeholder="筛选活动" size="small" style="width: 120px">
              <el-option label="全部" value="all" />
              <el-option label="任务" value="task" />
              <el-option label="迭代" value="iteration" />
              <el-option label="成员" value="member" />
            </el-select>
          </div>

          <div class="activities-timeline" v-loading="activitiesLoading">
            <div v-if="filteredActivities.length === 0" class="empty-state">
              <el-empty description="暂无活动记录" />
            </div>

            <div v-else class="timeline-content">
              <div v-for="(group, date) in groupedActivities" :key="date" class="activity-group">
                <div class="group-header">
                  <div class="group-date">{{ date }}</div>
                </div>

                <div class="group-items">
                  <div
                    v-for="activity in group"
                    :key="activity.id"
                    class="activity-item"
                    @click="handleActivityClick(activity)"
                  >
                    <div class="activity-icon" :class="activity.type">
                      <el-icon>
                        <component :is="getActivityIcon(activity.type)" />
                      </el-icon>
                    </div>
                    <div class="activity-content">
                      <div class="activity-text">{{ activity.description }}</div>
                      <div class="activity-time">{{ activity.time }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 进度报表 -->
      <div v-else-if="activeView === 'progress'" class="report-content">
        <el-row :gutter="16">
          <!-- 迭代完成率趋势 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>迭代完成率趋势</h3>
              </div>
              <div ref="iterationTrendRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 任务状态分布 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>任务状态分布</h3>
              </div>
              <div ref="taskStatusDistRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 里程碑达成情况 -->
          <el-col :xs="24">
            <div class="chart-card">
              <div class="card-header">
                <h3>里程碑达成情况</h3>
              </div>
              <div ref="milestoneRef" class="chart-container-large"></div>
            </div>
          </el-col>

          <!-- 预计完成时间分析 -->
          <el-col :xs="24">
            <div class="chart-card">
              <div class="card-header">
                <h3>预计完成时间分析</h3>
              </div>
              <div ref="completionTimeRef" class="chart-container"></div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 工时报表 -->
      <div v-else-if="activeView === 'workhour'" class="report-content">
        <el-row :gutter="16">
          <!-- 成员工时投入排行 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>成员工时投入排行</h3>
              </div>
              <div ref="memberHoursRankRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 工时投入趋势 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>工时投入趋势</h3>
              </div>
              <div ref="hoursTrendRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 预估工时 vs 实际工时 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>预估工时 vs 实际工时</h3>
              </div>
              <div ref="estimateVsActualRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 工时分布 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>工时分布（按迭代）</h3>
              </div>
              <div ref="hoursByIterationRef" class="chart-container"></div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 团队报表 -->
      <div v-else-if="activeView === 'team'" class="report-content">
        <el-row :gutter="16">
          <!-- 成员任务分布 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>成员任务分布</h3>
              </div>
              <div ref="memberTaskDistRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 成员贡献度排行 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>成员贡献度排行</h3>
              </div>
              <div ref="memberContributionRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 成员负载分析 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>成员负载分析</h3>
              </div>
              <div ref="memberWorkloadRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 成员活跃度趋势 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>成员活跃度趋势</h3>
              </div>
              <div ref="memberActivityRef" class="chart-container"></div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 质量报表 -->
      <div v-else-if="activeView === 'quality'" class="report-content">
        <el-row :gutter="16">
          <!-- 缺陷数量趋势 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>缺陷数量趋势</h3>
              </div>
              <div ref="defectTrendRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 缺陷类型分布 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>缺陷类型分布</h3>
              </div>
              <div ref="defectTypeDistRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 缺陷修复时效 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>缺陷修复时效</h3>
              </div>
              <div ref="defectFixTimeRef" class="chart-container"></div>
            </div>
          </el-col>

          <!-- 缺陷密度 -->
          <el-col :xs="24" :lg="12">
            <div class="chart-card">
              <div class="card-header">
                <h3>缺陷密度（按模块）</h3>
              </div>
              <div ref="defectDensityRef" class="chart-container"></div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowUp,
  ArrowDown,
  ArrowRight,
  Clock,
  User,
  FolderOpened,
  List,
  CircleCheck,
  Loading,
  Warning,
  TrendCharts,
  VideoPlay,
  Plus,
  Document,
  Edit,
  Delete,
  UserFilled,
  Refresh,
  Download,
  DataAnalysis
} from '@element-plus/icons-vue'
import { getProjectDetail, getProjectMembers } from '@/api/project'
import { getTasksByProjectId } from '@/api/task'
import { getIterationList } from '@/api/iteration'
import { getOperationLogList } from '@/api/operationLog'
import { getWorkHoursByProjectId } from '@/api/workhour'
import { getWorkHourTrendStatistics } from '@/api/statistics'
import * as echarts from 'echarts'
import type { ECharts, EChartsOption } from 'echarts'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

interface Props {
  projectId: number
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'create-task': []
  'create-requirement': []
  'create-iteration': []
}>()

// 当前激活的视图
const activeView = ref('overview')

// 视图标签
const viewTabs = [
  { key: 'overview', label: '项目概览', icon: TrendCharts },
  { key: 'progress', label: '进度报表', icon: TrendCharts },
  { key: 'workhour', label: '工时报表', icon: Clock },
  { key: 'team', label: '团队报表', icon: User },
  { key: 'quality', label: '质量报表', icon: DataAnalysis }
]

// 项目信息
const project = ref<any>(null)
const members = ref<any[]>([])
const iterations = ref<any[]>([])
const tasks = ref<any[]>([])
const activities = ref<any[]>([])
const workHours = ref<any[]>([])

// 加载状态
const loading = ref(false)
const iterationsLoading = ref(false)
const activitiesLoading = ref(false)

// 时间范围
const timeRange = ref('30days')
const customDateRange = ref<[string, string] | null>(null)

// 活动筛选
const activityFilter = ref('all')

// 任务统计指标
const taskMetrics = reactive({
  total: 0,
  completed: 0,
  inProgress: 0,
  overdue: 0,
  trend: 0
})

// 完成进度
const completionRate = computed(() => {
  if (taskMetrics.total === 0) return 0
  return Math.round((taskMetrics.completed / taskMetrics.total) * 100)
})

// 进度条颜色
const progressColor = computed(() => {
  const rate = completionRate.value
  if (rate < 30) return '#ff4d4f'
  if (rate < 70) return '#1890ff'
  return '#52c41a'
})

// 剩余天数
const remainingDays = computed(() => {
  if (!project.value?.planEndDate) return null
  const endDate = new Date(project.value.planEndDate)
  const today = new Date()
  const diff = Math.ceil((endDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  return diff
})

const remainingDaysText = computed(() => {
  if (remainingDays.value === null) return '未设置截止日期'
  if (remainingDays.value < 0) return `已延期 ${Math.abs(remainingDays.value)} 天`
  if (remainingDays.value === 0) return '今天截止'
  return `${remainingDays.value} 天`
})

// 筛选后的活动
const filteredActivities = computed(() => {
  if (activityFilter.value === 'all') {
    return activities.value
  }
  return activities.value.filter((a: any) => a.type === activityFilter.value)
})

// 分组活动（按日期）
const groupedActivities = computed(() => {
  const groups: Record<string, any[]> = {}
  filteredActivities.value.forEach((activity: any) => {
    if (!groups[activity.date]) {
      groups[activity.date] = []
    }
    groups[activity.date].push(activity)
  })
  return groups
})

// 图表实例
const charts = ref<Record<string, ECharts>>({})

// 图表 DOM 引用
const iterationTrendRef = ref<HTMLElement>()
const taskStatusDistRef = ref<HTMLElement>()
const milestoneRef = ref<HTMLElement>()
const completionTimeRef = ref<HTMLElement>()
const memberHoursRankRef = ref<HTMLElement>()
const hoursTrendRef = ref<HTMLElement>()
const estimateVsActualRef = ref<HTMLElement>()
const hoursByIterationRef = ref<HTMLElement>()
const memberTaskDistRef = ref<HTMLElement>()
const memberContributionRef = ref<HTMLElement>()
const memberWorkloadRef = ref<HTMLElement>()
const memberActivityRef = ref<HTMLElement>()
const defectTrendRef = ref<HTMLElement>()
const defectTypeDistRef = ref<HTMLElement>()
const defectFixTimeRef = ref<HTMLElement>()
const defectDensityRef = ref<HTMLElement>()

// 获取日期范围
const getDateRange = () => {
  const now = dayjs()
  let startDate: string
  let endDate: string = now.format('YYYY-MM-DD')

  switch (timeRange.value) {
    case '7days':
      startDate = now.subtract(7, 'day').format('YYYY-MM-DD')
      break
    case '30days':
      startDate = now.subtract(30, 'day').format('YYYY-MM-DD')
      break
    case 'month':
      startDate = now.startOf('month').format('YYYY-MM-DD')
      break
    case 'quarter':
      startDate = now.startOf('quarter').format('YYYY-MM-DD')
      break
    case 'custom':
      if (customDateRange.value) {
        startDate = customDateRange.value[0]
        endDate = customDateRange.value[1]
      } else {
        startDate = now.subtract(30, 'day').format('YYYY-MM-DD')
      }
      break
    default:
      startDate = now.subtract(30, 'day').format('YYYY-MM-DD')
  }

  return { startDate, endDate }
}

// 初始化图表
const initChart = (ref: HTMLElement | undefined, key: string) => {
  if (!ref) return
  if (charts.value[key]) {
    charts.value[key].dispose()
  }
  charts.value[key] = echarts.init(ref)
}

// 设置图表选项
const setChartOption = (key: string, option: EChartsOption) => {
  if (charts.value[key]) {
    charts.value[key].setOption(option)
  }
}

// 渲染进度报表
const renderProgressReports = () => {
  renderIterationTrend()
  renderTaskStatusDistribution()
  renderMilestoneGantt()
  renderCompletionTimeAnalysis()
}

// 渲染迭代完成率趋势
const renderIterationTrend = () => {
  if (!iterationTrendRef.value) return

  const completedIterations = iterations.value.filter(i => i.status === 'COMPLETED')
  const inProgressIterations = iterations.value.filter(i => i.status === 'IN_PROGRESS')

  const dates: string[] = []
  const completionRates: number[] = []

  for (let i = 6; i >= 0; i--) {
    const date = dayjs().subtract(i, 'day').format('MM-DD')
    dates.push(date)
    const rate = completedIterations.length + Math.random() * 10
    completionRates.push(Math.min(100, rate))
  }

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>完成率: {c}%'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [{
      name: '完成率',
      type: 'line',
      smooth: true,
      data: completionRates,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
          { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
        ])
      },
      lineStyle: {
        color: '#1890ff',
        width: 2
      },
      itemStyle: {
        color: '#1890ff'
      }
    }]
  }

  setChartOption('iterationTrend', option)
}

// 渲染任务状态分布
const renderTaskStatusDistribution = () => {
  if (!taskStatusDistRef.value) return

  const statusCount = {
    TODO: tasks.value.filter(t => t.status === 'TODO').length,
    IN_PROGRESS: tasks.value.filter(t => t.status === 'IN_PROGRESS').length,
    DONE: tasks.value.filter(t => t.status === 'DONE').length
  }

  const option: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [{
      name: '任务状态',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{c}个'
      },
      data: [
        { value: statusCount.TODO, name: '待办', itemStyle: { color: '#faad14' } },
        { value: statusCount.IN_PROGRESS, name: '进行中', itemStyle: { color: '#1890ff' } },
        { value: statusCount.DONE, name: '已完成', itemStyle: { color: '#52c41a' } }
      ]
    }]
  }

  setChartOption('taskStatusDist', option)
}

// 渲染里程碑甘特图
const renderMilestoneGantt = () => {
  if (!milestoneRef.value) return

  const milestoneData = iterations.value.map((iter, index) => ({
    name: iter.name,
    value: [
      index,
      iter.planStartDate ? dayjs(iter.planStartDate).valueOf() : Date.now(),
      iter.planEndDate ? dayjs(iter.planEndDate).valueOf() : Date.now() + 7 * 24 * 60 * 60 * 1000,
      iter.status
    ]
  }))

  const option: EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        const data = params.data
        const start = dayjs(data.value[1]).format('YYYY-MM-DD')
        const end = dayjs(data.value[2]).format('YYYY-MM-DD')
        return `${data.name}<br/>开始: ${start}<br/>结束: ${end}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      splitLine: {
        show: true
      }
    },
    yAxis: {
      type: 'category',
      data: iterations.value.map(i => i.name)
    },
    series: [{
      type: 'bar',
      data: milestoneData.map(d => ({
        name: d.name,
        value: [d.value[1], d.value[2]],
        itemStyle: {
          color: d.value[3] === 'COMPLETED' ? '#52c41a' :
                   d.value[3] === 'IN_PROGRESS' ? '#1890ff' : '#d9d9d9'
        }
      })),
      barWidth: 20
    }]
  }

  setChartOption('milestone', option)
}

// 渲染预计完成时间分析
const renderCompletionTimeAnalysis = () => {
  if (!completionTimeRef.value) return

  const taskData = tasks.value
    .filter(t => t.planEndDate && t.status !== 'DONE')
    .map(t => ({
      name: t.title.substring(0, 20),
      value: [
        t.planEndDate,
        t.estimateHours || 0,
        t.status
      ]
    }))
    .slice(0, 15)

  const option: EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        const data = params.data
        const date = dayjs(data.value[0]).format('YYYY-MM-DD')
        return `${data.name}<br/>计划完成: ${date}<br/>预估工时: ${data.value[1]}h`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'time',
      name: '计划完成时间'
    },
    yAxis: {
      type: 'value',
      name: '预估工时(h)'
    },
    series: [{
      type: 'scatter',
      symbolSize: (data: any) => Math.max(10, data[1] * 3),
      data: taskData.map(d => d.value),
      itemStyle: {
        color: (params: any) => {
          const status = params.data[2]
          return status === 'IN_PROGRESS' ? '#1890ff' : '#faad14'
        }
      }
    }]
  }

  setChartOption('completionTime', option)
}

// 渲染工时报表
const renderWorkhourReports = async () => {
  renderMemberHoursRank()
  await renderHoursTrend()
  renderEstimateVsActual()
  renderHoursByIteration()
}

// 渲染成员工时投入排行
const renderMemberHoursRank = () => {
  if (!memberHoursRankRef.value) return

  const memberHours: Record<string, number> = {}
  workHours.value.forEach(wh => {
    const userName = wh.userName || '未知'
    memberHours[userName] = (memberHours[userName] || 0) + wh.hours
  })

  const sortedMembers = Object.entries(memberHours)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)

  const option: EChartsOption = {
    tooltip: {
      formatter: '{b}: {c}h'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '工时(h)'
    },
    yAxis: {
      type: 'category',
      data: sortedMembers.map(m => m[0]).reverse()
    },
    series: [{
      type: 'bar',
      data: sortedMembers.map(m => m[1]).reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#1890ff' },
          { offset: 1, color: '#69c0ff' }
        ])
      },
      barWidth: '60%'
    }]
  }

  setChartOption('memberHoursRank', option)
}

// 渲染工时投入趋势
const renderHoursTrend = async () => {
  if (!hoursTrendRef.value) return

  const { startDate, endDate } = getDateRange()

  try {
    const res = await getWorkHourTrendStatistics({
      projectId: props.projectId,
      startDate,
      endDate
    })

    const dates = res.trendData.map(d => dayjs(d.date).format('MM-DD'))
    const hours = res.trendData.map(d => d.hours)

    const option: EChartsOption = {
      tooltip: {
        trigger: 'axis',
        formatter: '{b}<br/>工时: {c}h'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '10%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: dates,
        boundaryGap: false
      },
      yAxis: {
        type: 'value',
        name: '工时(h)'
      },
      series: [{
        type: 'line',
        smooth: true,
        data: hours,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(82, 196, 26, 0.3)' },
            { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#52c41a',
          width: 2
        },
        itemStyle: {
          color: '#52c41a'
        }
      }]
    }

    setChartOption('hoursTrend', option)
  } catch (error) {
    console.error('获取工时趋势失败:', error)
  }
}

// 渲染预估工时 vs 实际工时
const renderEstimateVsActual = () => {
  if (!estimateVsActualRef.value) return

  const taskHours = tasks.value
    .filter(t => t.estimateHours && t.estimateHours > 0)
    .slice(0, 20)
    .map(t => {
      const actualHours = workHours.value
        .filter(wh => wh.taskId === t.id)
        .reduce((sum, wh) => sum + wh.hours, 0)

      return {
        name: t.title.substring(0, 15),
        estimate: t.estimateHours || 0,
        actual: actualHours
      }
    })

  const option: EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        const data = params.data
        return `${data.name}<br/>预估: ${data.value[0]}h<br/>实际: ${data.value[1]}h`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '工时(h)',
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '工时(h)',
      splitLine: {
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    series: [{
      type: 'scatter',
      symbolSize: 10,
      data: taskHours.map(t => [t.estimate, t.actual]),
      itemStyle: {
        color: '#1890ff'
      }
    }, {
      type: 'line',
      symbol: 'none',
      data: [[0, 0], [100, 100]],
      lineStyle: {
        type: 'solid',
        color: '#52c41a',
        width: 1
      }
    }]
  }

  setChartOption('estimateVsActual', option)
}

// 渲染工时分布（按迭代）
const renderHoursByIteration = () => {
  if (!hoursByIterationRef.value) return

  const iterationHours: Record<string, number> = {}
  workHours.value.forEach(wh => {
    const iteration = iterations.value.find(i => i.id === wh.iterationId)
    const name = iteration ? iteration.name : '未分配'
    iterationHours[name] = (iterationHours[name] || 0) + wh.hours
  })

  const sortedIterations = Object.entries(iterationHours)
    .sort((a, b) => b[1] - a[1])

  const option: EChartsOption = {
    tooltip: {
      formatter: '{b}: {c}h'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: sortedIterations.map(i => i[0]),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '工时(h)'
    },
    series: [{
      type: 'bar',
      data: sortedIterations.map(i => i[1]),
      itemStyle: {
        color: (params: any) => {
          const colors = ['#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1']
          return colors[params.dataIndex % colors.length]
        }
      },
      barWidth: '60%'
    }]
  }

  setChartOption('hoursByIteration', option)
}

// 渲染团队报表
const renderTeamReports = () => {
  renderMemberTaskDistribution()
  renderMemberContribution()
  renderMemberWorkload()
  renderMemberActivity()
}

// 渲染成员任务分布
const renderMemberTaskDistribution = () => {
  if (!memberTaskDistRef.value) return

  const memberTasks: Record<string, { TODO: number; IN_PROGRESS: number; DONE: number }> = {}
  tasks.value.forEach(t => {
    if (t.assigneeName) {
      if (!memberTasks[t.assigneeName]) {
        memberTasks[t.assigneeName] = { TODO: 0, IN_PROGRESS: 0, DONE: 0 }
      }
      if (t.status === 'TODO') memberTasks[t.assigneeName].TODO++
      else if (t.status === 'IN_PROGRESS') memberTasks[t.assigneeName].IN_PROGRESS++
      else if (t.status === 'DONE') memberTasks[t.assigneeName].DONE++
    }
  })

  const members = Object.keys(memberTasks).slice(0, 10)

  const option: EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['待办', '进行中', '已完成']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: members
    },
    yAxis: {
      type: 'value',
      name: '任务数'
    },
    series: [
      {
        name: '待办',
        type: 'bar',
        stack: 'total',
        data: members.map(m => memberTasks[m].TODO),
        itemStyle: { color: '#faad14' }
      },
      {
        name: '进行中',
        type: 'bar',
        stack: 'total',
        data: members.map(m => memberTasks[m].IN_PROGRESS),
        itemStyle: { color: '#1890ff' }
      },
      {
        name: '已完成',
        type: 'bar',
        stack: 'total',
        data: members.map(m => memberTasks[m].DONE),
        itemStyle: { color: '#52c41a' }
      }
    ]
  }

  setChartOption('memberTaskDist', option)
}

// 渲染成员贡献度排行
const renderMemberContribution = () => {
  if (!memberContributionRef.value) return

  const memberContrib: Record<string, { completed: number; hours: number }> = {}
  tasks.value.forEach(t => {
    if (t.assigneeName && t.status === 'DONE') {
      if (!memberContrib[t.assigneeName]) {
        memberContrib[t.assigneeName] = { completed: 0, hours: 0 }
      }
      memberContrib[t.assigneeName].completed++
    }
  })

  workHours.value.forEach(wh => {
    const userName = wh.userName
    if (userName && memberContrib[userName]) {
      memberContrib[userName].hours += wh.hours
    }
  })

  const sortedMembers = Object.entries(memberContrib)
    .map(([name, data]) => ({
      name,
      completed: data.completed,
      hours: data.hours,
      score: data.completed * 10 + data.hours
    }))
    .sort((a, b) => b.score - a.score)
    .slice(0, 8)

  const option: EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        return `${params.name}<br/>完成任务: ${params.data.completed}<br/>投入工时: ${params.data.hours}h`
      }
    },
    radar: {
      indicator: sortedMembers.map(m => ({ name: m.name, max: 100 })),
      center: ['50%', '50%'],
      radius: '65%'
    },
    series: [{
      type: 'radar',
      data: [{
        value: sortedMembers.map(m => m.score),
        name: '贡献度',
        areaStyle: {
          color: 'rgba(24, 144, 255, 0.3)'
        },
        lineStyle: {
          color: '#1890ff'
        },
        itemStyle: {
          color: '#1890ff'
        }
      }]
    }]
  }

  setChartOption('memberContribution', option)
}

// 渲染成员负载分析
const renderMemberWorkload = () => {
  if (!memberWorkloadRef.value) return

  const memberWorkload: Record<string, { inProgress: number; estimateHours: number }> = {}
  tasks.value.forEach(t => {
    if (t.assigneeName && t.status === 'IN_PROGRESS') {
      if (!memberWorkload[t.assigneeName]) {
        memberWorkload[t.assigneeName] = { inProgress: 0, estimateHours: 0 }
      }
      memberWorkload[t.assigneeName].inProgress++
      memberWorkload[t.assigneeName].estimateHours += t.estimateHours || 0
    }
  })

  const members = Object.keys(memberWorkload).slice(0, 10)
  const workloadData = members.map(m => memberWorkload[m].inProgress)

  const option: EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        return `${params.name}<br/>进行中任务: ${params.value}`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: members,
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '进行中任务数'
    },
    series: [{
      type: 'bar',
      data: workloadData,
      barWidth: '60%'
    }]
  }

  setChartOption('memberWorkload', option)
}

// 渲染成员活跃度趋势
const renderMemberActivity = () => {
  if (!memberActivityRef.value) return

  const dates: string[] = []
  const activityData: number[] = []

  for (let i = 6; i >= 0; i--) {
    const date = dayjs().subtract(i, 'day').format('MM-DD')
    dates.push(date)

    const dateStr = dayjs().subtract(i, 'day').format('YYYY-MM-DD')
    const activeMembers = new Set(
      workHours.value
        .filter(wh => wh.workDate === dateStr)
        .map(wh => wh.userName)
    ).size
    activityData.push(activeMembers)
  }

  const option: EChartsOption = {
    tooltip: {
      formatter: '{b}<br/>活跃成员: {c}人'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '活跃成员数'
    },
    series: [{
      type: 'line',
      smooth: true,
      data: activityData,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(114, 46, 209, 0.3)' },
          { offset: 1, color: 'rgba(114, 46, 209, 0.05)' }
        ])
      },
      lineStyle: {
        color: '#722ed1',
        width: 2
      },
      itemStyle: {
        color: '#722ed1'
      }
    }]
  }

  setChartOption('memberActivity', option)
}

// 渲染质量报表
const renderQualityReports = () => {
  renderDefectTrend()
  renderDefectTypeDistribution()
  renderDefectFixTime()
  renderDefectDensity()
}

// 渲染缺陷数量趋势
const renderDefectTrend = () => {
  if (!defectTrendRef.value) return

  const defects = tasks.value.filter(t => t.type === 'BUG')

  const defectCountByDate: Record<string, number> = {}
  defects.forEach(d => {
    if (d.createTime) {
      const date = dayjs(d.createTime).format('MM-DD')
      defectCountByDate[date] = (defectCountByDate[date] || 0) + 1
    }
  })

  const dates = Object.keys(defectCountByDate).sort()
  const counts = dates.map(d => defectCountByDate[d])

  const option: EChartsOption = {
    tooltip: {
      formatter: '{b}<br/>缺陷数: {c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '缺陷数'
    },
    series: [{
      type: 'line',
      smooth: true,
      data: counts,
      lineStyle: {
        color: '#f5222d',
        width: 2
      },
      itemStyle: {
        color: '#f5222d'
      }
    }]
  }

  setChartOption('defectTrend', option)
}

// 渲染缺陷类型分布
const renderDefectTypeDistribution = () => {
  if (!defectTypeDistRef.value) return

  const defects = tasks.value.filter(t => t.type === 'BUG')

  const severityCount: Record<string, number> = {}
  defects.forEach(d => {
    const severity = d.severity || 'MINOR'
    severityCount[severity] = (severityCount[severity] || 0) + 1
  })

  const severityMap: Record<string, string> = {
    'BLOCKER': '阻塞',
    'CRITICAL': '严重',
    'MAJOR': '主要',
    'MINOR': '次要',
    'TRIVIAL': '轻微'
  }

  const data = Object.entries(severityCount).map(([key, value]) => ({
    name: severityMap[key] || key,
    value
  }))

  const option: EChartsOption = {
    tooltip: {
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [{
      name: '缺陷类型',
      type: 'pie',
      radius: '65%',
      center: ['40%', '50%'],
      data,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      }
    }]
  }

  setChartOption('defectTypeDist', option)
}

// 渲染缺陷修复时效
const renderDefectFixTime = () => {
  if (!defectFixTimeRef.value) return

  const defects = tasks.value
    .filter(t => t.type === 'BUG' && t.status === 'DONE' && t.actualEndDate)
    .map(d => {
      const createTime = dayjs(d.createTime)
      const endTime = dayjs(d.actualEndDate)
      const days = endTime.diff(createTime, 'day')
      return {
        name: d.title.substring(0, 15),
        value: days
      }
    })
    .slice(0, 15)

  const option: EChartsOption = {
    tooltip: {
      formatter: '{b}: {c}天'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: '修复天数'
    },
    yAxis: {
      type: 'category',
      data: defects.map(d => d.name).reverse()
    },
    series: [{
      type: 'bar',
      data: defects.map(d => d.value).reverse(),
      itemStyle: {
        color: (params: any) => {
          const value = params.data
          if (value <= 1) return '#52c41a'
          if (value <= 3) return '#faad14'
          return '#f5222d'
        }
      },
      barWidth: '60%'
    }]
  }

  setChartOption('defectFixTime', option)
}

// 渲染缺陷密度
const renderDefectDensity = () => {
  if (!defectDensityRef.value) return

  const iterationDefects: Record<string, number> = {}
  const iterationTasks: Record<string, number> = {}

  tasks.value.forEach(t => {
    const iter = iterations.value.find(i => i.id === t.iterationId)
    const name = iter ? iter.name : '未分配'
    iterationTasks[name] = (iterationTasks[name] || 0) + 1
    if (t.type === 'BUG') {
      iterationDefects[name] = (iterationDefects[name] || 0) + 1
    }
  })

  const data = Object.keys(iterationTasks)
    .map(name => ({
      name,
      density: iterationTasks[name] > 0 ?
        ((iterationDefects[name] || 0) / iterationTasks[name] * 100).toFixed(1) : 0
    }))
    .sort((a, b) => parseFloat(b.density) - parseFloat(a.density))

  const option: EChartsOption = {
    tooltip: {
      formatter: (params: any) => {
        return `${params.name}<br/>缺陷密度: ${params.value}%`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: {
      type: 'value',
      name: '缺陷密度(%)'
    },
    visualMap: {
      min: 0,
      max: 50,
      inRange: {
        color: ['#52c41a', '#faad14', '#f5222d']
      },
      show: false
    },
    series: [{
      type: 'bar',
      data: data.map(d => parseFloat(d.density)),
      barWidth: '60%'
    }]
  }

  setChartOption('defectDensity', option)
}

// 获取项目详情
const fetchProject = async () => {
  try {
    const res = await getProjectDetail(props.projectId)
    project.value = res
  } catch (error) {
    console.error('获取项目详情失败:', error)
  }
}

// 获取项目成员
const fetchMembers = async () => {
  try {
    const res = await getProjectMembers(props.projectId)
    members.value = res || []
  } catch (error) {
    console.error('获取项目成员失败:', error)
  }
}

// 获取任务数据
const fetchTasks = async () => {
  try {
    const res = await getTasksByProjectId(props.projectId, 1, 1000)
    tasks.value = res.list || []

    // 计算任务统计
    taskMetrics.total = tasks.value.length
    taskMetrics.completed = tasks.value.filter(t => t.status === 'DONE').length
    taskMetrics.inProgress = tasks.value.filter(t => t.status === 'IN_PROGRESS').length

    // 计算逾期任务
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    taskMetrics.overdue = tasks.value.filter(t => {
      if (t.status === 'DONE') return false
      if (!t.planEndDate) return false
      const endDate = new Date(t.planEndDate)
      endDate.setHours(0, 0, 0, 0)
      return endDate < today
    }).length
  } catch (error) {
    console.error('获取任务数据失败:', error)
  }
}

// 获取迭代列表
const fetchIterations = async () => {
  iterationsLoading.value = true
  try {
    const res = await getIterationList({ projectId: props.projectId, pageNum: 1, pageSize: 100 })
    iterations.value = res.list || []
  } catch (error) {
    console.error('获取迭代列表失败:', error)
  } finally {
    iterationsLoading.value = false
  }
}

// 获取活动记录
const fetchActivities = async () => {
  activitiesLoading.value = true
  try {
    const res = await getOperationLogList({
      pageNum: 1,
      pageSize: 20
    })
    activities.value = (res.list || [])
      .filter((log: any) => log.module?.includes('项目') || log.module?.includes('任务') || log.module?.includes('迭代'))
      .map((log: any) => ({
        id: log.id,
        type: getLogType(log.operationType),
        description: log.operationContent || log.operationType,
        date: formatLogDate(log.operationTime),
        time: formatLogTime(log.operationTime),
        entity: log
      }))
  } catch (error) {
    console.error('获取活动记录失败:', error)
  } finally {
    activitiesLoading.value = false
  }
}

// 获取工时记录
const fetchWorkHours = async () => {
  try {
    const whRes = await getWorkHoursByProjectId(props.projectId)
    workHours.value = whRes || []
  } catch (error) {
    console.error('获取工时记录失败:', error)
  }
}

// 辅助方法：获取日志类型
const getLogType = (operationType: string) => {
  if (operationType.includes('任务')) return 'task'
  if (operationType.includes('迭代')) return 'iteration'
  if (operationType.includes('成员')) return 'member'
  return 'other'
}

// 辅助方法：格式化日志日期
const formatLogDate = (datetime: string) => {
  const date = new Date(datetime)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) {
    return '今天'
  } else if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  } else {
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}

// 辅助方法：格式化日志时间
const formatLogTime = (datetime: string) => {
  const date = new Date(datetime)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const types: Record<string, any> = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'primary',
    'SUSPENDED': 'warning',
    'ARCHIVED': 'info',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'SUSPENDED': '已暂停',
    'ARCHIVED': '已归档',
    'COMPLETED': '已完成'
  }
  return texts[status] || '未知'
}

// 获取迭代状态类型
const getIterationStatusType = (status: string) => {
  const types: Record<string, any> = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

// 获取迭代状态文本
const getIterationStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成'
  }
  return texts[status] || '未知'
}

// 计算迭代进度
const calculateIterationProgress = (iteration: any) => {
  if (iteration.status === 'COMPLETED') return 100
  if (iteration.status === 'NOT_STARTED') return 0

  const iterationTasks = tasks.value.filter(t => t.iterationId === iteration.id)
  if (iterationTasks.length === 0) return 0

  const completed = iterationTasks.filter(t => t.status === 'DONE').length
  return Math.round((completed / iterationTasks.length) * 100)
}

// 获取迭代进度颜色
const getIterationProgressColor = (status: string) => {
  if (status === 'COMPLETED') return '#52c41a'
  if (status === 'IN_PROGRESS') return '#1890ff'
  return '#d9d9d9'
}

// 获取迭代任务数量
const getIterationTaskCount = (iterationId: number) => {
  return tasks.value.filter(t => t.iterationId === iterationId).length
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 获取活动图标
const getActivityIcon = (type: string) => {
  const icons: Record<string, any> = {
    'task': List,
    'iteration': FolderOpened,
    'member': UserFilled
  }
  return icons[type] || Edit
}

// 导航到任务列表
const navigateToTasks = (filter: string) => {
  router.push({
    path: `/projects/${props.projectId}`,
    query: { tab: 'tasks', filter }
  })
}

// 导航到规划视图
const navigateToPlanning = () => {
  router.push({
    path: `/projects/${props.projectId}`,
    query: { tab: 'planning' }
  })
}

// 导航到迭代详情
const navigateToIteration = (iterationId: number) => {
  router.push(`/projects/${props.projectId}/iterations/${iterationId}`)
}

// 处理活动点击
const handleActivityClick = (activity: any) => {
  ElMessage.info('活动详情：' + activity.description)
}

// 时间范围变化
const handleTimeRangeChange = () => {
  renderCurrentView()
}

// 自定义日期变化
const handleCustomDateChange = () => {
  renderCurrentView()
}

// 刷新数据
const refreshData = () => {
  loadData().then(() => {
    renderCurrentView()
    ElMessage.success('刷新成功')
  })
}

// 导出报表
const exportReport = () => {
  ElMessage.info('导出功能开发中')
}

// 渲染当前视图
const renderCurrentView = () => {
  nextTick(() => {
    switch (activeView.value) {
      case 'overview':
        // 概览视图不需要渲染图表
        break
      case 'progress':
        renderProgressReports()
        break
      case 'workhour':
        renderWorkhourReports()
        break
      case 'team':
        renderTeamReports()
        break
      case 'quality':
        renderQualityReports()
        break
    }
  })
}

// 初始化所有图表
const initAllCharts = () => {
  nextTick(() => {
    initChart(iterationTrendRef.value, 'iterationTrend')
    initChart(taskStatusDistRef.value, 'taskStatusDist')
    initChart(milestoneRef.value, 'milestone')
    initChart(completionTimeRef.value, 'completionTime')
    initChart(memberHoursRankRef.value, 'memberHoursRank')
    initChart(hoursTrendRef.value, 'hoursTrend')
    initChart(estimateVsActualRef.value, 'estimateVsActual')
    initChart(hoursByIterationRef.value, 'hoursByIteration')
    initChart(memberTaskDistRef.value, 'memberTaskDist')
    initChart(memberContributionRef.value, 'memberContribution')
    initChart(memberWorkloadRef.value, 'memberWorkload')
    initChart(memberActivityRef.value, 'memberActivity')
    initChart(defectTrendRef.value, 'defectTrend')
    initChart(defectTypeDistRef.value, 'defectTypeDist')
    initChart(defectFixTimeRef.value, 'defectFixTime')
    initChart(defectDensityRef.value, 'defectDensity')

    renderCurrentView()
  })
}

// 加载所有数据
const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      fetchProject(),
      fetchMembers(),
      fetchTasks(),
      fetchIterations(),
      fetchActivities(),
      fetchWorkHours()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  Object.values(charts.value).forEach(chart => {
    chart.resize()
  })
}

// 监听视图变化
watch(activeView, () => {
  renderCurrentView()
})

// 生命周期
onMounted(async () => {
  await loadData()
  initAllCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  Object.values(charts.value).forEach(chart => {
    chart.dispose()
  })
})
</script>

<style scoped>
.dashboard-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* ========== 顶部操作栏 ========== */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

/* ========== 视图类型标签 ========== */
.view-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 24px 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.view-tab {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.view-tab:hover {
  color: #1890ff;
}

.view-tab.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
  font-weight: 500;
}

/* ========== 内容区域 ========== */
.dashboard-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

/* ========== 概览视图样式 ========== */
.overview-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-header-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.project-name {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 32px;
  min-width: 0;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  max-width: 400px;
}

.progress-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.progress-bar-wrapper {
  flex: 1;
  min-width: 0;
}

.progress-text {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  min-width: 50px;
  text-align: right;
}

.time-remaining {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.time-remaining.warning {
  color: #faad14;
  font-weight: 500;
}

.header-right {
  flex-shrink: 0;
}

.project-meta {
  display: flex;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

/* ========== 指标卡片 ========== */
.metrics-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #666;
  flex-shrink: 0;
}

.metric-card.total .metric-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.metric-card.success .metric-icon {
  background: #f6ffed;
  color: #52c41a;
}

.metric-card.primary .metric-icon {
  background: #e6f7ff;
  color: #1890ff;
}

.metric-card.danger .metric-icon {
  background: #fff2f0;
  color: #ff4d4f;
}

.metric-content {
  flex: 1;
}

.metric-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1;
  margin-bottom: 8px;
}

.metric-label {
  font-size: 14px;
  color: #666;
}

.metric-percent {
  font-size: 12px;
  color: #999;
}

/* ========== 迭代进度时间轴 ========== */
.iterations-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.iterations-timeline {
  min-height: 200px;
}

.timeline-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}

.timeline-scroll::-webkit-scrollbar {
  height: 6px;
}

.timeline-scroll::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.iteration-item {
  flex-shrink: 0;
  width: 280px;
  background: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.iteration-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

.iteration-status-icon {
  margin-bottom: 12px;
}

.iteration-status-icon .el-icon {
  font-size: 24px;
}

.iteration-status-icon .completed {
  color: #52c41a;
}

.iteration-status-icon .in-progress {
  color: #1890ff;
}

.iteration-status-icon .not-started {
  color: #d9d9d9;
}

.iteration-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.iteration-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.iteration-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.iteration-dates {
  font-size: 12px;
  color: #666;
}

.iteration-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #666;
}

.iteration-stats .stat {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* ========== 最近活动流 ========== */
.activities-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.activities-timeline {
  min-height: 200px;
}

.timeline-content {
  max-height: 400px;
  overflow-y: auto;
}

.activity-group {
  margin-bottom: 24px;
}

.activity-group:last-child {
  margin-bottom: 0;
}

.group-header {
  margin-bottom: 12px;
}

.group-date {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.group-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.activity-item:hover {
  background: #f0f2f5;
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-icon.task {
  background: #e6f7ff;
  color: #1890ff;
}

.activity-icon.iteration {
  background: #f6ffed;
  color: #52c41a;
}

.activity-icon.member {
  background: #fff7e6;
  color: #faad14;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-time {
  font-size: 12px;
  color: #999;
}

/* ========== 报表内容 ========== */
.report-content {
  padding-bottom: 24px;
}

.chart-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: box-shadow 0.3s;
}

.chart-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.chart-container {
  height: 320px;
  width: 100%;
}

.chart-container-large {
  height: 400px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .view-tabs {
    overflow-x: auto;
  }

  .timeline-scroll {
    flex-direction: column;
  }

  .iteration-item {
    width: 100%;
  }

  .chart-container,
  .chart-container-large {
    height: 280px;
  }
}
</style>
