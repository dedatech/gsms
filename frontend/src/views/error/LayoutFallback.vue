<template>
  <div class="layout-fallback">
    <div class="error-container">
      <el-result
        icon="error"
        title="页面加载失败"
        sub-title="主布局组件加载失败，正在尝试重新加载..."
      >
        <template #extra>
          <el-button type="primary" @click="reloadPage">重新加载</el-button>
          <el-button @click="goBack">返回</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const reloadPage = () => {
  window.location.reload()
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.layout-fallback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.error-container {
  max-width: 600px;
  padding: 20px;
}
</style>
