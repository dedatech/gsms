<template>
  <div class="unified-work-item-view">
    <!-- 任务分组列表（包含迭代信息和父任务嵌套） -->
    <div class="content-area">
      <TaskGroupList
        :tasks="tasks"
        :iterations="iterations"
        :project-id="projectId"
        @create-task="handleCreateTask"
        @create-iteration="handleCreateIteration"
        @view-iteration="handleViewIteration"
        @edit-iteration="handleEditIteration"
        @edit-task="handleEditTask"
        @refresh="handleRefresh"
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
  iterations: IterationInfo[]
  tasks: TaskInfo[]
  taskTotal: number
  currentPage?: number
  pageSize?: number
  projectId?: number
}>()

// Emits
const emit = defineEmits<{
  createTask: [iterationId?: number, parentId?: number]
  createIteration: []
  viewIteration: [iteration: any]
  editIteration: [iteration: any]
  editTask: [task: any]
  paginationChange: [pageNum: number, pageSize: number]
  refresh: []  // 刷新任务列表
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

// 刷新任务列表
const handleRefresh = () => {
  emit('refresh')
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
  justify-content: center;
  gap: 12px;
  padding: 12px 20px;
  background: #f5f7fa;
  border-top: 1px solid #e4e7ed;
  font-size: 13px;
  color: #606266;
  z-index: 10;
}

/* Alert 样式调整 */
:deep(.el-alert) {
  border-radius: 4px;
}

:deep(.el-alert__title) {
  font-size: 14px;
}
</style>
