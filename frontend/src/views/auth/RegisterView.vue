<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>用户注册</h2>
      </template>
      <p>注册功能开发中...</p>
      <el-button @click="$router.push('/login')">返回登录</el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
// TODO: 实现注册功能
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-card {
  width: 400px;
  text-align: center;
}
</style>
