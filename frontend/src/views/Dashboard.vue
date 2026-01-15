<template>
  <div class="page-root">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">首页看板</h2>
        <div class="welcome-text">欢迎回来，{{ authStore.username }}</div>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleBatchCreateWorkHour">
          工时填报
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon project">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">项目数</div>
              <div class="stats-value">{{ dashboardData.projectCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon task">
              <el-icon><List /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">待办任务</div>
              <div class="stats-value">{{ dashboardData.pendingTaskCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon today">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">今日工时</div>
              <div class="stats-value">{{ dashboardData.todayHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon week">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">本周工时</div>
              <div class="stats-value">{{ dashboardData.weekHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工时统计 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="12">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon month">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">本月工时</div>
              <div class="stats-value">{{ dashboardData.monthHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">总计工时</div>
              <div class="stats-value">{{ dashboardData.totalHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tab内容区域 -->
    <el-card class="content-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="我的待办任务" name="tasks">
          <template #label>
            <span class="tab-label">
              我的待办任务
              <el-badge v-if="dashboardData.pendingTasks?.length" :value="dashboardData.pendingTasks.length" class="tab-badge" />
            </span>
          </template>
          <div v-if="dashboardData.pendingTasks?.length">
            <el-table :data="enhancedPendingTasks" stripe>
              <el-table-column prop="id" label="ID" width="70" />
              <el-table-column prop="title" label="任务标题" min-width="200" />
              <el-table-column label="所属项目" width="150">
                <template #default="{ row }">
                  <el-link type="primary" @click="goToProject(row.projectId)">
                    {{ getProjectName(row.projectId) }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column label="优先级" width="90">
                <template #default="{ row }">
                  <el-tag :type="getPriorityType(row.priority)" size="small">
                    {{ getPriorityText(row.priority) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" size="small">
                    {{ getStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="截止日期" width="110">
                <template #default="{ row }">
                  <span :class="getDueDateClass(row.planEndDate)">
                    {{ formatDate(row.planEndDate) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="goToTask(row.id)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="tab-footer">
              <el-button link type="primary" @click="goToTasks('TODO')">查看全部待办任务 →</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无待办任务" />
        </el-tab-pane>

        <el-tab-pane label="我的项目" name="projects">
          <template #label>
            <span class="tab-label">
              我的项目
              <el-badge v-if="dashboardData.projects?.length" :value="dashboardData.projects.length" class="tab-badge" />
            </span>
          </template>
          <div v-if="dashboardData.projects?.length">
            <el-table :data="dashboardData.projects" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="项目名称" min-width="200" />
              <el-table-column prop="code" label="项目编码" width="150" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getProjectStatusType(row.status)">
                    {{ getProjectStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100">
                <template #default="{ row }">
                  <el-button link type="primary" @click="goToProject(row.id)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="tab-footer">
              <el-button link type="primary" @click="$router.push('/projects')">查看全部项目 →</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无项目" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 工时填报对话框 -->
    <WorkHourBatchDialog
      v-model:visible="batchDialogVisible"
      :date="selectedDate"
      @success="fetchDashboardData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Folder, List, Clock, Calendar, DataLine, Plus } from '@element-plus/icons-vue'
import { getTaskStatusInfo, getTaskPriorityInfo, getProjectStatusInfo } from '@/utils/statusMapping'
import { useAuthStore } from '@/stores/auth'
import { getDashboardData } from '@/api/statistics'
import WorkHourBatchDialog from '@/components/WorkHourBatchDialog.vue'

const router = useRouter()
const authStore = useAuthStore()

// 工时填报对话框
const batchDialogVisible = ref(false)
const selectedDate = ref<string>()
const activeTab = ref('tasks') // 默认显示待办任务tab

// 打开工时填报对话框
const handleBatchCreateWorkHour = () => {
  selectedDate.value = undefined // 不传日期，使用今天
  batchDialogVisible.value = true
}

// 打开工时填报对话框（指定日期）
const handleBatchCreateWorkHourWithDate = (date: string) => {
  selectedDate.value = date
  batchDialogVisible.value = true
}

interface DashboardData {
  projectCount: number
  pendingTaskCount: number
  todayHours: number
  weekHours: number
  monthHours: number
  totalHours: number
  pendingTasks: Array<{
    id: number
    title: string
    status: string
    priority: string
    projectId: number
    planEndDate?: string
  }>
  projects: Array<{
    id: number
    name: string
    code: string
    status: string
  }>
}

const dashboardData = reactive<DashboardData>({
  projectCount: 0,
  pendingTaskCount: 0,
  todayHours: 0,
  weekHours: 0,
  monthHours: 0,
  totalHours: 0,
  pendingTasks: [],
  projects: []
})

// 增强的待办任务列表（添加项目名称）
const enhancedPendingTasks = computed(() => {
  return dashboardData.pendingTasks.map(task => ({
    ...task,
    // 如果后端没有返回项目名称，可以从projects列表中匹配
    projectName: getProjectName(task.projectId)
  }))
})

const loading = ref(false)

// 获取状态映射
const getStatusType = (status: string) => getTaskStatusInfo(status).type
const getStatusText = (status: string) => getTaskStatusInfo(status).text
const getPriorityType = (priority: string) => getTaskPriorityInfo(priority).type
const getPriorityText = (priority: string) => getTaskPriorityInfo(priority).text
const getProjectStatusType = (status: string) => getProjectStatusInfo(status).type
const getProjectStatusText = (status: string) => getProjectStatusInfo(status).text

// 获取看板数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    const data = await getDashboardData()
    Object.assign(dashboardData, data)
  } catch (error) {
    console.error('获取看板数据失败:', error)
    ElMessage.error('获取看板数据失败')
  } finally {
    loading.value = false
  }
}

// 跳转到任务详情
const goToTask = (taskId: number) => {
  router.push(`/tasks/${taskId}`)
}

// 跳转到任务列表（带状态过滤）
const goToTasks = (statusFilter?: string) => {
  if (statusFilter) {
    router.push({
      path: '/tasks',
      query: { status: statusFilter }
    })
  } else {
    router.push('/tasks')
  }
}

// 跳转到项目详情
const goToProject = (projectId: number) => {
  router.push(`/projects/${projectId}`)
}

// 获取项目名称
const getProjectName = (projectId: number): string => {
  const project = dashboardData.projects.find(p => p.id === projectId)
  return project?.name || `项目${projectId}`
}

// 格式化日期
const formatDate = (dateStr?: string): string => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diffTime = date.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays < 0) return '已逾期'
  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '明天'
  if (diffDays <= 7) return `${diffDays}天后`

  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}/${day}`
}

// 获取截止日期样式类
const getDueDateClass = (dateStr?: string): string => {
  if (!dateStr) return ''

  const date = new Date(dateStr)
  const now = new Date()
  const diffTime = date.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))

  if (diffDays < 0) return 'due-date-overdue'
  if (diffDays <= 2) return 'due-date-soon'
  return ''
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
/* ========== 首页特定样式 ========== */

/* 欢迎文本 */
.welcome-text {
  font-size: 14px;
  color: #666;
}

/* 统计图标渐变色 */
.stats-icon.project {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stats-icon.task {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.stats-icon.today {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.stats-icon.week {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
}

.stats-icon.month {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: #fff;
}

.stats-icon.total {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
  color: #fff;
}

/* Tab 相关 */
.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-badge {
  margin-left: 4px;
}

.tab-footer {
  margin-top: 16px;
  text-align: right;
}

/* 截止日期样式 */
.due-date-overdue {
  color: #f56c6c;
  font-weight: 500;
}

.due-date-soon {
  color: #e6a23c;
  font-weight: 500;
}
</style>
