<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>用户注册</h2>
          <p class="subtitle">GSMS 工时管理系统</p>
        </div>
      </template>

      <!-- 注册成功提示 -->
      <div v-if="registerSuccess" class="success-box">
        <el-result icon="success" title="注册成功" sub-title="注册成功，请联系管理员进行审核">
          <template #extra>
            <el-descriptions :column="1" border class="success-info">
              <el-descriptions-item label="用户名">{{ registerData.username }}</el-descriptions-item>
              <el-descriptions-item label="姓名">{{ registerData.nickname }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ registerData.email }}</el-descriptions-item>
            </el-descriptions>
            <div class="notice-box">
              <el-alert
                title="温馨提示"
                type="info"
                :closable="false"
                show-icon
              >
                <template #default>
                  <p>您的账号已创建成功，但当前处于<strong style="color: #E6A23C">待审核</strong>状态。</p>
                  <p>请联系系统管理员启用您的账号后，方可正常登录。</p>
                </template>
              </el-alert>
            </div>
            <el-button type="primary" size="large" @click="goToLogin" class="action-btn">
              返回登录
            </el-button>
          </template>
        </el-result>
      </div>

      <!-- 注册表单 -->
      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="80px"
        size="large"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20个字符）"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="姓名" prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入您的真实姓名"
            :prefix-icon="UserFilled"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="login-link">
          已有账号？
          <el-link type="primary" @click="goToLogin">立即登录</el-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, UserFilled, Message, Phone, Lock } from '@element-plus/icons-vue'
import { register, type RegisterReq } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const registerSuccess = ref(false)
const registerData = ref<RegisterReq>({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

// 自定义验证规则
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    const data: RegisterReq = {
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname,
      email: registerForm.email,
      phone: registerForm.phone
    }

    await register(data)

    // 保存注册信息用于成功页面显示
    registerData.value = data
    registerSuccess.value = true

    ElMessage.success('注册成功')
  } catch (error: any) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('注册失败:', error)
      ElMessage.error(error.message || '注册失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 返回登录
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 500px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.success-box {
  padding: 20px 0;
}

.success-info {
  margin: 20px 0;
}

.notice-box {
  margin: 20px 0;
}

.notice-box p {
  margin: 8px 0;
  line-height: 1.6;
}

.action-btn {
  margin-top: 20px;
}

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input__wrapper) {
  padding: 8px 15px;
}

:deep(.el-result__subtitle) {
  margin-top: 10px;
}
</style>
