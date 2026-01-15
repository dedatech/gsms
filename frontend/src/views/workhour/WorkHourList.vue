<template>
  <div class="page-root">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的工时</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleCreate">登记工时</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon today">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">今日工时</div>
              <div class="stats-value">{{ statistics.todayHours || 0 }} 小时</div>
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
              <div class="stats-value">{{ statistics.weekHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon month">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">本月工时</div>
              <div class="stats-value">{{ statistics.monthHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total">
              <el-icon><Histogram /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">总计工时</div>
              <div class="stats-value">{{ statistics.totalHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="项目">
          <el-select v-model="queryForm.projectId" placeholder="请选择项目" clearable filterable style="width: 200px" @change="handleQuery">
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工时记录列表 -->
    <el-card class="table-card">
      <el-table :data="workHourList" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="工作日期" width="120">
          <template #default="{ row }">
            {{ row.workDate }}
          </template>
        </el-table-column>
        <el-table-column label="项目" width="200">
          <template #default="{ row }">
            {{ row.projectName }}
          </template>
        </el-table-column>
        <el-table-column label="任务" width="200">
          <template #default="{ row }">
            {{ row.taskName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="hours" label="工时数" width="100">
          <template #default="{ row }">
            {{ row.hours }} 小时
          </template>
        </el-table-column>
        <el-table-column prop="content" label="工作内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="工作日期" prop="workDate">
          <el-date-picker
            v-model="formData.workDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="项目" prop="projectId">
          <el-select v-model="formData.projectId" placeholder="请选择项目" filterable style="width: 100%" @change="handleProjectChange">
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务">
          <el-select v-model="formData.taskId" placeholder="请选择任务（可选）" clearable filterable style="width: 100%">
            <el-option
              v-for="task in taskList"
              :key="task.id"
              :label="task.title"
              :value="task.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工时数" prop="hours">
          <el-input-number v-model="formData.hours" :min="0.5" :max="24" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工作内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述工作内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, Clock, Calendar, DataLine, Histogram } from '@element-plus/icons-vue'
import { getWorkHourList, createWorkHour, updateWorkHour, deleteWorkHour, getUserWorkHourStatistics, type WorkHourInfo } from '@/api/workhour'
import { getProjectList } from '@/api/project'
import { getTaskList } from '@/api/task'
import { useAuthStore } from '@/stores/auth'
import { getTaskStatusInfo, getTaskPriorityInfo, getWorkHourStatusInfo } from '@/utils/statusMapping'

// 获取 auth store
const authStore = useAuthStore()

// 获取当前用户ID
const getCurrentUserId = (): number => {
  return authStore.getCurrentUserId() || 1
}

// 查询表单
const queryForm = reactive({
  projectId: undefined as number | undefined,
  startDate: undefined as string | undefined,
  endDate: undefined as string | undefined
})

// 日期范围
const dateRange = ref<[string, string] | null>(null)

// 项目列表
const projectList = ref<any[]>([])

// 任务列表
const taskList = ref<any[]>([])

// 工时列表
const workHourList = ref<WorkHourInfo[]>([])
const loading = ref(false)

// 统计数据
const statistics = reactive({
  todayHours: 0,
  weekHours: 0,
  monthHours: 0,
  totalHours: 0
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  workDate: '',
  projectId: undefined as number | undefined,
  taskId: undefined as number | undefined,
  hours: 8,
  content: ''
})

const formRules: FormRules = {
  workDate: [{ required: true, message: '请选择工作日期', trigger: 'change' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  hours: [{ required: true, message: '请输入工时数', trigger: 'blur' }],
  content: [{ required: true, message: '请输入工作内容', trigger: 'blur' }]
}

// 获取工时状态信息
const getStatusType = (status: string) => getWorkHourStatusInfo(status).type
const getStatusText = (status: string) => getWorkHourStatusInfo(status).text

// 获取工时列表
const fetchWorkHours = async () => {
  loading.value = true
  try {
    const res = await getWorkHourList({
      projectId: queryForm.projectId,
      startDate: queryForm.startDate,
      endDate: queryForm.endDate,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    workHourList.value = res.list || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取工时列表失败:', error)
    ElMessage.error('获取工时列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const today = new Date()
    const weekStart = new Date(today)
    weekStart.setDate(today.getDate() - today.getDay())
    const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)

    // 获取当前用户ID
    const currentUserId = authStore.getCurrentUserId()

    // 获取今日工时
    const todayStr = today.toISOString().split('T')[0]
    const todayRes = await getUserWorkHourStatistics(currentUserId, todayStr, todayStr)
    statistics.todayHours = todayRes?.totalHours || 0

    // 获取本周工时
    const weekStartStr = weekStart.toISOString().split('T')[0]
    const todayStr2 = today.toISOString().split('T')[0]
    const weekRes = await getUserWorkHourStatistics(currentUserId, weekStartStr, todayStr2)
    statistics.weekHours = weekRes?.totalHours || 0

    // 获取本月工时
    const monthStartStr = monthStart.toISOString().split('T')[0]
    const monthRes = await getUserWorkHourStatistics(currentUserId, monthStartStr, todayStr2)
    statistics.monthHours = monthRes?.totalHours || 0

    // 获取总工时
    const totalRes = await getUserWorkHourStatistics(currentUserId)
    statistics.totalHours = totalRes?.totalHours || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取项目列表
const fetchProjects = async () => {
  try {
    const res = await getProjectList({
      pageNum: 1,
      pageSize: 1000
    })
    projectList.value = res.list || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 获取任务列表
const fetchTasks = async () => {
  if (!formData.projectId) {
    taskList.value = []
    return
  }
  try {
    const res = await getTaskList({
      projectId: formData.projectId,
      pageNum: 1,
      pageSize: 1000
    })
    taskList.value = res.list || []
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

// 日期范围变化
const handleDateRangeChange = (dates: [string, string] | null) => {
  if (dates && dates.length === 2) {
    queryForm.startDate = dates[0]
    queryForm.endDate = dates[1]
  } else {
    queryForm.startDate = undefined
    queryForm.endDate = undefined
  }
  handleQuery()
}

// 查询
const handleQuery = () => {
  pagination.pageNum = 1
  fetchWorkHours()
}

// 重置
const handleReset = () => {
  queryForm.projectId = undefined
  queryForm.startDate = undefined
  queryForm.endDate = undefined
  dateRange.value = null
  pagination.pageNum = 1
  fetchWorkHours()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchWorkHours()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  fetchWorkHours()
}

// 创建
const handleCreate = () => {
  dialogTitle.value = '登记工时'
  const today = new Date().toISOString().split('T')[0]
  formData.id = undefined
  formData.workDate = today
  formData.projectId = undefined
  formData.taskId = undefined
  formData.hours = 8
  formData.content = ''
  taskList.value = []
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: WorkHourInfo) => {
  dialogTitle.value = '编辑工时'
  formData.id = row.id
  formData.workDate = row.workDate
  formData.projectId = row.projectId
  formData.taskId = row.taskId
  formData.hours = row.hours
  formData.content = row.content
  fetchTasks()
  dialogVisible.value = true
}

// 项目变化时加载任务
const handleProjectChange = () => {
  formData.taskId = undefined
  fetchTasks()
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateWorkHour({
            id: formData.id,
            projectId: formData.projectId!,
            taskId: formData.taskId,
            workDate: formData.workDate,
            hours: formData.hours,
            content: formData.content
          })
          ElMessage.success('更新成功')
        } else {
          await createWorkHour({
            projectId: formData.projectId!,
            taskId: formData.taskId,
            workDate: formData.workDate,
            hours: formData.hours,
            content: formData.content
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchWorkHours()
        fetchStatistics()
      } catch (error) {
        console.error('操作失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除
const handleDelete = (row: WorkHourInfo) => {
  ElMessageBox.confirm(`确定要删除 ${row.workDate} 的工时记录吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteWorkHour(row.id)
        ElMessage.success('删除成功')
        fetchWorkHours()
        fetchStatistics()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchProjects()
  fetchWorkHours()
  fetchStatistics()
})
</script>

<style scoped>
/* ========== 工时页面特定样式 ========== */

/* 统计图标渐变色 */
.stats-icon.today {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stats-icon.week {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.stats-icon.month {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.stats-icon.total {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
}
</style>
