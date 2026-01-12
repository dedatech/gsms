<template>
  <div class="task-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">任务管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建任务</el-button>
      </div>
      <div class="header-right">
        <el-select
          v-model="searchForm.projectId"
          placeholder="选择项目"
          clearable
          style="width: 180px; margin-right: 16px"
          @change="fetchTasks"
        >
          <el-option
            v-for="project in projectList"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
        <el-select
          v-model="searchForm.assigneeId"
          placeholder="负责人"
          clearable
          style="width: 140px; margin-right: 16px"
          @change="fetchTasks"
        >
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.nickname"
            :value="user.id"
          />
        </el-select>
        <el-button-group>
          <el-button
            :type="viewMode === 'kanban' ? 'primary' : ''"
            @click="viewMode = 'kanban'"
          >
            <el-icon><Grid /></el-icon>
            看板
          </el-button>
          <el-button
            :type="viewMode === 'table' ? 'primary' : ''"
            @click="viewMode = 'table'"
          >
            <el-icon><List /></el-icon>
            列表
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 看板视图 -->
    <div v-if="viewMode === 'kanban'" class="kanban-view">
      <el-row :gutter="16">
        <el-col v-for="status in taskStatuses" :key="status.value" :xs="24" :sm="8" :md="6">
          <div
            class="kanban-column"
            :data-status="status.value"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDrop"
          >
            <div class="column-header">
              <div class="column-title">
                <div class="status-dot" :style="{ backgroundColor: status.color }"></div>
                <span>{{ status.label }}</span>
                <span class="task-count">{{ getTasksByStatus(status.value).length }}</span>
              </div>
            </div>
            <div class="column-body">
              <div
                v-for="task in getTasksByStatus(status.value)"
                :key="task.id"
                class="task-card"
                draggable="true"
                @dragstart="handleDragStart(task, $event)"
                @click="handleView(task)"
              >
                <div class="task-header">
                  <el-tag :type="getPriorityType(task.priority)" size="small">
                    {{ getPriorityText(task.priority) }}
                  </el-tag>
                  <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, task)">
                    <el-icon class="more-icon" @click.stop><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="addSubtask" :icon="Plus">添加子任务</el-dropdown-item>
                        <el-dropdown-item command="edit" :icon="Edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="delete" :icon="Delete" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <h4 class="task-title">{{ task.title }}</h4>
                <p class="task-description">{{ task.description || '暂无描述' }}</p>
                <div class="task-footer">
                  <div class="task-meta">
                    <el-avatar :size="24" :src="task.assigneeAvatar">
                      {{ (task.assigneeName || '未').charAt(0) }}
                    </el-avatar>
                    <span class="assignee-name">{{ task.assigneeName || '未分配' }}</span>
                  </div>
                  <div class="task-date">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ formatDate(task.planEndDate) }}</span>
                  </div>
                </div>
              </div>
              <el-empty
                v-if="getTasksByStatus(status.value).length === 0"
                description="暂无任务"
                :image-size="60"
              />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 表格视图 -->
    <div v-else class="table-view">
      <el-table :data="taskList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="任务标题" min-width="150">
          <template #default="{ row }">
            <div class="table-task-title">
              <el-tag :type="getPriorityType(row.priority)" size="small" style="margin-right: 8px">
                {{ getPriorityText(row.priority) }}
              </el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" width="120" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }">
            {{ getTypeText(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column prop="planStartDate" label="计划开始" width="110">
          <template #default="{ row }">
            {{ formatDate(row.planStartDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="planEndDate" label="计划结束" width="110">
          <template #default="{ row }">
            {{ formatDate(row.planEndDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualStartDate" label="实际开始" width="110">
          <template #default="{ row }">
            {{ formatDate(row.actualStartDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualEndDate" label="实际结束" width="110">
          <template #default="{ row }">
            {{ formatDate(row.actualEndDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" width="90" />
        <el-table-column prop="updateUserName" label="更新人" width="90" />
        <el-table-column prop="createTime" label="创建时间" width="155">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="155">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :icon="Plus"
              @click="handleAddSubtask(row)"
            >
              子任务
            </el-button>
            <el-button link type="primary" :icon="View" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="taskList.length === 0" description="暂无任务数据" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0 && viewMode === 'table'" class="pagination">
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchTasks"
        @current-change="fetchTasks"
      />
    </div>

    <!-- 新建/编辑任务对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务标题" prop="title">
              <el-input v-model="formData.title" placeholder="请输入任务标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!formData.parentId">
            <el-form-item label="所属项目" prop="projectId">
              <el-select v-model="formData.projectId" placeholder="请选择项目" style="width: 100%" @change="handleProjectChange">
                <el-option
                  v-for="project in projectList"
                  :key="project.id"
                  :label="project.name"
                  :value="project.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="formData.parentId">
            <el-form-item label="父任务">
              <el-input :value="parentTaskName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="!formData.parentId">
          <el-col :span="12">
            <el-form-item label="所属迭代">
              <el-select
                v-model="formData.iterationId"
                placeholder="可选择迭代（可选）"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="iter in projectIterations"
                  :key="iter.id"
                  :label="iter.name"
                  :value="iter.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- 占位 -->
          </el-col>
        </el-row>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="assigneeId">
              <el-select v-model="formData.assigneeId" placeholder="请选择负责人（可选）" clearable filterable style="width: 100%">
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
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="formData.priority" placeholder="请选择优先级" style="width: 100%">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="planStartDate">
              <el-date-picker
                v-model="formData.planStartDate"
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
                v-model="formData.planEndDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="formData.id" label="任务状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="TODO">待办</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="DONE">已完成</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Grid,
  List,
  Calendar,
  MoreFilled,
  Edit,
  Delete,
  View
} from '@element-plus/icons-vue'
import { getTaskList, createTask, updateTask, updateTaskStatus, deleteTask } from '@/api/task'
import { getProjectList, getProjectMembers } from '@/api/project'
import { getIterationList } from '@/api/iteration'
import { getAllUsers, type UserInfo } from '@/api/user'
import { getTaskStatusInfo, getTaskPriorityInfo, getTaskStatusOptions } from '@/utils/statusMapping'

const router = useRouter()

// 视图模式
const viewMode = ref<'kanban' | 'table'>('kanban')

// 任务状态配置
const taskStatuses = getTaskStatusOptions()

// 搜索表单
const searchForm = reactive({
  projectId: undefined as number | undefined,
  assigneeId: undefined as number | undefined,
  status: undefined as string | undefined,
  pageNum: 1,
  pageSize: 50
})

// 任务列表
const taskList = ref<any[]>([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  title: '',
  description: '',
  projectId: undefined as number | undefined,
  assigneeId: undefined as number | undefined,
  priority: 'MEDIUM',
  planStartDate: '',
  planEndDate: '',
  status: 'TODO',
  iterationId: undefined as number | undefined,  // 所属迭代（可选）
  parentId: undefined as number | undefined      // 父任务ID（用于子任务）
})

// 表单规则（动态计算）
const formRules = computed(() => {
  const rules: FormRules = {
    title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
  }

  // 只有在没有父任务时，才验证项目ID必填
  if (!formData.parentId) {
    rules.projectId = [{ required: true, message: '请选择所属项目', trigger: 'change' }]
  }

  return rules
})

const formRef = ref<FormInstance>()

// 父任务名称（用于显示）
const parentTaskName = computed(() => {
  if (parentTaskInfo.value) {
    return parentTaskInfo.value.title
  }
  if (!formData.parentId) return ''
  const parentTask = taskList.value.find(t => t.id === formData.parentId)
  return parentTask?.title || ''
})

// 项目列表
const projectList = ref<any[]>([])

// 用户列表
const userList = ref<UserInfo[]>([])

// 项目成员列表（根据选择的项目动态加载）
const projectMembers = ref<UserInfo[]>([])

// 项目迭代列表（根据选择的项目动态加载）
const projectIterations = ref<any[]>([])

// 父任务信息（用于子任务创建）
const parentTaskInfo = ref<TaskInfo | null>(null)

// 拖拽相关
const draggedTask = ref<TaskInfo | null>(null)

// 获取用户列表
const fetchUsers = async () => {
  try {
    const res = await getAllUsers()
    if (res.list) {
      userList.value = res.list
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 根据状态获取任务（使用computed以优化性能和响应式）
const todoTasks = computed(() => taskList.value.filter(task => task.status === 'TODO' || task.status === null))
const inProgressTasks = computed(() => taskList.value.filter(task => task.status === 'IN_PROGRESS'))
const doneTasks = computed(() => taskList.value.filter(task => task.status === 'DONE'))

// 辅助函数：根据状态值返回对应任务列表
const getTasksByStatus = (status: string) => {
  const statusMap: Record<string, any[]> = {
    'TODO': todoTasks.value,
    'IN_PROGRESS': inProgressTasks.value,
    'DONE': doneTasks.value
  }
  return statusMap[status] || []
}

// 拖拽开始
const handleDragStart = (task: TaskInfo, event: DragEvent) => {
  draggedTask.value = task
  if (event.dataTransfer) {
    event.dataTransfer.setData('text/plain', task.id.toString())
    event.dataTransfer.effectAllowed = 'move'
  }
  // 添加拖拽样式
  ;(event.target as HTMLElement).classList.add('dragging')
}

// 拖拽经过
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
  // 添加拖拽悬停样式
  const column = (event.currentTarget as HTMLElement)
  column.classList.add('drag-over')
}

// 拖拽离开
const handleDragLeave = (event: DragEvent) => {
  const column = (event.currentTarget as HTMLElement)
  column.classList.remove('drag-over')
}

// 放置任务
const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  const targetStatus = (event.currentTarget as HTMLElement).getAttribute('data-status')

  // 移除拖拽样式
  const draggingElements = document.querySelectorAll('.dragging')
  draggingElements.forEach(el => el.classList.remove('dragging'))

  // 移除所有列的拖拽悬停样式
  const dragOverElements = document.querySelectorAll('.drag-over')
  dragOverElements.forEach(el => el.classList.remove('drag-over'))

  if (draggedTask.value && targetStatus && draggedTask.value.status !== targetStatus) {
    try {
      await updateTaskStatus({
        id: draggedTask.value.id,
        status: targetStatus
      })
      ElMessage.success('任务状态已更新')
      fetchTasks() // 刷新任务列表
    } catch (error) {
      console.error('更新任务状态失败:', error)
      ElMessage.error('更新任务状态失败')
    }
  }

  draggedTask.value = null
}

// 获取任务列表
const fetchTasks = async () => {
  try {
    const res = await getTaskList(searchForm)
    // PageResult 格式：{ list: [], total: 0 }
    taskList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

// 获取项目列表
const fetchProjects = async () => {
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 100 })
    projectList.value = res.list || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 获取项目成员列表
const fetchProjectMembers = async (projectId: number) => {
  if (!projectId) {
    projectMembers.value = []
    return
  }
  try {
    const res = await getProjectMembers(projectId)
    console.log('项目成员响应:', res)
    // 根据实际API响应格式调整
    projectMembers.value = res || []
  } catch (error) {
    console.error('获取项目成员失败:', error)
    projectMembers.value = []
  }
}

// 获取项目迭代列表
const fetchProjectIterations = async (projectId: number) => {
  if (!projectId) {
    projectIterations.value = []
    return
  }
  try {
    const res = await getIterationList({ projectId, pageNum: 1, pageSize: 100 })
    projectIterations.value = res.list || []
  } catch (error) {
    console.error('获取迭代列表失败:', error)
    projectIterations.value = []
  }
}

// 监听项目选择变化
const handleProjectChange = (projectId: number) => {
  formData.assigneeId = undefined
  formData.iterationId = undefined  // 清空迭代选择
  fetchProjectMembers(projectId)
  fetchProjectIterations(projectId)  // 加载迭代列表
}

// 新建任务
const handleCreate = () => {
  dialogTitle.value = '新建任务'
  dialogVisible.value = true
  resetForm()
}

// 查看任务
const handleView = (task: TaskInfo) => {
  router.push(`/tasks/${task.id}`)
}

// 编辑任务
const handleEdit = (task: TaskInfo) => {
  dialogTitle.value = '编辑任务'
  dialogVisible.value = true

  // 如果有父任务，查找并保存父任务信息
  if (task.parentId) {
    const parent = taskList.value.find(t => t.id === task.parentId)
    if (parent) {
      parentTaskInfo.value = parent
    }
  } else {
    parentTaskInfo.value = null
  }

  Object.assign(formData, {
    id: task.id,
    title: task.title,
    description: task.description,
    projectId: task.projectId,
    assigneeId: task.assigneeId,
    priority: task.priority,
    planStartDate: task.planStartDate,
    planEndDate: task.planEndDate,
    status: task.status,
    iterationId: task.iterationId,  // 所属迭代
    parentId: task.parentId         // 父任务
  })

  // 加载项目成员和迭代列表
  if (task.projectId) {
    fetchProjectMembers(task.projectId)
    fetchProjectIterations(task.projectId)
  }
}

// 删除任务
const handleDelete = (task: TaskInfo) => {
  ElMessageBox.confirm(`确定要删除任务 "${task.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTask(task.id)
        ElMessage.success('删除成功')
        fetchTasks()
      } catch (error) {
        console.error('删除任务失败:', error)
      }
    })
    .catch(() => {})
}

// 卡片操作命令
const handleCommand = (command: string, task: TaskInfo) => {
  if (command === 'edit') {
    handleEdit(task)
  } else if (command === 'delete') {
    handleDelete(task)
  } else if (command === 'addSubtask') {
    handleAddSubtask(task)
  }
}

// 添加子任务
const handleAddSubtask = (parentTask: TaskInfo) => {
  dialogTitle.value = '添加子任务'
  dialogVisible.value = true
  resetForm()
  // 保存父任务信息
  parentTaskInfo.value = parentTask
  // 预设项目、迭代和父任务（自动继承）
  formData.projectId = parentTask.projectId
  formData.iterationId = parentTask.iterationId
  formData.parentId = parentTask.id
  // 加载项目成员（用于选择负责人）
  fetchProjectMembers(parentTask.projectId)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          // 编辑
          await updateTask({
            id: formData.id,
            title: formData.title,
            description: formData.description,
            projectId: formData.projectId!,  // 添加项目ID
            assigneeId: formData.assigneeId,
            priority: formData.priority,
            planStartDate: formData.planStartDate,
            planEndDate: formData.planEndDate,
            status: formData.status,
            iterationId: formData.iterationId,
            parentId: formData.parentId
          })
          ElMessage.success('更新成功')
        } else {
          // 新建
          await createTask({
            title: formData.title,
            description: formData.description,
            projectId: formData.projectId!,
            assigneeId: formData.assigneeId,
            priority: formData.priority,
            planStartDate: formData.planStartDate,
            planEndDate: formData.planEndDate,
            iterationId: formData.iterationId,
            parentId: formData.parentId
            // status 由后端默认设置为 TODO
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchTasks()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.title = ''
  formData.description = ''
  formData.projectId = undefined
  formData.assigneeId = undefined
  formData.priority = 'MEDIUM'
  formData.planStartDate = ''
  formData.planEndDate = ''
  formData.status = 'TODO'
  formData.iterationId = undefined  // 新增
  formData.parentId = undefined     // 新增
  parentTaskInfo.value = null       // 清空父任务信息
  formRef.value?.clearValidate()
}

// 获取优先级类型
// 获取优先级信息
const getPriorityType = (priority: string) => getTaskPriorityInfo(priority).type
const getPriorityText = (priority: string) => getTaskPriorityInfo(priority).text

// 获取状态信息
const getStatusType = (status: string) => getTaskStatusInfo(status).type
const getStatusText = (status: string) => getTaskStatusInfo(status).text

// 获取类型文本
const getTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'TASK': '任务',
    'BUG': '缺陷',
    'STORY': '用户故事'
  }
  return texts[type] || '-'
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

// 格式化日期时间
const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date
}

onMounted(() => {
  fetchUsers()
  fetchProjects()
  fetchTasks()
})
</script>

<style scoped>
.task-list {
  min-height: calc(100vh - 160px);
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
}

/* 看板视图 */
.kanban-view {
  margin-bottom: 24px;
}

.kanban-column {
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.kanban-column.drag-over {
  background: #e6f7ff;
  box-shadow: 0 0 0 2px #1890ff inset;
}

.column-header {
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #333;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.task-count {
  margin-left: auto;
  font-size: 12px;
  color: #8c8c8c;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.column-body {
  padding: 16px;
  min-height: 400px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.task-card {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.task-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.task-card.dragging {
  opacity: 0.5;
  cursor: move;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.more-icon {
  font-size: 18px;
  color: #8c8c8c;
  cursor: pointer;
  transition: color 0.3s;
}

.more-icon:hover {
  color: #333;
}

.task-title {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-description {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assignee-name {
  font-size: 12px;
  color: #595959;
}

.task-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8c8c8c;
}

/* 表格视图 */
.table-view {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  margin-bottom: 24px;
}

.table-task-title {
  display: flex;
  align-items: center;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

:deep(.el-empty) {
  padding: 40px 0;
}
</style>
