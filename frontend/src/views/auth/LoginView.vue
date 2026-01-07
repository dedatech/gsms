<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>GSMS 工时管理系统</h2>
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

const router = useRouter()
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
      const res: any = await login(loginForm)
      console.log('登录响应:', res) // 调试日志

      // 根据实际后端响应结构调整
      const token = res.token || res.data?.token || res
      console.log('保存的 token:', token) // 调试日志

      if (!token) {
        throw new Error('登录失败：未获取到 token')
      }

      // 保存 token 和用户名
      localStorage.setItem('token', token)
      localStorage.setItem('username', loginForm.username)
      ElMessage.success('登录成功')
      // 跳转到首页
      router.push('/projects')
    } catch (error: any) {
      console.error('登录错误:', error)
      ElMessage.error(error.message || '登录失败')
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

.login-card h2 {
  margin: 0;
  text-align: center;
  color: #333;
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
