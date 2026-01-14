<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="login-header">
          <img src="@/assets/logo/logo-tm-letters.svg" alt="TeamMaster" class="login-logo" />
          <h2>TeamMaster</h2>
          <p class="login-subtitle">统领工时管理平台</p>
        </div>
      </template>

      <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="footer-links">
            <router-link to="/register">还没有账号？立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(loginForm)
      console.log('登录响应:', res)

      // 后端返回的data直接是token字符串
      const token = res || res.data?.token
      console.log('获取的 token:', token)

      if (!token) {
        throw new Error('登录失败：未获取到 token')
      }

      // 使用 auth store 保存认证信息
      authStore.setAuth(token, loginForm.username)

      ElMessage.success('登录成功')
      // 跳转到Dashboard首页
      router.push('/dashboard')
    } catch (error: unknown) {
      console.error('登录错误:', error)
      const errorMsg = error instanceof Error ? error.message : '登录失败'
      ElMessage.error(errorMsg)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.login-header {
  text-align: center;
  padding: 10px 0;
}

.login-logo {
  width: 64px;
  height: 64px;
  margin-bottom: 10px;
}

.login-header h2 {
  margin: 10px 0 5px;
  text-align: center;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.login-subtitle {
  margin: 0;
  text-align: center;
  color: #666;
  font-size: 14px;
}

.footer-links {
  width: 100%;
  text-align: center;
}

.footer-links a {
  color: #409eff;
  text-decoration: none;
}

.footer-links a:hover {
  text-decoration: underline;
}
</style>
