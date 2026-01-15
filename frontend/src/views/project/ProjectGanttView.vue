<template>
  <div class="project-gantt-view">
    <div class="page-header">
      <h2 class="page-title">项目甘特图</h2>
      <div class="page-actions">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      </div>
    </div>

    <ProjectGantt :project-id="projectId" v-if="projectId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import ProjectGantt from '@/components/ProjectGantt.vue'

const route = useRoute()
const router = useRouter()
const projectId = ref<number>()

onMounted(() => {
  const id = route.params.id as string
  if (id) {
    projectId.value = Number(id)
  }
})

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.project-gantt-view {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.page-actions {
  display: flex;
  gap: 12px;
}
</style>
