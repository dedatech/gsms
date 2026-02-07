<template>
  <div class="task-list-item-wrapper" :style="{ paddingLeft: level * 24 + 'px' }">
    <div class="task-item-content">
      <!-- 展开/收起图标 -->
      <span
        v-if="hasChildren"
        class="expand-toggle"
        @click="toggleExpand"
      >
        <el-icon :class="{ expanded: isExpanded }">
          <ArrowRight />
        </el-icon>
      </span>
      <span v-else class="expand-placeholder"></span>

      <!-- 任务信息 -->
      <div class="task-info" @click="handleView">
        <span class="task-id">#{{ task.id }}</span>
        <el-tag v-if="task.type === 'REQUIREMENT'" size="small" type="warning" effect="plain">需求</el-tag>
        <el-tag v-else-if="task.type === 'TASK'" size="small" type="primary" effect="plain">任务</el-tag>
        <span class="task-title">{{ task.title }}</span>
        <el-tag :type="getStatusType(task.status)" size="small" effect="plain">
          {{ getStatusText(task.status) }}
        </el-tag>
        <el-tag :type="getPriorityType(task.priority)" size="small" effect="plain">
          {{ getPriorityText(task.priority) }}
        </el-tag>
        <span class="task-assignee">{{ task.assigneeName || '未分配' }}</span>
        <span class="task-hours">{{ task.estimateHours || 0 }}h / {{ task.actualHours || 0 }}h</span>
      </div>

      <!-- 操作按钮 -->
      <div class="task-actions">
        <el-button link type="primary" size="small" @click.stop="handleView">查看</el-button>
        <el-button link type="primary" size="small" @click.stop="handleEdit">编辑</el-button>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button link type="primary" size="small">
            更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="addChild">
                <el-icon><Plus /></el-icon>
                添加子任务
              </el-dropdown-item>
              <el-dropdown-item command="delete" divided>
                <el-icon><Delete /></el-icon>
                删除
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 递归渲染子任务 -->
    <div v-if="isExpanded && hasChildren" class="task-children">
      <TaskListItem
        v-for="child in children"
        :key="child.id"
        :task="child"
        :level="level + 1"
        @view="$emit('view', $event)"
        @edit="$emit('edit', $event)"
        @add-child="$emit('add-child', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight,
  ArrowDown,
  Plus,
  Delete
} from '@element-plus/icons-vue'
import type { TaskInfo } from '@/api/task'

// Props
const props = defineProps<{
  task: TaskInfo
  level: number
}>()

// Emits
const emit = defineEmits<{
  view: [task: TaskInfo]
  edit: [task: TaskInfo]
  addChild: [task: TaskInfo]
  delete: [task: TaskInfo]
}>()

// 展开状态（使用ref而不是computed）
const isExpanded = ref(true)

const hasChildren = computed(() => {
  return props.task.subtasks && props.task.subtasks.length > 0
})

const children = computed(() => {
  return props.task.subtasks || []
})

// 切换展开状态
const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
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

// 事件处理
const handleView = () => {
  emit('view', props.task)
}

const handleEdit = () => {
  emit('edit', props.task)
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'addChild':
      emit('addChild', props.task)
      break
    case 'delete':
      ElMessageBox.confirm(
        `确定要删除任务"${props.task.title}"吗？删除后将无法恢复。`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
        .then(() => {
          emit('delete', props.task)
        })
        .catch(() => {})
      break
  }
}
</script>

<style scoped>
.task-list-item-wrapper {
  margin-bottom: 8px;
}

.task-item-content {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f9f9f9;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  transition: transform 0.3s, opacity 0.3s, border-color 0.3s, background-color 0.3s;
  cursor: pointer;
}

.task-item-content:hover {
  background: #f0f0f0;
  border-color: #d0d0d0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.expand-toggle {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-right: 8px;
  color: #909399;
  transition: transform 0.3s, opacity 0.3s, border-color 0.3s, background-color 0.3s;
}

.expand-toggle:hover {
  color: #409eff;
}

.expand-toggle .el-icon {
  transition: transform 0.3s;
}

.expand-toggle .el-icon.expanded {
  transform: rotate(90deg);
}

.expand-placeholder {
  width: 20px;
  margin-right: 8px;
}

.task-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.task-id {
  color: #909399;
  font-size: 12px;
  font-weight: 500;
  flex-shrink: 0;
}

.task-title {
  flex: 1;
  min-width: 0;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-assignee {
  font-size: 13px;
  color: #666;
  flex-shrink: 0;
}

.task-hours {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.task-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}

.task-children {
  margin-top: 8px;
}
</style>

