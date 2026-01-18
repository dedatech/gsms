<template>
  <div class="project-selector">
    <el-dropdown trigger="click" @command="handleSelectProject">
      <div class="project-brand-sub">
        <span class="current-project-name">
          {{ currentProjectName }}
        </span>
        <el-icon class="el-icon--right"><ArrowDown /></el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <!-- 最近访问的项目 -->
          <div v-if="recentProjects.length > 0" class="project-section">
            <div class="section-title">最近访问</div>
            <el-dropdown-item
              v-for="project in recentProjects"
              :key="project.id"
              :command="project.id"
            >
              <div class="project-item">
                <div class="project-dot" :style="{ backgroundColor: getProjectColor(project.status) }"></div>
                <span class="project-name">{{ project.name }}</span>
              </div>
            </el-dropdown-item>
          </div>

          <!-- 所有项目 -->
          <div class="project-section">
            <div class="section-title">所有项目</div>
            <el-dropdown-item
              v-for="project in allProjects"
              :key="project.id"
              :command="project.id"
            >
              <div class="project-item">
                <div class="project-dot" :style="{ backgroundColor: getProjectColor(project.status) }"></div>
                <span class="project-name">{{ project.name }}</span>
              </div>
            </el-dropdown-item>
          </div>

          <!-- 查看所有项目 -->
          <el-dropdown-item divided command="view-all">
            <el-icon><Management /></el-icon>
            <span>查看所有项目</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Management } from '@element-plus/icons-vue'
import { useProjectStore } from '@/stores/project'
import type { ProjectInfoResp } from '@/api/project'

const router = useRouter()
const projectStore = useProjectStore()

// 当前项目名称
const currentProjectName = computed(() => {
  return projectStore.currentProject?.name || '请选择项目'
})

// 最近访问的项目
const recentProjects = computed(() => {
  return projectStore.recentProjectList.slice(0, 5)
})

// 所有项目（排除最近访问的）
const allProjects = computed(() => {
  const recentIds = new Set(recentProjects.value.map(p => p.id))
  return projectStore.projectList.filter(p => !recentIds.has(p.id))
})

// 获取项目状态颜色
const getProjectColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'NOT_STARTED': '#d9d9d9',
    'IN_PROGRESS': '#1890ff',
    'COMPLETED': '#52c41a',
    'ON_HOLD': '#faad14'
  }
  return colorMap[status] || '#d9d9d9'
}

// 处理项目选择
const handleSelectProject = async (command: string | number | object) => {
  if (command === 'view-all') {
    // 跳转到项目列表页
    router.push('/projects')
    return
  }

  const projectId = typeof command === 'number' ? command : parseInt(command as string)

  try {
    // 设置当前项目
    await projectStore.setCurrentProject(projectId)

    // 跳转到项目详情页
    router.push(`/projects/${projectId}`)

    ElMessage.success(`已切换到项目：${projectStore.currentProject?.name}`)
  } catch (error) {
    ElMessage.error('切换项目失败')
    console.error('切换项目失败:', error)
  }
}

// 组件挂载时加载项目列表
onMounted(async () => {
  try {
    if (projectStore.projectList.length === 0) {
      await projectStore.fetchProjectList()
    }
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
})
</script>

<style scoped>
.project-selector {
  display: inline-block;
}

.current-project-name {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-section {
  padding: 4px 0;
}

.section-title {
  padding: 8px 16px;
  font-size: 12px;
  color: #999;
  font-weight: 500;
}

.project-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.project-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.project-name {
  font-size: 14px;
  color: #333;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 下拉菜单样式优化 */
:deep(.el-dropdown-menu__item) {
  padding: 8px 16px;
  line-height: 1.5;
}

:deep(.el-dropdown-menu__item:hover) {
  background: #f5f5f5;
}
</style>
