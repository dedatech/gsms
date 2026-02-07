<template>
  <div class="view-mode-tabs-wrapper">
    <div class="view-mode-tabs">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="view-tab"
        :class="{ active: activeTab === tab.key }"
        @click="handleTabClick(tab)"
      >
        <el-icon v-if="tab.icon">
          <component :is="tab.icon" />
        </el-icon>
        <span>{{ tab.label }}</span>
        <el-badge
          v-if="tab.count !== undefined"
          :value="tab.count"
          :max="999"
          class="tab-badge"
        />
      </div>
    </div>

    <!-- 右侧操作按钮 -->
    <div class="view-actions">
      <el-tooltip content="筛选器" placement="bottom">
        <el-button
          :icon="Filter"
          circle
          text
          @click="handleFilter"
        />
      </el-tooltip>

      <el-tooltip content="刷新" placement="bottom">
        <el-button
          :icon="Refresh"
          circle
          text
          @click="handleRefresh"
        />
      </el-tooltip>

      <el-dropdown trigger="click" @command="handleMoreAction">
        <el-button circle text>
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="export">
              <el-icon><Download /></el-icon>
              <span>导出数据</span>
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              <span>看板设置</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataBoard,
  Grid,
  Filter,
  Refresh,
  MoreFilled,
  Download,
  Setting
} from '@element-plus/icons-vue'
import { useProjectStore } from '@/stores/project'

/**
 * 视图标签定义
 */
interface ViewTab {
  key: string
  label: string
  icon?: any
  count?: number
}

const projectStore = useProjectStore()

// 所有可用的视图标签
const allTabs: ViewTab[] = [
  { key: 'overview', label: '概览', icon: DataBoard },
  { key: 'kanban', label: '敏捷看板', icon: Grid },
  { key: 'list', label: '列表视图', icon: Grid }
]

// 当前活动的标签
const activeTab = computed(() => {
  return projectStore.currentViewMode
})

// 显示的标签列表（可以根据权限或配置动态调整）
const tabs = computed<ViewTab[]>(() => {
  return allTabs
})

// 处理标签点击
const handleTabClick = (tab: ViewTab) => {
  projectStore.setViewMode(tab.key as 'overview' | 'kanban' | 'list')
  console.log('切换到视图:', tab.key)
}

// 处理筛选按钮点击
const handleFilter = () => {
  // TODO: 打开筛选器对话框
  ElMessage.info('打开筛选器')
}

// 处理刷新按钮点击
const handleRefresh = () => {
  // TODO: 刷新当前视图数据
  ElMessage.success('刷新成功')
}

// 处理更多操作
const handleMoreAction = (command: string) => {
  switch (command) {
    case 'export':
      ElMessage.info('导出数据')
      break
    case 'settings':
      ElMessage.info('看板设置')
      break
  }
}
</script>

<style scoped>
.view-mode-tabs-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px 0;
  background: #fff;
  border-bottom: 1px solid var(--view-tab-border-color);
}

.view-mode-tabs {
  display: flex;
  gap: 24px;
}

.view-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-bottom: 12px;
  color: var(--view-tab-color);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: transform 0.3s, opacity 0.3s, border-color 0.3s, background-color 0.3s;
  font-size: 14px;
  position: relative;
}

.view-tab:hover {
  color: var(--view-tab-active-color);
}

.view-tab.active {
  color: var(--view-tab-active-color);
  border-bottom-color: var(--view-tab-active-color);
}

.view-tab .el-icon {
  font-size: 16px;
}

.tab-badge {
  margin-left: 4px;
}

/* 右侧操作按钮 */
.view-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-actions .el-button {
  width: 32px;
  height: 32px;
}

.view-actions .el-button:hover {
  background: #f5f5f5;
}
</style>
