<template>
  <div class="task-group-list">
    <!-- 统一的表格（支持有迭代和无迭代） -->
    <el-table
        :data="treeTableData"
        style="width: 100%"
        row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :expand-row-keys="expandedKeys"
        @expand-change="handleExpandChange"
        :row-class-name="getTableRowClass"
        :span-method="handleSpanMethod"
        default-expand-all
        border
      >
        <!-- 第一列：展开/折叠箭头 -->
        <el-table-column width="50">
        </el-table-column>

        <!-- 第二列：ID -->
        <el-table-column label="ID" width="60" align="center">
          <template #default="{ row }">
            <template v-if="row.type === 'task'">
              <span class="task-id">{{ row.id }}</span>
            </template>
          </template>
        </el-table-column>

        <!-- 第三列：任务名称 -->
        <el-table-column label="任务名称" min-width="300">
          <template #default="{ row }">
            <!-- 迭代行 - 合并单元格展示所有信息 -->
            <template v-if="row.type === 'iteration'">
              <div class="iteration-detail-cell">
                <div class="iteration-main">
                  <div class="iteration-left">
                    <el-icon><FolderOpened /></el-icon>
                    <span class="field-label">迭代名称：</span>
                    <span class="name">{{ row.name }}</span>
                    <span class="field-item">
                      <span class="field-label">状态：</span>
                      <el-tag size="small" :type="getIterationStatusType(row.status)">
                        {{ getIterationStatusText(row.status) }}
                      </el-tag>
                    </span>
                    <span v-if="row.planStartDate && row.planEndDate" class="field-item date-range">
                      <span class="field-label">时间：</span>
                      {{ row.planStartDate }} ~ {{ row.planEndDate }}
                    </span>
                    <span class="field-item task-count">
                      <span class="field-label">任务数量：</span>{{ row.taskCount }}
                    </span>
                  </div>
                  <div class="iteration-right">
                    <span class="field-label">进度：</span>
                    <el-progress
                      :percentage="row.progress"
                      :stroke-width="8"
                      :show-text="true"
                      :width="120"
                      style="width: 150px"
                    />
                  </div>
                </div>
              </div>
            </template>
            <!-- 任务行 -->
            <template v-else-if="row.type === 'task'">
              <div class="task-title-cell" :style="{ paddingLeft: (row.level || 0) * 24 + 'px' }">
                <!-- 根任务显示文件夹图标，子任务显示文档图标 -->
                <el-icon class="task-icon" :class="row.level === 0 ? 'root-task-icon' : 'subtask-icon'" :size="16">
                  <component :is="row.level === 0 ? Folder : Document" />
                </el-icon>
                <!-- 任务名称（可点击） -->
                <span class="title" @click.stop="handleViewTask(row)">{{ row.title }}</span>
              </div>
            </template>
          </template>
        </el-table-column>

        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <template v-if="row.type === 'task'">
              <el-tag
                v-if="row.priority"
                :type="getPriorityType(row.priority)"
                size="small"
              >
                {{ getPriorityText(row.priority) }}
              </el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </template>
        </el-table-column>

        <el-table-column label="负责人" width="120">
          <template #default="{ row }">
            <span v-if="row.type === 'task'">
              <span v-if="row.assigneeName">{{ row.assigneeName }}</span>
              <span v-else class="text-muted">未分配</span>
            </span>
          </template>
        </el-table-column>

        <el-table-column label="计划结束时间" width="120">
          <template #default="{ row }">
            <template v-if="row.type === 'task'">
              <span v-if="row.planEndDate" :class="getTaskDateClass(row.planEndDate, row.status)">
                {{ row.planEndDate }}
              </span>
              <span v-else>-</span>
            </template>
          </template>
        </el-table-column>

        <el-table-column label="预估工时" width="100" align="right">
          <template #default="{ row }">
            <template v-if="row.type === 'task'">
              <span v-if="row.estimateHours">{{ row.estimateHours }}h</span>
              <span v-else>-</span>
            </template>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <!-- 迭代操作 -->
            <template v-if="row.type === 'iteration'">
              <el-button
                link
                type="primary"
                size="small"
                @click.stop="handleCreateTask(row.id)"
              >
                新建任务
              </el-button>
              <el-button
                link
                type="primary"
                size="small"
                @click.stop="handleViewIteration(row)"
              >
                查看
              </el-button>
              <el-button
                link
                type="primary"
                size="small"
                @click.stop="handleEditIteration(row)"
              >
                编辑
              </el-button>
            </template>
            <!-- 任务操作 -->
            <template v-if="row.type === 'task'">
              <el-button
                link
                type="primary"
                size="small"
                @click.stop="handleCreateSubTask(row.id)"
              >
                新建子任务
              </el-button>
              <el-button
                link
                type="primary"
                size="small"
                @click.stop="handleViewTask(row)"
              >
                查看
              </el-button>
              <el-button
                link
                type="primary"
                size="small"
                @click.stop="handleEditTask(row)"
              >
                编辑
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="treeTableData.length === 0"
        description="暂无任务"
        :image-size="80"
      />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { FolderOpened, Plus, Document, Folder } from '@element-plus/icons-vue'
import type { TaskInfo } from '@/api/task'
import type { IterationInfo } from '@/api/iteration'

const router = useRouter()

// Props
const props = defineProps<{
  tasks: TaskInfo[]
  iterations: IterationInfo[]
}>()

// Emits
const emit = defineEmits<{
  createTask: [iterationId: number | undefined, parentId: number | undefined]
  createIteration: []
  viewIteration: [iteration: any]
  editIteration: [iteration: any]
  editTask: [task: any]
}>()

// 展开的行
const expandedKeys = ref<number[]>([])

// 表格数据行类型
interface TreeNode {
  id: number
  type: 'iteration' | 'task'
  name?: string
  title?: string
  status?: string
  priority?: string
  assigneeName?: string
  planStartDate?: string
  planEndDate?: string
  estimateHours?: number
  taskCount?: number
  progress?: number
  iterationId?: number
  createTime?: string
  hasChildren?: boolean
  isSubtask?: boolean  // 是否为子任务
  level?: number  // 层级深度（0=根任务，1=一级子任务，2=二级子任务...）
  parentId?: number  // 父任务ID
  children?: TreeNode[]
}

// 按创建时间排序的任务列表
const sortedTasks = computed(() => {
  return [...props.tasks].sort((a, b) => {
    const timeA = new Date(a.createTime || 0).getTime()
    const timeB = new Date(b.createTime || 0).getTime()
    return timeB - timeA
  })
})

// 构建树形表格数据
const treeTableData = computed(() => {
  // 如果有迭代，返回迭代节点 + 任务节点
  if (props.iterations.length > 0) {
    const tree: TreeNode[] = []

    props.iterations.forEach(iteration => {
      // 获取该迭代下的所有任务（扁平列表）
      const allIterTasks = sortedTasks.value.filter(t => t.iterationId === iteration.id)
      const completedCount = allIterTasks.filter(t => t.status === 'DONE').length
      const progress = allIterTasks.length > 0 ? Math.round((completedCount / allIterTasks.length) * 100) : 0

      // 迭代节点
      const iterationNode: TreeNode = {
        id: iteration.id,
        type: 'iteration',
        name: iteration.name,
        status: iteration.status,
        planStartDate: iteration.planStartDate || undefined,
        planEndDate: iteration.planEndDate || undefined,
        taskCount: allIterTasks.length,
        progress,
        hasChildren: allIterTasks.length > 0,
        children: []
      }

      // 使用客户端构建树形结构（通过 parentId）
      const taskTree = buildTreeData(allIterTasks)

      // 将构建好的树添加到迭代节点下
      iterationNode.children!.push(...taskTree)

      tree.push(iterationNode)
    })

    return tree
  } else {
    // 如果没有迭代，直接返回任务树（常规项目）
    return buildTreeData(sortedTasks.value)
  }
})

// 客户端构建树形结构（通过 parentId）
const buildTreeData = (flatTasks: TaskInfo[]): TreeNode[] => {
  if (!flatTasks || flatTasks.length === 0) return []

  // 创建 ID 到任务的映射
  const taskMap = new Map<number, TreeNode>()
  const rootTasks: TreeNode[] = []

  // 第一步：创建所有节点
  flatTasks.forEach(task => {
    const node: TreeNode = {
      id: task.id,
      type: 'task',
      title: task.title,
      status: task.status,
      priority: task.priority,
      assigneeName: task.assigneeName || undefined,
      planEndDate: task.planEndDate || undefined,
      estimateHours: task.estimateHours || undefined,
      iterationId: task.iterationId,
      isSubtask: !!task.parentId,  // 通过 parentId 判断是否为子任务
      parentId: task.parentId,
      level: 0,  // 初始层级，后续计算
      children: [],
      hasChildren: false
    }
    taskMap.set(task.id, node)
  })

  // 第二步：建立父子关系
  flatTasks.forEach(task => {
    const node = taskMap.get(task.id)!
    if (task.parentId) {
      const parent = taskMap.get(task.parentId)
      if (parent) {
        parent.children!.push(node)
        parent.hasChildren = true
      } else {
        // 找不到父任务，作为根任务处理
        rootTasks.push(node)
      }
    } else {
      rootTasks.push(node)
    }
  })

  // 第三步：递归计算每个节点的层级（level）
  const calculateLevel = (node: TreeNode, currentLevel: number) => {
    node.level = currentLevel
    if (node.children && node.children.length > 0) {
      node.children.forEach(child => {
        calculateLevel(child as TreeNode, currentLevel + 1)
      })
    }
  }

  rootTasks.forEach(root => calculateLevel(root, 0))

  return rootTasks
}

// 处理展开/折叠
const handleExpandChange = (row: TreeNode, expanded: boolean) => {
  if (expanded) {
    expandedKeys.value.push(row.id)
  } else {
    const index = expandedKeys.value.indexOf(row.id)
    if (index > -1) {
      expandedKeys.value.splice(index, 1)
    }
  }
}

// 单元格合并方法
const handleSpanMethod = ({ row, columnIndex }: { row: TreeNode; columnIndex: number }) => {
  if (row.type === 'iteration') {
    // 迭代行合并逻辑
    // 列索引：0=箭头, 1=ID, 2=任务名称, 3=优先级, 4=负责人, 5=时间, 6=工时, 7=操作
    if (columnIndex === 0) {
      return [1, 1] // 箭头列独立
    } else if (columnIndex === 1) {
      return [1, 1] // ID列独立
    } else if (columnIndex === 2) {
      return [1, 5] // 任务名称列合并5列（优先级、负责人、时间、工时）
    } else if (columnIndex >= 3 && columnIndex <= 6) {
      return [0, 0] // 被合并的列不显示
    } else if (columnIndex === 7) {
      return [1, 1] // 操作列独立
    }
  }
  // 任务行：不合并
  return [1, 1]
}

// 获取表格行样式
const getTableRowClass = ({ row }: { row: TreeNode }) => {
  if (row.type === 'iteration') {
    return 'iteration-row'
  }
  if (row.type === 'task') {
    if (row.status === 'DONE') return 'task-row-done'
    if (row.status === 'IN_PROGRESS') return 'task-row-in-progress'
  }
  return ''
}

// 获取日期样式类
const getTaskDateClass = (planEndDate: string, status: string) => {
  if (status === 'DONE') return ''

  const today = new Date()
  const endDate = new Date(planEndDate)
  const diffDays = Math.ceil((endDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))

  if (diffDays < 0) return 'text-danger'
  if (diffDays <= 3) return 'text-warning'
  return ''
}

// 在迭代下创建任务
const handleCreateTask = (iterationId?: number) => {
  emit('createTask', iterationId, undefined)
}

// 创建子任务
const handleCreateSubTask = (parentId: number) => {
  // 找到父任务，继承其 iterationId
  const parentTask = props.tasks.find(t => t.id === parentId)
  const iterationId = parentTask?.iterationId
  emit('createTask', iterationId, parentId)
}

// 创建迭代
const handleCreateIteration = () => {
  emit('createIteration')
}

// 查看迭代详情
const handleViewIteration = (iteration: TreeNode) => {
  emit('viewIteration', iteration)
}

// 编辑迭代
const handleEditIteration = (iteration: TreeNode) => {
  emit('editIteration', iteration)
}

// 编辑任务
const handleEditTask = (task: TreeNode) => {
  emit('editTask', task)
}

// 查看任务详情
const handleViewTask = (task: TaskInfo | TreeNode) => {
  router.push(`/tasks/${task.id}`)
}

// 获取迭代状态信息
// 状态映射（使用缓存优化）
const iterationStatusMap = new Map<string, { type: string; text: string }>()
const getIterationStatusInfo = (status: string) => {
  if (!iterationStatusMap.has(status)) {
    const info = {
      'NOT_STARTED': { type: 'info', text: '未开始' },
      'IN_PROGRESS': { type: 'primary', text: '进行中' },
      'DONE': { type: 'success', text: '已完成' }
    }[status] || { type: 'info', text: '未知' }
    iterationStatusMap.set(status, info)
  }
  return iterationStatusMap.get(status)!
}

const getIterationStatusType = (status: string) => getIterationStatusInfo(status).type
const getIterationStatusText = (status: string) => getIterationStatusInfo(status).text

// 获取任务状态信息
const getTaskStatusType = (status: string) => {
  const types: Record<string, any> = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'DONE': 'success'
  }
  return types[status] || 'info'
}

const getTaskStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'TODO': '待办',
    'IN_PROGRESS': '进行中',
    'DONE': '已完成'
  }
  return texts[status] || '未知'
}

// 获取优先级信息
const getPriorityType = (priority: string) => {
  const types: Record<string, any> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning'
  }
  return types[priority] || 'info'
}

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  }
  return texts[priority] || '未知'
}
</script>

<style scoped>
.task-group-list {
  padding: 0;
}

/* 迭代行样式 - 使用带蓝色调的背景，更明显区分 */
:deep(.iteration-row) {
  background-color: #ecf5ff; /* 浅蓝色，Element Plus 主色调 */
  font-weight: 500;
}

:deep(.iteration-row:hover) {
  background-color: #d9ecff !important; /* hover时深蓝色，对比更明显 */
}

:deep(.iteration-row td) {
  background-color: #ecf5ff;
}

/* 迭代详情单元格 */
.iteration-detail-cell {
  padding: 0;
}

.iteration-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
}

.iteration-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.iteration-left .el-icon {
  color: #409eff;
  font-size: 18px;
  flex-shrink: 0;
}

.field-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
  white-space: nowrap;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.iteration-left .name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  flex-shrink: 0;
}

.iteration-left .date-range {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.iteration-left .task-count {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.iteration-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 任务行样式 */
:deep(.el-table__row) {
  /* 移除整行的小手样式 */
}

:deep(.task-row-done) {
  background-color: #f9fafc;
  opacity: 0.8;
}

:deep(.task-row-done:hover) {
  background-color: #f0f2f5 !important;
}

:deep(.task-row-in-progress) {
  background-color: #fffbeb;
}

:deep(.task-row-in-progress:hover) {
  background-color: #fff3d6 !important;
}

/* 任务 ID 样式 */
.task-id {
  font-size: 13px;
  color: #909399;
  font-family: 'Consolas', 'Monaco', monospace;
  font-weight: 500;
}

/* 禁用第一列（ID列）的 Element Plus 自动缩进，让缩进只在名称列显示 */
:deep(.el-table__body-wrapper .el-table__body .el-table__row .el-table__cell:first-child) {
  padding-left: 8px !important;
}

.task-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 任务图标基础样式 */
.task-icon {
  flex-shrink: 0;
}

/* 根任务图标样式（文件夹） */
.root-task-icon {
  color: #E6A23C; /* 橙色，类似文件夹 */
}

/* 子任务图标样式（文档） */
.subtask-icon {
  color: #909399; /* 灰色 */
}

.task-title-cell .title {
  flex: 1;
  font-size: 14px;
  color: #409eff; /* 链接蓝色 */
  cursor: pointer; /* 小手样式 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
  transition: color 0.2s;
}

.task-title-cell .title:hover {
  color: #66b1ff; /* hover时更亮的蓝色 */
  text-decoration: underline; /* hover时添加下划线 */
}

/* 文本颜色 */
.text-muted {
  color: #909399;
}

.text-danger {
  color: #f56c6c;
  font-weight: 500;
}

.text-warning {
  color: #e6a23c;
  font-weight: 500;
}

/* 树形表格样式调整 */
:deep(.el-table__expand-icon) {
  color: #909399;
}

/* 移除子任务的缩进，因为展开按钮已在单独列 */
:deep(.el-table__indent) {
  padding-left: 0 !important;
}

/* 确保树形表格单元格内容垂直居中 */
:deep(.el-table__cell) {
  vertical-align: middle;
}

/* 展开按钮列居中 */
:deep(.el-table__column--selector .cell) {
  text-align: center;
}
</style>
