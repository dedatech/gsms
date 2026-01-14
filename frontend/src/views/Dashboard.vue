<template>
  <div class="dashboard">
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

    <!-- 待办任务列表 -->
    <el-card class="task-card" v-if="dashboardData.pendingTasks?.length">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的待办任务</span>
          <el-button link type="primary" @click="goToTasks('TODO')">查看全部</el-button>
        </div>
      </template>
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
    </el-card>

    <!-- 项目列表 -->
    <el-card class="project-card" v-if="dashboardData.projects?.length">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的项目</span>
          <el-button link type="primary" @click="$router.push('/projects')">查看全部</el-button>
        </div>
      </template>
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
    </el-card>

    <!-- 工时填报对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      width="900px"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="dialog-header">
          <span class="dialog-title">工时填报</span>
          <el-date-picker
            v-model="workDate"
            type="date"
            placeholder="选择日期"
            format="YYYY年MM月DD日"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </div>
      </template>

      <el-form :model="batchForm" ref="batchFormRef">
        <el-table :data="batchForm.items" border style="width: 100%; margin-bottom: 16px">
          <el-table-column label="功能内容" min-width="200">
            <template #default="{ row }">
              <el-input v-model="row.content" placeholder="请输入工作内容" />
            </template>
          </el-table-column>
          <el-table-column label="选择项目" width="180">
            <template #default="{ row }">
              <el-select
                v-model="row.projectId"
                placeholder="选择项目"
                @change="handleProjectChange(row)"
                style="width: 100%"
              >
                <el-option
                  v-for="project in projects"
                  :key="project.id"
                  :label="project.name"
                  :value="project.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="选择任务" width="180">
            <template #default="{ row }">
              <el-select
                v-model="row.taskId"
                placeholder="选择任务（可选）"
                clearable
                filterable
                style="width: 100%"
                :disabled="!row.projectId"
                @change="handleTaskChange(row)"
              >
                <el-option
                  v-for="task in getProjectTasks(row.projectId)"
                  :key="task.id"
                  :label="task.title"
                  :value="task.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="工时（小时）" width="130">
            <template #default="{ row }">
              <el-input-number
                v-model="row.hours"
                :min="0.5"
                :max="24"
                :step="0.5"
                :precision="1"
                controls-position="right"
                style="width: 100%"
              />
            </template>
          </el-table-column>
          <el-table-column label="剩余工时" width="100">
            <template #default="{ row }">
              <span :class="getRemainingHoursClass(row)">
                {{ getRemainingHours(row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ $index }">
              <el-button
                link
                type="danger"
                :icon="Delete"
                @click="removeRow($index)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div style="margin-bottom: 16px">
          <el-button type="primary" :icon="Plus" @click="addRow" plain>
            添加一行
          </el-button>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit" :loading="submitting">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Folder, List, Clock, Calendar, DataLine, Plus, Delete } from '@element-plus/icons-vue'
import { getTaskStatusInfo, getTaskPriorityInfo, getProjectStatusInfo } from '@/utils/statusMapping'
import { useAuthStore } from '@/stores/auth'
import { getDashboardData } from '@/api/statistics'
import { getProjectList } from '@/api/project'
import { getTaskList } from '@/api/task'
import { createWorkHoursBatch } from '@/api/workhour'

const router = useRouter()
const authStore = useAuthStore()

// 批量填报工时相关
const batchDialogVisible = ref(false)
const batchFormRef = ref()
const submitting = ref(false)
const projects = ref<any[]>([])
const allTasks = ref<any[]>([])
const workDate = ref(new Date().toISOString().split('T')[0]) // 统一填报日期
const taskRemainingHours = ref<Record<number, number>>({}) // 任务剩余工时缓存

const batchForm = reactive({
  items: [] as Array<{
    content: string
    projectId: number | undefined
    taskId: number | undefined
    hours: number
  }>
})

// 打开批量填报对话框
const handleBatchCreateWorkHour = async (date?: string) => {
  batchDialogVisible.value = true
  batchForm.items = []

  // 如果传入日期，使用该日期，否则使用今天
  if (date) {
    workDate.value = date
  } else {
    workDate.value = new Date().toISOString().split('T')[0]
  }

  // 加载项目列表
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 1000 })
    projects.value = res.list || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }

  // 加载任务列表
  try {
    const res = await getTaskList({ pageNum: 1, pageSize: 1000 })
    allTasks.value = res.list || []
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }

  // 智能默认：当用户只有一个活跃项目时自动选中
  const activeProjects = projects.value.filter(p =>
    p.status === 'NOT_STARTED' || p.status === 'IN_PROGRESS'
  )
  if (activeProjects.length === 1) {
    const defaultProjectId = activeProjects[0].id
    // 默认添加一行并自动选中项目
    addRow(defaultProjectId)
  } else {
    addRow()
  }
}

// 添加一行
const addRow = (defaultProjectId?: number) => {
  batchForm.items.push({
    content: '',
    projectId: defaultProjectId,
    taskId: undefined,
    hours: 1
  })
}

// 删除一行
const removeRow = (index: number) => {
  if (batchForm.items.length > 1) {
    batchForm.items.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一行')
  }
}

// 项目变化时清空任务
const handleProjectChange = (row: any) => {
  row.taskId = undefined
}

// 任务变化时计算剩余工时
const handleTaskChange = (row: any) => {
  // 触发重新计算剩余工时
  if (row.taskId) {
    fetchTaskRemainingHours(row.taskId)
  }
}

// 获取任务的剩余工时
const fetchTaskRemainingHours = async (taskId: number) => {
  if (taskRemainingHours.value[taskId]) {
    return // 已缓存
  }

  try {
    // TODO: 调用后端API获取任务剩余工时
    // const res = await getTaskRemainingHours(taskId)
    // taskRemainingHours.value[taskId] = res.remainingHours

    // 暂时使用任务estimateHours字段（如果有的话）
    const task = allTasks.value.find(t => t.id === taskId)
    if (task && task.estimateHours) {
      taskRemainingHours.value[taskId] = task.estimateHours
    }
  } catch (error) {
    console.error('获取任务剩余工时失败:', error)
  }
}

// 获取剩余工时显示文本
const getRemainingHours = (row: any): string => {
  if (row.taskId && taskRemainingHours.value[row.taskId]) {
    const remaining = taskRemainingHours.value[row.taskId]
    return `${remaining}h`
  }
  if (row.projectId) {
    // 显示项目剩余工时（TODO: 需要后端API）
    return '-'
  }
  return '-'
}

// 获取剩余工时样式类
const getRemainingHoursClass = (row: any): string => {
  if (!row.taskId || !taskRemainingHours.value[row.taskId]) {
    return ''
  }

  const remaining = taskRemainingHours.value[row.taskId]
  const hours = row.hours || 0

  if (remaining - hours < 0) {
    return 'remaining-hours-over' // 超出预估
  } else if (remaining - hours < 2) {
    return 'remaining-hours-low' // 即将用完
  } else {
    return 'remaining-hours-normal' // 正常
  }
}

// 获取项目的任务列表
const getProjectTasks = (projectId: number | undefined) => {
  if (!projectId) return []
  return allTasks.value.filter(task => task.projectId === projectId)
}

// 提交批量工时
const handleBatchSubmit = async () => {
  // 验证表单
  for (let i = 0; i < batchForm.items.length; i++) {
    const item = batchForm.items[i]
    if (!item.content) {
      ElMessage.error(`第 ${i + 1} 行：请输入功能内容`)
      return
    }
    if (!item.projectId) {
      ElMessage.error(`第 ${i + 1} 行：请选择项目`)
      return
    }
    if (!workDate.value) {
      ElMessage.error(`请选择填报日期`)
      return
    }
    if (item.hours < 0.5 || item.hours > 24) {
      ElMessage.error(`第 ${i + 1} 行：工时必须在 0.5-24 小时之间`)
      return
    }
  }

  submitting.value = true
  try {
    const userId = authStore.getCurrentUserId()
    const workHoursData = batchForm.items.map(item => ({
      projectId: item.projectId!,
      taskId: item.taskId,
      workDate: workDate.value, // 使用统一的填报日期
      hours: item.hours,
      content: item.content
    }))

    await createWorkHoursBatch(workHoursData)
    ElMessage.success('工时填报成功')
    batchDialogVisible.value = false

    // 刷新看板数据
    await fetchDashboardData()
  } catch (error) {
    console.error('工时填报失败:', error)
    ElMessage.error('工时填报失败')
  } finally {
    submitting.value = false
  }
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
.dashboard {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.welcome-text {
  font-size: 14px;
  color: #666;
}

.header-right {
  display: flex;
  align-items: center;
}

.stats-row {
  margin-bottom: 16px;
}

.stats-card {
  border-radius: 8px;
  overflow: hidden;
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

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

.stats-info {
  flex: 1;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.task-card,
.project-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f5f5;
  color: #333;
  font-weight: 500;
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

/* 对话框标题样式 */
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.dialog-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

/* 剩余工时样式 */
.remaining-hours-normal {
  color: #67c23a;
  font-weight: 500;
}

.remaining-hours-low {
  color: #e6a23c;
  font-weight: 500;
}

.remaining-hours-over {
  color: #f56c6c;
  font-weight: 500;
}
</style>
