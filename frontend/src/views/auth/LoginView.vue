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

        <el-form-item label="验证码" prop="captchaCode">
          <div style="display: flex; gap: 12px">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="请输入验证码"
              style="flex: 1"
              maxlength="4"
            />
            <div
              class="captcha-image"
              :title="'点击刷新验证码'"
              @click="fetchCaptcha"
            >
              <img
                v-if="captchaImage"
                :src="captchaImage"
                alt="验证码"
                style="height: 40px; cursor: pointer"
              />
              <span v-else style="color: #999">加载中...</span>
            </div>
          </div>
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
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { login, getCaptcha } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaUuid: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度为4位', trigger: 'blur' }
  ]
}

// 获取验证码
const fetchCaptcha = async () => {
  try {
    const res = await getCaptcha()
    console.log('验证码响应:', res)

    if (res && res.uuid && res.image) {
      loginForm.captchaUuid = res.uuid
      captchaImage.value = res.image
      // 清空验证码输入框
      loginForm.captchaCode = ''
    } else {
      ElMessage.error('获取验证码失败')
    }
  } catch (error) {
    console.error('获取验证码错误:', error)
    ElMessage.error('获取验证码失败')
  }
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

      // 加载用户权限和角色信息
      await authStore.refreshAuth()

      ElMessage.success('登录成功')
      // 跳转到Dashboard首页
      router.push('/dashboard')
    } catch (error: unknown) {
      console.error('登录错误:', error)
      const errorMsg = error instanceof Error ? error.message : '登录失败'
      ElMessage.error(errorMsg)
      // 登录失败后刷新验证码
      fetchCaptcha()
    } finally {
      loading.value = false
    }
  })
}

// 组件挂载时获取验证码
onMounted(() => {
  fetchCaptcha()
})
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

.captcha-image {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #f5f5f5;
  transition: all 0.3s;
}

.captcha-image:hover {
  border-color: #409eff;
  background-color: #fff;
}
</style>
