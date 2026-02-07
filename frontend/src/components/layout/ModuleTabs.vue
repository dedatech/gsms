<template>
  <div class="module-tabs-container">
    <div class="module-tabs">
      <div
        v-for="module in visibleModules"
        :key="module.key"
        class="module-tab"
        :class="{ active: activeModule === module.key }"
        @click="handleModuleClick(module)"
      >
        <el-icon v-if="module.icon">
          <component :is="module.icon" />
        </el-icon>
        <span>{{ module.label }}</span>
        <el-badge
          v-if="module.badge"
          :value="module.badge"
          :max="99"
          class="module-badge"
        />
      </div>
    </div>

    <!-- 更多模块下拉菜单 -->
    <el-dropdown v-if="hiddenModules.length > 0" trigger="click">
      <div class="module-tab-more">
        <el-icon><MoreFilled /></el-icon>
        <span>更多</span>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="module in hiddenModules"
            :key="module.key"
            @click="handleModuleClick(module)"
          >
            <el-icon v-if="module.icon">
              <component :is="module.icon" />
            </el-icon>
            <span>{{ module.label }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DataBoard,
  Document,
  Calendar,
  Grid,
  Warning,
  DataAnalysis,
  Folder,
  User,
  MoreFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

/**
 * 功能模块定义
 */
interface ModuleTab {
  key: string
  label: string
  icon?: any
  badge?: number
  path: string
  visible?: boolean
}

const activeModule = ref('overview')

// 所有可用模块
const allModules: ModuleTab[] = [
  { key: 'overview', label: '概览', icon: DataBoard, path: 'overview' },
  { key: 'requirements', label: '需求', icon: Document, path: 'requirements' },
  { key: 'planning', label: '规划', icon: Grid, path: 'planning' },
  { key: 'iterations', label: '迭代', icon: Calendar, path: 'iterations' },
  { key: 'defects', label: '缺陷', icon: Warning, path: 'defects' },
  { key: 'reports', label: '报表', icon: DataAnalysis, path: 'reports' },
  { key: 'docs', label: '文档', icon: Folder, path: 'docs' },
  { key: 'members', label: '成员', icon: User, path: 'members' }
]

// 可见的模块（前6个）
const visibleModules = computed(() => {
  return allModules.filter(m => m.visible !== false).slice(0, 6)
})

// 隐藏的模块（需要通过"更多"访问）
const hiddenModules = computed(() => {
  return allModules.filter(m => m.visible !== false).slice(6)
})

// 处理模块点击
const handleModuleClick = (module: ModuleTab) => {
  activeModule.value = module.key

  // 如果在项目详情页，则切换模块
  if (route.path.includes('/projects/')) {
    const projectId = route.params.projectId
    router.push(`/projects/${projectId}/${module.path}`)
  } else {
    // 否则跳转到对应页面
    router.push(`/${module.path}`)
  }
}

// 根据当前路由设置活动模块
const updateActiveModule = () => {
  const path = route.path
  if (path.includes('/projects/')) {
    const segments = path.split('/')
    const moduleKey = segments[segments.length - 1]
    activeModule.value = moduleKey
  } else if (path.startsWith('/dashboard')) {
    activeModule.value = 'overview'
  }
}

// 监听路由变化
import { watch } from 'vue'
watch(() => route.path, () => {
  updateActiveModule()
}, { immediate: true })
</script>

<style scoped>
.module-tabs-container {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 100%;
}

.module-tabs {
  display: flex;
  gap: 8px;
  height: 100%;
}

.module-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  height: 100%;
  color: var(--sidebar-text);
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: transform 0.3s, opacity 0.3s, border-color 0.3s, background-color 0.3s;
  font-size: 14px;
  position: relative;
}

.module-tab:hover {
  color: var(--top-nav-text-hover);
}

.module-tab.active {
  color: var(--module-tab-active);
  border-bottom-color: var(--module-tab-active);
}

.module-badge {
  margin-left: 4px;
}

.module-tab-more {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  height: 100%;
  color: var(--sidebar-text);
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.module-tab-more:hover {
  color: var(--top-nav-text-hover);
}

/* 图标样式 */
.module-tab .el-icon,
.module-tab-more .el-icon {
  font-size: 16px;
}
</style>
