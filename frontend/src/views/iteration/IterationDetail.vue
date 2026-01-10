<template>
  <div class="iteration-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2 class="page-title">{{ iteration?.name || '加载中...' }}</h2>
        <el-tag :type="getStatusType(iteration?.status)" size="large">
          {{ getStatusText(iteration?.status) }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button :icon="Edit" @click="handleEdit">编辑</el-button>
        <el-button :icon="Delete" type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="info">
        <el-collapse v-model="activeCollapse" class="info-collapse">
          <el-collapse-item name="basic" title="迭代基本信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="迭代ID">{{ iteration?.id }}</el-descriptions-item>
              <el-descriptions-item label="所属项目">
                <el-link type="primary" @click="goToProject">
                  {{ iteration?.projectName }}
                </el-link>
              </el-descriptions-item>
              <el-descriptions-item label="迭代名称" :span="2">{{ iteration?.name }}</el-descriptions-item>
              <el-descriptions-item label="迭代状态">
                <el-tag :type="getStatusType(iteration?.status)">
                  {{ getStatusText(iteration?.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="迭代描述" :span="2">
                <div class="description-content">{{ iteration?.description || '暂无描述' }}</div>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item name="date" title="时间信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="计划开始时间">
                {{ iteration?.planStartDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="计划结束时间">
                {{ iteration?.planEndDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="实际开始时间">
                <span :class="{ 'text-danger': !iteration?.actualStartDate && (iteration?.status === 'IN_PROGRESS' || iteration?.status === 'COMPLETED') }">
                  {{ iteration?.actualStartDate || '-' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="实际结束时间">
                {{ iteration?.actualEndDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间" :span="2">
                {{ formatDateTime(iteration?.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间" :span="2">
                {{ formatDateTime(iteration?.updateTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item name="creator" title="创建信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="创建人">
                {{ iteration?.createUserName }}
              </el-descriptions-item>
              <el-descriptions-item label="更新人">
                {{ iteration?.updateUserName }}
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>

      <!-- 关联任务 -->
      <el-tab-pane name="tasks">
        <template #label>
          <span>
            <el-icon><Files /></el-icon>
            关联任务
          </span>
        </template>
        <div class="tab-content">
          <el-empty description="关联任务功能开发中" :image-size="100" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑迭代对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑迭代"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editFormData" :rules="editFormRules" label-width="100px">
        <el-form-item label="迭代名称" prop="name">
          <el-input v-model="editFormData.name" placeholder="请输入迭代名称" />
        </el-form-item>
        <el-form-item label="迭代描述" prop="description">
          <el-input
            v-model="editFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入迭代描述"
          />
        </el-form-item>
        <el-form-item label="迭代状态" prop="status">
          <el-select v-model="editFormData.status" placeholder="请选择" style="width: 100%">
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始时间" prop="planStartDate">
          <el-date-picker
            v-model="editFormData.planStartDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="计划结束时间" prop="planEndDate">
          <el-date-picker
            v-model="editFormData.planEndDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Delete,
  Files
} from '@element-plus/icons-vue'
import { getIterationDetail, updateIteration, deleteIteration } from '@/api/iteration'
import { getIterationStatusInfo } from '@/utils/statusMapping'

const route = useRoute()
const router = useRouter()
const iterationId = computed(() => Number(route.params.id))

// 当前激活的标签页
const activeTab = ref('info')

// 折叠面板激活项
const activeCollapse = ref(['basic', 'date', 'creator'])

// 迭代信息
const iteration = ref<any>(null)

// 编辑对话框
const editDialogVisible = ref(false)
const editSubmitLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editFormData = reactive({
  name: '',
  description: '',
  status: '',
  planStartDate: '',
  planEndDate: ''
})
const editFormRules: FormRules = {
  name: [{ required: true, message: '请输入迭代名称', trigger: 'blur' }]
}

// 获取迭代详情
const fetchIteration = async () => {
  try {
    const res: any = await getIterationDetail(iterationId.value)
    iteration.value = res
  } catch (error) {
    console.error('获取迭代详情失败:', error)
    ElMessage.error('获取迭代详情失败')
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 跳转到项目详情
const goToProject = () => {
  if (iteration.value?.projectId) {
    router.push(`/projects/${iteration.value.projectId}`)
  }
}

// 编辑迭代
const handleEdit = () => {
  if (!iteration.value) return

  editFormData.name = iteration.value.name
  editFormData.description = iteration.value.description || ''
  editFormData.status = iteration.value.status || ''
  editFormData.planStartDate = iteration.value.planStartDate || ''
  editFormData.planEndDate = iteration.value.planEndDate || ''

  editDialogVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitLoading.value = true
      try {
        await updateIteration({
          id: iterationId.value,
          name: editFormData.name,
          description: editFormData.description,
          status: editFormData.status,
          planStartDate: editFormData.planStartDate,
          planEndDate: editFormData.planEndDate
        })
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchIteration()
      } catch (error) {
        console.error('更新迭代失败:', error)
      } finally {
        editSubmitLoading.value = false
      }
    }
  })
}

// 删除迭代
const handleDelete = () => {
  ElMessageBox.confirm(`确定要删除迭代 "${iteration.value?.name}" 吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteIteration(iterationId.value)
        ElMessage.success('删除成功')
        router.push('/iterations')
      } catch (error) {
        console.error('删除迭代失败:', error)
      }
    })
    .catch(() => {})
}

// 获取迭代状态信息
const getStatusType = (status: string) => getIterationStatusInfo(status).type
const getStatusText = (status: string) => getIterationStatusInfo(status).text

// 格式化日期时间
const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date
}

onMounted(() => {
  fetchIteration()
})
</script>

<style scoped>
.iteration-detail {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 标签页 */
.detail-tabs {
  background: #fff;
  padding: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 24px;
}

:deep(.el-tabs__item .el-icon) {
  margin-right: 4px;
  vertical-align: -2px;
}

/* 标签页内容 */
.tab-content {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 折叠面板 */
.info-collapse {
  margin-bottom: 24px;
}

:deep(.el-collapse-item__header) {
  font-size: 16px;
  font-weight: 500;
  padding: 16px 0;
  background: transparent;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-collapse-item__wrap) {
  background: transparent;
}

:deep(.el-collapse-item__content) {
  padding: 16px 0;
}

.description-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  color: #333;
}

.text-danger {
  color: #f56c6c;
}

:deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 500;
}

:deep(.el-descriptions__content) {
  color: #333;
}

:deep(.el-empty) {
  padding: 60px 0;
}
</style>
