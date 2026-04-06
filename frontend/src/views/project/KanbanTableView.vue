<template>
  <div class="kanban-table-view">
    <!-- 表格工具栏 -->
    <div class="table-toolbar">
      <h3 class="view-title">看板表格</h3>
      <div class="toolbar-filters">
        <el-select
          v-model="filterAssigneeId"
          placeholder="筛选负责人"
          clearable
          style="width: 180px"
          @change="handleFilterChange"
        >
          <el-option label="全部成员" :value="undefined" />
          <el-option
            v-for="member in members"
            :key="member.userId"
            :label="member.nickname"
            :value="member.userId"
          >
            <div class="member-option">
              <el-avatar :size="24">{{ member.nickname?.charAt(0) }}</el-avatar>
              <span>{{ member.nickname }}</span>
            </div>
          </el-option>
        </el-select>

        <el-select
          v-model="filterPriority"
          placeholder="筛选优先级"
          clearable
          style="width: 140px"
          @change="handleFilterChange"
        >
          <el-option label="全部优先级" :value="undefined" />
          <el-option label="🔴 高优先级" value="HIGH" />
          <el-option label="🟡 中优先级" value="MEDIUM" />
          <el-option label="🟢 低优先级" value="LOW" />
        </el-select>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="tableData.rows.length === 0" class="empty-container">
      <el-empty :description="!props.iterationId ? '请先在左侧选择一个迭代' : '该迭代下暂无需求数据'" :image-size="100" />
    </div>

    <!-- 看板表格 -->
    <el-table
      v-else
      :data="tableData.rows"
      border
      :height="tableHeight"
      class="kanban-table"
    >
      <!-- 需求列 -->
      <el-table-column
        label="需求列表"
        min-width="280px"
        fixed="left"
        class-name="requirement-column"
      >
        <template #default="{ row }">
          <div class="requirement-cell" @click="handleViewRequirement(row.requirement)">
            <div class="cell-header">
              <span class="item-number">#{{ row.requirement.id }}</span>
              <el-tag type="success" size="small">需求</el-tag>
            </div>
            <div class="cell-title">{{ row.requirement.title }}</div>
            <div class="cell-footer">
              <div class="assignee-info">
                <el-avatar :size="20" v-if="row.requirement.assigneeName">
                  {{ row.requirement.assigneeName?.charAt(0) }}
                </el-avatar>
                <span v-else class="unassigned">未分配</span>
              </div>
              <el-tag
                :type="getPriorityTag(row.requirement.priority)"
                size="small"
              >
                {{ getPriorityText(row.requirement.priority) }}
              </el-tag>
            </div>
            <div class="cell-actions">
              <el-button
                text
                type="primary"
                size="small"
                @click.stop="handleCreateTask(row.requirement)"
              >
                + 创建任务
              </el-button>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 待办列 -->
      <el-table-column
        label="待办"
        min-width="220px"
        class-name="task-column"
      >
        <template #header>
          <div class="column-header-cell">
            <span>待办 ({{ tableData.totalTodoTasks ?? 0 }})</span>
          </div>
        </template>
        <template #default="{ row }">
          <div
            class="task-list-cell"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDrop($event, row.requirement.id, 'TODO')"
          >
            <div
              v-for="task in (row.todoTasks || [])"
              :key="task.id"
              class="task-item"
              draggable="true"
              @dragstart="handleDragStart(task, $event)"
              @dragend="handleDragEnd"
              @click="handleViewTask(task)"
              @contextmenu.prevent="handleContextMenu(task, $event)"
            >
              <span class="task-number">#{{ task.id }}</span>
              <span class="task-title">{{ task.title }}</span>
              <div class="task-meta">
                <el-avatar :size="18" v-if="task.assigneeName">
                  {{ task.assigneeName?.charAt(0) }}
                </el-avatar>
                <span v-else class="unassigned">未</span>
                <el-tag :type="getTaskTypeTag(task.type)" size="small">
                  {{ getTaskTypeText(task.type) }}
                </el-tag>
              </div>
              <!-- 快捷操作按钮 -->
              <div class="task-actions" @click.stop>
                <el-dropdown trigger="click" @command="(cmd) => handleTaskAction(cmd, task)">
                  <el-icon class="more-icon"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item command="copy">复制</el-dropdown-item>
                      <el-dropdown-item command="inProgress">移到进行中</el-dropdown-item>
                      <el-dropdown-item command="done">移到已完成</el-dropdown-item>
                      <el-dropdown-item divided command="delete">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
            <div v-if="(row.todoTasks || []).length === 0" class="empty-cell">
              <el-icon :size="16"><Plus /></el-icon>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 进行中列 -->
      <el-table-column
        label="进行中"
        min-width="220px"
        class-name="task-column"
      >
        <template #header>
          <div class="column-header-cell">
            <span>进行中 ({{ tableData.totalInProgressTasks ?? 0 }})</span>
          </div>
        </template>
        <template #default="{ row }">
          <div
            class="task-list-cell"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDrop($event, row.requirement.id, 'IN_PROGRESS')"
          >
            <div
              v-for="task in (row.inProgressTasks || [])"
              :key="task.id"
              class="task-item"
              draggable="true"
              @dragstart="handleDragStart(task, $event)"
              @click="handleViewTask(task)"
            >
              <span class="task-number">#{{ task.id }}</span>
              <span class="task-title">{{ task.title }}</span>
              <div class="task-meta">
                <el-avatar :size="18" v-if="task.assigneeName">
                  {{ task.assigneeName?.charAt(0) }}
                </el-avatar>
                <span v-else class="unassigned">未</span>
                <el-tag :type="getTaskTypeTag(task.type)" size="small">
                  {{ getTaskTypeText(task.type) }}
                </el-tag>
              </div>
            </div>
            <div v-if="(row.inProgressTasks || []).length === 0" class="empty-cell">
              <el-icon :size="16"><Plus /></el-icon>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 已完成列 -->
      <el-table-column
        label="已完成"
        min-width="220px"
        class-name="task-column"
      >
        <template #header>
          <div class="column-header-cell">
            <span>已完成 ({{ tableData.totalDoneTasks ?? 0 }})</span>
          </div>
        </template>
        <template #default="{ row }">
          <div
            class="task-list-cell"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDrop($event, row.requirement.id, 'DONE')"
          >
            <div
              v-for="task in (row.doneTasks || [])"
              :key="task.id"
              class="task-item"
              draggable="true"
              @dragstart="handleDragStart(task, $event)"
              @click="handleViewTask(task)"
            >
              <span class="task-number">#{{ task.id }}</span>
              <span class="task-title">{{ task.title }}</span>
              <div class="task-meta">
                <el-avatar :size="18" v-if="task.assigneeName">
                  {{ task.assigneeName?.charAt(0) }}
                </el-avatar>
                <span v-else class="unassigned">未</span>
                <el-tag :type="getTaskTypeTag(task.type)" size="small">
                  {{ getTaskTypeText(task.type) }}
                </el-tag>
              </div>
            </div>
            <div v-if="(row.doneTasks || []).length === 0" class="empty-cell">
              <el-icon :size="16"><Plus /></el-icon>
            </div>
          </div>
        </template>
      </el-table-column>

      <!-- 关闭列 -->
      <el-table-column
        label="关闭"
        min-width="220px"
        class-name="task-column"
      >
        <template #header>
          <div class="column-header-cell">
            <span>关闭 ({{ tableData.totalClosedTasks ?? 0 }})</span>
          </div>
        </template>
        <template #default="{ row }">
          <div
            class="task-list-cell"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDrop($event, row.requirement.id, 'CLOSED')"
          >
            <div
              v-for="task in (row.closedTasks || [])"
              :key="task.id"
              class="task-item task-closed"
              @click="handleViewTask(task)"
            >
              <span class="task-number">#{{ task.id }}</span>
              <span class="task-title">{{ task.title }}</span>
              <div class="task-meta">
                <el-avatar :size="18" v-if="task.assigneeName">
                  {{ task.assigneeName?.charAt(0) }}
                </el-avatar>
                <span v-else class="unassigned">未</span>
                <el-tag :type="getTaskTypeTag(task.type)" size="small">
                  {{ getTaskTypeText(task.type) }}
                </el-tag>
              </div>
            </div>
            <div v-if="(row.closedTasks || []).length === 0" class="empty-cell">
              <el-icon :size="16"><Plus /></el-icon>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="taskDetailVisible"
      :title="`#${selectedTask?.id} ${selectedTask?.title}`"
      width="800px"
    >
      <div v-if="selectedTask" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务编号">{{ selectedTask.id }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag :type="getTaskTypeTag(selectedTask.type)" size="small">
              {{ getTaskTypeText(selectedTask.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusTag(selectedTask.status)" size="small">
              {{ getStatusText(selectedTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityTag(selectedTask.priority)" size="small">
              {{ getPriorityText(selectedTask.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">
            {{ selectedTask.assigneeName || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="预估工时">
            {{ selectedTask.estimateHours || '-' }} 小时
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ selectedTask.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="2">
            {{ selectedTask.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 创建任务对话框 -->
    <el-dialog
      v-model="createTaskVisible"
      title="创建任务"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="newTaskForm" label-width="100px">
        <el-form-item label="所属需求">
          <el-input :value="`#${selectedRequirement?.id} ${selectedRequirement?.title}`" disabled />
        </el-form-item>
        <el-form-item label="任务标题" required>
          <el-input v-model="newTaskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input
            v-model="newTaskForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="newTaskForm.assigneeId" placeholder="选择负责人" clearable style="width: 100%">
            <el-option
              v-for="member in members"
              :key="member.userId"
              :label="member.nickname"
              :value="member.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="newTaskForm.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预估工时">
          <el-input-number
            v-model="newTaskForm.estimateHours"
            :min="0"
            :max="999"
            :precision="1"
            placeholder="请输入预估工时"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999">小时</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createTaskVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTask" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import {
  getKanbanTableData,
  createTask,
  updateTaskStatus,
  deleteTask,
  type TaskInfo,
  type RequirementStatsResp,
  type KanbanTableData,
  type TaskCreateReq
} from '@/api/task'
import { getProjectMembers, type ProjectMember } from '@/api/project'

const router = useRouter()

// Props
const props = defineProps<{
  projectId: number
  iterationId?: number
}>()

// 数据定义
const tableData = ref<KanbanTableData>({
  rows: [],
  totalTodoTasks: 0,
  totalInProgressTasks: 0,
  totalTestingTasks: 0,
  totalDoneTasks: 0,
  totalReopenedTasks: 0,
  totalClosedTasks: 0
})

const members = ref<ProjectMember[]>([])
const filterAssigneeId = ref<number | undefined>(undefined)
const filterPriority = ref<string | undefined>(undefined)
const loading = ref(false)

// 表格高度
const tableHeight = computed(() => {
  return 'calc(100vh - 300px)'
})

// 对话框
const taskDetailVisible = ref(false)
const selectedTask = ref<TaskInfo | null>(null)
const createTaskVisible = ref(false)
const selectedRequirement = ref<RequirementStatsResp | null>(null)
const submitLoading = ref(false)
const newTaskForm = reactive({
  title: '',
  description: '',
  assigneeId: undefined as number | undefined,
  priority: 'MEDIUM',
  estimateHours: undefined as number | undefined
})

// 拖拽状态
const draggedTask = ref<TaskInfo | null>(null)

// 获取数据
const fetchKanbanData = async () => {
  loading.value = true
  try {
    // 如果没有指定 iterationId，显示提示信息
    if (!props.iterationId) {
      tableData.value = {
        rows: [],
        totalTodoTasks: 0,
        totalInProgressTasks: 0,
        totalTestingTasks: 0,
        totalDoneTasks: 0,
        totalReopenedTasks: 0,
        totalClosedTasks: 0
      }
      loading.value = false
      return
    }

    const data = await getKanbanTableData({
      projectId: props.projectId,
      iterationId: props.iterationId,
      assigneeId: filterAssigneeId.value,
      priority: filterPriority.value
    })
    tableData.value = data
  } catch (error) {
    console.error('获取看板数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const fetchMembers = async () => {
  try {
    const res = await getProjectMembers(props.projectId)
    members.value = res || []
  } catch (error) {
    console.error('获取成员列表失败:', error)
  }
}

// 筛选变更
const handleFilterChange = () => {
  fetchKanbanData()
}

// 查看需求详情
const handleViewRequirement = (requirement: RequirementStatsResp) => {
  router.push(`/projects/${props.projectId}/work-items/${requirement.id}`)
}

// 查看任务详情
const handleViewTask = (task: TaskInfo) => {
  router.push(`/projects/${props.projectId}/work-items/${task.id}`)
}

// 创建任务
const handleCreateTask = (requirement: RequirementStatsResp) => {
  selectedRequirement.value = requirement
  Object.assign(newTaskForm, {
    title: '',
    description: '',
    assigneeId: requirement.assigneeId,
    priority: requirement.priority,
    estimateHours: undefined
  })
  createTaskVisible.value = true
}

const handleSubmitTask = async () => {
  if (!newTaskForm.title) {
    ElMessage.warning('请输入任务标题')
    return
  }

  submitLoading.value = true
  try {
    await createTask({
      projectId: props.projectId,
      iterationId: props.iterationId || undefined,
      parentId: selectedRequirement.value?.id,
      title: newTaskForm.title,
      description: newTaskForm.description,
      type: 'TASK',
      priority: newTaskForm.priority as any,
      assigneeId: newTaskForm.assigneeId,
      status: 'TODO',
      estimateHours: newTaskForm.estimateHours
    })
    ElMessage.success('任务创建成功')
    createTaskVisible.value = false
    fetchKanbanData()
  } catch (error) {
    console.error('创建任务失败:', error)
    ElMessage.error('创建任务失败')
  } finally {
    submitLoading.value = false
  }
}

// 拖拽处理
const handleDragStart = (task: TaskInfo, event: DragEvent) => {
  draggedTask.value = task
  if (event.dataTransfer) {
    event.dataTransfer.setData('text/plain', task.id.toString())
    event.dataTransfer.effectAllowed = 'move'
  }
  ;(event.target as HTMLElement).classList.add('dragging')
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  ;(event.currentTarget as HTMLElement).classList.add('drag-over')
}

const handleDragLeave = (event: DragEvent) => {
  ;(event.currentTarget as HTMLElement).classList.remove('drag-over')
}

const handleDrop = async (event: DragEvent, requirementId: number, status: string) => {
  event.preventDefault()
  const cell = event.currentTarget as HTMLElement
  cell.classList.remove('drag-over')

  // 移除拖拽样式
  const draggingElements = document.querySelectorAll('.dragging')
  draggingElements.forEach(el => el.classList.remove('dragging'))

  if (draggedTask.value && draggedTask.value.status !== status) {
    try {
      await updateTaskStatus({
        id: draggedTask.value.id,
        status
      })
      ElMessage.success('状态已更新')
      fetchKanbanData()
    } catch (error) {
      console.error('状态更新失败:', error)
      ElMessage.error('状态更新失败')
    }
  }

  draggedTask.value = null
}

// 拖拽结束处理
const handleDragEnd = (event: DragEvent) => {
  const target = event.target as HTMLElement
  target.classList.remove('dragging')
}

// 右键菜单处理
const handleContextMenu = (task: TaskInfo, event: MouseEvent) => {
  // TODO: 实现自定义右键菜单
  console.log('Context menu for task:', task.id)
}

// 任务操作处理
const handleTaskAction = async (command: string, task: TaskInfo) => {
  switch (command) {
    case 'edit':
      router.push(`/projects/${props.projectId}/work-items/${task.id}`)
      break
    case 'copy':
      // TODO: 实现复制任务功能
      ElMessage.info('复制功能开发中')
      break
    case 'inProgress':
      try {
        await updateTaskStatus({ id: task.id, status: 'IN_PROGRESS' })
        ElMessage.success('已移到进行中')
        fetchKanbanData()
      } catch (error) {
        ElMessage.error('操作失败')
      }
      break
    case 'done':
      try {
        await updateTaskStatus({ id: task.id, status: 'DONE' })
        ElMessage.success('已移到已完成')
        fetchKanbanData()
      } catch (error) {
        ElMessage.error('操作失败')
      }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除这个任务吗？', '确认删除', {
          type: 'warning'
        })
        await deleteTask(task.id)
        ElMessage.success('删除成功')
        fetchKanbanData()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
      break
  }
}

// 工具函数
const getTaskTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    'REQUIREMENT': 'success',
    'TASK': 'primary',
    'BUG': 'danger'
  }
  return tags[type] || 'info'
}

const getTaskTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'REQUIREMENT': '需求',
    'TASK': '任务',
    'BUG': '缺陷'
  }
  return texts[type] || '未知'
}

const getPriorityTag = (priority: string) => {
  const tags: Record<string, string> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning'
  }
  return tags[priority] || 'info'
}

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  }
  return texts[priority] || '未知'
}

const getStatusTag = (status: string) => {
  const tags: Record<string, string> = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'DONE': 'success',
    'CLOSED': '',
    'TESTING': 'warning',
    'REOPENED': 'danger'
  }
  return tags[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'TODO': '待办',
    'IN_PROGRESS': '进行中',
    'DONE': '已完成',
    'CLOSED': '已关闭',
    'TESTING': '待验证',
    'REOPENED': '重新打开'
  }
  return texts[status] || '未知'
}

// 监听 iterationId 变化
watch(() => props.iterationId, () => {
  fetchKanbanData()
})

// 生命周期
onMounted(() => {
  fetchKanbanData()
  fetchMembers()
})
</script>

<style scoped>
.kanban-table-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.view-title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.toolbar-filters {
  display: flex;
  gap: 12px;
}

.member-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-container,
.empty-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

/* 表格样式 */
.kanban-table {
  flex: 1;
  border: 1px solid #e8e8e8;
  background: #f5f5f5;
}

.kanban-table :deep(.el-table__header-wrapper) {
  background: #fafafa;
}

.kanban-table :deep(.el-table__header th) {
  background: #fafafa;
  color: #333;
  font-weight: 500;
}

.kanban-table :deep(.el-table__body-wrapper) {
  overflow-y: auto;
  background: #f5f5f5;
}

.kanban-table :deep(.el-table__body tr) {
  background: #fff;
}

.kanban-table :deep(.el-table__body tr:hover > td) {
  background: #fafafa !important;
}

.kanban-table :deep(.el-table__body td) {
  background: #fff;
}

/* 列头样式 */
.column-header-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

/* 需求单元格 */
.requirement-cell {
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.requirement-cell:hover {
  background: #f5f5f5;
}

.cell-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.item-number {
  color: #1890ff;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 12px;
  font-weight: 500;
}

.cell-title {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 10px;
  font-weight: 500;
}

.cell-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.assignee-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.unassigned {
  font-size: 12px;
  color: #999;
}

.cell-actions {
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

/* 任务列表单元格 */
.task-list-cell {
  min-height: 80px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: all 0.2s;
}

.task-list-cell.drag-over {
  background: #e6f7ff;
  box-shadow: inset 0 0 0 2px #1890ff;
}

.task-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.task-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.1);
}

.task-item:hover .task-actions {
  opacity: 1;
}

.task-item.dragging {
  opacity: 0.5;
  cursor: move;
  transform: scale(1.05);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.task-actions {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0;
  transition: opacity 0.2s;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 2px;
}

.more-icon {
  cursor: pointer;
  color: #666;
  font-size: 16px;
}

.more-icon:hover {
  color: #1890ff;
}

.task-item.task-closed {
  opacity: 0.6;
  cursor: pointer;
}

.task-item.task-closed:hover {
  border-color: #d9d9d9;
  box-shadow: none;
}

.task-number {
  color: #1890ff;
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;
}

.task-title {
  flex: 1;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.empty-cell {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d9d9d9;
  border: 1px dashed #e8e8e8;
  border-radius: 4px;
  min-height: 60px;
}


/* 去除 el-table 默认的单元格 padding */
:deep(.task-column .cell) {
  padding: 8px;
}

/* 需求列保持正常 padding */
:deep(.requirement-column .cell) {
  padding: 12px 16px;
}
</style>
