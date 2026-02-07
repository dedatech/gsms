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
      </div>
      <div class="toolbar-right">
        <el-button type="primary" :icon="Plus" @click="handleCreateRequirement">
          新建需求
        </el-button>
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
        :title="`任务详情 #${currentTask?.id}`"
        width="800px"
        @close="handleCloseDetail"
      >
        <div v-if="currentTask" class="task-detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="任务编号">
              #{{ currentTask.id }}
            </el-descriptions-item>
            <el-descriptions-item label="任务类型">
              <el-tag v-if="currentTask.type === 'REQUIREMENT'" size="small" type="warning">需求</el-tag>
              <el-tag v-else-if="currentTask.type === 'TASK'" size="small" type="primary">任务</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="任务标题" :span="2">
              {{ currentTask.title }}
            </el-descriptions-item>
            <el-descriptions-item label="任务描述" :span="2">
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
            <el-descriptions-item label="预估工时">
              {{ currentTask.estimateHours || 0 }}h
            </el-descriptions-item>
            <el-descriptions-item label="实际工时">
              {{ currentTask.actualHours || 0 }}h
            </el-descriptions-item>
            <el-descriptions-item label="计划开始时间">
              {{ currentTask.planStartDate || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="计划结束时间">
              {{ currentTask.planEndDate || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ currentTask.createTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ currentTask.updateTime || '-' }}
            </el-descriptions-item>
          </el-descriptions>
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
      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="taskTotal"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        small
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Plus,
  ArrowRight,
  Folder,
  Document
} from '@element-plus/icons-vue'
import { getTasksByProjectId, type TaskInfo } from '@/api/task'

// Props
const props = defineProps<{
  projectId: number
}>()

// Emits
const emit = defineEmits<{
  createTask: [iterationId?: number, parentId?: number]
  viewTask: [task: TaskInfo]
  editTask: [task: TaskInfo]
  deleteTask: [task: TaskInfo]
}>()

// 任务列表（树形结构）
const tasks = ref<TaskInfo[]>([])
const taskTotal = ref(0)
const loading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 展开的任务 ID 集合
const expandedKeys = ref<Set<number>>(new Set())

// 详情弹窗
const detailDialogVisible = ref(false)
const currentTask = ref<FlatTask | null>(null)

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('')

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

// 获取任务列表（分页）
const fetchAllTasks = async () => {
  loading.value = true
  try {
    const res = await getTasksByProjectId(
      props.projectId,
      currentPage.value,
      pageSize.value
    )
    console.log('后端返回的任务数据:', res.list)
    tasks.value = res.list || []
    taskTotal.value = res.total || 0

    // 默认展开所有任务
    initExpandedKeys()
  } catch (error) {
    console.error('获取任务列表失败:', error)
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 分页事件处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
  fetchAllTasks()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchAllTasks()
}

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

  // 关键词搜索
  if (searchKeyword.value) {
    result = filterTasksByKeyword(result, searchKeyword.value)
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

const handleCreateRequirement = () => {
  emit('createTask', undefined, undefined)
}

// 查看任务详情
const handleViewDetail = (row: FlatTask) => {
  currentTask.value = row
  detailDialogVisible.value = true
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
  justify-content: center;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0; /* 防止被压缩 */
  background: #fafafa;
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

