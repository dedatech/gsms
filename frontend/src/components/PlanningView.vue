<template>
  <div class="planning-view">
    <!-- 左右两栏布局 -->
    <div class="planning-container">
      <!-- 左侧：迭代区域 -->
      <div class="iteration-panel">
        <div class="panel-header">
          <div class="header-left-section">
            <h3 class="panel-title">
              迭代规划
              <el-tag size="small" effect="plain" class="iteration-count-tag">
                {{ activeIterations.length }}
              </el-tag>
            </h3>
          </div>
          <div class="header-right-section">
            <el-link type="primary" @click="handleCreateIteration">
              <el-icon><Plus /></el-icon>
              新建迭代
            </el-link>
          </div>
        </div>

        <div class="iteration-list" v-loading="loading">
          <div
            v-for="iteration in activeIterations"
            :key="iteration.id"
            class="iteration-item"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDropToIteration($event, iteration.id)"
          >
            <!-- 迭代头部 -->
            <div class="iteration-header">
              <div class="header-main" @click="toggleIterationExpansion(iteration.id)">
                <div class="header-left">
                  <el-icon :class="{ 'expanded': expandedIterationIds.has(iteration.id) }">
                    <ArrowRight />
                  </el-icon>
                  <span class="iteration-number">#{{ iteration.id }}</span>
                  <span class="iteration-name">{{ iteration.name }}</span>
                  <el-tag size="small" type="info" effect="plain">
                    {{ getIterationRequirementCount(iteration.id) }}
                  </el-tag>
                </div>
                <div class="header-center">
                  <span
                    class="time-display"
                    @click.stop="handleEditTime(iteration)"
                  >
                    {{ getTimeRangeText(iteration) }}
                  </span>
                  <el-button
                    :type="getIterationButtonType(iteration)"
                    :disabled="!canStartIteration(iteration)"
                    size="small"
                    @click.stop="handleStartOrCompleteIteration(iteration)"
                  >
                    {{ getIterationButtonText(iteration) }}
                  </el-button>
                </div>
              </div>
              <div class="header-right">
                <el-dropdown trigger="click" @command="(cmd) => handleIterationCommand(cmd, iteration)">
                  <el-button link type="primary" size="small">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">
                        <el-icon><Edit /></el-icon>
                        编辑迭代
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>
                        <el-icon><Delete /></el-icon>
                        删除迭代
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <!-- 迭代下的需求列表 -->
            <div v-if="expandedIterationIds.has(iteration.id)" class="iteration-requirements">
              <div
                v-for="req in getIterationRequirements(iteration.id)"
                :key="req.id"
                class="requirement-item"
                :class="{ selected: selectedTaskIds.has(req.id) }"
                draggable="true"
                @dragstart="handleDragStart(req, $event)"
                @dragend="handleDragEnd"
                @click="handleViewRequirement(req)"
              >
                <div class="req-left">
                  <el-checkbox
                    :model-value="selectedTaskIds.has(req.id)"
                    @change="handleSelectTask(req.id, $event)"
                    @click.stop
                  />
                  <span class="req-id">#{{ req.id }}</span>
                  <span class="req-title">{{ req.title }}</span>
                </div>
                <div class="req-right">
                  <el-tag :type="getPriorityType(req.priority)" size="small" effect="plain">
                    {{ getPriorityText(req.priority) }}
                  </el-tag>
                  <span class="req-assignee">{{ req.assigneeName || '未分配' }}</span>
                  <el-tag :type="getStatusType(req.status)" size="small" effect="plain">
                    {{ getStatusText(req.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 快速添加需求 -->
              <div class="quick-add-section">
                <div v-if="editingIterationId === iteration.id" class="quick-add-input">
                  <el-input
                    v-model="newTaskTitle"
                    placeholder="输入需求名称，回车创建"
                    size="small"
                    @keyup.enter="handleQuickAddTask(iteration.id)"
                    @blur="handleCancelQuickAdd"
                    :ref="(el) => setQuickAddInput(el, iteration.id)"
                  />
                </div>
                <div v-else class="quick-add-trigger" @click="handleShowQuickAdd(iteration.id)">
                  <el-icon><Plus /></el-icon>
                  <span>添加需求</span>
                </div>
              </div>

              <!-- 空状态拖拽提示区域 -->
              <div
                v-if="getIterationRequirements(iteration.id).length === 0 && editingIterationId !== iteration.id"
                class="empty-drag-zone"
                :class="{ 'drag-over': isDraggingOverIteration(iteration.id) }"
                @dragover.prevent="handleDragOver($event, iteration.id)"
                @dragleave="handleDragLeave"
                @drop="handleDropToIteration($event, iteration.id)"
              >
                <el-icon class="drag-icon"><Plus /></el-icon>
                <p class="drag-text">拖拽需求到此处</p>
                <p class="drag-hint">或点击上方"添加需求"创建</p>
              </div>
            </div>
          </div>

          <el-empty
            v-if="activeIterations.length === 0 && !loading"
            description="暂无迭代"
            :image-size="80"
          />
        </div>
      </div>

      <!-- 右侧：未规划区域 -->
      <div class="unplanned-panel">
        <div class="panel-header">
          <h3 class="panel-title">未规划需求</h3>
        </div>

        <div class="unplanned-list" v-loading="loading">
          <!-- 未规划需求列表 -->
          <div
            class="iteration-item"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDropToUnplanned"
          >
            <div class="iteration-requirements">
              <div
                v-for="req in unplannedRequirements"
                :key="req.id"
                class="requirement-item"
                :class="{ selected: selectedTaskIds.has(req.id) }"
                draggable="true"
                @dragstart="handleDragStart(req, $event)"
                @dragend="handleDragEnd"
                @click="handleViewRequirement(req)"
              >
                <div class="req-left">
                  <el-checkbox
                    :model-value="selectedTaskIds.has(req.id)"
                    @change="handleSelectTask(req.id, $event)"
                    @click.stop
                  />
                  <span class="req-id">#{{ req.id }}</span>
                  <span class="req-title">{{ req.title }}</span>
                </div>
                <div class="req-right">
                  <el-tag :type="getPriorityType(req.priority)" size="small" effect="plain">
                    {{ getPriorityText(req.priority) }}
                  </el-tag>
                  <span class="req-assignee">{{ req.assigneeName || '未分配' }}</span>
                  <el-tag :type="getStatusType(req.status)" size="small" effect="plain">
                    {{ getStatusText(req.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 快速添加需求 -->
              <div class="quick-add-section">
                <div v-if="editingUnplanned" class="quick-add-input">
                  <el-input
                    v-model="newTaskTitle"
                    placeholder="输入需求名称，回车创建"
                    size="small"
                    @keyup.enter="handleQuickAddUnplanned"
                    @blur="handleCancelQuickAddUnplanned"
                    ref="unplannedInputRef"
                  />
                </div>
                <div v-else class="quick-add-trigger" @click="handleShowQuickAddUnplanned">
                  <el-icon><Plus /></el-icon>
                  <span>添加需求</span>
                </div>
              </div>

              <el-empty
                v-if="unplannedRequirements.length === 0 && !editingUnplanned"
                description="暂无需求"
                :image-size="60"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部批量操作栏 -->
    <div v-if="selectedTaskIds.size > 0" class="batch-action-bar">
      <div class="action-content">
        <span class="selected-count">已选择 {{ selectedTaskIds.size }} 项</span>
        <div class="action-buttons">
          <el-button link type="primary" @click="handleMoveToIteration">移动到迭代</el-button>
          <el-divider direction="vertical" />
          <el-button link type="primary" @click="handleMoveToUnplanned">移动到未规划</el-button>
        </div>
        <el-button link @click="handleClearSelection">取消选择</el-button>
      </div>
    </div>

    <!-- 需求详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="`需求详情 #${currentRequirement?.id}`"
      width="800px"
      @close="handleCloseDetail"
    >
      <div v-if="currentRequirement" class="requirement-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需求编号">
            #{{ currentRequirement.id }}
          </el-descriptions-item>
          <el-descriptions-item label="需求类型">
            <el-tag v-if="currentRequirement.type === 'REQUIREMENT'" size="small" type="warning">
              需求
            </el-tag>
            <el-tag v-else size="small" type="primary">
              任务
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">
            {{ currentRequirement.title }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentRequirement.description || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentRequirement.status)" size="small">
              {{ getStatusText(currentRequirement.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentRequirement.priority)" size="small">
              {{ getPriorityText(currentRequirement.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">
            {{ currentRequirement.assigneeName || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="预估工时">
            {{ currentRequirement.estimateHours || 0 }}h
          </el-descriptions-item>
          <el-descriptions-item label="迭代">
            {{ getIterationName(currentRequirement.iterationId) || '未规划' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentRequirement.createTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 迭代时间编辑弹窗 -->
    <el-dialog
      v-model="timeEditVisible"
      title="设置迭代时间"
      width="500px"
    >
      <el-form v-if="editingIteration" label-width="100px">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="timeEditForm.planStartDate"
            type="date"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="timeEditForm.planEndDate"
            type="date"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="timeEditVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTime" :loading="timeSaving">确定</el-button>
      </template>
    </el-dialog>

    <!-- 选择迭代弹窗 -->
    <el-dialog
      v-model="iterationSelectVisible"
      title="选择目标迭代"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="目标迭代">
          <el-select
            v-model="targetIterationId"
            placeholder="请选择要移动到的迭代"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="iter in availableIterations"
              :key="iter.id"
              :label="`#${iter.id} ${iter.name}`"
              :value="iter.id"
            >
              <span style="float: left">#{{ iter.id }} {{ iter.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
                {{ getIterationRequirementCount(iter.id) }} 个需求
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancelMoveToIteration">取消</el-button>
        <el-button type="primary" @click="handleConfirmMoveToIteration">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  ArrowRight,
  Edit,
  Delete,
  MoreFilled
} from '@element-plus/icons-vue'
import { getTasksByProjectId, createTask, type TaskInfo } from '@/api/task'
import { getIterationList, updateIteration, type IterationInfo } from '@/api/iteration'
import { updateTask, updateTaskIterationId } from '@/api/task'

// Props
const props = defineProps<{
  projectId: number
}>()

// Emits
const emit = defineEmits<{
  createIteration: []
  editIteration: [iteration: IterationInfo]
  deleteIteration: [iteration: IterationInfo]
}>()

// 数据
const iterations = ref<IterationInfo[]>([])
const allRequirements = ref<TaskInfo[]>([])
const loading = ref(false)

// 展开状态
const expandedIterationIds = ref<Set<number>>(new Set())
const isUnplannedExpanded = ref(true)

// 拖拽状态
const draggedRequirement = ref<TaskInfo | null>(null)
const draggingOverIterationId = ref<number | null>(null)

// 选中状态
const selectedTaskIds = ref<Set<number>>(new Set())

// 快速添加状态
const editingIterationId = ref<number | null>(null)
const editingUnplanned = ref(false)
const newTaskTitle = ref('')
const quickAddInputMap = new Map<number, InstanceType<typeof import('element-plus')['ElInput']>>()
const unplannedInputRef = ref<InstanceType<typeof import('element-plus')['ElInput']>>()

const setQuickAddInput = (el: any, iterationId: number) => {
  if (el) {
    quickAddInputMap.set(iterationId, el)
  }
}

// 详情弹窗
const detailDialogVisible = ref(false)
const currentRequirement = ref<TaskInfo | null>(null)

// 时间编辑弹窗
const timeEditVisible = ref(false)
const editingIteration = ref<IterationInfo | null>(null)
const timeEditForm = ref({
  planStartDate: '',
  planEndDate: ''
})
const timeSaving = ref(false)

// 计算属性：只显示未完成的迭代
const activeIterations = computed(() => {
  return iterations.value.filter(iter => iter.status !== 'COMPLETED')
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 获取迭代列表（使用大 pageSize 获取所有迭代）
    const iterRes = await getIterationList({ projectId: props.projectId, pageNum: 1, pageSize: 100 })
    iterations.value = iterRes.list || []

    // 获取所有需求
    const reqRes = await getTasksByProjectId(props.projectId, 1, 10000)
    allRequirements.value = reqRes.list || []

    // 默认展开所有迭代
    initExpandedIterations()
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化展开状态
const initExpandedIterations = () => {
  const keys = new Set<number>()
  iterations.value.forEach(iter => {
    if (iter.status !== 'COMPLETED') {
      keys.add(iter.id)
    }
  })
  expandedIterationIds.value = keys
}

// 未规划的需求（没有 iterationId 或 iterationId 为 null）
const unplannedRequirements = computed(() => {
  return allRequirements.value.filter(req => !req.iterationId)
})

// 获取迭代下的需求
const getIterationRequirements = (iterationId: number) => {
  return allRequirements.value.filter(req => req.iterationId === iterationId)
}

// 获取迭代需求数量
const getIterationRequirementCount = (iterationId: number) => {
  return getIterationRequirements(iterationId).length
}

// 切换迭代展开状态
const toggleIterationExpansion = (iterationId: number) => {
  if (expandedIterationIds.value.has(iterationId)) {
    expandedIterationIds.value.delete(iterationId)
  } else {
    expandedIterationIds.value.add(iterationId)
  }
  expandedIterationIds.value = new Set(expandedIterationIds.value)
}

// 切换未规划区域展开状态
const toggleUnplannedExpansion = () => {
  isUnplannedExpanded.value = !isUnplannedExpanded.value
}

// 显示未规划快速添加输入框
const handleShowQuickAddUnplanned = async () => {
  editingUnplanned.value = true
  newTaskTitle.value = ''

  await nextTick()
  setTimeout(() => {
    if (unplannedInputRef.value?.focus) {
      unplannedInputRef.value.focus()
    }
  }, 100)
}

// 取消未规划快速添加
const handleCancelQuickAddUnplanned = () => {
  editingUnplanned.value = false
  newTaskTitle.value = ''
}

// 快速添加未规划需求
const handleQuickAddUnplanned = async () => {
  const title = newTaskTitle.value.trim()
  if (!title) {
    handleCancelQuickAddUnplanned()
    return
  }

  // 1. 乐观更新：立即在前端添加"待创建"状态的需求
  const tempId = Date.now()
  const tempRequirement: TaskInfo = {
    id: tempId,
    title: title,
    status: 'TODO',
    priority: 'MEDIUM',
    projectId: props.projectId,
    iterationId: undefined,
    type: 'REQUIREMENT',
    assigneeName: '创建中...'
  }
  allRequirements.value.push(tempRequirement)

  // 2. 清空输入，保持焦点
  newTaskTitle.value = ''
  await nextTick()

  if (unplannedInputRef.value?.focus) {
    unplannedInputRef.value.focus()
  }

  // 3. 后台异步创建
  try {
    const realData = await createTask({
      projectId: props.projectId,
      title: title,
      status: 'TODO',
      priority: 'MEDIUM',
      type: 'REQUIREMENT'
    })

    // 4. 创建成功：用真实数据替换临时数据
    const index = allRequirements.value.findIndex(r => r.id === tempId)
    if (index !== -1) {
      allRequirements.value[index] = realData
    }

    ElMessage.success('需求创建成功')
  } catch (error) {
    // 5. 创建失败：移除临时数据
    const index = allRequirements.value.findIndex(r => r.id === tempId)
    if (index !== -1) {
      allRequirements.value.splice(index, 1)
    }

    // 创建失败时，关闭输入框
    editingUnplanned.value = false
  }
}

// 获取迭代名称
const getIterationName = (iterationId?: number) => {
  if (!iterationId) return ''
  const iter = iterations.value.find(i => i.id === iterationId)
  return iter?.name || ''
}

// 获取时间范围文本
const getTimeRangeText = (iteration: IterationInfo) => {
  const start = iteration.planStartDate || '未设置'
  const end = iteration.planEndDate || '未设置'
  return `${start} - ${end}`
}

// 获取迭代按钮类型
const getIterationButtonType = (iteration: IterationInfo) => {
  // 不使用着色
  return ''
}

// 获取迭代按钮文本
const getIterationButtonText = (iteration: IterationInfo) => {
  if (iteration.status === 'IN_PROGRESS') {
    return '完成迭代'
  }
  return '开始迭代'
}

// 是否可以开始迭代
const canStartIteration = (iteration: IterationInfo) => {
  const taskCount = getIterationRequirementCount(iteration.id)
  return taskCount > 0 && iteration.status === 'NOT_STARTED'
}

// 状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'TODO': '待办',
    'IN_PROGRESS': '进行中',
    'DONE': '已完成'
  }
  return map[status] || status
}

// 状态类型
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    'TODO': 'info',
    'IN_PROGRESS': 'warning',
    'DONE': 'success'
  }
  return map[status] || 'info'
}

// 优先级文本
const getPriorityText = (priority: string) => {
  const map: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  }
  return map[priority] || priority
}

// 优先级类型
const getPriorityType = (priority: string) => {
  const map: Record<string, any> = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return map[priority] || 'info'
}

// 选择任务
const handleSelectTask = (taskId: number, checked: boolean) => {
  if (checked) {
    selectedTaskIds.value.add(taskId)
  } else {
    selectedTaskIds.value.delete(taskId)
  }
  selectedTaskIds.value = new Set(selectedTaskIds.value)
}

// 清除选择
const handleClearSelection = () => {
  selectedTaskIds.value.clear()
}

// 移动到未规划
const handleMoveToUnplanned = async () => {
  if (selectedTaskIds.value.size === 0) return

  try {
    for (const taskId of selectedTaskIds.value) {
      await updateTaskIterationId(taskId, undefined)
    }
    ElMessage.success(`已移动 ${selectedTaskIds.value.size} 个需求到未规划`)
    selectedTaskIds.value.clear()
    await fetchData()
  } catch (error) {
    console.error('移动失败:', error)
    ElMessage.error('移动失败')
  }
}

// 迭代选择弹窗
const iterationSelectVisible = ref(false)
const targetIterationId = ref<number | undefined>(undefined)

// 移动到迭代
const handleMoveToIteration = () => {
  if (selectedTaskIds.value.size === 0) return

  // 获取选中需求所在的迭代ID（排除当前迭代）
  const selectedReqs = allRequirements.value.filter(req => selectedTaskIds.value.has(req.id))
  const currentIterationIds = new Set(selectedReqs.map(req => req.iterationId).filter(Boolean) as number[])

  // 如果所有选中的需求都在同一个迭代中，排除该迭代
  if (currentIterationIds.size === 1) {
    const singleIterationId = Array.from(currentIterationIds)[0]
    targetIterationId.value = undefined
    iterationSelectVisible.value = true
  } else {
    // 选中需求来自不同迭代或未规划，可以选择任何迭代
    targetIterationId.value = undefined
    iterationSelectVisible.value = true
  }
}

// 确认移动到迭代
const handleConfirmMoveToIteration = async () => {
  if (!targetIterationId.value) {
    ElMessage.warning('请选择目标迭代')
    return
  }

  try {
    for (const taskId of selectedTaskIds.value) {
      await updateTaskIterationId(taskId, targetIterationId.value)
    }

    ElMessage.success(`已移动 ${selectedTaskIds.value.size} 个需求到迭代`)
    iterationSelectVisible.value = false
    targetIterationId.value = undefined
    selectedTaskIds.value.clear()
    await fetchData()
  } catch (error) {
    console.error('移动失败:', error)
    ElMessage.error('移动失败')
  }
}

// 取消移动
const handleCancelMoveToIteration = () => {
  iterationSelectVisible.value = false
  targetIterationId.value = undefined
}

// 获取可选择的迭代列表（排除当前迭代）
const availableIterations = computed(() => {
  if (selectedTaskIds.value.size === 0) return activeIterations.value

  // 获取选中需求所在的迭代ID
  const selectedReqs = allRequirements.value.filter(req => selectedTaskIds.value.has(req.id))
  const currentIterationIds = new Set(selectedReqs.map(req => req.iterationId).filter(Boolean) as number[])

  // 如果所有选中需求都在同一个迭代中，排除该迭代
  if (currentIterationIds.size === 1) {
    const singleIterationId = Array.from(currentIterationIds)[0]
    return activeIterations.value.filter(iter => iter.id !== singleIterationId)
  }

  // 否则可以选择任何迭代
  return activeIterations.value
})

// 拖拽开始
const handleDragStart = (req: TaskInfo, event: DragEvent) => {
  draggedRequirement.value = req
  if (event.dataTransfer) {
    event.dataTransfer.setData('text/plain', req.id.toString())
    event.dataTransfer.effectAllowed = 'move'
  }
  ;(event.target as HTMLElement).classList.add('dragging')
}

// 拖拽结束
const handleDragEnd = (event: DragEvent) => {
  ;(event.target as HTMLElement).classList.remove('dragging')
  draggedRequirement.value = null
  draggingOverIterationId.value = null

  // 移除所有拖拽样式
  const dragOverElements = document.querySelectorAll('.drag-over')
  dragOverElements.forEach(el => {
    el.classList.remove('drag-over')
  })
}

// 拖拽经过目标区域
const handleDragOver = (event: DragEvent, iterationId?: number) => {
  event.preventDefault()
  ;(event.currentTarget as HTMLElement).classList.add('drag-over')
  if (iterationId) {
    draggingOverIterationId.value = iterationId
  }
}

// 拖拽离开目标区域
const handleDragLeave = (event: DragEvent) => {
  ;(event.currentTarget as HTMLElement).classList.remove('drag-over')
  draggingOverIterationId.value = null
}

// 判断是否正在拖拽到某个迭代
const isDraggingOverIteration = (iterationId: number) => {
  return draggingOverIterationId.value === iterationId
}

// 拖拽放置到迭代
const handleDropToIteration = async (event: DragEvent, iterationId: number) => {
  event.preventDefault()

  // 移除拖拽样式
  const draggingElements = document.querySelectorAll('.dragging, .drag-over')
  draggingElements.forEach(el => {
    el.classList.remove('dragging')
    el.classList.remove('drag-over')
  })

  draggingOverIterationId.value = null

  if (!draggedRequirement.value) return

  // 如果需求已经在该迭代中，不需要移动
  if (draggedRequirement.value.iterationId === iterationId) {
    draggedRequirement.value = null
    return
  }

  try {
    // 更新需求的迭代ID
    await updateTaskIterationId(draggedRequirement.value.id, iterationId)

    ElMessage.success('需求已移动到迭代')
    draggedRequirement.value = null

    // 重新获取数据
    await fetchData()
  } catch (error) {
    console.error('移动需求失败:', error)
    ElMessage.error('移动需求失败')
  }
}

// 拖拽放置到未规划区域
const handleDropToUnplanned = async (event: DragEvent) => {
  event.preventDefault()

  // 移除拖拽样式
  const draggingElements = document.querySelectorAll('.dragging, .drag-over')
  draggingElements.forEach(el => {
    el.classList.remove('dragging')
    el.classList.remove('drag-over')
  })

  if (!draggedRequirement.value) return

  // 如果需求已经在未规划状态，不需要移动
  if (!draggedRequirement.value.iterationId) {
    draggedRequirement.value = null
    return
  }

  try {
    // 更新需求的迭代ID为null
    await updateTaskIterationId(draggedRequirement.value.id, undefined)

    ElMessage.success('需求已移动到未规划')
    draggedRequirement.value = null

    // 重新获取数据
    await fetchData()
  } catch (error) {
    console.error('移动需求失败:', error)
    ElMessage.error('移动需求失败')
  }
}

// 显示快速添加输入框
const handleShowQuickAdd = async (iterationId: number) => {
  editingIterationId.value = iterationId
  newTaskTitle.value = ''

  // 等待 DOM 更新后聚焦
  await nextTick()
  setTimeout(() => {
    const input = quickAddInputMap.get(iterationId)
    if (input?.focus) {
      input.focus()
    }
  }, 100)
}

// 取消快速添加
const handleCancelQuickAdd = () => {
  editingIterationId.value = null
  newTaskTitle.value = ''
}

// 快速添加需求
const handleQuickAddTask = async (iterationId: number) => {
  const title = newTaskTitle.value.trim()
  if (!title) {
    handleCancelQuickAdd()
    return
  }

  // 1. 乐观更新：立即在前端添加"待创建"状态的需求
  const tempId = Date.now()
  const tempRequirement: TaskInfo = {
    id: tempId,
    title: title,
    status: 'TODO',
    priority: 'MEDIUM',
    projectId: props.projectId,
    iterationId: iterationId,
    type: 'REQUIREMENT',
    assigneeName: '创建中...'
  }
  allRequirements.value.push(tempRequirement)

  // 2. 清空输入，保持焦点
  newTaskTitle.value = ''
  await nextTick()

  const input = quickAddInputMap.get(iterationId)
  if (input?.focus) {
    input.focus()
  }

  // 3. 后台异步创建
  try {
    const realData = await createTask({
      projectId: props.projectId,
      iterationId: iterationId,
      title: title,
      status: 'TODO',
      priority: 'MEDIUM',
      type: 'REQUIREMENT'
    })

    // 4. 创建成功：用真实数据替换临时数据
    const index = allRequirements.value.findIndex(r => r.id === tempId)
    if (index !== -1) {
      allRequirements.value[index] = realData
    }

    ElMessage.success('需求创建成功')
  } catch (error) {
    // 5. 创建失败：移除临时数据
    const index = allRequirements.value.findIndex(r => r.id === tempId)
    if (index !== -1) {
      allRequirements.value.splice(index, 1)
    }

    // 创建失败时，关闭输入框
    editingIterationId.value = null
  }
}

// 编辑迭代时间
const handleEditTime = (iteration: IterationInfo) => {
  editingIteration.value = iteration
  timeEditForm.value = {
    planStartDate: iteration.planStartDate || '',
    planEndDate: iteration.planEndDate || ''
  }
  timeEditVisible.value = true
}

// 保存时间
const handleSaveTime = async () => {
  if (!editingIteration.value) return

  timeSaving.value = true
  try {
    await updateIteration({
      id: editingIteration.value.id,
      planStartDate: timeEditForm.value.planStartDate || undefined,
      planEndDate: timeEditForm.value.planEndDate || undefined
    })

    ElMessage.success('时间设置成功')
    timeEditVisible.value = false
    await fetchData()
  } catch (error) {
    console.error('设置时间失败:', error)
    ElMessage.error('设置时间失败')
  } finally {
    timeSaving.value = false
  }
}

// 开始或完成迭代
const handleStartOrCompleteIteration = async (iteration: IterationInfo) => {
  const isStart = iteration.status === 'NOT_STARTED'
  const confirmMsg = isStart
    ? `确定要开始迭代"${iteration.name}"吗？`
    : `确定要完成迭代"${iteration.name}"吗？`

  try {
    await ElMessageBox.confirm(confirmMsg, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const newStatus = isStart ? 'IN_PROGRESS' : 'COMPLETED'
    await updateIteration({
      id: iteration.id,
      status: newStatus
    })

    ElMessage.success(isStart ? '迭代已开始' : '迭代已完成')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 新建迭代
const handleCreateIteration = () => {
  emit('createIteration')
}

// 事件处理
const handleIterationCommand = (command: string, iteration: IterationInfo) => {
  switch (command) {
    case 'edit':
      emit('editIteration', iteration)
      break
    case 'delete':
      emit('deleteIteration', iteration)
      break
  }
}

const handleViewRequirement = (req: TaskInfo) => {
  currentRequirement.value = req
  detailDialogVisible.value = true
}

const handleCloseDetail = () => {
  currentRequirement.value = null
  detailDialogVisible.value = false
}

// 刷新列表
const refresh = () => {
  fetchData()
}

// 组件挂载
onMounted(() => {
  fetchData()
})

// 暴露刷新方法
defineExpose({
  refresh
})
</script>

<style scoped>
.planning-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止外层滚动 */
}

.planning-container {
  flex: 1;
  display: flex;
  gap: 0;
  padding: 0;
  overflow: hidden; /* 防止外层滚动 */
  min-height: 0; /* 允许缩小 */
}

/* 左侧迭代面板 */
.iteration-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f9f9f9;
  overflow: hidden;
  border-right: 1px solid #e8e8e8;
  min-height: 0; /* 允许缩小 */
}

.iteration-panel .panel-header {
  flex-shrink: 0;
}

/* 右侧未规划面板 */
.unplanned-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f9f9f9;
  overflow: hidden;
  min-height: 0; /* 允许缩小 */
}

/* 面板头部 */
.panel-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 10;
}

.header-left-section {
  flex: 1;
}

.header-right-section {
  flex-shrink: 0;
}

.panel-title {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.iteration-count-tag {
  font-weight: 600;
}

/* 迭代列表 */
.iteration-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 12px 12px 12px;
  min-height: 0; /* 重要：允许 flex 子项缩小 */
}

.iteration-item {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.iteration-item:last-child {
  margin-bottom: 0;
}

.iteration-item.drag-over {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 迭代头部 */
.iteration-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.header-main {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  user-select: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.header-left:hover {
  opacity: 0.8;
}

.header-left .el-icon {
  transition: transform 0.3s;
  color: #909399;
}

.header-left .el-icon.expanded {
  transform: rotate(90deg);
}

.iteration-number {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  flex-shrink: 0;
}

.iteration-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.header-center {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.time-display {
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.time-display:hover {
  background: #f0f0f0;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
  margin-left: 8px;
}

/* 迭代下的需求列表 */
.iteration-requirements {
  padding: 0 12px 12px 12px;
}

/* 未规划列表 */
.unplanned-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 12px 12px 12px;
  transition: all 0.3s;
  min-height: 0; /* 重要：允许 flex 子项缩小 */
}

.unplanned-list.drag-over {
  background: #e6f7ff;
  border-radius: 8px;
}

/* 需求项 */
.requirement-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  margin-bottom: 0;
  cursor: pointer;
  transition: all 0.3s;
}

.requirement-item:hover {
  background: #f5f7fa;
}

.requirement-item.selected {
  background: #ecf5ff;
  border-bottom-color: #409eff;
}

.requirement-item.dragging {
  opacity: 0.5;
  cursor: move;
}

.req-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.req-id {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  flex-shrink: 0;
}

.req-title {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.req-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.req-assignee {
  font-size: 12px;
  color: #606266;
}

/* 空状态拖拽区域 */
.empty-drag-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  margin-top: 12px;
  border: 2px dashed #e8e8e8;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s;
  cursor: pointer;
}

.empty-drag-zone.drag-over {
  border-color: #409eff;
  background: #ecf5ff;
  transform: scale(1.02);
}

.empty-drag-zone .drag-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.empty-drag-zone.drag-over .drag-icon {
  color: #409eff;
  transform: scale(1.1);
}

.empty-drag-zone .drag-text {
  margin: 0;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.empty-drag-zone .drag-hint {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #909399;
}

/* 快速添加区域 */
.quick-add-section {
  margin-top: 8px;
}

.quick-add-trigger {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  color: #409eff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-add-trigger:hover {
  color: #66b1ff;
}

.quick-add-input {
  padding: 0 4px;
  margin-bottom: 0;
}

.quick-add-input :deep(.el-input__wrapper) {
  padding: 8px 12px;
  box-shadow: none;
  border-bottom: 1px solid #e8e8e8;
  border-radius: 0;
  background: #fff;
}

.quick-add-input :deep(.el-input__wrapper):focus {
  background: #fff;
  border-bottom-color: #409eff;
}

.quick-add-input :deep(.el-input__inner) {
  color: #333;
}

/* 底部批量操作栏 */
.batch-action-bar {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.action-content {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 10px 20px;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selected-count {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

/* 详情弹窗 */
.requirement-detail {
  padding: 20px 0;
}

/* 滚动条样式 - 完全隐藏但保持滚动功能 */
.iteration-list::-webkit-scrollbar,
.unplanned-list::-webkit-scrollbar {
  display: none; /* Webkit浏览器：完全隐藏滚动条 */
  width: 0;
  height: 0;
}

/* Firefox 滚动条 - 完全隐藏 */
.iteration-list,
.unplanned-list {
  scrollbar-width: none; /* Firefox：完全隐藏滚动条 */
}
</style>
