<template>
  <el-dialog
    v-model="dialogVisible"
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <template #header>
      <div class="dialog-header">
        <div class="title-date-group">
          <span class="dialog-title">工时填报</span>
          <span class="date-separator">—</span>
          <el-date-picker
            v-model="workDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="inline-date-picker"
          />
        </div>
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
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleBatchSubmit" :loading="submitting">
        提交
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getProjectList } from '@/api/project'
import { getTaskList } from '@/api/task'
import { createWorkHoursBatch } from '@/api/workhour'

interface Props {
  visible: boolean
  date?: string
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const authStore = useAuthStore()

// 对话框显示状态
const dialogVisible = ref(false)
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

// 监听 visible 变化
watch(() => props.visible, async (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    await openDialog()
  }
})

// 监听 dialogVisible 变化，同步到父组件
watch(dialogVisible, (newVal) => {
  if (!newVal) {
    emit('update:visible', false)
  }
})

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
}

// 打开对话框
const openDialog = async () => {
  batchForm.items = []

  // 如果传入日期，使用该日期，否则使用今天
  if (props.date) {
    workDate.value = props.date
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
    dialogVisible.value = false
    emit('success')
  } catch (error) {
    console.error('工时填报失败:', error)
    ElMessage.error('工时填报失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* 对话框标题样式 */
.dialog-header {
  display: flex;
  align-items: center;
  width: 100%;
}

.title-date-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.date-separator {
  font-size: 18px;
  color: #dcdfe6;
  font-weight: 300;
}

:deep(.inline-date-picker) {
  width: 140px;
}

:deep(.inline-date-picker .el-input__wrapper) {
  border-radius: 4px;
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
