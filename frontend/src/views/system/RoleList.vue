<template>
  <div class="role-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">角色管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建角色</el-button>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索角色名称"
          :prefix-icon="Search"
          clearable
          style="width: 240px; margin-right: 16px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="searchForm.roleLevel"
          placeholder="角色级别"
          clearable
          style="width: 140px; margin-right: 16px"
          @change="handleSearch"
        >
          <el-option label="系统级" :value="1" />
          <el-option label="项目级" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 角色表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="list" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="code" label="角色编码" width="180" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="roleLevel" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.roleLevel === 1 ? 'danger' : 'primary'" size="small">
              {{ row.roleLevel === 1 ? '系统级' : '项目级' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAssignPermissions(row)">
              分配权限
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewUsers(row)">
              查看用户
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

    <!-- 新建/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入角色编码，如：PROJECT_MANAGER"
            :disabled="isEdit"
            clearable
          />
          <div class="form-tip">只能包含大写字母和下划线</div>
        </el-form-item>
        <el-form-item label="角色级别" prop="roleLevel">
          <el-radio-group v-model="formData.roleLevel">
            <el-radio :label="1">系统级</el-radio>
            <el-radio :label="2">项目级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="900px"
      @close="handlePermissionDialogClose"
    >
      <div class="permission-header">
        <span>角色: <strong>{{ currentRole?.name }}</strong></span>
        <div class="permission-actions">
          <el-button size="small" @click="handleSelectAll">全选</el-button>
          <el-button size="small" @click="handleClearAll">清空</el-button>
        </div>
      </div>

      <el-table
        ref="permissionTableRef"
        :data="permissionTableData"
        stripe
        border
        max-height="500"
        @selection-change="handlePermissionSelectionChange"
        row-key="id"
      >
        <el-table-column type="selection" width="55" :reserve-selection="true" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="name" label="权限名称" width="150" />
        <el-table-column prop="code" label="权限编码" width="200" />
        <el-table-column prop="description" label="描述" min-width="200" />
      </el-table>

      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissionsSubmit" :loading="submitLoading">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看用户对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      title="拥有该角色的用户"
      width="600px"
    >
      <div class="user-list">
        <el-table :data="roleUsers" stripe border max-height="400">
          <el-table-column prop="id" label="用户ID" width="80" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column prop="nickname" label="姓名" width="150" />
        </el-table>
        <el-empty v-if="roleUsers.length === 0" description="暂无用户" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  assignPermissions,
  getRolePermissions,
  getRoleUsers,
  type RoleInfo,
  type RoleCreateReq,
  type RoleUpdateReq
} from '@/api/role'
import { getAllPermissions, type PermissionInfo } from '@/api/permission'

// 状态定义
const list = ref<RoleInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  roleLevel: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建角色')
const isEdit = ref(false)
const submitLoading = ref(false)

// 表单
const formRef = ref<FormInstance>()
const formData = reactive<RoleCreateReq & { id?: number }>({
  name: '',
  code: '',
  description: '',
  roleLevel: 1
})

// 表单验证规则
const formRules: FormRules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ],
  roleLevel: [{ required: true, message: '请选择角色级别', trigger: 'change' }]
}

// 权限对话框
const permissionDialogVisible = ref(false)
const permissionTableRef = ref()
const currentRole = ref<RoleInfo>()
const selectedPermissions = ref<PermissionInfo[]>([])
const allPermissions = ref<PermissionInfo[]>([])
const permissionTableData = computed(() => {
  return allPermissions.value.map(p => ({
    ...p,
    module: getModuleFromCode(p.code)
  }))
})

// 用户对话框
const userDialogVisible = ref(false)
const roleUsers = ref<{ id: number; username: string; nickname: string }[]>([])

// 生命周期
onMounted(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getRoleList(searchForm)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
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
  searchForm.code = ''
  searchForm.roleLevel = undefined
  searchForm.pageNum = 1
  fetchData()
}

// 新建角色
const handleCreate = () => {
  dialogTitle.value = '新建角色'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row: RoleInfo) => {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description,
    roleLevel: row.roleLevel
  })
  dialogVisible.value = true
}

// 删除角色
const handleDelete = async (row: RoleInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除角色"${row.name}"吗？`, '提示', {
      type: 'warning'
    })

    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除角色失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await updateRole(formData as RoleUpdateReq)
      ElMessage.success('更新成功')
    } else {
      await createRole(formData as RoleCreateReq)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.code = ''
  formData.description = ''
  formData.roleLevel = 1
  formRef.value?.clearValidate()
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 分配权限
const handleAssignPermissions = async (row: RoleInfo) => {
  currentRole.value = row

  // 加载所有权限
  try {
    allPermissions.value = await getAllPermissions()

    // 加载角色已有权限
    const permissionIds = await getRolePermissions(row.id)
    selectedPermissions.value = allPermissions.value.filter(p =>
      permissionIds.includes(p.id)
    )

    permissionDialogVisible.value = true

    // 等待DOM更新后设置选中状态
    setTimeout(() => {
      if (permissionTableRef.value) {
        permissionTableRef.value.clearSelection()
        selectedPermissions.value.forEach(row => {
          permissionTableRef.value!.toggleRowSelection(row, true)
        })
      }
    }, 100)
  } catch (error) {
    console.error('加载权限失败:', error)
    ElMessage.error('加载权限失败')
  }
}

// 权限选择变化
const handlePermissionSelectionChange = (selection: PermissionInfo[]) => {
  selectedPermissions.value = selection
}

// 全选
const handleSelectAll = () => {
  permissionTableRef.value?.toggleAllSelection()
}

// 清空选择
const handleClearAll = () => {
  permissionTableRef.value?.clearSelection()
}

// 提交权限分配
const handleAssignPermissionsSubmit = async () => {
  if (!currentRole.value) return

  try {
    submitLoading.value = true
    const permissionIds = selectedPermissions.value.map(p => p.id)
    await assignPermissions(currentRole.value.id, permissionIds)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    console.error('权限分配失败:', error)
    ElMessage.error(error.message || '权限分配失败')
  } finally {
    submitLoading.value = false
  }
}

// 关闭权限对话框
const handlePermissionDialogClose = () => {
  currentRole.value = undefined
  selectedPermissions.value = []
}

// 查看用户
const handleViewUsers = async (row: RoleInfo) => {
  try {
    const userIds = await getRoleUsers(row.id)

    // TODO: 根据userIds获取用户详情
    // 临时显示ID列表
    roleUsers.value = userIds.map(id => ({
      id,
      username: `用户${id}`,
      nickname: `用户${id}`
    }))

    userDialogVisible.value = true
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

// 工具函数
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getModuleFromCode = (code: string): string => {
  if (code.startsWith('PROJECT_')) return '项目管理'
  if (code.startsWith('TASK_')) return '任务管理'
  if (code.startsWith('WORKHOUR_')) return '工时管理'
  if (code.startsWith('USER_')) return '用户管理'
  if (code.startsWith('ROLE_')) return '角色管理'
  return '其他'
}
</script>

<style scoped>
.role-list {
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

.permission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 4px;
}

.permission-actions {
  display: flex;
  gap: 8px;
}

.user-list {
  margin-top: 16px;
}
</style>
