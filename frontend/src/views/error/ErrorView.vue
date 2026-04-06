<template>
  <div class="error-view">
    <el-result
      icon="error"
      title="页面加载失败"
      sub-title="页面组件加载失败，请刷新页面重试"
    >
      <template #extra>
        <el-button type="primary" @click="reloadPage">刷新页面</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const reloadPage = () => {
  window.location.reload()
}

const goHome = () => {
  router.push('/login').catch(() => {
    window.location.href = '/login'
  })
}
</script>

<style scoped>
.error-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
</style>
