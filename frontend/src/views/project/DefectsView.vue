<template>
  <div class="defects-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">缺陷管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreateDefect">
          新建缺陷
        </el-button>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索缺陷标题或编号"
          :prefix-icon="Search"
          clearable
          style="width: 300px"
          @input="handleSearch"
        />
        <el-button :icon="Filter" @click="showFilter = !showFilter">
          筛选
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div v-if="showFilter" class="filter-bar">
      <el-form :model="searchForm" inline>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="全部状态"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="待修复" value="TODO" />
            <el-option label="修复中" value="IN_PROGRESS" />
            <el-option label="待验证" value="TESTING" />
            <el-option label="已关闭" value="DONE" />
            <el-option label="重新打开" value="REOPENED" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select
            v-model="searchForm.priority"
            placeholder="全部优先级"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select
            v-model="searchForm.severity"
            placeholder="全部严重程度"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="轻微" value="TRIVIAL" />
            <el-option label="次要" value="MINOR" />
            <el-option label="主要" value="MAJOR" />
            <el-option label="严重" value="CRITICAL" />
            <el-option label="致命" value="BLOCKER" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select
            v-model="searchForm.assigneeId"
            placeholder="全部负责人"
            clearable
            filterable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleResetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 缺陷列表 -->
    <div class="defects-table">
      <el-table
        :data="defects"
        v-loading="loading"
        stripe
        @row-click="handleViewDefect"
        style="cursor: pointer"
      >
        <el-table-column prop="id" label="编号" width="100">
          <template #default="{ row }">
            <span class="defect-number">#{{ row.id }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="300">
          <template #default="{ row }">
            <div class="defect-title">
              <el-tag
                :type="getSeverityType(row.severity)"
                size="small"
                style="margin-right: 8px"
              >
                {{ getSeverityText(row.severity) }}
              </el-tag>
              <span>{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="120">
          <template #default="{ row }">
            <span>{{ row.assigneeName || '未分配' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              :icon="View"
              @click.stop="handleViewDefect(row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              :icon="Edit"
              @click.stop="handleEditDefect(row)"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              :icon="Delete"
              @click.stop="handleDeleteDefect(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 新建/编辑缺陷对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="缺陷标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="请输入缺陷标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="缺陷描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述缺陷现象"
          />
        </el-form-item>

        <el-form-item label="复现步骤">
          <el-input
            v-model="formData.reproductionSteps"
            type="textarea"
            :rows="4"
            placeholder="请描述如何复现该缺陷"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="严重程度" prop="severity">
              <el-select v-model="formData.severity" style="width: 100%">
                <el-option label="致命" value="BLOCKER" />
                <el-option label="严重" value="CRITICAL" />
                <el-option label="主要" value="MAJOR" />
                <el-option label="次要" value="MINOR" />
                <el-option label="轻微" value="TRIVIAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="formData.priority">
                <el-radio value="LOW">低</el-radio>
                <el-radio value="MEDIUM">中</el-radio>
                <el-radio value="HIGH">高</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属迭代">
              <el-select
                v-model="formData.iterationId"
                placeholder="选填"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="iter in iterations"
                  :key="iter.id"
                  :label="iter.name"
                  :value="iter.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="assigneeId">
              <el-select
                v-model="formData.assigneeId"
                placeholder="请选择负责人"
                filterable
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="user in availableUsers"
                  :key="user.id"
                  :label="user.nickname"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="附件">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            :auto-upload="false"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :limit="5"
            multiple
          >
            <el-button :icon="Upload">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持图片、视频等文件，单个文件不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 缺陷详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="缺陷详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="currentDefect" class="defect-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="缺陷编号">
            #{{ currentDefect.id }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDefect.status)" size="small">
              {{ getStatusText(currentDefect.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">
            {{ currentDefect.title }}
          </el-descriptions-item>
          <el-descriptions-item label="严重程度">
            <el-tag :type="getSeverityType(currentDefect.severity)" size="small">
              {{ getSeverityText(currentDefect.severity) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentDefect.priority)" size="small">
              {{ getPriorityText(currentDefect.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">
            {{ currentDefect.assigneeName || '未分配' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(currentDefect.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="缺陷描述" :span="2">
            {{ currentDefect.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="复现步骤" :span="2">
            {{ currentDefect.reproductionSteps || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 状态流转 -->
        <div class="status-workflow">
          <h4>状态流转</h4>
          <div class="workflow-actions">
            <el-button
              v-if="canChangeStatus('TODO')"
              size="small"
              @click="handleChangeStatus('TODO')"
            >
              设为待修复
            </el-button>
            <el-button
              v-if="canChangeStatus('IN_PROGRESS')"
              size="small"
              type="primary"
              @click="handleChangeStatus('IN_PROGRESS')"
            >
              开始修复
            </el-button>
            <el-button
              v-if="canChangeStatus('TESTING')"
              size="small"
              type="warning"
              @click="handleChangeStatus('TESTING')"
            >
              提交验证
            </el-button>
            <el-button
              v-if="canChangeStatus('DONE')"
              size="small"
              type="success"
              @click="handleChangeStatus('DONE')"
            >
              验证通过
            </el-button>
            <el-button
              v-if="canChangeStatus('REOPENED')"
              size="small"
              type="danger"
              @click="handleChangeStatus('REOPENED')"
            >
              重新打开
            </el-button>
          </div>
        </div>

        <!-- 评论 -->
        <div class="comments-section">
          <h4>评论</h4>
          <div class="comment-input">
            <el-input
              v-model="newComment"
              type="textarea"
              :rows="3"
              placeholder="添加评论..."
            />
            <el-button
              type="primary"
              size="small"
              style="margin-top: 8px"
              @click="handleAddComment"
            >
              发表评论
            </el-button>
          </div>
          <div class="comment-list">
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-header">
                <span class="comment-author">{{ comment.authorName }}</span>
                <span class="comment-time">{{ formatDateTime(comment.createTime) }}</span>
              </div>
              <div class="comment-content">{{ comment.content }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadUserFile, type UploadFile } from 'element-plus'
import {
  Plus,
  Search,
  Filter,
  View,
  Edit,
  Delete,
  Upload
} from '@element-plus/icons-vue'
import { getTasksByProjectId, createTask, updateTask, deleteTask, updateTaskStatus, type TaskInfo } from '@/api/task'
import { getIterationList, type IterationInfo } from '@/api/iteration'
import { getProjectMembers } from '@/api/project'
import type { UserInfo } from '@/api/user'

interface Props {
  projectId: number
}

const props = defineProps<Props>()

// 数据定义
const defects = ref<TaskInfo[]>([])
const total = ref(0)
const loading = ref(false)
const showFilter = ref(false)

// 可用用户列表
const availableUsers = ref<UserInfo[]>([])
const iterations = ref<IterationInfo[]>([])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: undefined as string | undefined,
  priority: undefined as string | undefined,
  severity: undefined as string | undefined,
  assigneeId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 20
})

// 新建/编辑对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => formData.id ? '编辑缺陷' : '新建缺陷')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  title: '',
  description: '',
  reproductionSteps: '',
  severity: 'MAJOR',
  priority: 'MEDIUM',
  assigneeId: undefined as number | undefined,
  iterationId: undefined as number | undefined
})
const formRules: FormRules = {
  title: [{ required: true, message: '请输入缺陷标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入缺陷描述', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择严重程度', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

// 附件列表
const fileList = ref<UploadUserFile[]>([])

// 详情对话框
const detailDialogVisible = ref(false)
const currentDefect = ref<TaskInfo | null>(null)
const newComment = ref('')
const comments = ref<any[]>([])

// 获取项目成员
const fetchMembers = async () => {
  try {
    const res = await getProjectMembers(props.projectId)
    availableUsers.value = (res || []).map((m: any) => ({
      id: m.userId,
      nickname: m.nickname,
      username: m.username
    }))
  } catch (error) {
    console.error('获取项目成员失败:', error)
  }
}

// 获取迭代列表
const fetchIterations = async () => {
  try {
    const res = await getIterationList({
      projectId: props.projectId,
      pageNum: 1,
      pageSize: 100
    })
    iterations.value = res.list || []
  } catch (error) {
    console.error('获取迭代列表失败:', error)
  }
}

// 获取缺陷列表
const fetchDefects = async () => {
  loading.value = true
  try {
    const res = await getTasksByProjectId(
      props.projectId,
      searchForm.pageNum,
      searchForm.pageSize
    )
    // 筛选类型为 BUG 的任务
    defects.value = (res.list || []).filter(task => task.type === 'BUG')
    total.value = res.total || 0
  } catch (error) {
    console.error('获取缺陷列表失败:', error)
    ElMessage.error('获取缺陷列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchDefects()
}

// 重置筛选
const handleResetFilter = () => {
  searchForm.keyword = ''
  searchForm.status = undefined
  searchForm.priority = undefined
  searchForm.severity = undefined
  searchForm.assigneeId = undefined
  handleSearch()
}

// 分页变化
const handlePageChange = (page: number) => {
  searchForm.pageNum = page
  fetchDefects()
}

const handleSizeChange = (size: number) => {
  searchForm.pageSize = size
  searchForm.pageNum = 1
  fetchDefects()
}

// 新建缺陷
const handleCreateDefect = () => {
  Object.assign(formData, {
    id: undefined,
    title: '',
    description: '',
    reproductionSteps: '',
    severity: 'MAJOR',
    priority: 'MEDIUM',
    assigneeId: undefined,
    iterationId: undefined
  })
  fileList.value = []
  dialogVisible.value = true
}

// 编辑缺陷
const handleEditDefect = (defect: TaskInfo) => {
  Object.assign(formData, {
    id: defect.id,
    title: defect.title,
    description: defect.description,
    reproductionSteps: (defect as any).reproductionSteps || '',
    severity: (defect as any).severity || 'MAJOR',
    priority: defect.priority,
    assigneeId: defect.assigneeId,
    iterationId: defect.iterationId
  })
  fileList.value = []
  dialogVisible.value = true
}

// 查看缺陷
const handleViewDefect = (defect: TaskInfo) => {
  router.push(`/projects/${props.projectId}/work-items/${defect.id}`)
}

// 删除缺陷
const handleDeleteDefect = (defect: TaskInfo) => {
  ElMessageBox.confirm(`确定要删除缺陷 "${defect.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTask(defect.id)
        ElMessage.success('删除成功')
        fetchDefects()
      } catch (error) {
        console.error('删除缺陷失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const taskData = {
        projectId: props.projectId,
        title: formData.title,
        description: formData.description,
        type: 'BUG',
        priority: formData.priority,
        assigneeId: formData.assigneeId,
        iterationId: formData.iterationId,
        status: 'TODO'
      }

      if (formData.id) {
        // 编辑
        await updateTask({
          id: formData.id,
          ...taskData
        })
        ElMessage.success('更新成功')
      } else {
        // 新建
        await createTask(taskData)
        ElMessage.success('创建成功')
      }

      dialogVisible.value = false
      fetchDefects()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(formData.id ? '更新失败' : '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 文件上传相关
const handlePreview = (file: UploadFile) => {
  console.log('Preview file:', file)
}

const handleRemove = (file: UploadFile) => {
  console.log('Remove file:', file)
}

// 状态流转判断
const canChangeStatus = (newStatus: string) => {
  if (!currentDefect.value) return false
  const currentStatus = currentDefect.value.status

  // 定义状态流转规则
  const workflowRules: Record<string, string[]> = {
    'TODO': ['IN_PROGRESS'], // 待修复 → 修复中
    'IN_PROGRESS': ['TESTING', 'TODO', 'DONE'], // 修复中 → 待验证/待修复/已关闭
    'TESTING': ['DONE', 'REOPENED', 'IN_PROGRESS'], // 待验证 → 已关闭/重新打开/修复中
    'DONE': ['REOPENED'], // 已关闭 → 重新打开
    'REOPENED': ['TODO'] // 重新打开 → 待修复
  }

  return workflowRules[currentStatus]?.includes(newStatus) || false
}

// 修改状态
const handleChangeStatus = async (newStatus: string) => {
  if (!currentDefect.value) return

  try {
    await updateTaskStatus({
      id: currentDefect.value.id,
      status: newStatus
    })
    ElMessage.success('状态已更新')
    detailDialogVisible.value = false
    fetchDefects()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 添加评论
const handleAddComment = () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  // 模拟添加评论
  comments.value.push({
    id: Date.now(),
    authorName: '当前用户',
    content: newComment.value,
    createTime: new Date().toISOString()
  })

  newComment.value = ''
  ElMessage.success('评论已添加')
}

// 辅助方法：获取状态类型
const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'TESTING': 'warning',
    'DONE': 'success',
    'REOPENED': 'danger'
  }
  return types[status] || 'info'
}

// 辅助方法：获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'TODO': '待修复',
    'IN_PROGRESS': '修复中',
    'TESTING': '待验证',
    'DONE': '已关闭',
    'REOPENED': '重新打开'
  }
  return texts[status] || '未知'
}

// 辅助方法：获取优先级类型
const getPriorityType = (priority: string) => {
  const types: Record<string, any> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning'
  }
  return types[priority] || 'info'
}

// 辅助方法：获取优先级文本
const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  }
  return texts[priority] || '未知'
}

// 辅助方法：获取严重程度类型
const getSeverityType = (severity: string) => {
  const types: Record<string, any> = {
    'TRIVIAL': 'info',
    'MINOR': '',
    'MAJOR': 'warning',
    'CRITICAL': 'danger',
    'BLOCKER': 'danger'
  }
  return types[severity] || 'info'
}

// 辅助方法：获取严重程度文本
const getSeverityText = (severity: string) => {
  const texts: Record<string, string> = {
    'TRIVIAL': '轻微',
    'MINOR': '次要',
    'MAJOR': '主要',
    'CRITICAL': '严重',
    'BLOCKER': '致命'
  }
  return texts[severity] || '未知'
}

// 格式化日期时间
const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date
}

// 刷新数据
const refresh = () => {
  fetchDefects()
}

// 暴露方法给父组件
defineExpose({
  refresh
})

// 生命周期
onMounted(() => {
  fetchMembers()
  fetchIterations()
  fetchDefects()
})
</script>

<style scoped>
.defects-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
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

.filter-bar {
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.defects-table {
  flex: 1;
  overflow: hidden;
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
}

.defects-table :deep(.el-table) {
  flex: 1;
}

.defect-number {
  color: #1890ff;
  font-family: 'Monaco', 'Consolas', monospace;
  font-weight: 500;
}

.defect-title {
  display: flex;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.defect-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.status-workflow {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.status-workflow h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.workflow-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.comments-section {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.comments-section h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.comment-input {
  margin-bottom: 16px;
}

.comment-list {
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 12px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  color: #666;
  line-height: 1.5;
}
</style>
