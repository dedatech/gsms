<template>
  <el-dialog
    v-model="dialogVisible"
    title="个人信息"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 用户头像和基本信息 -->
    <div class="profile-header">
      <el-avatar :size="80" :src="userInfo.avatar">
        {{ userInfo.username?.charAt(0).toUpperCase() }}
      </el-avatar>
      <div class="user-basic-info">
        <h3>{{ userInfo.nickname || userInfo.username }}</h3>
        <p class="username">@{{ userInfo.username }}</p>
      </div>
    </div>

    <!-- 表单 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="profile-form"
    >
      <!-- 基本信息区域 -->
      <div class="form-section">
        <div class="section-title">基本信息</div>

        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" disabled />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input
            v-model="formData.nickname"
            placeholder="请输入昵称"
            maxlength="20"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
            type="email"
          />
        </el-form-item>

        <el-form-item label="电话" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入电话"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="部门">
          <el-input v-model="userInfo.departmentName" disabled />
        </el-form-item>

        <el-form-item label="创建时间">
          <el-input v-model="userInfo.createTime" disabled />
        </el-form-item>
      </div>

      <!-- 修改密码区域 -->
      <div class="form-section">
        <div class="section-title">修改密码</div>
        <p class="section-tip">留空则不修改密码</p>

        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="formData.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="formData.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="formData.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
            clearable
          />
        </el-form-item>
      </div>
    </el-form>

    <!-- 底部按钮 -->
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getCurrentUserInfo, updateCurrentUserInfo, changePassword, type UserInfo } from '@/api/user'

// Props
interface Props {
  visible: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'success': []
}>()

// 对话框显示状态
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// Form ref
const formRef = ref<FormInstance>()

// Auth store
const authStore = useAuthStore()

// 用户信息
const userInfo = ref<Partial<UserInfo>>({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  departmentName: '',
  createTime: ''
})

// 表单数据
const formData = reactive({
  nickname: '',
  email: '',
  phone: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 保存中状态
const saving = ref(false)

// 表单验证规则
const formRules: FormRules = {
  nickname: [
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  newPassword: [
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (rule, value, callback) => {
        if (formData.newPassword && value !== formData.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const data = await getCurrentUserInfo()
    userInfo.value = data

    // 填充表单
    formData.nickname = data.nickname || ''
    formData.email = data.email || ''
    formData.phone = data.phone || ''
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
  }
}

// 保存
const handleSave = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    saving.value = true

    // 准备基本信息更新数据
    const updateData: any = {}
    if (formData.nickname !== userInfo.value.nickname) {
      updateData.nickname = formData.nickname
    }
    if (formData.email !== userInfo.value.email) {
      updateData.email = formData.email
    }
    if (formData.phone !== userInfo.value.phone) {
      updateData.phone = formData.phone
    }

    // 判断是否需要修改密码
    const needChangePassword = formData.oldPassword && formData.newPassword

    // 并发执行更新操作
    const promises = []

    if (Object.keys(updateData).length > 0) {
      promises.push(updateCurrentUserInfo(updateData))
    }

    if (needChangePassword) {
      promises.push(changePassword({
        oldPassword: formData.oldPassword,
        newPassword: formData.newPassword
      }))
    }

    if (promises.length === 0) {
      ElMessage.info('没有任何修改')
      return
    }

    await Promise.all(promises)

    // 更新 auth store 中的用户信息
    if (updateData.nickname) {
      authStore.updateUserInfo({ username: updateData.nickname })
    }

    ElMessage.success('保存成功')

    // 清空密码字段
    formData.oldPassword = ''
    formData.newPassword = ''
    formData.confirmPassword = ''

    // 重新加载用户信息
    await loadUserInfo()

    emit('success')
  } catch (error: any) {
    console.error('保存失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('保存失败，请重试')
    }
  } finally {
    saving.value = false
  }
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}

// 关闭对话框
const handleClose = () => {
  // 清空密码字段
  formData.oldPassword = ''
  formData.newPassword = ''
  formData.confirmPassword = ''
}

// 监听对话框显示状态
watch(() => props.visible, (visible) => {
  if (visible) {
    loadUserInfo()
    // 重置表单验证
    formRef.value?.clearValidate()
  }
})
</script>

<style scoped>
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 20px;
}

.user-basic-info h3 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.username {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.profile-form {
  max-height: 500px;
  overflow-y: auto;
}

.form-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.section-tip {
  margin: -12px 0 12px 0;
  font-size: 12px;
  color: #999;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 输入框禁用状态样式 */
:deep(.el-input.is-disabled .el-input__inner) {
  background-color: #f5f7fa;
  color: #909399;
}
</style>
