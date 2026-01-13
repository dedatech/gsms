<template>
  <div class="project-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">项目管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建项目</el-button>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索项目名称"
          :prefix-icon="Search"
          clearable
          style="width: 240px; margin-right: 16px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="searchForm.status"
          placeholder="项目状态"
          clearable
          style="width: 140px; margin-right: 16px"
          @change="handleSearch"
        >
          <el-option label="未开始" value="NOT_STARTED" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已暂停" value="SUSPENDED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
        <el-button-group>
          <el-button
            :type="viewMode === 'kanban' ? 'primary' : ''"
            @click="viewMode = 'kanban'"
          >
            <el-icon><Grid /></el-icon>
            看板
          </el-button>
          <el-button
            :type="viewMode === 'table' ? 'primary' : ''"
            @click="viewMode = 'table'"
          >
            <el-icon><List /></el-icon>
            列表
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 看板视图 -->
    <div v-if="viewMode === 'kanban'" class="kanban-view">
      <el-row :gutter="16">
        <el-col v-for="status in projectStatuses" :key="status.value" :xs="24" :sm="12" :md="6">
          <div
            class="kanban-column"
            :data-status="status.value"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @drop="handleDrop"
          >
            <div class="column-header">
              <div class="column-title">
                <div class="status-dot" :style="{ backgroundColor: status.color }"></div>
                <span>{{ status.label }}</span>
                <span class="project-count">{{ getProjectsByStatus(status.value).length }}</span>
              </div>
            </div>
            <div class="column-body">
              <div
                v-for="project in getProjectsByStatus(status.value)"
                :key="project.id"
                class="project-card"
                draggable="true"
                @dragstart="handleDragStart(project, $event)"
                @click="handleView(project)"
              >
                <div class="card-header">
                  <div class="project-icon" :style="{ backgroundColor: status.color }">
                    <el-icon :size="20"><FolderOpened /></el-icon>
                  </div>
                  <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, project)">
                    <el-icon class="more-icon" @click.stop><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="view" :icon="View">查看</el-dropdown-item>
                        <el-dropdown-item command="gantt" :icon="View">甘特图</el-dropdown-item>
                        <el-dropdown-item command="edit" :icon="Edit">编辑</el-dropdown-item>
                        <el-dropdown-item command="delete" :icon="Delete" divided>删除</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <h3 class="project-name">{{ project.name }}</h3>
                <p class="project-code">{{ project.code }}</p>
                <p class="project-description">{{ project.description || '暂无描述' }}</p>
                <div class="card-footer">
                  <div class="project-meta">
                    <span class="meta-item">
                      <el-icon><User /></el-icon>
                      {{ getManagerName(project.managerId) || '未设置' }}
                    </span>
                  </div>
                  <div class="project-time">
                    <span>{{ formatDate(project.createTime) }}</span>
                  </div>
                </div>
              </div>
              <el-empty
                v-if="getProjectsByStatus(status.value).length === 0"
                description="暂无项目"
                :image-size="60"
              />
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 表格视图 -->
    <div v-else class="table-view">
      <el-table :data="projectList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" min-width="150">
          <template #default="{ row }">
            <div class="table-project-name">
              <div
                class="project-dot"
                :style="{ backgroundColor: getStatusColor(row.status) }"
              ></div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="项目编码" width="120" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="项目经理" width="110">
          <template #default="{ row }">
            {{ getManagerName(row.managerId) || '未设置' }}
          </template>
        </el-table-column>
        <el-table-column prop="planStartDate" label="计划开始" width="110">
          <template #default="{ row }">
            {{ row.planStartDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="planEndDate" label="计划结束" width="110">
          <template #default="{ row }">
            {{ row.planEndDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="actualStartDate" label="实际开始" width="110">
          <template #default="{ row }">
            {{ row.actualStartDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="actualEndDate" label="实际结束" width="110">
          <template #default="{ row }">
            {{ row.actualEndDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" width="100" />
        <el-table-column prop="updateUserName" label="更新人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleView(row)">查看</el-button>
            <el-button link type="success" :icon="Grid" @click="handleGantt(row)">甘特图</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="projectList.length === 0" description="暂无项目数据" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0 && viewMode === 'table'" class="pagination">
      <el-pagination
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :pager-count="7"
        @size-change="fetchProjects"
        @current-change="fetchProjects"
      />
    </div>

    <!-- 新建/编辑项目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入项目编码" />
        </el-form-item>
        <el-form-item label="项目类型" prop="projectType" required>
          <el-radio-group v-model="formData.projectType" class="project-type-group">
            <el-radio value="SCHEDULE" class="project-type-radio">
              <div class="project-type-option">
                <div class="type-header">
                  <el-icon :size="24" color="#409eff"><Calendar /></el-icon>
                  <span class="type-title">常规型项目</span>
                </div>
                <div class="type-desc">适合简单项目，直接管理任务，无需迭代</div>
                <div class="type-features">
                  <el-tag size="small" type="info">简单直接</el-tag>
                  <el-tag size="small" type="success">快速上手</el-tag>
                </div>
              </div>
            </el-radio>
            <el-radio value="LARGE_SCALE" class="project-type-radio">
              <div class="project-type-option">
                <div class="type-header">
                  <el-icon :size="24" color="#67c23a"><FolderOpened /></el-icon>
                  <span class="type-title">中大型项目</span>
                </div>
                <div class="type-desc">适合复杂项目，支持迭代管理（如 Sprint）</div>
                <div class="type-features">
                  <el-tag size="small" type="warning">迭代管理</el-tag>
                  <el-tag size="small" type="danger">敏捷开发</el-tag>
                </div>
              </div>
            </el-radio>
          </el-radio-group>
          <el-alert
            type="info"
            :closable="false"
            style="margin-top: 12px"
            show-icon
          >
            <template #title>
              <span style="font-size: 12px">
                💡 提示：项目类型创建后无法修改，请根据项目规模谨慎选择
              </span>
            </template>
          </el-alert>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="项目经理" prop="managerId">
          <el-select v-model="formData.managerId" placeholder="请选择项目经理" style="width: 100%">
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio value="NOT_STARTED">未开始</el-radio>
            <el-radio value="IN_PROGRESS">进行中</el-radio>
            <el-radio value="SUSPENDED">已暂停</el-radio>
            <el-radio value="ARCHIVED">已归档</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="计划开始时间" prop="planStartDate">
              <el-date-picker
                v-model="formData.planStartDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束时间" prop="planEndDate">
              <el-date-picker
                v-model="formData.planEndDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Search,
  Grid,
  List,
  FolderOpened,
  User,
  MoreFilled,
  Edit,
  Delete,
  View
} from '@element-plus/icons-vue'
import { getProjectList, createProject, updateProject, deleteProject } from '@/api/project'
import { getAllUsers, type UserInfo } from '@/api/user'
import { getProjectStatusOptions, getProjectStatusInfo } from '@/utils/statusMapping'

const router = useRouter()

// 视图模式
const viewMode = ref<'kanban' | 'table'>('kanban')

// 项目状态配置
const projectStatuses = getProjectStatusOptions()

// 拖拽相关
const draggedProject = ref<any>(null)

// 根据状态获取项目（使用computed以优化性能和响应式）
const notStartedProjects = computed(() => projectList.value.filter(project => project.status === 'NOT_STARTED'))
const inProgressProjects = computed(() => projectList.value.filter(project => project.status === 'IN_PROGRESS'))
const suspendedProjects = computed(() => projectList.value.filter(project => project.status === 'SUSPENDED'))
const archivedProjects = computed(() => projectList.value.filter(project => project.status === 'ARCHIVED'))

// 辅助函数：根据状态值返回对应项目列表
const getProjectsByStatus = (status: string) => {
  const statusMap: Record<string, any[]> = {
    'NOT_STARTED': notStartedProjects.value,
    'IN_PROGRESS': inProgressProjects.value,
    'SUSPENDED': suspendedProjects.value,
    'ARCHIVED': archivedProjects.value
  }
  return statusMap[status] || []
}

// 拖拽开始
const handleDragStart = (project: ProjectInfo, event: DragEvent) => {
  draggedProject.value = project
  if (event.dataTransfer) {
    event.dataTransfer.setData('text/plain', project.id.toString())
    event.dataTransfer.effectAllowed = 'move'
  }
  // 添加拖拽样式
  ;(event.target as HTMLElement).classList.add('dragging')
}

// 拖拽经过
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
  // 添加拖拽悬停样式
  const column = (event.currentTarget as HTMLElement)
  column.classList.add('drag-over')
}

// 拖拽离开
const handleDragLeave = (event: DragEvent) => {
  const column = (event.currentTarget as HTMLElement)
  column.classList.remove('drag-over')
}

// 放置项目
const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  const targetStatus = (event.currentTarget as HTMLElement).getAttribute('data-status')

  // 移除拖拽样式
  const draggingElements = document.querySelectorAll('.dragging')
  draggingElements.forEach(el => el.classList.remove('dragging'))

  // 移除所有列的拖拽悬停样式
  const dragOverElements = document.querySelectorAll('.drag-over')
  dragOverElements.forEach(el => el.classList.remove('drag-over'))

  if (draggedProject.value && targetStatus && draggedProject.value.status !== targetStatus) {
    try {
      await updateProject({
        id: draggedProject.value.id,
        status: targetStatus
      })
      ElMessage.success('项目状态已更新')
      fetchProjects() // 刷新项目列表
    } catch (error) {
      console.error('更新项目状态失败:', error)
      ElMessage.error('更新项目状态失败')
    }
  }

  draggedProject.value = null
}

// 搜索表单
const searchForm = reactive({
  name: '',
  status: undefined as string | undefined,
  pageNum: 1,
  pageSize: 10
})

// 项目列表
const projectList = ref<any[]>([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  id: undefined as number | undefined,
  name: '',
  code: '',
  projectType: 'SCHEDULE',
  description: '',
  managerId: undefined as number | undefined,
  status: 'NOT_STARTED',
  planStartDate: '',
  planEndDate: ''
})

// 表单规则
const formRules: FormRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入项目编码', trigger: 'blur' }],
  projectType: [{ required: true, message: '请选择项目类型', trigger: 'change' }],
  managerId: [{ required: true, message: '请选择项目经理', trigger: 'change' }]
}

const formRef = ref<FormInstance>()

// 用户列表
const userList = ref<UserInfo[]>([])

// 获取用户列表
const fetchUsers = async () => {
  try {
    const res = await getAllUsers()
    if (res.list) {
      userList.value = res.list
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 获取项目列表
const fetchProjects = async () => {
  try {
    const res = await getProjectList(searchForm)
    // PageResult 格式：{ list: [], total: 0 }
    projectList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchProjects()
}

// 新建项目
const handleCreate = () => {
  dialogTitle.value = '新建项目'
  dialogVisible.value = true
  resetForm()
}

// 查看项目
const handleView = (project: ProjectInfo) => {
  router.push(`/projects/${project.id}`)
}

// 查看甘特图
const handleGantt = (project: ProjectInfo) => {
  router.push(`/projects/${project.id}/gantt`)
}

// 编辑项目
const handleEdit = (project: ProjectInfo) => {
  dialogTitle.value = '编辑项目'
  dialogVisible.value = true
  Object.assign(formData, {
    id: project.id,
    name: project.name,
    code: project.code,
    description: project.description,
    managerId: project.managerId,
    status: project.status,
    planStartDate: project.planStartDate || '',
    planEndDate: project.planEndDate || ''
  })
}

// 删除项目
const handleDelete = (project: ProjectInfo) => {
  ElMessageBox.confirm(`确定要删除项目 "${project.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteProject(project.id)
        ElMessage.success('删除成功')
        fetchProjects()
      } catch (error) {
        console.error('删除项目失败:', error)
      }
    })
    .catch(() => {})
}

// 卡片操作命令
const handleCommand = (command: string, project: ProjectInfo) => {
  if (command === 'view') {
    handleView(project)
  } else if (command === 'gantt') {
    handleGantt(project)
  } else if (command === 'edit') {
    handleEdit(project)
  } else if (command === 'delete') {
    handleDelete(project)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          // 编辑
          await updateProject({
            id: formData.id,
            name: formData.name,
            description: formData.description,
            status: formData.status,
            planStartDate: formData.planStartDate || undefined,
            planEndDate: formData.planEndDate || undefined
          })
          ElMessage.success('更新成功')
        } else {
          // 新建
          await createProject({
            name: formData.name,
            code: formData.code,
            projectType: formData.projectType,
            description: formData.description,
            managerId: formData.managerId!,
            status: formData.status,
            planStartDate: formData.planStartDate || undefined,
            planEndDate: formData.planEndDate || undefined
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchProjects()
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.code = ''
  formData.projectType = 'SCHEDULE'
  formData.description = ''
  formData.managerId = undefined
  formData.status = 'NOT_STARTED'
  formData.planStartDate = ''
  formData.planEndDate = ''
  formRef.value?.clearValidate()
}

// 获取项目状态信息
const getStatusColor = (status: string) => getProjectStatusInfo(status).color
const getStatusType = (status: string) => getProjectStatusInfo(status).type
const getStatusText = (status: string) => getProjectStatusInfo(status).text

// 获取项目经理名称
const getManagerName = (managerId: number) => {
  if (!managerId) return null
  const user = userList.value.find(u => u.id === managerId)
  return user?.nickname
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchUsers()
  fetchProjects()
})
</script>

<style scoped>
.project-list {
  min-height: calc(100vh - 160px);
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
}

/* 看板视图 */
.kanban-view {
  margin-bottom: 24px;
}

.kanban-column {
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.kanban-column.drag-over {
  background: #e6f7ff;
  box-shadow: 0 0 0 2px #1890ff inset;
}

.column-header {
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #333;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.project-count {
  margin-left: auto;
  font-size: 12px;
  color: #8c8c8c;
  background: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.column-body {
  padding: 16px;
  min-height: 400px;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.project-card {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.project-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.project-card.dragging {
  opacity: 0.5;
  cursor: move;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.project-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.more-icon {
  font-size: 18px;
  color: #8c8c8c;
  cursor: pointer;
  transition: color 0.3s;
}

.more-icon:hover {
  color: #333;
}

.project-name {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-code {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #8c8c8c;
}

.project-description {
  margin: 0 0 12px 0;
  font-size: 12px;
  color: #595959;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 36px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.project-meta {
  display: flex;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #595959;
}

.project-time {
  font-size: 12px;
  color: #8c8c8c;
}

/* 表格视图 */
.table-view {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  margin-bottom: 24px;
}

.table-project-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.project-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

:deep(.el-empty) {
  padding: 60px 0;
}

/* 项目类型选择样式 */
.project-type-group :deep(.el-radio-group) {
  display: flex !important;
  flex-direction: column !important;
  width: 100%;
}

/* 直接针对 el-radio label 元素应用样式 */
.project-type-group :deep(.el-radio) {
  display: flex !important;
  width: 100%;
  margin-bottom: 20px !important;
  margin-right: 0 !important;
  border: 2px solid #dcdfe6 !important;
  border-radius: 8px !important;
  padding: 20px !important;
  transition: all 0.3s;
  background: #fff !important;
  cursor: pointer;
  align-items: flex-start;
  height: auto !important;
  line-height: normal !important;
}

/* 隐藏默认的 radio 圆点 */
.project-type-group :deep(.el-radio__input) {
  display: none !important;
}

/* 确保内部圆点也被隐藏 */
.project-type-group :deep(.el-radio__inner) {
  display: none !important;
}

.project-type-group :deep(.el-radio__original) {
  display: none !important;
}

.project-type-group :deep(.el-radio__label) {
  width: 100% !important;
  padding: 0 !important;
  margin-left: 0 !important;
}

/* 悬停效果 */
.project-type-group :deep(.el-radio:hover) {
  border-color: #409eff !important;
  background-color: #f0f7ff !important;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2) !important;
  transform: translateY(-2px);
}

/* 选中状态 */
.project-type-group :deep(.el-radio.is-checked) {
  border-color: #409eff !important;
  background-color: #ecf5ff !important;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.3) !important;
}

.project-type-option {
  width: 100%;
}

.type-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.type-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.type-desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.6;
}

.type-features {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
