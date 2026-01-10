<template>
  <div class="iteration-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">迭代管理</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建迭代</el-button>
      </div>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="项目">
          <el-select v-model="queryForm.projectId" placeholder="请选择项目" clearable filterable style="width: 200px" @change="handleQuery">
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 150px" @change="handleQuery">
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 迭代列表 -->
    <el-card class="table-card">
      <el-table :data="iterationList" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="迭代名称" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
        <el-table-column label="所属项目" width="200">
          <template #default="{ row }">
            {{ getProjectName(row.projectId) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="计划时间" width="200">
          <template #default="{ row }">
            <div class="date-info">
              <div>{{ row.planStartDate || '-' }}</div>
              <div>{{ row.planEndDate || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="实际时间" width="200">
          <template #default="{ row }">
            <div class="date-info">
              <div>{{ row.actualStartDate || '-' }}</div>
              <div>{{ row.actualEndDate || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createUserName" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="项目" prop="projectId">
          <el-select v-model="formData.projectId" placeholder="请选择项目" filterable style="width: 100%">
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="迭代名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入迭代名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入迭代描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="未开始" value="NOT_STARTED" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划开始时间" prop="planStartDate">
          <el-date-picker
            v-model="formData.planStartDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="计划结束时间" prop="planEndDate">
          <el-date-picker
            v-model="formData.planEndDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Edit, Delete, View } from '@element-plus/icons-vue'
import { getIterationList, createIteration, updateIteration, deleteIteration, type IterationInfo } from '@/api/iteration'
import { getProjectList } from '@/api/project'
import { getIterationStatusInfo } from '@/utils/statusMapping'

const router = useRouter()

// 查询表单
const queryForm = reactive({
  projectId: undefined as number | undefined,
  status: undefined as string | undefined
})

// 项目列表
const projectList = ref<any[]>([])

// 迭代列表
const iterationList = ref<IterationInfo[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  projectId: undefined as number | undefined,
  name: '',
  description: '',
  status: 'NOT_STARTED',
  planStartDate: '',
  planEndDate: ''
})

const formRules: FormRules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  name: [{ required: true, message: '请输入迭代名称', trigger: 'blur' }]
}

// 获取项目名称
const getProjectName = (projectId: number) => {
  const project = projectList.value.find(p => p.id === projectId)
  return project ? project.name : '-'
}

// 获取迭代状态信息
const getStatusType = (status: string) => getIterationStatusInfo(status).type
const getStatusText = (status: string) => getIterationStatusInfo(status).text

// 获取迭代列表
const fetchIterations = async () => {
  loading.value = true
  try {
    const res = await getIterationList({
      projectId: queryForm.projectId,
      status: queryForm.status,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    iterationList.value = res.list || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('获取迭代列表失败:', error)
    ElMessage.error('获取迭代列表失败')
  } finally {
    loading.value = false
  }
}

// 获取项目列表
const fetchProjects = async () => {
  try {
    const res = await getProjectList({
      pageNum: 1,
      pageSize: 1000
    })
    projectList.value = res.list || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 查询
const handleQuery = () => {
  pagination.pageNum = 1
  fetchIterations()
}

// 重置
const handleReset = () => {
  queryForm.projectId = undefined
  queryForm.status = undefined
  pagination.pageNum = 1
  fetchIterations()
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchIterations()
}

// 当前页变化
const handleCurrentChange = (page: number) => {
  pagination.pageNum = page
  fetchIterations()
}

// 创建
const handleCreate = () => {
  dialogTitle.value = '新建迭代'
  formData.id = undefined
  formData.projectId = undefined
  formData.name = ''
  formData.description = ''
  formData.status = 'NOT_STARTED'
  formData.planStartDate = ''
  formData.planEndDate = ''
  dialogVisible.value = true
}

// 查看
const handleView = (row: IterationInfo) => {
  router.push(`/iterations/${row.id}`)
}

// 编辑
const handleEdit = (row: IterationInfo) => {
  dialogTitle.value = '编辑迭代'
  formData.id = row.id
  formData.projectId = row.projectId
  formData.name = row.name
  formData.description = row.description || ''
  formData.status = row.status
  formData.planStartDate = row.planStartDate || ''
  formData.planEndDate = row.planEndDate || ''
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateIteration({
            id: formData.id,
            name: formData.name,
            description: formData.description,
            status: formData.status,
            planStartDate: formData.planStartDate,
            planEndDate: formData.planEndDate
          })
          ElMessage.success('更新成功')
        } else {
          await createIteration({
            projectId: formData.projectId!,
            name: formData.name,
            description: formData.description,
            status: formData.status,
            planStartDate: formData.planStartDate,
            planEndDate: formData.planEndDate
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchIterations()
      } catch (error) {
        console.error('操作失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 删除
const handleDelete = (row: IterationInfo) => {
  ElMessageBox.confirm(`确定要删除迭代 "${row.name}" 吗？删除后将无法恢复！`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteIteration(row.id)
        ElMessage.success('删除成功')
        fetchIterations()
      } catch (error) {
        console.error('删除失败:', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchProjects()
  fetchIterations()
})
</script>

<style scoped>
.iteration-list {
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

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 0;
}

.table-card {
  background: #fff;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.date-info {
  font-size: 12px;
  line-height: 1.6;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f5f5;
  color: #333;
  font-weight: 500;
}
</style>
