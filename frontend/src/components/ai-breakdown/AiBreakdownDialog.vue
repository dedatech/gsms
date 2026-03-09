<template>
  <el-dialog
    v-model="dialogVisible"
    title="AI 需求拆分"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- 步骤1：填写需求信息 -->
    <RequirementForm
      v-if="currentStep === 1"
      v-model:form-data="formData"
      :loading="loading"
      @submit="handleSubmit"
      @cancel="handleClose"
    />

    <!-- 步骤2：查看拆分结果 -->
    <BreakdownResult
      v-if="currentStep === 2"
      :result="breakdownResult"
      :loading="loading"
      @batch-create="handleBatchCreate"
      @retry="handleRetry"
      @close="handleClose"
    />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { RequirementBreakdownReq, RequirementBreakdownResp } from '@/api/ai'
import { breakdownRequirement } from '@/api/ai'
import type { TaskCreateReq } from '@/api/task'
import { createTask } from '@/api/task'
import { useAuthStore } from '@/stores/auth'
import RequirementForm from './RequirementForm.vue'
import BreakdownResult from './BreakdownResult.vue'

// Props
const props = defineProps<{
  iterationId?: number
  projectId?: number
  parentTaskId?: number  // 被拆分的父任务 ID
}>()

// Emits
const emit = defineEmits<{
  success: []
}>()

// 获取当前用户信息
const authStore = useAuthStore()

// 对话框状态
const dialogVisible = ref(false)
const currentStep = ref(1)
const loading = ref(false)

// 表单数据
const formData = reactive<RequirementBreakdownReq>({
  requirement: '',
  projectType: '',
  teamSize: undefined,
  expectedDays: undefined
})

// 拆分结果
const breakdownResult = ref<RequirementBreakdownResp | null>(null)

// 优先级映射
const priorityMap: Record<string, string> = {
  '高': 'HIGH',
  '中': 'MEDIUM',
  '低': 'LOW'
}

// 打开对话框
const open = () => {
  dialogVisible.value = true
  currentStep.value = 1
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.requirement = ''
  formData.projectType = ''
  formData.teamSize = undefined
  formData.expectedDays = undefined
  breakdownResult.value = null
}

// 提交需求拆分
const handleSubmit = async (data: RequirementBreakdownReq) => {
  loading.value = true
  try {
    // 响应拦截器已经提取了 data，response 直接就是 RequirementBreakdownResp
    const result: RequirementBreakdownResp = await breakdownRequirement(data)
    breakdownResult.value = result
    currentStep.value = 2
    ElMessage.success('需求拆分成功！')
  } catch (error: any) {
    console.error('需求拆分失败:', error)
    ElMessage.error(error.message || '需求拆分失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 批量创建任务
const handleBatchCreate = async () => {
  if (!breakdownResult.value?.subTasks || !props.projectId) {
    ElMessage.error('缺少必要信息')
    return
  }

  loading.value = true
  let successCount = 0
  let failCount = 0

  try {
    for (const subTask of breakdownResult.value.subTasks) {
      try {
        const taskData: TaskCreateReq = {
          title: subTask.title,
          description: subTask.description,
          projectId: props.projectId,
          iterationId: props.iterationId,
          parentId: props.parentTaskId,  // 设置父任务 ID
          priority: priorityMap[subTask.priority] || 'MEDIUM',
          status: 'TODO',
          estimateHours: Math.round(subTask.estimatedDays * 8) // 人天转小时
        }

        await createTask(taskData)
        successCount++
      } catch (error) {
        console.error(`创建任务失败: ${subTask.title}`, error)
        failCount++
      }
    }

    if (successCount > 0) {
      ElMessage.success(`成功创建 ${successCount} 个任务${failCount > 0 ? `，${failCount} 个失败` : ''}`)
      emit('success')
      dialogVisible.value = false
    } else {
      ElMessage.error('任务创建失败')
    }
  } catch (error: any) {
    console.error('批量创建任务失败:', error)
    ElMessage.error(error.message || '批量创建任务失败')
  } finally {
    loading.value = false
  }
}

// 重新拆分
const handleRetry = () => {
  currentStep.value = 1
  breakdownResult.value = null
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 暴露方法供外部调用
defineExpose({
  open
})
</script>

<style scoped>
:deep(.el-dialog__body) {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}
</style>
