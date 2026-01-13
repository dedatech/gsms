<template>
  <div class="project-detail">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h2 class="page-title">{{ project?.name || '加载中...' }}</h2>
        <el-tag :type="getStatusType(project?.status)" size="large">
          {{ getStatusText(project?.status) }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button :icon="Edit" type="primary" @click="handleEdit">编辑项目</el-button>
        <el-button :icon="Delete" type="danger" @click="handleDelete">删除项目</el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 项目信息标签 -->
      <el-tab-pane label="项目信息" name="info">
        <el-collapse v-model="activeCollapse" class="info-collapse">
          <el-collapse-item name="basic" title="基本信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="项目编码">{{ project?.code }}</el-descriptions-item>
              <el-descriptions-item label="项目名称">{{ project?.name }}</el-descriptions-item>
              <el-descriptions-item label="项目状态" :span="2">
                <el-tag :type="getStatusType(project?.status)">
                  {{ getStatusText(project?.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="项目描述" :span="2">
                {{ project?.description || '暂无描述' }}
              </el-descriptions-item>
              <el-descriptions-item label="项目经理">{{ project?.managerName || '未设置' }}</el-descriptions-item>
              <el-descriptions-item label="创建人">{{ project?.createUserName }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>

          <el-collapse-item name="date" title="时间信息">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="计划开始时间">{{ project?.planStartDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="计划结束时间">{{ project?.planEndDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="实际开始时间">{{ project?.actualStartDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="实际结束时间">{{ project?.actualEndDate || '-' }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDateTime(project?.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="更新时间">{{ formatDateTime(project?.updateTime) }}</el-descriptions-item>
            </el-descriptions>
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>

      <!-- 项目成员标签 -->
      <el-tab-pane name="members">
        <template #label>
          <span>
            <el-icon><User /></el-icon>
            项目成员
            <el-badge :value="members.length" class="tab-badge" />
          </span>
        </template>
        <div class="tab-content">
          <div class="content-header">
            <div class="header-title">
              <h3>项目成员</h3>
              <span class="subtitle">共 {{ members.length }} 位成员</span>
            </div>
            <el-button type="primary" :icon="Plus" @click="handleAddMember">
              添加成员
            </el-button>
          </div>

          <div v-if="members.length > 0" class="member-list">
            <div v-for="member in members" :key="member.id" class="member-item">
              <el-avatar :size="48">{{ (member.nickname || 'U').charAt(0) }}</el-avatar>
              <div class="member-info">
                <div class="member-name">{{ member.nickname }}</div>
                <el-tag :type="getRoleType(member.roleType)" size="small">
                  {{ member.roleName }}
                </el-tag>
              </div>
              <div class="member-time">
                加入时间: {{ formatDateTime(member.createTime) }}
              </div>
              <el-button
                link
                type="danger"
                :icon="Delete"
                @click="handleRemoveMember(member)"
              >
                移除
              </el-button>
            </div>
          </div>
          <el-empty v-else description="暂无成员，点击右上角添加" :image-size="100" />
        </div>
      </el-tab-pane>

      <!-- 迭代管理标签 - 仅中大型项目显示 -->
      <el-tab-pane v-if="project?.projectType === 'LARGE_SCALE'" name="iterations">
        <template #label>
          <span>
            <el-icon><FolderOpened /></el-icon>
            迭代管理
            <el-badge :value="iterations.length" class="tab-badge" />
          </span>
        </template>
        <div class="tab-content">
          <div class="content-header">
            <div class="header-title">
              <h3>迭代列表</h3>
              <span class="subtitle">共 {{ iterations.length }} 个迭代</span>
            </div>
            <el-button type="primary" :icon="Plus" @click="handleCreateIteration">
              新建迭代
            </el-button>
          </div>

          <!-- 迭代统计 -->
          <div class="task-stats">
            <div class="stat-item">
              <div class="stat-label">全部</div>
              <div class="stat-value">{{ iterations.length }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">未开始</div>
              <div class="stat-value todo">{{ iterationStats.notStarted }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">进行中</div>
              <div class="stat-value inProgress">{{ iterationStats.inProgress }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">已完成</div>
              <div class="stat-value done">{{ iterationStats.completed }}</div>
            </div>
          </div>

          <!-- 迭代列表 -->
          <el-table :data="iterations" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="迭代名称" min-width="200">
              <template #default="{ row }">
                <el-link type="primary" @click="handleViewIteration(row)">{{ row.name }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getIterationStatusType(row.status)" size="small">
                  {{ getIterationStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="planStartDate" label="开始日期" width="110" />
            <el-table-column prop="planEndDate" label="结束日期" width="110" />
            <el-table-column prop="taskCount" label="任务数" width="80">
              <template #default="{ row }">
                {{ row.taskCount || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="handleViewIteration(row)">查看</el-button>
                <el-button link type="primary" :icon="Edit" @click="handleEditIteration(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDeleteIteration(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="iterations.length === 0" description="暂无迭代，点击右上角创建" :image-size="100" />
        </div>
      </el-tab-pane>

      <!-- 项目任务标签 -->
      <el-tab-pane name="tasks">
        <template #label>
          <span>
            <el-icon><List /></el-icon>
            项目任务
            <el-badge :value="taskTotal" class="tab-badge" />
          </span>
        </template>
        <div class="tab-content">
          <div class="content-header">
            <div class="header-title">
              <h3>项目任务</h3>
              <span class="subtitle">共 {{ taskTotal }} 个任务</span>
            </div>
            <el-button type="primary" :icon="Plus" @click="handleCreateTask">
              新建任务
            </el-button>
          </div>

          <!-- 任务统计 -->
          <div class="task-stats">
            <div class="stat-item">
              <div class="stat-label">全部</div>
              <div class="stat-value">{{ taskStats.total }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">待办</div>
              <div class="stat-value todo">{{ taskStats.todo }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">进行中</div>
              <div class="stat-value inProgress">{{ taskStats.inProgress }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">已完成</div>
              <div class="stat-value done">{{ taskStats.done }}</div>
            </div>
          </div>

          <!-- 任务列表 -->
          <el-table :data="tasks" stripe style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="任务标题" min-width="200">
              <template #default="{ row }">
                <el-link type="primary" @click="handleViewTask(row)">{{ row.title }}</el-link>
              </template>
            </el-table-column>
            <el-table-column prop="assigneeName" label="负责人" width="110" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="getTaskStatusType(row.status)" size="small">
                  {{ getTaskStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="90">
              <template #default="{ row }">
                <el-tag :type="getPriorityType(row.priority)" size="small">
                  {{ getPriorityText(row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="planEndDate" label="计划结束" width="110" />
            <el-table-column prop="createTime" label="创建时间" width="160" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :icon="View" @click="handleViewTask(row)">查看</el-button>
                <el-button link type="primary" :icon="Edit" @click="handleEditTask(row)">编辑</el-button>
                <el-button link type="danger" :icon="Delete" @click="handleDeleteTask(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination">
            <el-pagination
              v-model:current-page="taskSearchForm.pageNum"
              v-model:page-size="taskSearchForm.pageSize"
              :total="taskTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="fetchTasks"
              @current-change="fetchTasks"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 甘特图标签 -->
      <el-tab-pane name="gantt">
        <template #label>
          <span>
            <el-icon><Grid /></el-icon>
            甘特图
          </span>
        </template>
        <div class="tab-content gantt-tab-content">
          <ProjectGantt v-if="activeTab === 'gantt'" :project-id="projectId" />
        </div>
      </el-tab-pane>
    </el-tabs>

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

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="memberDialogVisible"
      title="添加项目成员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="memberFormRef" :model="memberFormData" :rules="memberFormRules" label-width="100px">
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="memberFormData.userId"
            placeholder="请选择用户"
            filterable
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
        <el-form-item label="成员角色" prop="roleType">
          <el-select v-model="memberFormData.roleType" placeholder="请选择角色" style="width: 100%">
            <el-option label="项目管理员" :value="1" />
            <el-option label="项目成员" :value="2" />
            <el-option label="只读访客" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMemberSubmit" :loading="memberSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 新建任务对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      title="新建任务"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="taskFormRef" :model="taskFormData" :rules="taskFormRules" label-width="100px">
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
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTaskSubmit" :loading="taskSubmitLoading">
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
  InfoFilled,
  User,
  List,
  Plus,
  View,
  Grid,
  FolderOpened
} from '@element-plus/icons-vue'
import { getProjectDetail, updateProject, deleteProject, getProjectMembers, addProjectMember, removeProjectMember } from '@/api/project'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/task'
import { getAllUsers, type UserInfo } from '@/api/user'
import { getProjectStatusInfo } from '@/utils/statusMapping'
import { getIterationList, createIteration, updateIteration, deleteIteration, type IterationInfo } from '@/api/iteration'
import ProjectGantt from '@/components/ProjectGantt.vue'

const route = useRoute()
const router = useRouter()
const projectId = computed(() => Number(route.params.id))

// 当前激活的标签页
const activeTab = ref('info')

// 折叠面板激活项
const activeCollapse = ref(['basic', 'date'])

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

// 任务统计
const taskStats = computed(() => {
  const stats = { total: tasks.value.length, todo: 0, inProgress: 0, done: 0 }
  tasks.value.forEach(task => {
    if (task.status === 'TODO' || task.status === null) stats.todo++
    else if (task.status === 'IN_PROGRESS') stats.inProgress++
    else if (task.status === 'DONE') stats.done++
  })
  return stats
})

// 迭代列表（仅中大型项目使用）
const iterations = ref<IterationInfo[]>([])
const iterationStats = computed(() => {
  const stats = { notStarted: 0, inProgress: 0, completed: 0 }
  iterations.value.forEach(iter => {
    if (iter.status === 'NOT_STARTED') stats.notStarted++
    else if (iter.status === 'IN_PROGRESS') stats.inProgress++
    else if (iter.status === 'COMPLETED') stats.completed++
  })
  return stats
})

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

// 添加成员对话框
const memberDialogVisible = ref(false)
const memberSubmitLoading = ref(false)
const memberFormRef = ref<FormInstance>()
const memberFormData = reactive({
  userId: undefined as number | undefined,
  roleType: 2 // 默认为普通成员
})
const memberFormRules: FormRules = {
  userId: [{ required: true, message: '请选择用户', trigger: 'change' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

// 新建任务对话框
const taskDialogVisible = ref(false)
const taskSubmitLoading = ref(false)
const taskFormRef = ref<FormInstance>()
const taskFormData = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  status: 'TODO',
  assigneeId: undefined as number | undefined,
  planStartDate: '',
  planEndDate: '',
  estimateHours: undefined as number | undefined
})
const taskFormRules: FormRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }]
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
    // 如果是中大型项目，获取迭代列表
    if (res.projectType === 'LARGE_SCALE') {
      await fetchIterations()
    }
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
  } catch (error) {
    console.error('获取项目成员失败:', error)
  }
}

// 获取可用用户列表
const fetchAvailableUsers = async () => {
  try {
    const res = await getAllUsers()
    if (res.list) {
      // 过滤掉已经是项目成员的用户
      const memberIds = members.value.map(m => m.id)
      availableUsers.value = res.list.filter((u: UserInfo) => !memberIds.includes(u.id))
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 获取任务列表
const fetchTasks = async () => {
  try {
    taskSearchForm.projectId = projectId.value
    const res = await getTaskList(taskSearchForm)
    tasks.value = res.list || []
    taskTotal.value = res.total || 0
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

// 获取迭代列表（仅中大型项目）
const fetchIterations = async () => {
  if (project.value?.projectType !== 'LARGE_SCALE') {
    return
  }
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

// 添加成员
const handleAddMember = () => {
  fetchAvailableUsers()
  memberFormData.userId = undefined
  memberFormData.roleType = 2 // 重置为普通成员
  memberDialogVisible.value = true
}

// 提交添加成员
const handleAddMemberSubmit = async () => {
  if (!memberFormRef.value) return

  await memberFormRef.value.validate(async (valid) => {
    if (valid) {
      memberSubmitLoading.value = true
      try {
        await addProjectMember(projectId.value, memberFormData.userId!, memberFormData.roleType)
        ElMessage.success('添加成功')
        memberDialogVisible.value = false
        fetchMembers()
      } catch (error) {
        console.error('添加成员失败:', error)
      } finally {
        memberSubmitLoading.value = false
      }
    }
  })
}

// 移除成员
const handleRemoveMember = (member: ProjectMemberInfo) => {
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
const handleCreateTask = () => {
  // 重置表单
  Object.assign(taskFormData, {
    title: '',
    description: '',
    priority: 'MEDIUM',
    status: 'TODO',
    assigneeId: undefined,
    planStartDate: '',
    planEndDate: '',
    estimateHours: undefined
  })
  // 打开对话框
  taskDialogVisible.value = true
}

// 提交新建任务
const handleCreateTaskSubmit = async () => {
  if (!taskFormRef.value) return

  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return

    taskSubmitLoading.value = true
    try {
      const taskData = {
        projectId: projectId.value,
        title: taskFormData.title,
        description: taskFormData.description,
        priority: taskFormData.priority,
        status: taskFormData.status,
        assigneeId: taskFormData.assigneeId,
        planStartDate: taskFormData.planStartDate || undefined,
        planEndDate: taskFormData.planEndDate || undefined,
        estimateHours: taskFormData.estimateHours
      }

      await createTask(taskData)
      ElMessage.success('任务创建成功')
      taskDialogVisible.value = false
      fetchTasks() // 刷新任务列表
    } catch (error) {
      console.error('创建任务失败:', error)
      ElMessage.error('创建任务失败')
    } finally {
      taskSubmitLoading.value = false
    }
  })
}

// 查看任务
const handleViewTask = (task: TaskInfo) => {
  router.push(`/tasks/${task.id}`)
}

// 编辑任务
const handleEditTask = (task: TaskInfo) => {
  ElMessage.info('编辑任务功能开发中')
  console.log('编辑任务:', task)
}

// 删除任务
const handleDeleteTask = (task: TaskInfo) => {
  ElMessageBox.confirm(`确定要删除任务 "${task.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTask(task.id)
        ElMessage.success('删除成功')
        fetchTasks()
      } catch (error) {
        console.error('删除任务失败:', error)
      }
    })
    .catch(() => {})
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

const handleEditIteration = (iteration: IterationInfo) => {
  // 填充表单
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
      } catch (error) {
        console.error('删除迭代失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleViewIteration = (iteration: IterationInfo) => {
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
.project-detail {
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

.tab-badge {
  margin-left: 8px;
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

/* 成员列表 */
.member-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.member-item:hover {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-color: #1890ff;
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.member-time {
  font-size: 12px;
  color: #8c8c8c;
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
</style>
