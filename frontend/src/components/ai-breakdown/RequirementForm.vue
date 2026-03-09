<template>
  <div class="requirement-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      @submit.prevent="handleSubmit"
    >
      <!-- 需求描述 -->
      <el-form-item label="需求描述" prop="requirement">
        <el-input
          v-model="formData.requirement"
          type="textarea"
          :rows="6"
          maxlength="5000"
          show-word-limit
          placeholder="请详细描述您要开发的功能需求，例如：&#10;• 实现用户登录、注册功能&#10;• 支持第三方账号登录（微信、QQ）&#10;• 包含找回密码功能&#10;• 需要记录登录日志"
          :disabled="loading"
        />
      </el-form-item>

      <!-- 项目类型 -->
      <el-form-item label="项目类型" prop="projectType">
        <el-select
          v-model="formData.projectType"
          placeholder="请选择项目类型"
          :disabled="loading"
          style="width: 100%"
        >
          <el-option
            v-for="type in projectTypes"
            :key="type.value"
            :label="type.label"
            :value="type.value"
          />
        </el-select>
      </el-form-item>

      <!-- 团队规模和期望完成时间 -->
      <el-form-item label="团队规模">
        <el-input-number
          v-model="formData.teamSize"
          :min="1"
          :max="50"
          :disabled="loading"
          placeholder="团队人数"
          style="width: 100%"
        />
        <div class="form-tip">选填，用于更准确的工时估算</div>
      </el-form-item>

      <el-form-item label="期望时间">
        <el-input-number
          v-model="formData.expectedDays"
          :min="1"
          :max="365"
          :disabled="loading"
          placeholder="天数"
          style="width: 100%"
        />
        <div class="form-tip">选填，期望完成时间（天）</div>
      </el-form-item>

      <!-- 操作按钮 -->
      <el-form-item>
        <div class="form-actions">
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ loading ? 'AI 正在分析中...' : '开始 AI 拆分' }}
          </el-button>
          <el-button @click="handleCancel" :disabled="loading">取消</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { RequirementBreakdownReq } from '@/api/ai'

// Props
const props = defineProps<{
  formData: RequirementBreakdownReq
  loading: boolean
}>()

// Emits
const emit = defineEmits<{
  'update:formData': [value: RequirementBreakdownReq]
  submit: [data: RequirementBreakdownReq]
  cancel: []
}>()

// 表单引用
const formRef = ref<FormInstance>()

// 项目类型选项
const projectTypes = [
  { label: 'Web 应用', value: 'Web应用' },
  { label: '移动应用', value: '移动应用' },
  { label: '桌面应用', value: '桌面应用' },
  { label: '小程序', value: '小程序' },
  { label: '后端服务', value: '后端服务' },
  { label: '数据分析', value: '数据分析' },
  { label: '其他', value: '其他' }
]

// 表单验证规则
const formRules: FormRules = {
  requirement: [
    { required: true, message: '请输入需求描述', trigger: 'blur' },
    { min: 10, message: '需求描述至少需要 10 个字符', trigger: 'blur' }
  ],
  projectType: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (valid) {
      emit('submit', props.formData)
    }
  })
}

// 取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.requirement-form {
  padding: 10px 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

:deep(.el-form-item__content) {
  display: flex;
  flex-direction: column;
}

:deep(.el-textarea__inner) {
  font-family: inherit;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>
