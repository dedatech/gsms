<template>
  <div class="iteration-selector-wrapper">
    <el-dropdown trigger="click" @command="handleSelectIteration">
      <div class="iteration-select-trigger">
        <!-- 状态点 -->
        <div
          class="iteration-status-dot"
          :class="getStatusClass(currentIteration?.status)"
        ></div>

        <!-- 迭代名称 -->
        <span class="iteration-name">{{ currentIterationName }}</span>

        <!-- 进度百分比 -->
        <span v-if="currentIteration?.progress" class="iteration-progress">
          {{ currentIteration.progress }}%
        </span>

        <!-- 下拉箭头 -->
        <el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </div>

      <template #dropdown>
        <el-dropdown-menu>
          <!-- 当前迭代 -->
          <div v-if="currentIteration" class="iteration-section">
            <div class="section-title">当前迭代</div>
            <el-dropdown-item
              :command="currentIteration.id"
              class="iteration-item active"
            >
              <div class="iteration-item-content">
                <div
                  class="iteration-status-dot"
                  :class="getStatusClass(currentIteration.status)"
                ></div>
                <div class="iteration-info">
                  <div class="iteration-name-text">{{ currentIteration.name }}</div>
                  <div class="iteration-meta">
                    <span v-if="currentIteration.startDate" class="iteration-date">
                      {{ formatDate(currentIteration.startDate) }} - {{ formatDate(currentIteration.endDate) }}
                    </span>
                    <span v-if="currentIteration.progress" class="iteration-progress-text">
                      进度 {{ currentIteration.progress }}%
                    </span>
                  </div>
                </div>
              </div>
            </el-dropdown-item>
          </div>

          <!-- 其他迭代 -->
          <div v-if="otherIterations.length > 0" class="iteration-section">
            <div class="section-title">所有迭代</div>
            <el-dropdown-item
              v-for="iteration in otherIterations"
              :key="iteration.id"
              :command="iteration.id"
              class="iteration-item"
            >
              <div class="iteration-item-content">
                <div
                  class="iteration-status-dot"
                  :class="getStatusClass(iteration.status)"
                ></div>
                <div class="iteration-info">
                  <div class="iteration-name-text">{{ iteration.name }}</div>
                  <div class="iteration-meta">
                    <span v-if="iteration.startDate" class="iteration-date">
                      {{ formatDate(iteration.startDate) }} - {{ formatDate(iteration.endDate) }}
                    </span>
                    <span v-if="iteration.progress" class="iteration-progress-text">
                      进度 {{ iteration.progress }}%
                    </span>
                  </div>
                </div>
              </div>
            </el-dropdown-item>
          </div>

          <!-- 查看所有迭代 -->
          <el-dropdown-item divided command="view-all" class="view-all-item">
            <el-icon><Management /></el-icon>
            <span>查看所有迭代</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, Management } from '@element-plus/icons-vue'
import { useProjectStore } from '@/stores/project'
import type { IterationInfo } from '@/stores/project'

/**
 * 迭代信息（扩展）
 */
export interface ExtendedIterationInfo extends IterationInfo {
  progress?: number // 进度百分比
  startDate?: string
  endDate?: string
}

const projectStore = useProjectStore()

// 当前迭代
const currentIteration = computed<ExtendedIterationInfo | undefined>(() => {
  return projectStore.currentIteration as ExtendedIterationInfo | undefined
})

// 当前迭代名称
const currentIterationName = computed(() => {
  return currentIteration.value?.name || '请选择迭代'
})

// 其他迭代列表（排除当前迭代）
const otherIterations = computed<ExtendedIterationInfo[]>(() => {
  // TODO: 从后端获取所有迭代列表
  return []
})

// 获取状态样式类
const getStatusClass = (status?: string) => {
  const statusMap: Record<string, string> = {
    'NOT_STARTED': 'iteration-status-not-started',
    'IN_PROGRESS': 'iteration-status-in-progress',
    'COMPLETED': 'iteration-status-completed'
  }
  return status ? statusMap[status] || 'iteration-status-not-started' : 'iteration-status-not-started'
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 处理迭代选择
const handleSelectIteration = async (command: string | number | object) => {
  if (command === 'view-all') {
    // TODO: 跳转到迭代管理页
    ElMessage.info('查看所有迭代')
    return
  }

  const iterationId = typeof command === 'number' ? command : parseInt(command as string)

  // TODO: 根据ID切换迭代
  console.log('切换到迭代:', iterationId)
  ElMessage.success('已切换迭代')
}

// 组件挂载时加载当前迭代
onMounted(() => {
  // 如果有当前项目，加载其活跃迭代
  if (projectStore.currentProjectId) {
    // TODO: 调用 API 获取当前活跃迭代
    // const iteration = await getActiveIteration(projectStore.currentProjectId)
    // projectStore.setCurrentIteration(iteration)
  }
})
</script>

<style scoped>
.iteration-selector-wrapper {
  display: inline-block;
}

.iteration-select-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: var(--iteration-select-bg);
  border: 1px solid var(--iteration-select-border);
  border-radius: 4px;
  cursor: pointer;
  min-width: 200px;
  transition: all 0.3s;
  font-size: 14px;
}

.iteration-select-trigger:hover {
  border-color: var(--module-tab-active);
}

.iteration-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.iteration-status-not-started {
  background: var(--iteration-status-not-started);
}

.iteration-status-in-progress {
  background: var(--iteration-status-in-progress);
}

.iteration-status-completed {
  background: var(--iteration-status-completed);
}

.iteration-name {
  flex: 1;
  color: #333;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.iteration-progress {
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

/* 下拉菜单样式 */
.iteration-section {
  padding: 4px 0;
}

.section-title {
  padding: 8px 16px;
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

.iteration-item {
  padding: 0 !important;
}

.iteration-item-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 16px;
  width: 100%;
}

.iteration-info {
  flex: 1;
  min-width: 0;
}

.iteration-name-text {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
}

.iteration-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.iteration-date {
  color: #666;
}

.iteration-progress-text {
  color: #1890ff;
}

.view-all-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item) {
  padding: 0;
  line-height: 1.5;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f5f5f5;
}

:deep(.el-dropdown-menu__item.iteration-item.active) {
  background: var(--sidebar-active-bg);
}
</style>
