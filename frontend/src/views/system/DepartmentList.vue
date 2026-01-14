<template>
  <div class="department-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">部门管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建部门
        </el-button>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索部门名称"
          clearable
          style="width: 200px; margin-right: 16px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 部门表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="list" stripe v-loading="loading" border row-key="id">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="部门名称" width="200" />
        <el-table-column label="父部门" width="150">
          <template #default="{ row }">
            {{ getParentDepartmentName(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="层级" width="80" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="handleAddChild(row)">
              添加子部门
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 编辑部门对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="父部门" prop="parentId">
          <el-select
            v-model="editForm.parentId"
            placeholder="请选择父部门"
            clearable
            :disabled="isChildDepartment"
            style="width: 100%"
            @change="handleParentChange"
          >
            <el-option label="无（顶级部门）" :value="0" />
            <el-option
              v-for="dept in availableParentDepartments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
          <div v-if="isChildDepartment" class="form-tip">
            当前为添加子部门，父部门已固定
          </div>
        </el-form-item>
        <el-form-item label="层级" v-if="!isCreateMode">
          <el-input-number v-model="editForm.level" :min="0" :max="10" disabled />
          <div class="form-tip">层级由父部门自动计算</div>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="editForm.sort" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'

// 部门信息类型定义
export interface DepartmentInfo {
  id: number
  name: string
  parentId: number
  level: number
  sort: number
  remark?: string
  createTime?: string
  updateTime?: string
}

// 部门查询参数
export interface DepartmentQuery {
  name?: string
  parentId?: number
  pageNum?: number
  pageSize?: number
}

// API 调用
import request from '@/api/request'

const getDepartmentList = (params: DepartmentQuery) => {
  return request.get('/departments', { params })
}

const getAllDepartments = () => {
  return request.get('/departments', { params: { pageNum: 1, pageSize: 1000 } })
}

const createDepartment = (data: {
  name: string
  parentId: number
  level: number
  sort: number
  remark?: string
}) => {
  return request.post('/departments', data)
}

const updateDepartment = (data: {
  id: number
  name?: string
  parentId?: number
  level?: number
  sort?: number
  remark?: string
}) => {
  return request.put('/departments', data)
}

const deleteDepartment = (id: number) => {
  return request.delete(`/departments/${id}`)
}

// 状态定义
const list = ref<DepartmentInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索表单
const searchForm = reactive<DepartmentQuery>({
  name: '',
  pageNum: 1,
  pageSize: 10
})

// 所有部门列表（用于父部门选择）
const allDepartures = ref<DepartmentInfo[]>([])

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const isEdit = ref(false)
const isChildDepartment = ref(false) // 是否添加子部门
const editForm = reactive({
  id: 0,
  name: '',
  parentId: 0,
  level: 0,
  sort: 0,
  remark: ''
})

const submitLoading = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  if (isChildDepartment.value) {
    return '添加子部门'
  }
  return isEdit.value ? '编辑部门' : '新建部门'
})

// 是否为创建模式（新建或添加子部门）
const isCreateMode = computed(() => {
  return !isEdit.value || isChildDepartment.value
})

// 可选择的父部门列表（排除自己和自己的子部门）
const availableParentDepartments = computed(() => {
  if (isEdit.value && !isChildDepartment.value) {
    // 编辑模式：排除自己和自己的子孙部门
    const excludeIds = new Set([editForm.id])
    const findChildren = (parentId: number) => {
      allDepartures.value.filter(d => d.parentId === parentId).forEach(child => {
        excludeIds.add(child.id)
        findChildren(child.id)
      })
    }
    findChildren(editForm.id)
    return allDepartures.value.filter(d => !excludeIds.has(d.id))
  } else if (isChildDepartment.value) {
    // 添加子部门模式：只显示当前选中的父部门
    return allDepartures.value.filter(d => d.id === editForm.parentId)
  }
  return allDepartures.value
})

// 表单验证规则
const editFormRules: FormRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 50, message: '部门名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' },
    { type: 'number', min: 0, message: '排序必须大于等于 0', trigger: 'blur' }
  ]
}

// 获取父部门名称
const getParentDepartmentName = (parentId: number) => {
  if (parentId === 0) return '无（顶级部门）'
  const parent = allDepartures.value.find(d => d.id === parentId)
  return parent ? parent.name : `ID: ${parentId}`
}

// 计算部门层级
const calculateLevel = (parentId: number): number => {
  if (parentId === 0) return 0
  const parent = allDepartures.value.find(d => d.id === parentId)
  return parent ? parent.level + 1 : 0
}

// 父部门改变时自动计算层级
const handleParentChange = (parentId: number) => {
  editForm.level = calculateLevel(parentId)
}

// 生命周期
onMounted(async () => {
  fetchData()
  // 加载所有部门用于父部门选择
  try {
    const res = await getAllDepartments()
    allDepartures.value = res.list || []
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentList(searchForm)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.name = ''
  searchForm.pageNum = 1
  fetchData()
}

// 新建部门
const handleCreate = () => {
  isEdit.value = false
  isChildDepartment.value = false
  editForm.id = 0
  editForm.name = ''
  editForm.parentId = 0
  editForm.level = 0
  editForm.sort = 0
  editForm.remark = ''
  editDialogVisible.value = true
}

// 添加子部门
const handleAddChild = (row: DepartmentInfo) => {
  isEdit.value = false
  isChildDepartment.value = true
  editForm.id = 0
  editForm.name = ''
  editForm.parentId = row.id
  editForm.level = row.level + 1 // 自动计算层级
  editForm.sort = 0
  editForm.remark = ''
  editDialogVisible.value = true
}

// 编辑部门
const handleEdit = (row: DepartmentInfo) => {
  isEdit.value = true
  isChildDepartment.value = false
  editForm.id = row.id
  editForm.name = row.name
  editForm.parentId = row.parentId
  editForm.level = row.level
  editForm.sort = row.sort
  editForm.remark = row.remark || ''
  editDialogVisible.value = true
}

// 提交编辑/新建
const handleSubmitEdit = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()
    submitLoading.value = true

    // 创建或更新时都重新计算层级
    const calculatedLevel = calculateLevel(editForm.parentId)

    if (isEdit.value && !isChildDepartment.value) {
      // 编辑部门
      await updateDepartment({
        id: editForm.id,
        name: editForm.name,
        parentId: editForm.parentId,
        level: calculatedLevel,
        sort: editForm.sort,
        remark: editForm.remark || undefined
      })
      ElMessage.success('部门更新成功')
    } else {
      // 新建部门或添加子部门
      await createDepartment({
        name: editForm.name,
        parentId: editForm.parentId,
        level: calculatedLevel,
        sort: editForm.sort,
        remark: editForm.remark || undefined
      })
      ElMessage.success('部门创建成功')
    }

    editDialogVisible.value = false
    fetchData()
    // 重新加载部门列表
    const res = await getAllDepartments()
    allDepartures.value = res.list || []
  } catch (error: any) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 删除部门
const handleDelete = async (row: DepartmentInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除部门"${row.name}"吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    fetchData()
    // 重新加载部门列表
    const res = await getAllDepartments()
    allDepartures.value = res.list || []
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 关闭编辑对话框
const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
  editForm.id = 0
  editForm.name = ''
  editForm.parentId = 0
  editForm.level = 0
  editForm.sort = 0
  editForm.remark = ''
  isEdit.value = false
  isChildDepartment.value = false
}

// 工具函数
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
.department-list {
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

.table-card {
  margin-bottom: 24px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
