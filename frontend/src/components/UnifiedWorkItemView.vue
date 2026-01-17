<template>
  <div class="unified-work-item-view">
    <!-- 任务分组列表（包含迭代信息和父任务嵌套） -->
    <div class="content-area">
      <TaskGroupList
        :tasks="tasks"
        :iterations="iterations"
        @create-task="handleCreateTask"
        @create-iteration="handleCreateIteration"
        @view-iteration="handleViewIteration"
        @edit-iteration="handleEditIteration"
        @edit-task="handleEditTask"
      />
    </div>

    <!-- 底部状态栏 -->
    <div class="status-footer">
      <div class="status-stats">
        <span class="status-item">
          <i class="el-icon-files"></i>
          全部: {{ totalTasks }}
        </span>
        <span class="separator">|</span>
        <span class="status-item todo">
          待办: {{ todoTasks }}
        </span>
        <span class="separator">|</span>
        <span class="status-item in-progress">
          进行中: {{ inProgressTasks }}
        </span>
        <span class="separator">|</span>
        <span class="status-item done">
          已完成: {{ doneTasks }}
        </span>
        <span v-if="isLargeScale && iterations.length > 0" class="separator">|</span>
        <span v-if="isLargeScale && iterations.length > 0" class="status-item">
          <i class="el-icon-folder"></i>
          迭代: {{ iterations.length }}
        </span>
      </div>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="taskTotal"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        small
        background
        class="footer-pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TaskInfo } from '@/api/task'
import type { IterationInfo } from '@/api/iteration'
import TaskGroupList from '@/components/TaskGroupList.vue'

// Props
const props = defineProps<{
  projectType?: string
  iterations: IterationInfo[]
  tasks: TaskInfo[]
  taskTotal: number
  currentPage?: number
  pageSize?: number
}>()

// Emits
const emit = defineEmits<{
  createTask: [iterationId?: number, parentId?: number]
  createIteration: []
  viewIteration: [iteration: any]
  editIteration: [iteration: any]
  editTask: [task: any]
  paginationChange: [pageNum: number, pageSize: number]
}>()

// 分页参数（从 props 获取，如果没有则使用默认值）
const currentPage = computed({
  get: () => props.currentPage || 1,
  set: (val: number) => emit('paginationChange', val, props.pageSize || 10)
})

const pageSize = computed({
  get: () => props.pageSize || 10,
  set: (val: number) => emit('paginationChange', props.currentPage || 1, val)
})

// 分页事件处理
const handleSizeChange = (size: number) => {
  emit('paginationChange', props.currentPage || 1, size)
}

const handleCurrentChange = (page: number) => {
  emit('paginationChange', page, props.pageSize || 10)
}

// 是否是中大型项目
const isLargeScale = computed(() => props.projectType === 'LARGE_SCALE')

// 任务统计 - 递归统计所有任务（包括子任务）
const totalTasks = computed(() => {
  return countAllTasks(props.tasks)
})

const todoTasks = computed(() => {
  return countTasksByStatus(props.tasks, 'TODO')
})

const inProgressTasks = computed(() => {
  return countTasksByStatus(props.tasks, 'IN_PROGRESS')
})

const doneTasks = computed(() => {
  return countTasksByStatus(props.tasks, 'DONE')
})

// 递归统计所有任务数量
const countAllTasks = (tasks: any[]): number => {
  let count = 0
  tasks.forEach(task => {
    count += 1
    if (task.subtasks && task.subtasks.length > 0) {
      count += countAllTasks(task.subtasks)
    }
  })
  return count
}

// 递归统计指定状态的任务数量
const countTasksByStatus = (tasks: any[], status: string): number => {
  let count = 0
  tasks.forEach(task => {
    const taskStatus = task.status || 'TODO'
    if (taskStatus === status) {
      count += 1
    }
    if (task.subtasks && task.subtasks.length > 0) {
      count += countTasksByStatus(task.subtasks, status)
    }
  })
  return count
}

// 创建任务
const handleCreateTask = (iterationId?: number, parentId?: number) => {
  emit('createTask', iterationId, parentId)
}

// 创建迭代
const handleCreateIteration = () => {
  emit('createIteration')
}

// 查看迭代详情
const handleViewIteration = (iteration: any) => {
  emit('viewIteration', iteration)
}

// 编辑迭代
const handleEditIteration = (iteration: any) => {
  emit('editIteration', iteration)
}

// 编辑任务
const handleEditTask = (task: any) => {
  emit('editTask', task)
}
</script>

<style scoped>
.unified-work-item-view {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.content-area {
  flex: 1;
  overflow-y: auto;
}

/* 底部状态栏 */
.status-footer {
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 20px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  font-size: 13px;
  color: #606266;
  z-index: 10;
}

.status-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.footer-pagination {
  flex-shrink: 0;
}

.pagination-debug {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination-debug span {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 500;
}

.pagination-wrapper {
  display: flex;
  align-items: center;
}

.total-text {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.status-item.todo {
  color: #e6a23c;
}

.status-item.in-progress {
  color: #409eff;
}

.status-item.done {
  color: #67c23a;
}

.separator {
  color: #dcdfe6;
  margin: 0 4px;
}

/* Alert 样式调整 */
:deep(.el-alert) {
  border-radius: 4px;
}

:deep(.el-alert__title) {
  font-size: 14px;
}
</style>
