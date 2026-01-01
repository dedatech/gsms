<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面未找到</p>
    <el-button @click="$router.push('/projects')">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
.not-found h1 {
  font-size: 72px;
  color: #409eff;
}
</style>
