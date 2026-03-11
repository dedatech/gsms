<template>
  <div class="page-root">
    <!-- 项目导航栏 -->
    <div class="project-navigation">
      <!-- 左侧：项目选择器 -->
      <div class="nav-left">
        <div class="project-selector-wrapper">
          <ProjectSelector />
        </div>
      </div>

      <!-- 中间：模块标签 -->
      <div class="nav-center">
        <div class="module-tabs">
          <div
            v-for="tab in moduleTabs"
            :key="tab.key"
            class="module-tab"
            :class="{ active: activeModule === tab.key }"
            @click="activeModule = tab.key"
          >
            <span>{{ tab.label }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧：操作按钮 -->
      <div class="nav-right">
        <el-button :icon="Setting" circle text />
        <el-button type="primary" :icon="Plus">新建</el-button>
      </div>
    </div>

    <!-- 内容区域（根据模块标签显示不同内容） -->
    <div class="content-area">
      <!-- 概览 -->
      <div v-if="activeModule === 'overview'" class="module-content">
        <el-alert title="概览视图开发中" type="info" :closable="false" />
      </div>

      <!-- 需求 -->
      <div v-else-if="activeModule === 'requirements'" class="module-content">
        <RequirementsView
          ref="requirementsViewRef"
          :project-id="projectId"
          @create-task="handleCreateTask"
          @view-task="handleViewTask"
          @edit-task="handleEditTask"
          @delete-task="handleDeleteTask"
          @attachment-uploaded="handleAttachmentUploaded"
        />
      </div>

      <!-- 规划 -->
      <div v-else-if="activeModule === 'planning'" class="module-content">
        <PlanningView
          ref="planningViewRef"
          :project-id="projectId"
          @create-iteration="handleCreateIteration"
          @edit-iteration="handleEditIteration"
          @delete-iteration="handleDeleteIteration"
          @create-requirement="handleCreateRequirement"
          @edit-requirement="handleEditTask"
          @delete-requirement="handleDeleteTask"
          @attachment-uploaded="handleAttachmentUploaded"
        />
      </div>

      <!-- 迭代 -->
      <div v-else-if="activeModule === 'iteration'" class="module-content">
        <UnifiedWorkItemView
          :iterations="iterations"
          :tasks="tasks"
          :task-total="taskTotal"
          :project-id="projectId"
          :current-page="taskSearchForm.pageNum"
          :page-size="taskSearchForm.pageSize"
          @create-task="handleCreateTask"
          @create-iteration="handleCreateIteration"
          @view-iteration="handleViewIteration"
          @edit-iteration="handleEditIteration"
          @edit-task="handleEditTask"
          @pagination-change="handlePaginationChange"
          @refresh="handleRefreshTasks"
        />
      </div>

      <!-- 缺陷 -->
      <div v-else-if="activeModule === 'defect'" class="module-content">
        <el-alert title="缺陷视图开发中" type="info" :closable="false" />
      </div>

      <!-- 报表 -->
      <div v-else-if="activeModule === 'report'" class="module-content">
        <el-alert title="报表视图开发中" type="info" :closable="false" />
      </div>

      <!-- 文档 -->
      <div v-else-if="activeModule === 'document'" class="module-content">
        <el-alert title="文档视图开发中" type="info" :closable="false" />
      </div>

      <!-- 成员 -->
      <div v-else-if="activeModule === 'member'" class="module-content">
        <ProjectMemberView
          ref="memberViewRef"
          :project-id="projectId"
        />
      </div>

      <!-- 附件 -->
      <div v-else-if="activeModule === 'attachment'" class="module-content">
        <ProjectAttachmentList
          ref="projectAttachmentListRef"
          :project-id="projectId"
          :can-upload="canUploadAttachment"
        />
      </div>
    </div>

    <!-- 编辑项目对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑项目"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="editFormRef" :model="editFormData" :rules="editFormRules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="editFormData.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="editFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-radio-group v-model="editFormData.status">
            <el-radio value="NOT_STARTED">未开始</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="SUSPENDED">已暂停</el-radio>
            <el-radio value="ARCHIVED">已归档</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit" :loading="editSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建任务对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      :title="taskFormData.parentId ? '新建子任务' : '新建任务'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="taskFormRef" :model="taskFormData" :rules="taskFormRules" label-width="100px">
        <el-form-item label="所属迭代" v-if="!taskFormData.parentId">
          <el-select
            v-model="taskFormData.iterationId"
            placeholder="选填：可选择关联迭代"
            filterable
            clearable
            style="width: 100%"
            :disabled="!!taskFormData.iterationId"
          >
            <el-option
              v-for="iter in iterations"
              :key="iter.id"
              :label="`${iter.name} (${getIterationStatusText(iter.status)})`"
              :value="iter.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="父任务" v-if="taskFormData.parentId">
          <el-input :value="getParentTaskName(taskFormData.parentId)" disabled />
        </el-form-item>
        <el-form-item label="任务标题" prop="title">
          <el-input
            v-model="taskFormData.title"
            placeholder="请输入任务标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input
            v-model="taskFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="taskFormData.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-radio-group v-model="taskFormData.status">
            <el-radio value="TODO">待办</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select
            v-model="taskFormData.assigneeId"
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
        <el-form-item label="计划开始时间">
          <el-date-picker
            v-model="taskFormData.planStartDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划结束时间">
          <el-date-picker
            v-model="taskFormData.planEndDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预估工时">
          <el-input-number
            v-model="taskFormData.estimateHours"
            :min="0"
            :max="999"
            :precision="1"
            placeholder="请输入预估工时"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999">小时</span>
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="taskAttachments"
            :limit="10"
            multiple
            action="#"
          >
            <el-button type="primary" :icon="Upload">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传多个附件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button @click="handleCreateTaskSubmit(false)" :loading="taskSubmitLoading">
          保存并关闭
        </el-button>
        <el-button
          v-if="taskFormData.parentId"
          type="primary"
          @click="handleCreateTaskSubmit(true)"
          :loading="taskSubmitLoading"
        >
          保存并继续创建
        </el-button>
        <el-button
          v-else
          type="primary"
          @click="handleCreateTaskSubmit(false)"
          :loading="taskSubmitLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 迭代对话框 -->
    <el-dialog
      v-model="iterationDialogVisible"
      :title="iterationDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="iterationFormRef" :model="iterationFormData" :rules="iterationFormRules" label-width="100px">
        <el-form-item label="迭代名称" prop="name">
          <el-input
            v-model="iterationFormData.name"
            placeholder="请输入迭代名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="迭代描述">
          <el-input
            v-model="iterationFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入迭代描述"
          />
        </el-form-item>
        <el-form-item label="迭代状态" prop="status">
          <el-radio-group v-model="iterationFormData.status">
            <el-radio value="NOT_STARTED">未开始</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="COMPLETED">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划开始时间">
          <el-date-picker
            v-model="iterationFormData.planStartDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划结束时间">
          <el-date-picker
            v-model="iterationFormData.planEndDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="iterationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleIterationSubmit" :loading="iterationSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑任务对话框 -->
    <el-dialog
      v-model="editTaskDialogVisible"
      title="编辑任务"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="editTaskFormRef" :model="editTaskFormData" :rules="editTaskFormRules" label-width="100px">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="editTaskFormData.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input
            v-model="editTaskFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="editTaskFormData.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="任务状态">
          <el-radio-group v-model="editTaskFormData.status">
            <el-radio value="TODO">待办</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="DONE">已完成</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select
            v-model="editTaskFormData.assigneeId"
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
        <el-form-item label="计划开始时间">
          <el-date-picker
            v-model="editTaskFormData.planStartDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="计划结束时间">
          <el-date-picker
            v-model="editTaskFormData.planEndDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预估工时">
          <el-input-number
            v-model="editTaskFormData.estimateHours"
            :min="0"
            :max="999"
            :precision="1"
            placeholder="请输入预估工时"
            style="width: 100%"
          />
          <span style="margin-left: 10px; color: #999">小时</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editTaskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditTaskSubmit" :loading="editTaskLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 项目信息侧边栏 -->
    <ProjectInfoSidebar
      v-model:visible="showProjectInfo"
      :project="project"
      :members="members"
      :available-users="availableUsers"
      :member-count="members.length"
      :iteration-count="iterations.length"
      :task-count="taskTotal"
      :can-manage-members="true"
      @add-member="handleAddMemberSubmit"
      @remove-member="handleRemoveMember"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  ArrowLeft,
  ArrowDown,
  ArrowRight,
  Edit,
  Delete,
  InfoFilled,
  List,
  Plus,
  View,
  Grid,
  Document,
  FolderOpened,
  VideoPlay,
  TrendCharts,
  User,
  Setting,
  Filter,
  Clock,
  Upload
} from '@element-plus/icons-vue'
import { getProjectDetail, updateProject, deleteProject, getProjectMembers, addProjectMember, removeProjectMember } from '@/api/project'
import { getTaskList, getTasksByProjectId, createTask, updateTask, deleteTask, getTaskDetail } from '@/api/task'
import { getAllUsers, type UserInfo } from '@/api/user'
import { getProjectStatusInfo } from '@/utils/statusMapping'
import { getIterationList, createIteration, updateIteration, deleteIteration, type IterationInfo } from '@/api/iteration'
import { uploadAttachment } from '@/api/attachment'
import ProjectGantt from '@/components/ProjectGantt.vue'
import ProjectInfoSidebar from '@/components/ProjectInfoSidebar.vue'
import UnifiedWorkItemView from '@/components/UnifiedWorkItemView.vue'
import RequirementsView from '@/components/RequirementsView.vue'
import PlanningView from '@/components/PlanningView.vue'
import ProjectMemberView from './ProjectMemberView.vue'
import AttachmentList from '@/components/AttachmentList.vue'
import ProjectAttachmentList from '@/components/ProjectAttachmentList.vue'
import IterationSelector from '@/components/layout/IterationSelector.vue'
import ViewModeTabs from '@/components/layout/ViewModeTabs.vue'
import ProjectSelector from '@/components/layout/ProjectSelector.vue'
import type { UploadFile, UploadUserFile, UploadProps } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const projectId = computed(() => Number(route.params.id))

// 附件上传权限：项目成员都可以上传附件
const canUploadAttachment = computed(() => authStore.isAuthenticated)

// 当前激活的标签页（默认显示工作项列表，符合"工作项为核心"理念）
const activeTab = ref('workItems')

// 当前激活的模块标签
const activeModule = ref('overview')

// RequirementsView 组件引用
const requirementsViewRef = ref<InstanceType<typeof RequirementsView>>()

// PlanningView 组件引用
const planningViewRef = ref<InstanceType<typeof PlanningView>>()

// ProjectMemberView 组件引用
const memberViewRef = ref<InstanceType<typeof ProjectMemberView>>()

// AttachmentList 组件引用
const attachmentListRef = ref<InstanceType<typeof AttachmentList>>()

// ProjectAttachmentList 组件引用
const projectAttachmentListRef = ref<InstanceType<typeof ProjectAttachmentList>>()

// 模块标签定义
const moduleTabs = [
  { key: 'overview', label: '概览' },
  { key: 'requirements', label: '需求' },
  { key: 'planning', label: '规划' },
  { key: 'iteration', label: '迭代' },
  { key: 'defect', label: '缺陷' },
  { key: 'report', label: '报表' },
  { key: 'document', label: '文档' },
  { key: 'attachment', label: '附件' },
  { key: 'member', label: '成员' }
]

// ONES 风格：当前视图标签
const currentViewTab = ref('kanban')

// 视图标签定义
const viewTabs = [
  { key: 'overview', label: '概览' },
  { key: 'kanban', label: '敏捷看板' },
  { key: 'list', label: '列表视图' }
]

// 筛选器数量
const filterCount = ref(0)

// ========== 需求池视图 ==========
// 需求筛选器
const requirementFilter = ref('all')

// ========== 规划视图 ==========
// 展开的迭代列表
const expandedIterations = ref<number[]>([])

// 待规划任务（没有分配迭代的任务）
const unplannedTasks = computed(() => {
  return tasks.value.filter(t => !t.iterationId)
})

// 获取指定迭代的任务列表
const getIterationTasks = (iterationId: number) => {
  return tasks.value.filter(t => t.iterationId === iterationId)
}

// 获取指定迭代的任务数量
const getIterationTaskCount = (iterationId: number) => {
  return getIterationTasks(iterationId).length
}

// 获取指定迭代的预估工时
const getIterationEstimateHours = (iterationId: number) => {
  return getIterationTasks(iterationId).reduce((sum, t) => sum + (t.estimateHours || 0), 0)
}

// 切换迭代展开/收起状态
const toggleIteration = (iterationId: number) => {
  const idx = expandedIterations.value.indexOf(iterationId)
  if (idx > -1) {
    expandedIterations.value.splice(idx, 1)
  } else {
    expandedIterations.value.push(iterationId)
  }
}

// 格式化日期（简化版，只显示月/日）
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 看板列定义（三列）
const kanbanColumns = computed(() => {
  return [
    {
      key: 'todo',
      label: '未开始',
      color: '#faad14',
      tasks: tasks.value.filter(t => t.status === 'TODO' || t.status === null)
    },
    {
      key: 'inProgress',
      label: '进行中',
      color: '#1890ff',
      tasks: tasks.value.filter(t => t.status === 'IN_PROGRESS')
    },
    {
      key: 'done',
      label: '已完成',
      color: '#52c41a',
      tasks: tasks.value.filter(t => t.status === 'DONE')
    }
  ].map(col => ({
    ...col,
    count: col.tasks.length
  }))
})

// 获取负责人名称
const getAssigneeName = (assigneeId?: number) => {
  if (!assigneeId) return '未分配'
  const member = members.value.find(m => m.userId === assigneeId)
  return member ? member.nickname : '未知用户'
}

// 项目信息侧边栏显示状态
const showProjectInfo = ref(false)

// 项目信息
const project = ref<any>(null)

// 项目成员
const members = ref<any[]>([])

// 可用用户列表
const availableUsers = ref<UserInfo[]>([])

// 任务列表
const tasks = ref<any[]>([])
const taskTotal = ref(0)
const taskSearchForm = reactive({
  projectId: projectId.value,
  pageNum: 1,
  pageSize: 10
})

// 迭代列表（仅中大型项目使用）
const iterations = ref<IterationInfo[]>([])

// 编辑项目对话框
const editDialogVisible = ref(false)
const editSubmitLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editFormData = reactive({
  name: '',
  description: '',
  status: ''
})
const editFormRules: FormRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
}

// 新建任务对话框
const taskDialogVisible = ref(false)
const taskSubmitLoading = ref(false)
const taskFormRef = ref<FormInstance>()
const uploadRef = ref()
const taskAttachments = ref<UploadUserFile[]>([])
const taskFormData = reactive({
  title: '',
  description: '',
  type: 'REQUIREMENT',  // 默认为需求
  priority: 'MEDIUM',
  status: 'TODO',
  assigneeId: undefined as number | undefined,
  iterationId: undefined as number | undefined,
  parentId: undefined as number | undefined,
  planStartDate: '',
  planEndDate: '',
  estimateHours: undefined as number | undefined
})
const taskFormRules: FormRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
}

// 处理文件选择
const handleFileChange: UploadProps['onChange'] = (uploadFile, uploadFiles) => {
  taskAttachments.value = uploadFiles
}

// 处理文件移除
const handleFileRemove: UploadProps['onRemove'] = (uploadFile, uploadFiles) => {
  taskAttachments.value = uploadFiles
}

// 获取父任务名称
const getParentTaskName = (parentId: number) => {
  const parentTask = tasks.value.find(t => t.id === parentId)
  return parentTask ? parentTask.title : '未知任务'
}

// 迭代对话框
const iterationDialogVisible = ref(false)
const iterationSubmitLoading = ref(false)
const iterationFormRef = ref<FormInstance>()
const iterationDialogTitle = computed(() => iterationFormData.id ? '编辑迭代' : '新建迭代')
const iterationFormData = reactive({
  id: undefined as number | undefined,
  name: '',
  description: '',
  status: 'NOT_STARTED',
  planStartDate: '',
  planEndDate: ''
})
const iterationFormRules: FormRules = {
  name: [{ required: true, message: '请输入迭代名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择迭代状态', trigger: 'change' }]
}

// 获取项目详情
const fetchProject = async () => {
  try {
    const res = await getProjectDetail(projectId.value)
    project.value = res

    // 同步设置 projectStore 的当前项目，使项目选择器联动
    const { useProjectStore } = await import('@/stores/project')
    const projectStore = useProjectStore()
    await projectStore.setCurrentProject(projectId.value)

    // 获取迭代列表（所有项目都可以使用迭代功能）
    await fetchIterations()
  } catch (error) {
    console.error('获取项目详情失败:', error)
    ElMessage.error('获取项目详情失败')
  }
}

// 获取项目成员
const fetchMembers = async () => {
  try {
    const res = await getProjectMembers(projectId.value)
    members.value = res || []
    // 获取成员后更新可用用户列表
    await fetchAvailableUsers()
  } catch (error) {
    console.error('获取项目成员失败:', error)
  }
}

// 获取可用用户列表（用于任务负责人）
const fetchAvailableUsers = async () => {
  try {
    // 任务负责人应该是项目成员
    if (members.value.length > 0) {
      // 使用项目成员作为可选的负责人列表
      // 注意：members中的userId才是用户ID，id是成员表ID
      availableUsers.value = members.value.map(m => ({
        id: m.userId,  // 使用userId而不是id
        nickname: m.nickname,
        username: m.username || ''
      }))
    } else {
      // 如果没有项目成员，获取所有用户
      const res = await getAllUsers()
      availableUsers.value = res.list || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 获取任务列表
const fetchTasks = async () => {
  try {
    // 使用专门的项目任务接口，传入分页参数
    const res = await getTasksByProjectId(
      projectId.value,
      taskSearchForm.pageNum,
      taskSearchForm.pageSize
    )
    // 强制触发响应式更新：先清空再赋值
    tasks.value = []
    await nextTick()
    tasks.value = res.list || []
    taskTotal.value = res.total || 0
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

// 分页变化处理
const handlePaginationChange = (pageNum: number, pageSize: number) => {
  taskSearchForm.pageNum = pageNum
  taskSearchForm.pageSize = pageSize
  fetchTasks()
}

// 刷新任务列表（AI 拆分成功后调用）
const handleRefreshTasks = () => {
  fetchTasks()
  fetchIterations() // 同时刷新迭代，因为任务数量可能变化
}

// 获取迭代列表
const fetchIterations = async () => {
  try {
    const res = await getIterationList({ projectId: projectId.value, pageNum: 1, pageSize: 100 })
    iterations.value = res.list || []
  } catch (error) {
    console.error('获取迭代列表失败:', error)
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 编辑项目
const handleEdit = () => {
  editFormData.name = project.value.name
  editFormData.description = project.value.description
  editFormData.status = project.value.status
  editDialogVisible.value = true
}

// 提交编辑
const handleEditSubmit = async () => {
  if (!editFormRef.value) return

  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editSubmitLoading.value = true
      try {
        await updateProject({
          id: projectId.value,
          name: editFormData.name,
          description: editFormData.description,
          status: editFormData.status
        })
        ElMessage.success('更新成功')
        editDialogVisible.value = false
        fetchProject()
      } catch (error) {
        console.error('更新项目失败:', error)
      } finally {
        editSubmitLoading.value = false
      }
    }
  })
}

// 删除项目
const handleDelete = () => {
  ElMessageBox.confirm(`确定要删除项目 "${project.value.name}" 吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteProject(projectId.value)
        ElMessage.success('删除成功')
        router.push('/projects')
      } catch (error) {
        console.error('删除项目失败:', error)
      }
    })
    .catch(() => {})
}

// 提交添加成员（由侧边栏调用）
const handleAddMemberSubmit = async (userId: number, roleType: number) => {
  try {
    await addProjectMember(projectId.value, userId, roleType)
    ElMessage.success('添加成功')
    fetchMembers()
  } catch (error) {
    console.error('添加成员失败:', error)
  }
}

// 移除成员
const handleRemoveMember = (member: any) => {
  ElMessageBox.confirm(`确定要移除成员 "${member.nickname}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await removeProjectMember(projectId.value, member.id)
        ElMessage.success('移除成功')
        fetchMembers()
      } catch (error) {
        console.error('移除成员失败:', error)
      }
    })
    .catch(() => {})
}

// 新建任务
const handleCreateTask = (iterationId?: number, parentId?: number) => {
  // 重置表单
  Object.assign(taskFormData, {
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'TODO',
    assigneeId: undefined,
    iterationId: iterationId || undefined,
    parentId: parentId || undefined,
    planStartDate: '',
    planEndDate: '',
    estimateHours: undefined
  })
  // 重置附件列表
  taskAttachments.value = []
  uploadRef.value?.clearFiles()
  taskDialogVisible.value = true
}

// 新建需求（由规划视图调用）
const handleCreateRequirement = (iterationId?: number) => {
  handleCreateTask(iterationId)
}

// 编辑迭代
const handleEditIteration = (iteration: any) => {
  // 打开编辑迭代对话框
  Object.assign(iterationFormData, {
    id: iteration.id,
    name: iteration.name,
    description: iteration.description || '',
    status: iteration.status,
    planStartDate: iteration.planStartDate || '',
    planEndDate: iteration.planEndDate || ''
  })
  iterationDialogVisible.value = true
}

// 编辑任务对话框
const editTaskDialogVisible = ref(false)
const editTaskFormRef = ref<FormInstance>()
const editTaskLoading = ref(false)
const editTaskFormData = reactive({
  id: 0,
  title: '',
  description: '',
  priority: 'MEDIUM',
  assigneeId: undefined as number | undefined,
  status: 'TODO',
  planStartDate: '',
  planEndDate: '',
  estimateHours: undefined as number | undefined
})
const editTaskFormRules: FormRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
}

// 编辑任务
const handleEditTask = async (task: any) => {
  try {
    // 先获取任务详情，确保数据完整
    const taskDetail = await getTaskDetail(task.id)

    // 使用详情数据回填表单
    editTaskFormData.id = taskDetail.id
    editTaskFormData.title = taskDetail.title
    editTaskFormData.description = taskDetail.description || ''
    editTaskFormData.priority = taskDetail.priority || 'MEDIUM'
    editTaskFormData.assigneeId = taskDetail.assigneeId
    editTaskFormData.status = taskDetail.status || 'TODO'
    editTaskFormData.planStartDate = taskDetail.planStartDate || ''
    editTaskFormData.planEndDate = taskDetail.planEndDate || ''
    editTaskFormData.estimateHours = taskDetail.estimateHours

    editTaskDialogVisible.value = true
  } catch (error) {
    console.error('获取任务详情失败:', error)
    ElMessage.error('获取任务详情失败')
  }
}

// 提交编辑任务
const handleEditTaskSubmit = async () => {
  if (!editTaskFormRef.value) return

  await editTaskFormRef.value.validate(async (valid) => {
    if (!valid) return

    editTaskLoading.value = true
    try {
      await updateTask({
        id: editTaskFormData.id,
        projectId: projectId.value,  // 传递当前项目ID
        title: editTaskFormData.title,
        description: editTaskFormData.description,
        priority: editTaskFormData.priority,
        assigneeId: editTaskFormData.assigneeId,
        status: editTaskFormData.status,
        planStartDate: editTaskFormData.planStartDate || undefined,
        planEndDate: editTaskFormData.planEndDate || undefined,
        estimateHours: editTaskFormData.estimateHours
      })
      ElMessage.success('更新成功')
      editTaskDialogVisible.value = false
      // 刷新任务列表
      fetchTasks()
      // 刷新需求视图
      if (requirementsViewRef.value) {
        requirementsViewRef.value.refresh()
      }
      // 刷新规划视图
      if (planningViewRef.value) {
        planningViewRef.value.refresh()
      }
    } catch (error) {
      console.error('更新任务失败:', error)
      ElMessage.error('更新任务失败')
    } finally {
      editTaskLoading.value = false
    }
  })
}

// 查看任务
const handleViewTask = (task: any) => {
  // 跳转到任务详情页
  router.push(`/tasks/${task.id}`)
}

// 删除任务
const handleDeleteTask = async (task: any) => {
  try {
    await deleteTask(task.id)
    ElMessage.success('删除成功')
    // 刷新需求视图
    if (requirementsViewRef.value) {
      requirementsViewRef.value.refresh()
    }
    // 刷新规划视图
    if (planningViewRef.value) {
      planningViewRef.value.refresh()
    }
    // 也刷新主任务列表（用于迭代视图）
    await fetchTasks()
  } catch (error) {
    console.error('删除任务失败:', error)
    ElMessage.error('删除失败')
  }
}

// 附件上传后刷新项目附件列表
const handleAttachmentUploaded = () => {
  if (projectAttachmentListRef.value) {
    projectAttachmentListRef.value.refresh()
  }
}

// 提交新建任务
const handleCreateTaskSubmit = async (continueCreating: boolean = false) => {
  if (!taskFormRef.value) return

  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return

    taskSubmitLoading.value = true
    try {
      const taskData = {
        projectId: projectId.value,
        title: taskFormData.title,
        description: taskFormData.description,
        type: taskFormData.type,  // 添加任务类型
        priority: taskFormData.priority,
        status: taskFormData.status,
        assigneeId: taskFormData.assigneeId,
        iterationId: taskFormData.iterationId,
        parentId: taskFormData.parentId,
        planStartDate: taskFormData.planStartDate || undefined,
        planEndDate: taskFormData.planEndDate || undefined,
        estimateHours: taskFormData.estimateHours
      }

      const result = await createTask(taskData)
      const newTaskId = result.id

      // 上传附件（如果有）
      if (taskAttachments.value.length > 0 && newTaskId) {
        const uploadPromises = taskAttachments.value
          .filter(file => file.raw) // 只处理有原始文件的项
          .map(file => uploadAttachment('task', newTaskId, file.raw!))

        try {
          await Promise.all(uploadPromises)
          ElMessage.success(`任务创建成功，已上传 ${uploadPromises.length} 个附件`)
        } catch (uploadError) {
          console.error('附件上传失败:', uploadError)
          ElMessage.warning('任务创建成功，但部分附件上传失败')
        }
      } else {
        ElMessage.success('任务创建成功')
      }

      // 刷新任务列表
      fetchTasks()
      // 刷新需求视图
      if (requirementsViewRef.value) {
        requirementsViewRef.value.refresh()
      }
      // 刷新规划视图
      if (planningViewRef.value) {
        planningViewRef.value.refresh()
      }

      // 如果是继续创建模式，只重置部分字段
      if (continueCreating) {
        // 保留 parentId、iterationId、priority、status、assigneeId、时间等
        // 只清空标题、描述和附件
        const preservedFields = {
          parentId: taskFormData.parentId,
          iterationId: taskFormData.iterationId,
          priority: taskFormData.priority,
          status: taskFormData.status,
          assigneeId: taskFormData.assigneeId,
          planStartDate: taskFormData.planStartDate,
          planEndDate: taskFormData.planEndDate,
          estimateHours: taskFormData.estimateHours
        }

        Object.assign(taskFormData, {
          title: '',
          description: '',
          type: 'REQUIREMENT',
          ...preservedFields
        })

        // 重置附件列表
        taskAttachments.value = []
        uploadRef.value?.clearFiles()

        // 聚焦到标题输入框，方便继续输入
        nextTick(() => {
          const titleInput = document.querySelector('.el-dialog input[placeholder*="任务标题"]') as HTMLInputElement
          titleInput?.focus()
        })
      } else {
        // 非继续创建模式，关闭对话框
        taskDialogVisible.value = false
      }
    } catch (error) {
      console.error('创建任务失败:', error)
      ElMessage.error('创建任务失败')
    } finally {
      taskSubmitLoading.value = false
    }
  })
}

// 获取项目状态信息
const getStatusType = (status: string) => getProjectStatusInfo(status).type
const getStatusText = (status: string) => getProjectStatusInfo(status).text

// 获取角色类型
const getRoleType = (roleType: number) => {
  const types: Record<number, any> = {
    1: 'danger',      // 项目管理员
    2: '',           // 项目成员
    3: 'info'        // 只读访客
  }
  return types[roleType] || 'info'
}

// 获取任务状态类型
const getTaskStatusType = (status: string) => {
  const types: Record<string, any> = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'DONE': 'success'
  }
  return types[status] || 'info'
}

// 获取任务状态文本
const getTaskStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'TODO': '待办',
    'IN_PROGRESS': '进行中',
    'DONE': '已完成'
  }
  return texts[status] || '未知'
}

// 获取优先级类型
const getPriorityType = (priority: string) => {
  const types: Record<string, any> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning'
  }
  return types[priority] || 'info'
}

// 获取优先级文本
const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  }
  return texts[priority] || '未知'
}

// 格式化日期时间
const formatDateTime = (date: string) => {
  if (!date) return '-'
  return date
}

// 迭代相关方法
const handleCreateIteration = () => {
  // 重置表单
  Object.assign(iterationFormData, {
    id: undefined,
    name: '',
    description: '',
    status: 'NOT_STARTED',
    planStartDate: '',
    planEndDate: ''
  })
  iterationDialogVisible.value = true
}

const handleIterationSubmit = async () => {
  if (!iterationFormRef.value) return

  await iterationFormRef.value.validate(async (valid) => {
    if (!valid) return

    iterationSubmitLoading.value = true
    try {
      if (iterationFormData.id) {
        // 编辑迭代
        await updateIteration({
          id: iterationFormData.id,
          name: iterationFormData.name,
          description: iterationFormData.description,
          status: iterationFormData.status,
          planStartDate: iterationFormData.planStartDate || undefined,
          planEndDate: iterationFormData.planEndDate || undefined
        })
        ElMessage.success('更新成功')
      } else {
        // 创建迭代
        await createIteration({
          projectId: projectId.value,
          name: iterationFormData.name,
          description: iterationFormData.description,
          status: iterationFormData.status,
          planStartDate: iterationFormData.planStartDate || undefined,
          planEndDate: iterationFormData.planEndDate || undefined
        })
        ElMessage.success('创建成功')
      }
      iterationDialogVisible.value = false
      fetchIterations() // 刷新迭代列表
      // 刷新规划视图
      if (planningViewRef.value) {
        planningViewRef.value.refresh()
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(iterationFormData.id ? '更新失败' : '创建失败')
    } finally {
      iterationSubmitLoading.value = false
    }
  })
}

const handleDeleteIteration = (iteration: IterationInfo) => {
  ElMessageBox.confirm(`确定要删除迭代 "${iteration.name}" 吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteIteration(iteration.id)
        ElMessage.success('删除成功')
        fetchIterations() // 刷新迭代列表
        // 刷新规划视图
        if (planningViewRef.value) {
          planningViewRef.value.refresh()
        }
      } catch (error) {
        console.error('删除迭代失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleViewIteration = (iteration: any) => {
  router.push(`/projects/${projectId.value}/iterations/${iteration.id}`)
}

// 获取迭代状态类型
const getIterationStatusType = (status: string) => {
  const types: Record<string, any> = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success'
  }
  return types[status] || 'info'
}

// 获取迭代状态文本
const getIterationStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成'
  }
  return texts[status] || '未知'
}

// 项目类型辅助方法
const getProjectTypeTag = (projectType: string) => {
  const types: Record<string, any> = {
    'SCHEDULE': 'success',
    'LARGE_SCALE': 'warning'
  }
  return types[projectType] || 'info'
}

const getProjectTypeText = (projectType: string) => {
  const texts: Record<string, string> = {
    'SCHEDULE': '常规型项目',
    'LARGE_SCALE': '中大型项目'
  }
  return texts[projectType] || '未知'
}

onMounted(() => {
  fetchProject()
  fetchMembers()
  fetchTasks()
})
</script>

<style scoped>
/* 页面根容器 */
.page-root {
  padding: 0;
  margin: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 内容区域 */
.content-area {
  flex: 1;
  overflow: hidden;
  min-height: 0;
}

.module-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}


/* 项目导航栏 */
.project-navigation {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.project-selector-wrapper {
  display: flex;
  align-items: center;
}

.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 0;
}

.module-tabs {
  display: flex;
  gap: 4px;
  padding: 4px;
  background: #f5f5f5;
  border-radius: 6px;
}

.module-tab {
  padding: 6px 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  white-space: nowrap;
}

.module-tab:hover {
  color: #1890ff;
}

.module-tab.active {
  background: #fff;
  color: #1890ff;
  font-weight: 500;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 标签页 */
.tabs-container {
  position: relative;
  background: #fff;
  padding: 8px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.detail-tabs {
  background: transparent;
  padding: 0;
  box-shadow: none;
}

.tab-right-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 8px;
}

:deep(.el-tabs__header) {
  margin-bottom: 8px;
}

:deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 12px;
}

:deep(.el-tabs__item .el-icon) {
  margin-right: 4px;
  vertical-align: -2px;
}

.tab-badge {
  margin-left: 8px;
}

/* ========== ONES 风格内容区 ========== */
.ones-style-content {
  padding: 0;
}

/* 迭代和操作按钮区 */
.iteration-actions-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.iteration-actions-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 视图标签行 */
.view-tabs-bar {
  display: flex;
  gap: 32px;
  padding: 16px 24px 0;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.view-tab-item {
  padding-bottom: 12px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.view-tab-item:hover {
  color: #1890ff;
}

.view-tab-item.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
  font-weight: 500;
}

/* 看板视图 */
.kanban-board-view {
  padding: 6px;
  background: #f0f2f5;
  min-height: calc(100vh - 300px);
}

.kanban-column {
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  min-height: 400px;
}

.kanban-column-header {
  padding: 6px 8px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  font-size: 14px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.column-count {
  font-size: 12px;
}

.kanban-column-body {
  padding: 4px;
}

/* 增强型任务卡片 */
.task-card-enhanced {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 6px 8px;
  margin-bottom: 6px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.task-card-enhanced:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.task-card-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.task-card-number {
  font-size: 12px;
  color: #1890ff;
  font-family: 'Monaco', 'Consolas', monospace;
  font-weight: 500;
}

.task-status-tag {
  font-size: 12px;
}

.task-card-title {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.task-card-assignee,
.task-card-progress {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.task-card-assignee .el-icon,
.task-card-progress .el-icon {
  font-size: 14px;
}

.empty-state {
  padding: 40px 0;
}

/* 标签页右侧操作按钮 */
.tab-actions {
  display: flex;
  gap: 8px;
  padding-right: 16px;
}

/* 标签页内容 */
.tab-content {
  min-height: 400px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.content-header :deep(.el-alert) {
  max-width: 400px;
  padding: 8px 16px;
}

.content-header :deep(.el-alert__title) {
  font-size: 13px;
  line-height: 1.5;
}

.header-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.subtitle {
  margin-left: 12px;
  font-size: 14px;
  color: #8c8c8c;
  font-weight: normal;
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

/* 任务统计 */
.task-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-value.todo {
  color: #d9d9d9;
}

.stat-value.inProgress {
  color: #1890ff;
}

.stat-value.done {
  color: #52c41a;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
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

/* 甘特图 Tab */
.gantt-tab-content {
  height: calc(100vh - 320px);
  min-height: 500px;
}

:deep(.gantt-tab-content .project-gantt) {
  height: 100%;
}

/* ========== 需求池视图 ========== */
.requirements-pool-view {
  min-height: 500px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.requirements-table {
  margin-top: 16px;
}

.task-number {
  color: #1890ff;
  font-family: 'Monaco', 'Consolas', monospace;
  font-weight: 500;
}

/* ========== 规划视图 ========== */
.planning-view {
  min-height: 500px;
}

.iterations-list,
.unplanned-area {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  min-height: 400px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

/* 迭代卡片 */
.iteration-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.iteration-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
  transition: all 0.3s;
}

.iteration-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.iteration-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  background: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.expand-icon {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s;
}

.iteration-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.task-count-badge {
  margin-left: auto;
}

.iteration-card-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  font-size: 12px;
  color: #666;
  background: #fff;
}

.iteration-date {
  color: #666;
}

.iteration-hours {
  color: #1890ff;
  font-weight: 500;
}

.iteration-tasks {
  padding: 12px 16px;
  background: #fafafa;
  border-top: 1px solid #f0f0f0;
}

/* 迷你任务卡片 */
.mini-task-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.mini-task-card:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 4px rgba(24, 144, 255, 0.1);
}

.mini-task-card:last-child {
  margin-bottom: 0;
}

.task-type-icon {
  font-size: 16px;
  color: #1890ff;
  flex-shrink: 0;
}

.task-title {
  flex: 1;
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unplanned-tasks {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.unplanned-tasks:empty {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 滚动条样式优化 - 完全隐藏但保持滚动功能 */
.unplanned-tasks::-webkit-scrollbar {
  display: none; /* Webkit浏览器：完全隐藏滚动条 */
  width: 0;
  height: 0;
}

/* Firefox 滚动条 - 完全隐藏 */
.unplanned-tasks {
  scrollbar-width: none; /* Firefox：完全隐藏滚动条 */
}
</style>
