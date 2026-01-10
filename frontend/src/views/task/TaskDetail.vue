<template>
  <div class="task-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2 class="page-title">{{ task?.title || '加载中...' }}</h2>
        <el-tag :type="getStatusType(task?.status)" size="large">
          {{ getStatusText(task?.status) }}
        </el-tag>
        <el-tag :type="getPriorityType(task?.priority)" size="large" style="margin-left: 8px">
          {{ getPriorityText(task?.priority) }}
        </el-tag>
      </div>
      <div class="header-right">
        <!-- 状态快捷操作 -->
        <el-button
          v-if="task?.status === 'TODO' || task?.status === null"
          type="primary"
          :icon="VideoPlay"
          @click="handleStartTask"
        >
          开始任务
        </el-button>
        <el-button
          v-if="task?.status === 'IN_PROGRESS'"
          type="success"
          :icon="Select"
          @click="handleCompleteTask"
        >
          完成任务
        </el-button>
        <el-button
          v-if="task?.status === 'DONE'"
          :icon="RefreshLeft"
          @click="handleReopenTask"
        >
          重新打开
        </el-button>
        <el-button :icon="Edit" @click="handleEdit">编辑</el-button>
        <el-button :icon="Delete" type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="info">
        <el-collapse v-model="activeCollapse" class="info-collapse">
          <el-collapse-item name="basic" title="任务基本信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="任务ID">{{ task?.id }}</el-descriptions-item>
              <el-descriptions-item label="任务类型">
                {{ getTypeText(task?.type) }}
              </el-descriptions-item>
              <el-descriptions-item label="任务标题" :span="2">{{ task?.title }}</el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="getPriorityType(task?.priority)">
                  {{ getPriorityText(task?.priority) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="任务状态">
                <el-tag :type="getStatusType(task?.status)">
                  {{ getStatusText(task?.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="所属项目" :span="2">
                <el-link type="primary" @click="goToProject">
                  {{ task?.projectName }}
                </el-link>
              </el-descriptions-item>
              <el-descriptions-item label="负责人">
                {{ task?.assigneeName || '未分配' }}
              </el-descriptions-item>
              <el-descriptions-item label="迭代">
                {{ task?.iterationId || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="任务描述" :span="2">
                <div class="description-content">{{ task?.description || '暂无描述' }}</div>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item name="date" title="时间信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="计划开始时间">
                {{ task?.planStartDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="计划结束时间">
                {{ task?.planEndDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="实际开始时间">
                <span :class="{ 'text-danger': !task?.actualStartDate && (task?.status === 'IN_PROGRESS' || task?.status === 'DONE') }">
                  {{ task?.actualStartDate || '-' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="实际结束时间">
                {{ task?.actualEndDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="预估工时">
                {{ task?.estimateHours ? task.estimateHours + ' 小时' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(task?.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间" :span="2">
                {{ formatDateTime(task?.updateTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item name="creator" title="创建信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="创建人">
                {{ task?.createUserName }}
              </el-descriptions-item>
              <el-descriptions-item label="更新人">
                {{ task?.updateUserName }}
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>

      <!-- 工时记录 -->
      <el-tab-pane name="workhours">
        <template #label>
          <span>
            <el-icon><Clock /></el-icon>
            工时记录
          </span>
        </template>
        <div class="tab-content">
          <el-empty description="工时记录功能开发中" :image-size="100" />
        </div>
      </el-tab-pane>

      <!-- 子任务 -->
      <el-tab-pane name="subtasks">
        <template #label>
          <span>
            <el-icon><Files /></el-icon>
            子任务
          </span>
        </template>
        <div class="tab-content">
          <el-empty description="子任务功能开发中" :image-size="100" />
        </div>
      </el-tab-pane>

      <!-- 操作历史 -->
      <el-tab-pane name="history">
        <template #label>
          <span>
            <el-icon><Clock /></el-icon>
            操作历史
          </span>
        </template>
        <div class="tab-content">
          <el-empty description="操作历史功能开发中" :image-size="100" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑任务对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑任务"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editFormData" :rules="editFormRules" label-width="110px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="editFormData.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="editFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务类型" prop="type">
              <el-select v-model="editFormData.type" placeholder="请选择" style="width: 100%">
                <el-option label="任务" value="TASK" />
                <el-option label="需求" value="REQUIREMENT" />
                <el-option label="缺陷" value="BUG" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="editFormData.priority" placeholder="请选择" style="width: 100%">
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="assigneeId">
              <el-select v-model="editFormData.assigneeId" placeholder="请选择负责人" clearable filterable style="width: 100%">
                <el-option
                  v-for="user in projectMembers"
                  :key="user.id"
                  :label="user.nickname"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务状态" prop="status">
              <el-select v-model="editFormData.status" placeholder="请选择" style="width: 100%">
                <el-option label="待办" value="TODO" />
                <el-option label="进行中" value="IN_PROGRESS" />
                <el-option label="已完成" value="DONE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="planStartDate">
              <el-date-picker
                v-model="editFormData.planStartDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束时间" prop="planEndDate">
              <el-date-picker
                v-model="editFormData.planEndDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Delete,
  VideoPlay,
  Select,
  RefreshLeft,
  Clock,
  Files
} from '@element-plus/icons-vue'
import { getTaskDetail, updateTask, updateTaskStatus, deleteTask } from '@/api/task'
import { getProjectMembers } from '@/api/project'
import { getAllUsers } from '@/api/user'
import { getTaskStatusInfo, getTaskPriorityInfo } from '@/utils/statusMapping'

const route = useRoute()
const router = useRouter()
const taskId = computed(() => Number(route.params.id))

// 当前激活的标签页
const activeTab = ref('info')

// 折叠面板激活项
const activeCollapse = ref(['basic', 'date', 'creator'])

// 任务信息
const task = ref<any>(null)

// 项目成员列表
const projectMembers = ref<any[]>([])

// 编辑对话框
const editDialogVisible = ref(false)
const editSubmitLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editFormData = reactive({
  title: '',
  description: '',
  type: '',
  priority: '',
  assigneeId: undefined as number | undefined,
  status: '',
  planStartDate: '',
  planEndDate: ''
})
const editFormRules: FormRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
}

// 获取任务详情
const fetchTask = async () => {
  try {
    const res = await getTaskDetail(taskId.value)
    task.value = res
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  }
}

// 获取项目成员
const fetchProjectMembers = async () => {
  if (!task.value?.projectId) return

  try {
    const res = await getProjectMembers(task.value.projectId)
    projectMembers.value = res || []
  } catch (error) {
    console.error('获取项目成员失败:', error)
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 跳转到项目详情
const goToProject = () => {
  if (task.value?.projectId) {
    router.push(`/projects/${task.value.projectId}`)
  }
}

// 开始任务
const handleStartTask = async () => {
  try {
    await updateTaskStatus({
      id: taskId.value,
      status: 'IN_PROGRESS'
    })
    ElMessage.success('任务已开始')
    fetchTask()
  } catch (error) {
    console.error('开始任务失败:', error)
  }
}

// 完成任务
const handleCompleteTask = async () => {
  try {
    await updateTaskStatus({
      id: taskId.value,
      status: 'DONE'
    })
    ElMessage.success('任务已完成')
    fetchTask()
  } catch (error) {
    console.error('完成任务失败:', error)
  }
}

// 重新打开任务
const handleReopenTask = async () => {
  try {
    await updateTaskStatus({
      id: taskId.value,
      status: 'TODO'
    })
    ElMessage.success('任务已重新打开')
    fetchTask()
  } catch (error) {
    console.error('重新打开任务失败:', error)
  }
}

// 编辑任务
const handleEdit = () => {
  if (!task.value) return

  editFormData.title = task.value.title
  editFormData.description = task.value.description || ''
  editFormData.type = task.value.type || ''
  editFormData.priority = task.value.priority || ''
  editFormData.assigneeId = task.value.assigneeId
  editFormData.status = task.value.status || ''
  editFormData.planStartDate = task.value.planStartDate || ''
  editFormData.planEndDate = task.value.planEndDate || ''

  editDialogVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitLoading.value = true
      try {
        await updateTask({
          id: taskId.value,
          title: editFormData.title,
          description: editFormData.description,
          type: editFormData.type,
          priority: editFormData.priority,
          assigneeId: editFormData.assigneeId,
          status: editFormData.status,
          planStartDate: editFormData.planStartDate,
          planEndDate: editFormData.planEndDate
        })
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchTask()
      } catch (error) {
        console.error('更新任务失败:', error)
      } finally {
        editSubmitLoading.value = false
      }
    }
  })
}

// 删除任务
const handleDelete = () => {
  ElMessageBox.confirm(`确定要删除任务 "${task.value?.title}" 吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTask(taskId.value)
        ElMessage.success('删除成功')
        router.push('/tasks')
      } catch (error) {
        console.error('删除任务失败:', error)
      }
    })
    .catch(() => {})
}

// 获取任务状态和优先级信息
const getStatusType = (status: string) => getTaskStatusInfo(status).type
const getStatusText = (status: string) => getTaskStatusInfo(status).text
const getPriorityType = (priority: string) => getTaskPriorityInfo(priority).type
const getPriorityText = (priority: string) => getTaskPriorityInfo(priority).text

// 获取类型文本
const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'TASK': '任务',
    'REQUIREMENT': '需求',
    'BUG': '缺陷'
  }
  return texts[type] || '-'
}

// 格式化日期时间
const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date
}

onMounted(() => {
  fetchTask()
  fetchProjectMembers()
})
</script>

<style scoped>
.task-detail {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 标签页 */
.detail-tabs {
  background: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 24px;
}

:deep(.el-tabs__item .el-icon) {
  margin-right: 4px;
  vertical-align: -2px;
}

/* 标签页内容 */
.tab-content {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 折叠面板 */
.info-collapse {
  margin-bottom: 24px;
}

:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: 500;
  padding: 16px 0;
  background: transparent;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-collapse-item__wrap) {
  background: transparent;
}

:deep(.el-collapse-item__content) {
  padding: 16px 0;
}

.description-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  color: #333;
}

.text-danger {
  color: #f56c6c;
}

:deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #333;
}

:deep(.el-empty) {
  padding: 60px 0;
}
</style>
