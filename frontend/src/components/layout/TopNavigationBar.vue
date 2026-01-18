<template>
  <div class="top-navigation">
    <!-- 项目页面：显示完整导航 -->
    <template v-if="isProjectPage">
      <!-- 左侧：当前项目区 -->
      <div class="top-nav-left">
        <div class="current-project-label">
          <el-icon><FolderOpened /></el-icon>
          <span>当前项目</span>
        </div>
        <ProjectSelector />
      </div>

      <!-- 中间：功能模块区 -->
      <div class="top-nav-center">
        <ModuleTabs />
      </div>

      <!-- 右侧：操作区 -->
      <div class="top-nav-right">
        <!-- 设置按钮 -->
        <el-tooltip content="设置" placement="bottom">
          <el-button
            :icon="Setting"
            circle
            text
            @click="handleSettings"
          />
        </el-tooltip>

        <!-- 添加按钮 -->
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新建
        </el-button>
      </div>
    </template>

    <!-- 非项目页面：导航栏为空 -->
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Setting, Plus, FolderOpened } from '@element-plus/icons-vue'
import ProjectSelector from './ProjectSelector.vue'
import ModuleTabs from './ModuleTabs.vue'

const route = useRoute()

// 判断是否在项目详情页面
// 路由格式：/projects/:id 或 /projects/:id/xxx
const isProjectPage = computed(() => {
  const path = route.path
  // 匹配 /projects/数字 或 /projects/数字/xxx
  return /^\/projects\/\d+(\/|$)/.test(path)
})

// 处理设置按钮点击
const handleSettings = () => {
  // TODO: 打开设置对话框或跳转到设置页
  console.log('打开设置')
}

// 处理添加按钮点击
const handleAdd = () => {
  // TODO: 根据当前上下文显示添加菜单
  console.log('新建项目/任务')
}
</script>

<style scoped>
.top-navigation {
  height: var(--top-nav-height);
  background: var(--top-nav-bg);
  border-bottom: 1px solid var(--top-nav-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.top-nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.current-project-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.current-project-label .el-icon {
  font-size: 16px;
}

.top-nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 0; /* 防止flex子项溢出 */
}

.top-nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
</style>
