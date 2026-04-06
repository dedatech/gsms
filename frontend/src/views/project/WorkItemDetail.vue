<template>
  <div class="work-item-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2 class="page-title">{{ workItem?.title || '加载中...' }}</h2>
        <el-tag :type="getStatusType(workItem?.status)" size="large">
          {{ getStatusText(workItem?.status) }}
        </el-tag>
        <el-tag :type="getPriorityType(workItem?.priority)" size="large" style="margin-left: 8px">
          {{ getPriorityText(workItem?.priority) }}
        </el-tag>
        <el-tag v-if="workItem?.type === 'BUG'" :type="getSeverityType(workItem?.severity)" size="large" style="margin-left: 8px">
          {{ getSeverityText(workItem?.severity) }}
        </el-tag>
      </div>
      <div class="header-right">
        <!-- 状态快捷操作 - 根据类型显示不同按钮 -->
        <template v-if="workItem?.type === 'BUG'">
          <el-button
            v-if="workItem?.status === 'TODO' || workItem?.status === 'REOPENED'"
            type="primary"
            :icon="VideoPlay"
            @click="handleStartTask"
          >
            开始修复
          </el-button>
          <el-button
            v-if="workItem?.status === 'IN_PROGRESS'"
            type="success"
            :icon="Select"
            @click="handleFixComplete"
          >
            提交验证
          </el-button>
          <el-button
            v-if="workItem?.status === 'TESTING'"
            type="success"
            :icon="Check"
            @click="handleVerifyPass"
          >
            验证通过
          </el-button>
          <el-button
            v-if="workItem?.status === 'TESTING'"
            type="warning"
            :icon="Close"
            @click="handleVerifyFail"
          >
            验证失败
          </el-button>
          <el-button
            v-if="workItem?.status === 'DONE'"
            type="info"
            :icon="FolderChecked"
            @click="handleCloseTask"
          >
            关闭
          </el-button>
        </template>
        <template v-else>
          <el-button
            v-if="workItem?.status === 'TODO'"
            type="primary"
            :icon="VideoPlay"
            @click="handleStartTask"
          >
            开始任务
          </el-button>
          <el-button
            v-if="workItem?.status === 'IN_PROGRESS'"
            type="success"
            :icon="Select"
            @click="handleCompleteTask"
          >
            完成任务
          </el-button>
          <el-button
            v-if="workItem?.status === 'DONE'"
            :icon="RefreshLeft"
            @click="handleReopenTask"
          >
            重新打开
          </el-button>
        </template>
        <el-button :icon="Edit" @click="handleEdit">编辑</el-button>
        <el-button :icon="Delete" type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="info">
        <el-collapse v-model="activeCollapse" class="info-collapse">
          <!-- 基本信息 -->
          <el-collapse-item name="basic" :title="getTypeTitle(workItem?.type)">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="工作项ID">{{ workItem?.id }}</el-descriptions-item>
              <el-descriptions-item label="类型">
                {{ getTypeText(workItem?.type) }}
              </el-descriptions-item>
              <el-descriptions-item label="标题" :span="2">{{ workItem?.title }}</el-descriptions-item>
              <el-descriptions-item label="优先级">
                <el-tag :type="getPriorityType(workItem?.priority)">
                  {{ getPriorityText(workItem?.priority) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusType(workItem?.status)">
                  {{ getStatusText(workItem?.status) }}
                </el-tag>
              </el-descriptions-item>

              <!-- 缺陷特有字段：严重程度 -->
              <el-descriptions-item v-if="workItem?.type === 'BUG'" label="严重程度">
                <el-tag :type="getSeverityType(workItem?.severity)">
                  {{ getSeverityText(workItem?.severity) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item v-else label="所属迭代">
                <el-link
                  v-if="workItem?.iterationId"
                  type="primary"
                  @click="goToIteration"
                >
                  {{ workItem?.iterationName || `迭代${workItem?.iterationId}` }}
                </el-link>
                <span v-else>-</span>
              </el-descriptions-item>

              <!-- 所属项目 -->
              <el-descriptions-item label="所属项目" :span="2">
                <el-link type="primary" @click="goToProject">
                  {{ workItem?.projectName }}
                </el-link>
              </el-descriptions-item>

              <!-- 负责人 -->
              <el-descriptions-item label="负责人">
                {{ workItem?.assigneeName || '未分配' }}
              </el-descriptions-item>

              <!-- 缺陷特有字段：修复版本 -->
              <el-descriptions-item v-if="workItem?.type === 'BUG'" label="修复版本">
                {{ workItem?.fixVersion || '-' }}
              </el-descriptions-item>
              <!-- 非缺陷显示所属迭代 -->
              <el-descriptions-item v-else label="所属迭代">
                <el-link
                  v-if="workItem?.iterationId"
                  type="primary"
                  @click="goToIteration"
                >
                  {{ workItem?.iterationName || `迭代${workItem?.iterationId}` }}
                </el-link>
                <span v-else>-</span>
              </el-descriptions-item>

              <!-- 描述 -->
              <el-descriptions-item label="描述" :span="2">
                <div class="description-content">{{ workItem?.description || '暂无描述' }}</div>
              </el-descriptions-item>

              <!-- 缺陷特有字段：复现步骤 -->
              <el-descriptions-item v-if="workItem?.type === 'BUG'" label="复现步骤" :span="2">
                <div class="reproduction-steps">{{ workItem?.reproductionSteps || '暂无复现步骤' }}</div>
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <!-- 时间信息 -->
          <el-collapse-item name="date" title="时间信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="计划开始时间">
                {{ workItem?.planStartDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="计划结束时间">
                {{ workItem?.planEndDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="实际开始时间">
                <span :class="{ 'text-danger': !workItem?.actualStartDate && (workItem?.status === 'IN_PROGRESS' || workItem?.status === 'DONE' || workItem?.status === 'TESTING') }">
                  {{ workItem?.actualStartDate || '-' }}
                </span>
              </el-descriptions-item>
              <el-descriptions-item label="实际结束时间">
                {{ workItem?.actualEndDate || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="预估工时">
                {{ workItem?.estimateHours ? workItem.estimateHours + ' 小时' : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(workItem?.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间" :span="2">
                {{ formatDateTime(workItem?.updateTime) }}
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <!-- 创建信息 -->
          <el-collapse-item name="creator" title="创建信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="创建人">
                {{ workItem?.createUserName }}
              </el-descriptions-item>
              <el-descriptions-item label="更新人">
                {{ workItem?.updateUserName }}
              </el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>

      <!-- 工时记录 - 仅任务和需求显示 -->
      <el-tab-pane v-if="workItem?.type !== 'BUG'" name="workhours">
        <template #label>
          <span>
            <el-icon><Clock /></el-icon>
            工时记录
          </span>
        </template>
        <div class="tab-content-workhours">
          <div class="content-header">
            <div class="header-title">
              <h3>工时记录</h3>
              <span class="subtitle">总工时: {{ totalWorkHours }} 小时</span>
            </div>
            <el-button type="primary" :icon="Plus" @click="handleAddWorkHour">
              登记工时
            </el-button>
          </div>

          <el-table :data="workHours" stripe v-loading="workHoursLoading">
            <el-table-column prop="workDate" label="日期" width="110" />
            <el-table-column prop="hours" label="工时数" width="90">
              <template #default="{ row }">
                <span class="hours-text">{{ row.hours }} 小时</span>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="说明" min-width="200" show-overflow-tooltip />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditWorkHour(row)">编辑</el-button>
                <el-button link type="danger" @click="handleDeleteWorkHour(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="!workHoursLoading && workHours.length === 0" description="暂无工时记录" :image-size="100" />
        </div>
      </el-tab-pane>

      <!-- 缺陷特有：附件 -->
      <el-tab-pane v-if="workItem?.type === 'BUG'" name="attachments">
        <template #label>
          <span>
            <el-icon><Paperclip /></el-icon>
            附件
          </span>
        </template>
        <div class="tab-content">
          <el-empty v-if="!workItem?.attachments || workItem.attachments.length === 0" description="暂无附件" :image-size="100" />
          <div v-else class="attachments-list">
            <div v-for="(attachment, index) in workItem.attachments" :key="index" class="attachment-item">
              <el-icon><Document /></el-icon>
              <el-link type="primary">{{ attachment }}</el-link>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 子任务 - 仅需求显示 -->
      <el-tab-pane v-if="workItem?.type === 'REQUIREMENT'" name="subtasks">
        <template #label>
          <span>
            <el-icon><Files /></el-icon>
            关联任务 ({{ subtasks.length }})
          </span>
        </template>
        <div class="subtasks-content">
          <el-table
            :data="subtasks"
            v-loading="subtasksLoading"
            row-key="id"
            empty-text="暂无关联任务"
            border
            stripe
            style="width: 100%"
          >
            <el-table-column prop="title" label="任务标题" min-width="200" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small">
                  {{ getPriorityText(row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assigneeName" label="负责人" width="120" align="center">
              <template #default="{ row }">
                {{ row.assigneeName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="planEndDate" label="截止日期" width="120" align="center">
              <template #default="{ row }">
                {{ row.planEndDate || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-link type="primary" @click="goToWorkItemDetail(row.id)">查看</el-link>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 操作历史 -->
      <el-tab-pane name="history">
        <template #label>
          <span>
            <el-icon><Clock /></el-icon>
            操作历史
          </span>
        </template>
        <div class="tab-content">
          <el-timeline>
            <el-timeline-item
              v-for="item in operationHistory"
              :key="item.id"
              :timestamp="item.timestamp"
              placement="top"
            >
              <el-card>
                <h4>{{ item.title }}</h4>
                <p>{{ item.description }}</p>
                <div class="history-user">
                  <el-avatar :size="24" :src="item.avatar">{{ item.user?.charAt(0) }}</el-avatar>
                  <span>{{ item.user }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="operationHistory.length === 0" description="暂无操作历史" :image-size="100" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="`编辑${getTypeText(workItem?.type)}`"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editFormData" :rules="editFormRules" label-width="110px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editFormData.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>

        <!-- 缺陷特有字段：复现步骤 -->
        <el-form-item v-if="editFormData.type === 'BUG'" label="复现步骤">
          <el-input
            v-model="editFormData.reproductionSteps"
            type="textarea"
            :rows="4"
            placeholder="请描述如何复现问题"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="类型" prop="type">
              <el-select v-model="editFormData.type" placeholder="请选择" style="width: 100%" :disabled="true">
                <el-option label="需求" value="REQUIREMENT" />
                <el-option label="任务" value="TASK" />
                <el-option label="缺陷" value="BUG" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-select v-model="editFormData.priority" placeholder="请选择" style="width: 100%">
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 缺陷特有字段：严重程度 -->
        <el-form-item v-if="editFormData.type === 'BUG'" label="严重程度">
          <el-select v-model="editFormData.severity" placeholder="请选择" style="width: 100%">
            <el-option label="致命" value="BLOCKER" />
            <el-option label="严重" value="CRITICAL" />
            <el-option label="一般" value="MAJOR" />
            <el-option label="轻微" value="MINOR" />
            <el-option label="提示" value="TRIVIAL" />
          </el-select>
        </el-form-item>

        <!-- 缺陷特有字段：修复版本 -->
        <el-form-item v-if="editFormData.type === 'BUG'" label="修复版本">
          <el-input v-model="editFormData.fixVersion" placeholder="如：v1.0.0" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="assigneeId">
              <el-select v-model="editFormData.assigneeId" placeholder="请选择负责人" clearable filterable style="width: 100%">
                <el-option
                  v-for="user in projectMembers"
                  :key="user.id"
                  :label="user.nickname"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="editFormData.status" placeholder="请选择" style="width: 100%">
                <!-- 任务/需求的状态选项 -->
                <template v-if="editFormData.type !== 'BUG'">
                  <el-option label="待办" value="TODO" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="DONE" />
                  <el-option label="已关闭" value="CLOSED" />
                </template>
                <!-- 缺陷的状态选项 -->
                <template v-else>
                  <el-option label="待处理" value="TODO" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="待验证" value="TESTING" />
                  <el-option label="已完成" value="DONE" />
                  <el-option label="重新打开" value="REOPENED" />
                  <el-option label="已关闭" value="CLOSED" />
                </template>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="planStartDate">
              <el-date-picker
                v-model="editFormData.planStartDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束时间" prop="planEndDate">
              <el-date-picker
                v-model="editFormData.planEndDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="预估工时">
          <el-input-number
            v-model="editFormData.estimateHours"
            :min="0"
            :max="1000"
            :step="0.5"
            :precision="1"
            style="width: 200px"
          />
          <span style="margin-left: 10px; color: #999">小时</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 工时记录对话框 -->
    <el-dialog
      v-model="workHourDialogVisible"
      :title="workHourEditMode ? '编辑工时' : '登记工时'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="workHourFormRef" :model="workHourFormData" :rules="workHourFormRules" label-width="100px">
        <el-form-item label="日期" prop="workDate">
          <el-date-picker
            v-model="workHourFormData.workDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="工时数" prop="hours">
          <el-input-number
            v-model="workHourFormData.hours"
            :min="0.5"
            :max="24"
            :step="0.5"
            :precision="1"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999">小时</span>
        </el-form-item>
        <el-form-item label="说明" prop="content">
          <el-input
            v-model="workHourFormData.content"
            type="textarea"
            :rows="3"
            placeholder="请输入工时说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="workHourDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleWorkHourSubmit" :loading="workHourSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Delete,
  VideoPlay,
  Select,
  RefreshLeft,
  Clock,
  Files,
  Plus,
  Check,
  Close,
  FolderChecked,
  Paperclip,
  Document
} from '@element-plus/icons-vue'
import { getTaskDetail, updateTask, updateTaskStatus, deleteTask, getSubtasks } from '@/api/task'
import { getProjectMembers } from '@/api/project'
import { getWorkHourList, createWorkHour, updateWorkHour, deleteWorkHour, type WorkHourInfo } from '@/api/workhour'
import { getTaskStatusInfo, getTaskPriorityInfo } from '@/utils/statusMapping'

const route = useRoute()
const router = useRouter()
const workItemId = computed(() => Number(route.params.workItemId))
const projectId = computed(() => Number(route.params.projectId))

// 当前激活的标签页
const activeTab = ref('info')

// 折叠面板激活项
const activeCollapse = ref(['basic', 'date', 'creator'])

// 工作项信息
const workItem = ref<any>(null)

// 项目成员列表
const projectMembers = ref<any[]>([])

// 子任务列表（需求专用）
const subtasks = ref<any[]>([])
const subtasksLoading = ref(false)

// 操作历史
const operationHistory = ref<any[]>([])

// 编辑对话框
const editDialogVisible = ref(false)
const editSubmitLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editFormData = reactive({
  title: '',
  description: '',
  type: '',
  priority: '',
  assigneeId: undefined as number | undefined,
  status: '',
  planStartDate: '',
  planEndDate: '',
  estimateHours: 0,
  // 缺陷特有字段
  severity: '',
  reproductionSteps: '',
  fixVersion: ''
})
const editFormRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

// 工时记录相关
const workHours = ref<WorkHourInfo[]>([])
const workHoursLoading = ref(false)
const workHourDialogVisible = ref(false)
const workHourEditMode = ref(false)
const workHourSubmitLoading = ref(false)
const workHourFormRef = ref<FormInstance>()
const workHourFormData = reactive({
  id: 0,
  workDate: '',
  hours: 1,
  content: ''
})
const workHourFormRules: FormRules = {
  workDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  hours: [{ required: true, message: '请输入工时数', trigger: 'blur' }],
  content: [{ required: true, message: '请输入工时说明', trigger: 'blur' }]
}

// 计算总工时
const totalWorkHours = computed(() => {
  return workHours.value.reduce((sum, item) => sum + item.hours, 0)
})

// 获取工作项详情
const fetchWorkItem = async () => {
  try {
    const res = await getTaskDetail(workItemId.value)
    workItem.value = res
  } catch (error) {
    console.error('获取工作项详情失败:', error)
    ElMessage.error('获取工作项详情失败')
  }
}

// 获取项目成员
const fetchProjectMembers = async () => {
  if (!projectId.value) return

  try {
    const res = await getProjectMembers(projectId.value)
    projectMembers.value = res || []
  } catch (error) {
    console.error('获取项目成员失败:', error)
  }
}

// 获取子任务列表（需求专用）
const fetchSubtasks = async () => {
  if (!workItem.value?.id) return

  subtasksLoading.value = true
  try {
    const res = await getSubtasks(workItem.value.id)
    subtasks.value = res || []
  } catch (error) {
    console.error('获取子任务失败:', error)
    ElMessage.error('获取子任务失败')
  } finally {
    subtasksLoading.value = false
  }
}

// 获取工时记录
const fetchWorkHours = async () => {
  if (!workItem.value) return

  workHoursLoading.value = true
  try {
    const res = await getWorkHourList({
      taskId: workItemId.value,
      pageNum: 1,
      pageSize: 100
    })
    workHours.value = res?.list || []
  } catch (error) {
    console.error('获取工时记录失败:', error)
  } finally {
    workHoursLoading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 跳转到项目详情
const goToProject = () => {
  if (workItem.value?.projectId) {
    router.push(`/projects/${workItem.value.projectId}`)
  }
}

// 跳转到迭代详情
const goToIteration = () => {
  if (workItem.value?.iterationId) {
    router.push(`/iterations/${workItem.value.iterationId}`)
  }
}

// 跳转到工作项详情
const goToWorkItemDetail = (itemId: number) => {
  router.push(`/projects/${projectId.value}/work-items/${itemId}`)
}

// 获取类型标题
const getTypeTitle = (type?: string) => {
  const titles: Record<string, string> = {
    'TASK': '任务基本信息',
    'REQUIREMENT': '需求基本信息',
    'BUG': '缺陷基本信息'
  }
  return titles[type || ''] || '基本信息'
}

// 获取类型文本
const getTypeText = (type?: string) => {
  const texts: Record<string, string> = {
    'TASK': '任务',
    'REQUIREMENT': '需求',
    'BUG': '缺陷'
  }
  return texts[type || '-'] || '-'
}

// 获取状态信息
const getStatusType = (status?: string) => {
  if (!status) return ''
  return getTaskStatusInfo(status).type
}
const getStatusText = (status?: string) => {
  if (!status) return ''
  return getTaskStatusInfo(status).text
}

// 获取优先级信息
const getPriorityType = (priority?: string) => {
  if (!priority) return ''
  return getTaskPriorityInfo(priority).type
}
const getPriorityText = (priority?: string) => {
  if (!priority) return ''
  return getTaskPriorityInfo(priority).text
}

// 获取严重程度信息（缺陷专用）
const getSeverityType = (severity?: string) => {
  const types: Record<string, string> = {
    'BLOCKER': 'danger',
    'CRITICAL': 'warning',
    'MAJOR': '',
    'MINOR': 'info',
    'TRIVIAL': 'info'
  }
  return types[severity || ''] || ''
}
const getSeverityText = (severity?: string) => {
  const texts: Record<string, string> = {
    'BLOCKER': '致命',
    'CRITICAL': '严重',
    'MAJOR': '一般',
    'MINOR': '轻微',
    'TRIVIAL': '提示'
  }
  return texts[severity || ''] || '-'
}

// 格式化日期时间
const formatDateTime = (date?: string) => {
  if (!date) return '-'
  return date
}

// 缺陷专用：开始修复
const handleStartBug = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'IN_PROGRESS'
    })
    ElMessage.success('开始修复')
    fetchWorkItem()
  } catch (error) {
    console.error('开始修复失败:', error)
  }
}

// 缺陷专用：提交验证
const handleFixComplete = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'TESTING'
    })
    ElMessage.success('已提交验证')
    fetchWorkItem()
  } catch (error) {
    console.error('提交验证失败:', error)
  }
}

// 缺陷专用：验证通过
const handleVerifyPass = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'DONE'
    })
    ElMessage.success('验证通过，缺陷已完成')
    fetchWorkItem()
  } catch (error) {
    console.error('验证失败:', error)
  }
}

// 缺陷专用：验证失败（重新打开）
const handleVerifyFail = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'REOPENED'
    })
    ElMessage.warning('验证失败，缺陷已重新打开')
    fetchWorkItem()
  } catch (error) {
    console.error('重新打开失败:', error)
  }
}

// 缺陷专用：关闭
const handleCloseTask = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'CLOSED'
    })
    ElMessage.success('缺陷已关闭')
    fetchWorkItem()
  } catch (error) {
    console.error('关闭失败:', error)
  }
}

// 任务/需求专用：开始任务
const handleStartTask_Task = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'IN_PROGRESS'
    })
    ElMessage.success('任务已开始')
    fetchWorkItem()
  } catch (error) {
    console.error('开始任务失败:', error)
  }
}

// 任务/需求专用：完成任务
const handleCompleteTask = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'DONE'
    })
    ElMessage.success('任务已完成')
    fetchWorkItem()
  } catch (error) {
    console.error('完成任务失败:', error)
  }
}

// 任务/需求专用：重新打开
const handleReopenTask = async () => {
  try {
    await updateTaskStatus({
      id: workItemId.value,
      status: 'TODO'
    })
    ElMessage.success('任务已重新打开')
    fetchWorkItem()
  } catch (error) {
    console.error('重新打开任务失败:', error)
  }
}

// 编辑工作项
const handleEdit = () => {
  if (!workItem.value) return

  editFormData.title = workItem.value.title
  editFormData.description = workItem.value.description || ''
  editFormData.type = workItem.value.type || ''
  editFormData.priority = workItem.value.priority || ''
  editFormData.assigneeId = workItem.value.assigneeId
  editFormData.status = workItem.value.status || ''
  editFormData.planStartDate = workItem.value.planStartDate || ''
  editFormData.planEndDate = workItem.value.planEndDate || ''
  editFormData.estimateHours = workItem.value.estimateHours || 0
  editFormData.severity = workItem.value.severity || ''
  editFormData.reproductionSteps = workItem.value.reproductionSteps || ''
  editFormData.fixVersion = workItem.value.fixVersion || ''

  editDialogVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitLoading.value = true
      try {
        await updateTask({
          id: workItemId.value,
          title: editFormData.title,
          description: editFormData.description,
          type: editFormData.type,
          priority: editFormData.priority,
          assigneeId: editFormData.assigneeId,
          status: editFormData.status,
          planStartDate: editFormData.planStartDate,
          planEndDate: editFormData.planEndDate,
          estimateHours: editFormData.estimateHours,
          severity: editFormData.severity,
          reproductionSteps: editFormData.reproductionSteps,
          fixVersion: editFormData.fixVersion
        })
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchWorkItem()
      } catch (error) {
        console.error('更新失败:', error)
      } finally {
        editSubmitLoading.value = false
      }
    }
  })
}

// 删除工作项
const handleDelete = () => {
  const typeText = getTypeText(workItem.value?.type)
  ElMessageBox.confirm(`确定要删除${typeText} "${workItem.value?.title}" 吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTask(workItemId.value)
        ElMessage.success('删除成功')
        router.push(`/projects/${projectId.value}`)
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

// 添加工时记录
const handleAddWorkHour = () => {
  if (!projectId.value) {
    ElMessage.error('缺少项目信息')
    return
  }

  Object.assign(workHourFormData, {
    id: 0,
    workDate: new Date().toISOString().split('T')[0],
    hours: 1,
    content: ''
  })
  workHourEditMode.value = false
  workHourDialogVisible.value = true
}

// 编辑工时记录
const handleEditWorkHour = (row: WorkHourInfo) => {
  Object.assign(workHourFormData, {
    id: row.id,
    workDate: row.workDate,
    hours: row.hours,
    content: row.content
  })
  workHourEditMode.value = true
  workHourDialogVisible.value = true
}

// 提交工时记录
const handleWorkHourSubmit = async () => {
  if (!workHourFormRef.value) return

  await workHourFormRef.value.validate(async (valid) => {
    if (!valid) return

    workHourSubmitLoading.value = true
    try {
      if (workHourEditMode.value) {
        await updateWorkHour({
          id: workHourFormData.id,
          projectId: projectId.value,
          taskId: workItemId.value,
          workDate: workHourFormData.workDate,
          hours: workHourFormData.hours,
          content: workHourFormData.content
        })
        ElMessage.success('更新成功')
      } else {
        await createWorkHour({
          projectId: projectId.value,
          taskId: workItemId.value,
          workDate: workHourFormData.workDate,
          hours: workHourFormData.hours,
          content: workHourFormData.content
        })
        ElMessage.success('登记成功')
      }
      workHourDialogVisible.value = false
      fetchWorkHours()
    } catch (error) {
      console.error('提交工时记录失败:', error)
      ElMessage.error(workHourEditMode.value ? '更新失败' : '登记失败')
    } finally {
      workHourSubmitLoading.value = false
    }
  })
}

// 删除工时记录
const handleDeleteWorkHour = (row: WorkHourInfo) => {
  ElMessageBox.confirm(`确定要删除这条工时记录吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteWorkHour(row.id)
        ElMessage.success('删除成功')
        fetchWorkHours()
      } catch (error) {
        console.error('删除工时记录失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'subtasks' && workItem.value?.type === 'REQUIREMENT') {
    fetchSubtasks()
  }
})

onMounted(() => {
  fetchWorkItem()
  fetchProjectMembers()
})
</script>

<style scoped>
.work-item-detail {
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

.tab-content-workhours {
  padding: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header-title {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.header-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.subtitle {
  font-size: 14px;
  color: #666;
}

.hours-text {
  font-weight: 500;
  color: #409eff;
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

.description-content,
.reproduction-steps {
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

/* 附件列表 */
.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 4px;
}

/* 操作历史 */
.history-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}

:deep(.el-timeline-item__timestamp) {
  color: #999;
}

:deep(.el-timeline-item__wrapper) {
  padding-left: 20px;
}

:deep(.el-card) {
  margin-bottom: 0;
}

:deep(.el-card h4) {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-card p) {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
}
</style>
