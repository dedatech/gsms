<template>
  <div class="requirements-view">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索需求..."
          :prefix-icon="Search"
          clearable
          style="width: 300px"
          @input="handleSearch"
        />
        <el-select
          v-model="statusFilter"
          placeholder="筛选状态"
          clearable
          style="width: 150px"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="待办" value="TODO" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已完成" value="DONE" />
        </el-select>
        <el-select
          v-model="priorityFilter"
          placeholder="筛选优先级"
          clearable
          style="width: 150px"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="高" value="HIGH" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="低" value="LOW" />
        </el-select>
        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          style="width: 150px"
          @change="handleSort"
        >
          <el-option label="默认排序" value="default" />
          <el-option label="按优先级" value="priority" />
          <el-option label="按创建时间" value="createTime" />
          <el-option label="按计划时间" value="planDate" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button
          v-if="selectedTasks.length > 0"
          type="primary"
          :icon="Check"
          @click="handleBatchComplete"
        >
          批量完成 ({{ selectedTasks.length }})
        </el-button>
        <el-button
          v-if="selectedTasks.length > 0"
          type="danger"
          :icon="Delete"
          @click="handleBatchDelete"
        >
          批量删除 ({{ selectedTasks.length }})
        </el-button>
        <span class="task-summary">{{ filteredTasks.length }} 个需求</span>
      </div>
    </div>

    <!-- 需求表格（父子结构表格） -->
    <div class="requirements-table" v-loading="loading">
      <el-table
        :data="flatTaskList"
        row-key="id"
        border
        stripe
        style="width: 100%"
        :flexible="true"
      >
        <!-- 任务编号 -->
        <el-table-column label="编号" width="100">
          <template #default="{ row }">
            <span class="task-id">#{{ row.id }}</span>
          </template>
        </el-table-column>

        <!-- 任务标题 -->
        <el-table-column label="标题" min-width="300">
          <template #default="{ row }">
            <div class="task-title-cell" :style="{ paddingLeft: (row.level || 0) * 24 + 'px' }">
              <!-- 展开/折叠图标 -->
              <span
                v-if="row.hasChildren"
                class="expand-toggle"
                @click.stop="toggleExpand(row)"
              >
                <el-icon :class="{ expanded: expandedKeys.has(row.id) }">
                  <ArrowRight />
                </el-icon>
              </span>
              <span v-else class="expand-placeholder"></span>

              <!-- 任务图标：根任务用文件夹，子任务用文档 -->
              <el-icon class="task-icon" :size="16">
                <component :is="row.level === 0 ? Folder : Document" />
              </el-icon>

              <!-- 任务类型标签 -->
              <el-tag v-if="row.type === 'REQUIREMENT'" size="small" type="warning" effect="plain" style="margin-right: 8px">
                需求
              </el-tag>
              <el-tag v-else-if="row.type === 'TASK'" size="small" type="primary" effect="plain" style="margin-right: 8px">
                任务
              </el-tag>
              <!-- 任务标题（可点击查看详情） -->
              <span class="title-link" @click="handleViewDetail(row)">{{ row.title }}</span>
              <!-- 子任务数量浮动标签 -->
              <el-tag
                v-if="row.subtaskCount > 0"
                size="small"
                type="info"
                effect="plain"
                class="subtask-badge"
              >
                {{ row.subtaskCount }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <!-- 优先级 -->
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small" effect="plain">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="plain">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 负责人 -->
        <el-table-column label="负责人" width="120">
          <template #default="{ row }">
            <span class="assignee">{{ row.assigneeName || '未分配' }}</span>
          </template>
        </el-table-column>

        <!-- 工时 -->
        <el-table-column label="工时" width="150">
          <template #default="{ row }">
            <span class="hours">
              {{ row.estimateHours || 0 }}h / {{ row.actualHours || 0 }}h
            </span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 任务详情弹窗 -->
      <el-dialog
        v-model="detailDialogVisible"
        :title="`工作项详情 #${currentTask?.id}`"
        width="900px"
        @close="handleCloseDetail"
      >
        <div v-if="currentTask" class="task-detail-tabs">
          <el-tabs v-model="activeDetailTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="编号">
                  #{{ currentTask.id }}
                </el-descriptions-item>
                <el-descriptions-item label="类型">
                  <el-tag v-if="currentTask.type === 'REQUIREMENT'" size="small" type="warning">需求</el-tag>
                  <el-tag v-else-if="currentTask.type === 'TASK'" size="small" type="primary">任务</el-tag>
                  <el-tag v-else-if="currentTask.type === 'BUG'" size="small" type="danger">缺陷</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="标题" :span="2">
                  {{ currentTask.title }}
                </el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">
                  {{ currentTask.description || '无' }}
                </el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="getStatusType(currentTask.status)" size="small">
                    {{ getStatusText(currentTask.status) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="优先级">
                  <el-tag :type="getPriorityType(currentTask.priority)" size="small">
                    {{ getPriorityText(currentTask.priority) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="负责人">
                  {{ currentTask.assigneeName || '未分配' }}
                </el-descriptions-item>
                <el-descriptions-item label="所属迭代">
                  {{ currentTask.iterationName || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="预估工时">
                  {{ currentTask.estimateHours || 0 }}h
                </el-descriptions-item>
                <el-descriptions-item label="计划开始时间">
                  {{ currentTask.planStartDate || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="计划结束时间">
                  {{ currentTask.planEndDate || '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ currentTask.createTime || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>

            <!-- 工时记录（仅任务/需求） -->
            <el-tab-pane v-if="currentTask.type !== 'BUG'" name="workhours">
              <template #label>
                <span>
                  <el-icon><Clock /></el-icon>
                  工时记录
                </span>
              </template>
              <div class="workhours-content">
                <div class="workhours-summary">
                  <span>总工时: {{ currentTask.actualHours || 0 }}h</span>
                </div>
                <el-table :data="workHourList" stripe style="width: 100%">
                  <el-table-column prop="workDate" label="日期" width="110" />
                  <el-table-column prop="hours" label="工时数" width="80">
                    <template #default="{ row }">
                      {{ row.hours }}h
                    </template>
                  </el-table-column>
                  <el-table-column prop="content" label="说明" min-width="200" />
                  <el-table-column prop="createTime" label="创建时间" width="160" />
                </el-table>
                <el-empty v-if="!workHourList || workHourList.length === 0" description="暂无工时记录" :image-size="60" />
              </div>
            </el-tab-pane>

            <!-- 缺陷特有字段 -->
            <el-tab-pane v-if="currentTask.type === 'BUG'" name="defect">
              <template #label>
                <span>
                  <el-icon><Warning /></el-icon>
                  缺陷信息
                </span>
              </template>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="严重程度">
                  <el-tag :type="getSeverityType(currentTask.severity)" size="small">
                    {{ getSeverityText(currentTask.severity) }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="复现步骤" :span="2">
                  <div class="reproduction-steps">{{ currentTask.reproductionSteps || '无' }}</div>
                </el-descriptions-item>
                <el-descriptions-item label="修复版本">
                  {{ currentTask.fixVersion || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>
          </el-tabs>

          <div class="detail-actions">
            <el-button type="primary" @click="handleEditTask(currentTask)">编辑</el-button>
            <el-button @click="handleCloseDetail">关闭</el-button>
          </div>
        </div>
      </el-dialog>

      <!-- 空状态 -->
      <el-empty
        v-if="filteredTasks.length === 0 && !loading"
        description="暂无需求数据"
        :image-size="120"
      />
    </div>

    <!-- 底部状态栏 -->
    <div class="status-footer">
      <span class="task-summary">共 {{ filteredTasks.length }} 个需求</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  ArrowRight,
  Folder,
  Document,
  Clock,
  Warning,
  Check,
  Delete,
} from '@element-plus/icons-vue'
import { getTasksByProjectId, type TaskInfo } from '@/api/task'
import { updateTaskStatus } from '@/api/task'
import { getWorkHourList, type WorkHourInfo } from '@/api/workhour'

// Props
const props = defineProps<{
  projectId: number
}>()

// Emits
const emit = defineEmits<{
  viewTask: [task: TaskInfo]
  editTask: [task: TaskInfo]
  deleteTask: [task: TaskInfo]
}>()

// 任务列表（树形结构）
const tasks = ref<TaskInfo[]>([])
const loading = ref(false)

// 展开的任务 ID 集合
const expandedKeys = ref<Set<number>>(new Set())

// 详情弹窗
const detailDialogVisible = ref(false)
const currentTask = ref<FlatTask | null>(null)
const activeDetailTab = ref('basic')
const workHourList = ref<WorkHourInfo[]>([])

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('')
const priorityFilter = ref('')
const sortBy = ref<'default' | 'priority' | 'createTime' | 'planDate'>('default')
const sortOrder = ref<'asc' | 'desc'>('desc')
const selectedTasks = ref<number[]>([])

// 扁平化的任务列表（带 level 属性）
interface FlatTask extends TaskInfo {
  level: number
  hasChildren: boolean
  subtaskCount: number
}

const flatTaskList = computed(() => {
  const result: FlatTask[] = []
  const taskList = filteredTasks.value

  const flatten = (list: TaskInfo[], level: number) => {
    list.forEach(task => {
      const subtaskCount = task.subtasks ? task.subtasks.length : 0
      const flatTask: FlatTask = {
        ...task,
        level,
        hasChildren: !!(task.subtasks && task.subtasks.length > 0),
        subtaskCount
      }
      result.push(flatTask)

      // 如果展开或有子任务，递归添加子任务
      if (task.subtasks && task.subtasks.length > 0 && expandedKeys.value.has(task.id)) {
        flatten(task.subtasks, level + 1)
      }
    })
  }

  flatten(taskList, 0)
  return result
})

// 获取任务列表（不分页，一次性加载所有）
const fetchAllTasks = async () => {
  loading.value = true
  try {
    // 使用一个大的 pageSize 来获取所有任务
    const res = await getTasksByProjectId(
      props.projectId,
      1,
      1000  // 设置一个足够大的值来获取所有任务
    )
    console.log('后端返回的任务数据:', res.list)
    tasks.value = res.list || []

    // 默认展开所有任务
    initExpandedKeys()
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 计算总子任务数量
const totalSubtaskCount = computed(() => {
  let count = 0
  const countSubtasks = (taskList: TaskInfo[]) => {
    taskList.forEach(task => {
      if (task.subtasks && task.subtasks.length > 0) {
        count += task.subtasks.length
        countSubtasks(task.subtasks)
      }
    })
  }
  countSubtasks(filteredTasks.value)
  return count
})

// 初始化展开状态（默认展开所有）
const initExpandedKeys = () => {
  const keys = new Set<number>()
  const collectKeys = (taskList: TaskInfo[]) => {
    taskList.forEach(task => {
      if (task.subtasks && task.subtasks.length > 0) {
        keys.add(task.id)
        collectKeys(task.subtasks)
      }
    })
  }
  collectKeys(tasks.value)
  expandedKeys.value = keys
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAllTasks()
})

// 构建任务树（使用后端返回的 subtasks 字段）
const filteredTasks = computed(() => {
  let result = tasks.value

  // 状态筛选
  if (statusFilter.value) {
    result = filterTasksByStatus(result, statusFilter.value)
  }

  // 优先级筛选
  if (priorityFilter.value) {
    result = filterTasksByPriority(result, priorityFilter.value)
  }

  // 关键词搜索
  if (searchKeyword.value) {
    result = filterTasksByKeyword(result, searchKeyword.value)
  }

  // 排序
  if (sortBy.value !== 'default') {
    result = sortTasks(result, sortBy.value, sortOrder.value)
  }

  return result
})

// 递归过滤状态（保留匹配的父任务及其子任务）
const filterTasksByStatus = (taskList: TaskInfo[], status: string): TaskInfo[] => {
  return taskList.filter(task => {
    // 如果当前任务匹配状态，显示它及其所有子任务
    if (task.status === status) return true

    // 如果子任务中有匹配状态的，显示父任务及匹配的子任务
    if (task.subtasks && task.subtasks.length > 0) {
      const filteredChildren = filterTasksByStatus(task.subtasks, status)
      if (filteredChildren.length > 0) {
        task.subtasks = filteredChildren
        return true
      }
    }
    return false
  })
}

// 递归过滤优先级（保留匹配的父任务及其子任务）
const filterTasksByPriority = (taskList: TaskInfo[], priority: string): TaskInfo[] => {
  return taskList.filter(task => {
    // 如果当前任务匹配优先级，显示它及其所有子任务
    if (task.priority === priority) return true

    // 如果子任务中有匹配优先级的，显示父任务及匹配的子任务
    if (task.subtasks && task.subtasks.length > 0) {
      const filteredChildren = filterTasksByPriority(task.subtasks, priority)
      if (filteredChildren.length > 0) {
        task.subtasks = filteredChildren
        return true
      }
    }
    return false
  })
}

// 排序任务
const sortTasks = (taskList: TaskInfo[], sortBy: string, order: 'asc' | 'desc'): TaskInfo[] => {
  const sorted = [...taskList].sort((a, b) => {
    let comparison = 0

    switch (sortBy) {
      case 'priority':
        const priorityOrder = { 'HIGH': 3, 'MEDIUM': 2, 'LOW': 1 }
        comparison = (priorityOrder[a.priority as keyof typeof priorityOrder] || 0) -
                    (priorityOrder[b.priority as keyof typeof priorityOrder] || 0)
        break
      case 'createTime':
        comparison = new Date(a.createTime || '').getTime() - new Date(b.createTime || '').getTime()
        break
      case 'planDate':
        comparison = new Date(a.planEndDate || '').getTime() - new Date(b.planEndDate || '').getTime()
        break
      default:
        comparison = 0
    }

    return order === 'asc' ? comparison : -comparison
  })

  // 递归排序子任务
  return sorted.map(task => {
    if (task.subtasks && task.subtasks.length > 0) {
      return {
        ...task,
        subtasks: sortTasks(task.subtasks, sortBy, order)
      }
    }
    return task
  })
}

// 递归搜索关键词（保留匹配的父任务及其子任务）
const filterTasksByKeyword = (taskList: TaskInfo[], keyword: string): TaskInfo[] => {
  const lowerKeyword = keyword.toLowerCase()
  return taskList.filter(task => {
    // 如果当前任务标题包含关键词，显示它及其所有子任务
    const titleMatch = task.title?.toLowerCase().includes(lowerKeyword)
    if (titleMatch) return true

    // 如果子任务中有包含关键词的，显示父任务及匹配的子任务
    if (task.subtasks && task.subtasks.length > 0) {
      const filteredChildren = filterTasksByKeyword(task.subtasks, keyword)
      if (filteredChildren.length > 0) {
        task.subtasks = filteredChildren
        return true
      }
    }
    return false
  })
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

// 严重程度文本（缺陷用）
const getSeverityText = (severity: string) => {
  const map: Record<string, string> = {
    'BLOCKER': '致命',
    'CRITICAL': '严重',
    'MAJOR': '一般',
    'MINOR': '轻微',
    'TRIVIAL': '提示'
  }
  return map[severity] || '-'
}

// 严重程度类型
const getSeverityType = (severity: string) => {
  const map: Record<string, any> = {
    'BLOCKER': 'danger',
    'CRITICAL': 'danger',
    'MAJOR': 'warning',
    'MINOR': 'info',
    'TRIVIAL': 'info'
  }
  return map[severity] || 'info'
}

// 切换展开/折叠
const toggleExpand = (row: FlatTask) => {
  if (expandedKeys.value.has(row.id)) {
    expandedKeys.value.delete(row.id)
  } else {
    expandedKeys.value.add(row.id)
  }
  // 强制更新
  expandedKeys.value = new Set(expandedKeys.value)
}

// 事件处理
const handleSearch = () => {
  // 搜索由computed自动处理
}

const handleFilter = () => {
  // 筛选由computed自动处理
}

const handleSort = () => {
  // 切换排序顺序
  if (sortBy.value !== 'default') {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  }
}

// 批量完成任务
const handleBatchComplete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量完成 ${selectedTasks.value.length} 个任务吗？`,
      '批量操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const promises = selectedTasks.value.map(taskId =>
      updateTaskStatus({ id: taskId, status: 'DONE' })
    )

    await Promise.all(promises)
    ElMessage.success('批量操作成功')
    selectedTasks.value = []
    refresh()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  } finally {
    loading.value = false
  }
}

// 批量删除任务
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要批量删除 ${selectedTasks.value.length} 个任务吗？删除后将无法恢复！`,
      '批量删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    // TODO: 实现批量删除逻辑（需要后端支持或循环调用删除接口）
    ElMessage.success('批量删除成功')
    selectedTasks.value = []
    refresh()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 处理编辑任务（添加到现有代码中）
const handleEditTask = (task: any) => {
  emit('editTask', task)
}

// 查看任务详情
const handleViewDetail = (row: FlatTask) => {
  currentTask.value = row
  activeDetailTab.value = 'basic'
  detailDialogVisible.value = true
  // 获取工时记录
  if (row.type !== 'REQUIREMENT') {
    fetchWorkHours(row.id)
  }
}

// 获取工时记录
const fetchWorkHours = async (taskId: number) => {
  try {
    const res = await getWorkHourList({
      taskId,
      pageNum: 1,
      pageSize: 100
    })
    workHourList.value = res?.list || []
  } catch (error) {
    console.error('获取工时记录失败:', error)
  }
}

// 关闭详情弹窗
const handleCloseDetail = () => {
  currentTask.value = null
  detailDialogVisible.value = false
}

// 刷新列表
const refresh = () => {
  fetchAllTasks()
}

// 暴露刷新方法供父组件调用
defineExpose({
  refresh
})
</script>

<style scoped>
.requirements-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 需求表格 */
.requirements-table {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  padding: 16px 20px 8px 20px; /* 底部 padding 减小为 8px */
  min-height: 0; /* 允许 flex 子项缩小 */
}

/* 表格滚动条样式 - 完全隐藏但保持滚动功能 */
.requirements-table::-webkit-scrollbar {
  display: none; /* Webkit浏览器：完全隐藏滚动条 */
  width: 0;
  height: 0;
}

.requirements-table {
  scrollbar-width: none; /* Firefox：完全隐藏滚动条 */
}

.task-id {
  color: #909399;
  font-size: 13px;
  font-weight: 500;
}

.task-title-cell {
  display: flex;
  align-items: center;
}

.task-title-cell .expand-toggle {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-right: 8px;
  color: #909399;
  transition: transform 0.3s, opacity 0.3s, border-color 0.3s, background-color 0.3s;
  flex-shrink: 0;
}

.task-title-cell .expand-toggle:hover {
  color: #409eff;
}

.task-title-cell .expand-toggle .el-icon {
  transition: transform 0.3s;
}

.task-title-cell .expand-toggle .el-icon.expanded {
  transform: rotate(90deg);
}

.task-title-cell .expand-placeholder {
  width: 20px;
  margin-right: 8px;
  flex-shrink: 0;
}

.task-title-cell .task-icon {
  margin-right: 8px;
  color: #909399;
  flex-shrink: 0;
}

.task-title-cell .title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.task-title-cell .title-link {
  font-size: 14px;
  color: #409eff;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.3s;
}

.task-title-cell .title-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.assignee {
  font-size: 13px;
  color: #606266;
}

.hours {
  font-size: 13px;
  color: #909399;
}

/* 底部状态栏 */
.status-footer {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  background: #fafafa;
}

.task-summary {
  font-size: 13px;
  color: #606266;
}

/* 子任务数量浮动标签 */
.task-title-cell .subtask-badge {
  margin-left: auto;
  font-size: 12px;
  background-color: #ecf5ff;
  border-color: #d9ecff;
  color: #409eff;
  font-weight: 500;
  min-width: 24px;
  text-align: center;
  flex-shrink: 0;
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 4px;
  overflow: hidden;
}

/* 表头样式 */
:deep(.el-table__header-wrapper) {
  th {
    background-color: #f5f7fa !important;
    color: #606266 !important;
    font-weight: 600;
  }
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 任务详情弹窗样式 */
.task-detail {
  padding: 20px 0;
}
</style>

