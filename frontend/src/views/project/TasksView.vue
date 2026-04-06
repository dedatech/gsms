<template>
  <div class="tasks-view">
    <!-- 三栏布局 -->
    <div class="three-column-layout">
      <!-- 左栏：迭代+需求导航树 -->
      <div class="left-column navigation-tree-column">
        <div class="tree-toolbar">
          <h3>迭代与需求</h3>
          <div class="toolbar-actions">
            <el-tooltip content="全部展开" placement="top">
              <el-button :icon="ArrowDown" text circle size="small" @click="expandAll" />
            </el-tooltip>
            <el-tooltip content="全部折叠" placement="top">
              <el-button :icon="ArrowRight" text circle size="small" @click="collapseAll" />
            </el-tooltip>
          </div>
        </div>
        <div class="navigation-tree">
          <!-- 未分配任务节点 -->
          <div
            v-if="unassignedTasks.length > 0"
            class="iteration-node"
            :class="{ active: selectedIterationId === null }"
            @click="handleSelectUnassigned"
          >
            <div class="iteration-header">
              <el-icon><FolderOpened /></el-icon>
              <span class="iteration-name">未分配任务</span>
              <el-badge :value="unassignedTasks.length" class="task-badge" />
            </div>
          </div>

          <!-- 迭代列表 -->
          <div
            v-for="iteration in iterationList"
            :key="iteration.id"
            class="iteration-node"
            :class="{
              active: selectedIterationId === iteration.id,
              'status-in-progress': iteration.status === 'IN_PROGRESS',
              'status-completed': iteration.status === 'COMPLETED',
              'status-not-started': iteration.status === 'NOT_STARTED'
            }"
          >
            <div class="iteration-header" @click="handleSelectIteration(iteration.id)">
              <!-- 状态图标 -->
              <el-icon class="status-icon">
                <component :is="getIterationStatusIcon(iteration.status)" />
              </el-icon>

              <!-- 展开/折叠图标 -->
              <el-icon
                class="expand-icon"
                :class="{ expanded: iteration.expanded }"
                @click.stop="toggleIterationExpand(iteration)"
              >
                <ArrowRight />
              </el-icon>

              <span class="iteration-name">{{ iteration.name }}</span>

              <!-- 状态标签 -->
              <el-tag v-if="showStatusTags" size="small" :type="getIterationStatusTagType(iteration.status)" class="status-tag">
                {{ getIterationStatusText(iteration.status) }}
              </el-tag>

              <!-- 任务数量 -->
              <el-badge :value="iteration.taskCount" class="task-badge" />
            </div>

            <!-- 需求列表 -->
            <div v-if="iteration.expanded" class="iteration-body">
              <div
                v-for="requirement in iteration.requirements"
                :key="requirement.id"
                class="requirement-node"
                :class="{ active: selectedRequirementId === requirement.id }"
                @click.stop="handleSelectRequirement(requirement)"
              >
                <div class="requirement-header">
                  <el-icon class="requirement-icon"><Document /></el-icon>
                  <span class="requirement-title">{{ requirement.title }}</span>
                  <el-badge :value="requirement.taskCount" class="task-badge" type="info" />
                </div>
              </div>

              <!-- 如果没有需求，显示提示 -->
              <div v-if="iteration.requirements.length === 0" class="empty-requirements">
                <span class="empty-text">暂无需求</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中栏：工作项列表 -->
      <div class="middle-column work-items-column">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="toolbar-left">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索工作项..."
              :prefix-icon="Search"
              clearable
              style="width: 240px"
              @input="handleSearch"
            />
            <el-select v-model="selectedTaskType" placeholder="类型" clearable style="width: 120px" @change="handleFilterChange">
              <el-option label="全部" :value="undefined" />
              <el-option label="任务" value="TASK" />
              <el-option label="需求" value="REQUIREMENT" />
              <el-option label="缺陷" value="BUG" />
            </el-select>
          </div>
          <div class="toolbar-right">
            <el-button-group>
              <el-button
                :type="viewMode === 'list' ? 'primary' : ''"
                :icon="List"
                @click="viewMode = 'list'"
              />
              <el-button
                :type="viewMode === 'kanban' ? 'primary' : ''"
                :icon="Grid"
                @click="viewMode = 'kanban'"
              />
              <el-button
                :type="viewMode === 'tree' ? 'primary' : ''"
                :icon="Share"
                @click="viewMode = 'tree'"
              />
            </el-button-group>
            <el-button type="primary" :icon="Plus" @click="handleCreateTask">
              新建工作项
            </el-button>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-if="viewMode === 'list'" class="list-view">
          <el-table
            :data="filteredTasks"
            :show-header="true"
            stripe
            @row-click="handleSelectTask"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="id" label="编号" width="80">
              <template #default="{ row }">
                <span class="task-number">{{ row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="getTaskTypeTag(row.type)" size="small">
                  {{ getTaskTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assigneeName" label="负责人" width="100" />
            <el-table-column prop="iterationName" label="迭代" width="120" />
            <el-table-column prop="priority" label="优先级" width="80">
              <template #default="{ row }">
                <el-tag :type="getPriorityTag(row.priority)" size="small">
                  {{ getPriorityText(row.priority) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" size="small" @click.stop="handleEditTask(row)">
                  编辑
                </el-button>
                <el-button text type="danger" size="small" @click.stop="handleDeleteTask(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 看板视图 -->
        <div v-else-if="viewMode === 'kanban'" class="kanban-view">
          <el-row :gutter="16">
            <el-col
              v-for="column in kanbanColumns"
              :key="column.key"
              :xs="24"
              :sm="12"
              :md="8"
            >
              <div
                class="kanban-column"
                @dragover.prevent="handleDragOver"
                @dragleave="handleDragLeave"
                @drop="handleDropToStatus($event, column.key)"
              >
                <div class="column-header">
                  <div class="status-dot" :style="{ backgroundColor: column.color }"></div>
                  <span class="column-title">{{ column.label }}</span>
                  <el-badge :value="column.tasks.length" class="column-badge" />
                </div>
                <div class="column-body">
                  <div
                    v-for="task in column.tasks"
                    :key="task.id"
                    class="task-card"
                    draggable="true"
                    @dragstart="handleDragStart(task, $event)"
                    @click="handleSelectTask(task)"
                  >
                    <div class="task-card-header">
                      <span class="task-number">{{ task.id }}</span>
                      <el-tag :type="getTaskTypeTag(task.type)" size="small">
                        {{ getTaskTypeText(task.type) }}
                      </el-tag>
                    </div>
                    <div class="task-card-title">{{ task.title }}</div>
                    <div class="task-card-footer">
                      <el-avatar :size="24" v-if="task.assigneeName">
                        {{ task.assigneeName?.charAt(0) }}
                      </el-avatar>
                      <el-tag :type="getPriorityTag(task.priority)" size="small">
                        {{ getPriorityText(task.priority) }}
                      </el-tag>
                    </div>
                  </div>
                  <el-empty v-if="column.tasks.length === 0" description="暂无数据" :image-size="60" />
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 树形视图 -->
        <div v-else-if="viewMode === 'tree'" class="tree-view">
          <el-table
            :data="treeTasks"
            :show-header="true"
            stripe
            row-key="id"
            :tree-props="{ children: 'subtasks', hasChildren: 'hasChildren' }"
            @row-click="handleSelectTask"
          >
            <el-table-column prop="id" label="编号" width="80">
              <template #default="{ row }">
                <span class="task-number">{{ row.id }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="200" />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="getTaskTypeTag(row.type)" size="small">
                  {{ getTaskTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assigneeName" label="负责人" width="100" />
          </el-table>
        </div>
      </div>

      <!-- 右栏：详情面板 -->
      <div class="right-column detail-panel-column">
        <div v-if="selectedTask" class="detail-panel">
          <div class="panel-header">
            <div class="task-number">{{ selectedTask.id }}</div>
            <el-button text :icon="Close" @click="selectedTask = null" />
          </div>

          <div class="panel-body">
            <!-- 标题和状态 -->
            <div class="detail-header">
              <h2 class="task-title">{{ selectedTask.title }}</h2>
              <el-dropdown trigger="click" @command="handleStatusChange">
                <el-tag :type="getStatusTag(selectedTask.status)" style="cursor: pointer">
                  {{ getStatusText(selectedTask.status) }}
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-tag>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="TODO">待办</el-dropdown-item>
                    <el-dropdown-item command="IN_PROGRESS">进行中</el-dropdown-item>
                    <el-dropdown-item command="DONE">已完成</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <!-- 负责人 -->
            <div class="detail-section">
              <div class="section-label">负责人</div>
              <div class="section-value">
                <el-avatar :size="32" v-if="selectedTask.assigneeName">
                  {{ selectedTask.assigneeName?.charAt(0) }}
                </el-avatar>
                <span v-if="selectedTask.assigneeName">{{ selectedTask.assigneeName }}</span>
                <span v-else class="empty-text">未分配</span>
              </div>
            </div>

            <!-- 描述 -->
            <div class="detail-section">
              <div class="section-label">描述</div>
              <div class="section-value description">
                {{ selectedTask.description || '暂无描述' }}
              </div>
            </div>

            <!-- 关联信息 -->
            <div class="detail-section" v-if="selectedTask.parentId">
              <div class="section-label">父级需求</div>
              <div class="section-value">
                <el-link type="primary" @click="handleViewParent(selectedTask.parentId)">
                  #{{ selectedTask.parentId }}
                </el-link>
              </div>
            </div>

            <!-- 工时信息 -->
            <div class="detail-section">
              <div class="section-label">工时</div>
              <div class="section-value">
                <div class="hours-info">
                  <span>预估：{{ selectedTask.estimateHours || '-' }}小时</span>
                  <span>实际：{{ actualHours }}小时</span>
                </div>
              </div>
            </div>

            <!-- 元数据 -->
            <div class="detail-section">
              <div class="section-label">元数据</div>
              <div class="section-value metadata">
                <div>创建时间：{{ selectedTask.createTime }}</div>
                <div>更新时间：{{ selectedTask.updateTime }}</div>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-detail">
          <el-empty description="选择一个工作项查看详情" :image-size="80" />
        </div>
      </div>
    </div>

    <!-- 新建工作项对话框 -->
    <el-dialog
      v-model="taskDialogVisible"
      :title="taskDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="taskFormRef" :model="taskFormData" :rules="taskFormRules" label-width="100px">
        <el-form-item label="工作项类型" prop="type">
          <el-radio-group v-model="taskFormData.type">
            <el-radio value="REQUIREMENT">需求</el-radio>
            <el-radio value="TASK">任务</el-radio>
            <el-radio value="BUG">缺陷</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="所属迭代">
          <el-select
            v-model="taskFormData.iterationId"
            placeholder="选填：可选择关联迭代"
            filterable
            clearable
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
        <el-form-item label="父级任务">
          <el-select
            v-model="taskFormData.parentId"
            placeholder="选填：选择父级任务"
            filterable
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="task in parentTaskOptions"
              :key="task.id"
              :label="`#${task.id} ${task.title}`"
              :value="task.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="taskFormData.title"
            placeholder="请输入标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="taskFormData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="taskFormData.priority">
            <el-radio value="LOW">低</el-radio>
            <el-radio value="MEDIUM">中</el-radio>
            <el-radio value="HIGH">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="taskFormData.status">
            <el-radio value="TODO">待办</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="DONE">已完成</el-radio>
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
        <el-button type="primary" @click="handleTaskSubmit" :loading="taskSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 新建迭代对话框 -->
    <el-dialog
      v-model="iterationDialogVisible"
      title="新建迭代"
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Search,
  List,
  Grid,
  Share,
  Close,
  ArrowRight,
  ArrowDown
} from '@element-plus/icons-vue'
import { getTasksByProjectId, createTask, updateTask, deleteTask, updateTaskStatus, updateTaskIterationId, type TaskInfo, type TaskCreateReq } from '@/api/task'
import { getIterationList, createIteration, type IterationInfo } from '@/api/iteration'
import { getAllUsers, type UserInfo } from '@/api/user'
import { getProjectMembers } from '@/api/project'

const route = useRoute()
const projectId = computed(() => Number(route.params.id))

// 数据定义
const tasks = ref<TaskInfo[]>([])
const iterations = ref<IterationInfo[]>([])
const availableUsers = ref<UserInfo[]>([])

// 视图模式
const viewMode = ref<'list' | 'kanban' | 'tree'>('list')

// 搜索和筛选
const searchKeyword = ref('')
const selectedTaskType = ref<string>()
const selectedIterationId = ref<number | null>(null)
const selectedRequirementId = ref<number | null>(null)
const selectedTaskId = ref<number>()
const selectedTask = ref<TaskInfo | null>(null)
const showStatusTags = ref(true) // 显示状态标签

// 拖拽状态
const draggedTask = ref<TaskInfo | null>(null)

// 新建工作项对话框
const taskDialogVisible = ref(false)
const taskDialogTitle = computed(() => '新建工作项')
const taskSubmitLoading = ref(false)
const taskFormRef = ref<FormInstance>()
const taskFormData = reactive<TaskCreateReq>({
  projectId: projectId.value,
  title: '',
  description: '',
  type: 'TASK',
  priority: 'MEDIUM',
  status: 'TODO',
  assigneeId: undefined,
  iterationId: undefined,
  parentId: undefined,
  estimateHours: undefined
})
const taskFormRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

// 新建迭代对话框
const iterationDialogVisible = ref(false)
const iterationSubmitLoading = ref(false)
const iterationFormRef = ref<FormInstance>()
const iterationFormData = reactive({
  projectId: projectId.value,
  name: '',
  description: '',
  planStartDate: '',
  planEndDate: ''
})
const iterationFormRules: FormRules = {
  name: [{ required: true, message: '请输入迭代名称', trigger: 'blur' }]
}

// 迭代列表数据结构（包含需求）
interface IterationNode {
  id: number
  name: string
  status: string
  taskCount: number
  expanded: boolean
  requirements: {
    id: number
    title: string
    taskCount: number
  }[]
}

// 迭代列表（包含需求）
const iterationList = computed<IterationNode[]>(() => {
  return iterations.value.map(iteration => {
    // 获取该迭代下的所有需求
    const requirements = tasks.value.filter(
      t => t.iterationId === iteration.id && t.type === 'REQUIREMENT'
    ).map(req => ({
      id: req.id,
      title: req.title,
      taskCount: tasks.value.filter(
        t => t.parentId === req.id || t.id === req.id
      ).length
    }))

    // 获取该迭代下的所有任务（包括直接分配给迭代的）
    const iterationTasks = tasks.value.filter(
      t => t.iterationId === iteration.id && !t.parentId
    )

    return {
      id: iteration.id,
      name: iteration.name,
      status: iteration.status,
      taskCount: iterationTasks.length,
      expanded: false,
      requirements
    }
  })
})

// 未分配的任务（没有迭代）
const unassignedTasks = computed(() => {
  return tasks.value.filter(t => !t.iterationId)
})

// 过滤后的任务列表
const filteredTasks = computed(() => {
  let result = tasks.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t =>
      t.title.toLowerCase().includes(keyword) ||
      t.id.toString().includes(keyword)
    )
  }

  // 类型筛选
  if (selectedTaskType.value) {
    result = result.filter(t => t.type === selectedTaskType.value)
  }

  // 迭代筛选
  if (selectedIterationId.value) {
    result = result.filter(t => t.iterationId === selectedIterationId.value)
  }

  // 需求筛选
  if (selectedRequirementId.value) {
    result = result.filter(t => t.parentId === selectedRequirementId.value || t.id === selectedRequirementId.value)
  }

  return result
})

// 看板列定义
const kanbanColumns = computed(() => {
  return [
    {
      key: 'TODO',
      label: '待办',
      color: '#faad14',
      tasks: filteredTasks.value.filter(t => t.status === 'TODO')
    },
    {
      key: 'IN_PROGRESS',
      label: '进行中',
      color: '#1890ff',
      tasks: filteredTasks.value.filter(t => t.status === 'IN_PROGRESS')
    },
    {
      key: 'DONE',
      label: '已完成',
      color: '#52c41a',
      tasks: filteredTasks.value.filter(t => t.status === 'DONE')
    }
  ]
})

// 树形视图数据
const treeTasks = computed(() => {
  const requirements = tasks.value.filter(t => t.type === 'REQUIREMENT')
  return requirements.map(req => ({
    ...req,
    subtasks: tasks.value.filter(t => t.parentId === req.id)
  }))
})

// 实际工时（从工时记录计算）
const actualHours = computed(() => {
  if (!selectedTask.value?.estimateHours) return '-'
  // TODO: 从工时记录表获取实际工时
  // 临时方案：使用剩余工时计算
  const remaining = selectedTask.value.estimateHours - 0
  return remaining.toFixed(1)
})

// 获取数据
const fetchTasks = async () => {
  try {
    const res = await getTasksByProjectId(projectId.value, 1, 1000)
    tasks.value = res.list || []
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

const fetchIterations = async () => {
  try {
    const res = await getIterationList({ projectId: projectId.value, pageNum: 1, pageSize: 100 })
    iterations.value = res.list || []
  } catch (error) {
    console.error('获取迭代列表失败:', error)
  }
}

const fetchAvailableUsers = async () => {
  try {
    const members = await getProjectMembers(projectId.value)
    if (members && members.length > 0) {
      availableUsers.value = members.map(m => ({
        id: m.userId,
        nickname: m.nickname,
        username: m.username || ''
      }))
    } else {
      const res = await getAllUsers()
      availableUsers.value = res.list || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 事件处理
const handleSearch = () => {
  // 搜索逻辑在 computed 中处理
}

const handleFilterChange = () => {
  // 筛选逻辑在 computed 中处理
}

// 选择未分配的任务
const handleSelectUnassigned = () => {
  selectedIterationId.value = null
  selectedRequirementId.value = null
}

// 选择迭代
const handleSelectIteration = (iterationId: number) => {
  selectedIterationId.value = iterationId
  selectedRequirementId.value = null
}

// 选择需求
const handleSelectRequirement = (requirement: { id: number; title: string }) => {
  selectedRequirementId.value = requirement.id
  // 自动展开所属迭代
  const iteration = iterationList.value.find(iter =>
    iter.requirements.some(req => req.id === requirement.id)
  )
  if (iteration) {
    selectedIterationId.value = iteration.id
  }
}

// 展开/折叠迭代
const toggleIterationExpand = (iteration: IterationNode) => {
  iteration.expanded = !iteration.expanded
}

// 全部展开
const expandAll = () => {
  iterationList.value.forEach(iter => {
    iter.expanded = true
  })
}

// 全部折叠
const collapseAll = () => {
  iterationList.value.forEach(iter => {
    iter.expanded = false
  })
}

// 获取迭代状态图标
const getIterationStatusIcon = (status: string) => {
  const icons: Record<string, any> = {
    'NOT_STARTED': Calendar,
    'IN_PROGRESS': VideoPlay,
    'COMPLETED': CircleCheck
  }
  return icons[status] || Calendar
}

// 获取迭代状态标签类型
const getIterationStatusTagType = (status: string) => {
  const types: Record<string, string> = {
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

const handleSelectTask = (task: TaskInfo) => {
  selectedTaskId.value = task.id
  selectedTask.value = task
}

const handleCreateTask = () => {
  Object.assign(taskFormData, {
    projectId: projectId.value,
    title: '',
    description: '',
    type: 'TASK',
    priority: 'MEDIUM',
    status: 'TODO',
    assigneeId: undefined,
    iterationId: selectedIterationId.value || undefined,
    parentId: undefined,
    estimateHours: undefined
  })
  taskDialogVisible.value = true
}

const handleEditTask = (task: TaskInfo) => {
  selectedTask.value = task
  Object.assign(taskFormData, {
    projectId: projectId.value,
    title: task.title,
    description: task.description,
    type: task.type,
    priority: task.priority,
    status: task.status,
    assigneeId: task.assigneeId,
    iterationId: task.iterationId,
    parentId: task.parentId,
    estimateHours: task.estimateHours
  })
  taskDialogVisible.value = true
}

const handleDeleteTask = (task: TaskInfo) => {
  ElMessageBox.confirm(`确定要删除工作项 "${task.title}" 吗？`, '提示', {
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
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const handleTaskSubmit = async () => {
  if (!taskFormRef.value) return

  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return

    taskSubmitLoading.value = true
    try {
      if (selectedTask.value) {
        // 更新
        await updateTask({
          id: selectedTask.value.id,
          ...taskFormData
        })
        ElMessage.success('更新成功')
      } else {
        // 新建
        await createTask(taskFormData)
        ElMessage.success('创建成功')
      }
      taskDialogVisible.value = false
      fetchTasks()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    } finally {
      taskSubmitLoading.value = false
    }
  })
}

const handleStatusChange = async (status: string) => {
  if (!selectedTask.value) return

  try {
    await updateTaskStatus({
      id: selectedTask.value.id,
      status
    })
    ElMessage.success('状态更新成功')
    fetchTasks()
    // 更新选中任务的状态
    if (selectedTask.value) {
      selectedTask.value.status = status
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  }
}

const handleCreateIteration = () => {
  Object.assign(iterationFormData, {
    projectId: projectId.value,
    name: '',
    description: '',
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
      await createIteration(iterationFormData)
      ElMessage.success('创建成功')
      iterationDialogVisible.value = false
      fetchIterations()
    } catch (error) {
      console.error('创建失败:', error)
      ElMessage.error('创建失败')
    } finally {
      iterationSubmitLoading.value = false
    }
  })
}

// 拖拽处理
const handleDragStart = (task: TaskInfo, event: DragEvent) => {
  draggedTask.value = task
  if (event.dataTransfer) {
    event.dataTransfer.setData('text/plain', task.id.toString())
    event.dataTransfer.effectAllowed = 'move'
  }
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  ;(event.currentTarget as HTMLElement).classList.add('drag-over')
}

const handleDragLeave = (event: DragEvent) => {
  ;(event.currentTarget as HTMLElement).classList.remove('drag-over')
}

const handleDropToStatus = async (event: DragEvent, status: string) => {
  event.preventDefault()
  ;(event.currentTarget as HTMLElement).classList.remove('drag-over')

  if (draggedTask.value && draggedTask.value.status !== status) {
    try {
      await updateTaskStatus({
        id: draggedTask.value.id,
        status
      })
      ElMessage.success('状态更新成功')
      fetchTasks()
    } catch (error) {
      console.error('状态更新失败:', error)
      ElMessage.error('状态更新失败')
    }
  }

  draggedTask.value = null
}

const handleDropToIteration = async (event: DragEvent, iterationId: number) => {
  event.preventDefault()

  if (draggedTask.value) {
    try {
      await updateTaskIterationId(draggedTask.value.id, iterationId === 0 ? undefined : iterationId)
      ElMessage.success('迭代更新成功')
      fetchTasks()
    } catch (error) {
      console.error('迭代更新失败:', error)
      ElMessage.error('迭代更新失败')
    }
  }

  draggedTask.value = null
}

// 工具函数
const getTaskTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    'REQUIREMENT': 'success',
    'TASK': 'primary',
    'BUG': 'danger'
  }
  return tags[type] || 'info'
}

const getTaskTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'REQUIREMENT': '需求',
    'TASK': '任务',
    'BUG': '缺陷'
  }
  return texts[type] || '未知'
}

const getStatusTag = (status: string) => {
  const tags: Record<string, string> = {
    'TODO': 'info',
    'IN_PROGRESS': 'primary',
    'DONE': 'success'
  }
  return tags[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'TODO': '待办',
    'IN_PROGRESS': '进行中',
    'DONE': '已完成'
  }
  return texts[status] || '未知'
}

const getPriorityTag = (priority: string) => {
  const tags: Record<string, string> = {
    'LOW': 'info',
    'MEDIUM': '',
    'HIGH': 'warning'
  }
  return tags[priority] || 'info'
}

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  }
  return texts[priority] || '未知'
}

const handleViewParent = (parentId: number) => {
  const parent = tasks.value.find(t => t.id === parentId)
  if (parent) {
    handleSelectTask(parent)
  }
}

// 快捷键支持
const handleKeydown = (event: KeyboardEvent) => {
  // N - 新建
  if (event.key === 'n' || event.key === 'N') {
    if (!taskDialogVisible.value) {
      handleCreateTask()
    }
  }
  // F - 搜索
  else if (event.key === 'f' || event.key === 'F') {
    if (!taskDialogVisible.value) {
      const searchInput = document.querySelector('.toolbar-left .el-input__inner') as HTMLInputElement
      searchInput?.focus()
    }
  }
  // Esc - 关闭详情面板
  else if (event.key === 'Escape') {
    if (selectedTask.value) {
      selectedTask.value = null
      selectedTaskId.value = undefined
    }
  }
}

// 生命周期
onMounted(() => {
  fetchTasks()
  fetchIterations()
  fetchAvailableUsers()
  document.addEventListener('keydown', handleKeydown)
})

// 清理事件监听
import { onUnmounted } from 'vue'
onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.tasks-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 三栏布局 */
.three-column-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 0;
}

/* 左栏：导航树 */
.left-column {
  width: 220px;
  min-width: 220px;
  max-width: 220px;
  background: #fff;
  border-right: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 树形工具栏 */
.tree-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}

.tree-toolbar h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.toolbar-actions {
  display: flex;
  gap: 4px;
}

/* 导航树容器 */
.navigation-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

/* 迭代节点状态样式 */
.iteration-node.status-in-progress {
  border-left: 3px solid #1890ff;
}

.iteration-node.status-completed {
  border-left: 3px solid #52c41a;
}

.iteration-node.status-not-started {
  border-left: 3px solid #d9d9d9;
}

.iteration-node {
  padding: 8px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: 4px;
  border-left: 3px solid transparent;
}

.iteration-node:hover {
  background: #f5f5f5;
}

.iteration-node.active {
  background: #e6f7ff;
}

/* 状态图标 */
.status-icon {
  font-size: 16px;
  margin-right: 4px;
}

.status-in-progress .status-icon {
  color: #1890ff;
}

.status-completed .status-icon {
  color: #52c41a;
}

.status-not-started .status-icon {
  color: #d9d9d9;
}

/* 迭代头部 */
.iteration-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.iteration-name {
  flex: 1;
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag {
  font-size: 11px;
  margin-right: 4px;
}

.task-badge {
  margin-left: auto;
}

/* 迭代内容（需求列表） */
.iteration-body {
  margin-top: 8px;
  padding-left: 20px;
}

/* 需求节点 */
.requirement-node {
  padding: 6px 8px;
  margin-bottom: 4px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.requirement-node:hover {
  background: #f5f5f5;
}

.requirement-node.active {
  background: #e6f7ff;
  color: #1890ff;
}

.requirement-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.requirement-icon {
  color: #52c41a;
  font-size: 14px;
}

.requirement-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #666;
}

.requirement-node.active .requirement-title {
  color: #1890ff;
}

/* 空需求提示 */
.empty-requirements {
  padding: 8px;
  text-align: center;
}

.empty-text {
  font-size: 12px;
  color: #999;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

/* 中栏：工作项列表 */
.middle-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f5f5;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list-view,
.kanban-view,
.tree-view {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.task-number {
  color: #1890ff;
  font-family: 'Monaco', 'Consolas', monospace;
  font-weight: 500;
}

/* 看板视图 */
.kanban-column {
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  min-height: 400px;
  transition: all 0.3s;
}

.kanban-column.drag-over {
  background: #e6f7ff;
  box-shadow: 0 0 0 2px #1890ff inset;
}

.column-header {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.column-title {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.column-badge {
  margin-left: auto;
}

.column-body {
  padding: 8px;
  min-height: 300px;
}

.task-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.task-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.task-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.task-card-title {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.task-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 右栏：详情面板 */
.right-column {
  width: 400px;
  min-width: 400px;
  background: #fff;
  border-left: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 24px;
}

.task-title {
  flex: 1;
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
}

.detail-section {
  margin-bottom: 24px;
}

.section-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  font-weight: 500;
}

.section-value {
  font-size: 14px;
  color: #333;
}

.section-value.description {
  white-space: pre-wrap;
  line-height: 1.6;
  color: #666;
}

.section-value.metadata {
  font-size: 12px;
  color: #999;
}

.section-value.metadata > div {
  margin-bottom: 4px;
}

.hours-info {
  display: flex;
  gap: 16px;
}

.empty-text {
  color: #999;
}

.empty-detail {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

/* 滚动条样式 */
.iteration-tree::-webkit-scrollbar,
.column-body::-webkit-scrollbar,
.panel-body::-webkit-scrollbar {
  width: 6px;
}

.iteration-tree::-webkit-scrollbar-thumb,
.column-body::-webkit-scrollbar-thumb,
.panel-body::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.iteration-tree::-webkit-scrollbar-thumb:hover,
.column-body::-webkit-scrollbar-thumb:hover,
.panel-body::-webkit-scrollbar-thumb:hover {
  background: #bfbfbf;
}
</style>
