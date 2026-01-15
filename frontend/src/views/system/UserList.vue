<template>
  <div class="page-root">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">用户管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建用户</el-button>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchForm.username"
          placeholder="搜索用户名"
          :prefix-icon="Search"
          clearable
          style="width: 200px; margin-right: 16px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model="searchForm.nickname"
          placeholder="搜索姓名"
          clearable
          style="width: 200px; margin-right: 16px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 用户表格 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="list" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="departmentName" label="部门" min-width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'" size="small">
              {{ row.status === 'NORMAL' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAssignRoles(row)">
              分配角色
            </el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="warning" size="small" @click="handleResetPassword(row)">
              重置密码
            </el-button>
            <el-button
              link
              :type="row.status === 'NORMAL' ? 'warning' : 'success'"
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'NORMAL' ? '禁用' : '启用' }}
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

    <!-- 分配角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="分配角色"
      width="500px"
      @close="handleRoleDialogClose"
    >
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.username }} ({{ currentUser?.nickname }})</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="selectedRoleIds"
            multiple
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in allRoles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRolesSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEdit ? '编辑用户' : '新建用户'"
      width="600px"
      @close="handleEditDialogClose"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="editForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="部门" prop="departmentId">
          <el-tree-select
            v-model="editForm.departmentId"
            :data="departmentTree"
            :props="treeProps"
            value-key="id"
            :render-after-expand="false"
            check-strictly
            default-expand-all
            placeholder="请选择部门"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio label="NORMAL">正常</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="500px"
      @close="handleResetPasswordDialogClose"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordFormRules"
        label-width="100px"
      >
        <el-form-item label="用户">
          <span>{{ resetPasswordUser?.username }} ({{ resetPasswordUser?.nickname }})</span>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（6-20位）"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitResetPassword" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getUserList, assignUserRoles, getUserRoles, createUser, updateUser, deleteUser, resetPassword, type UserInfo, type UserQuery } from '@/api/user'
import { getRoleList, type RoleInfo } from '@/api/role'
import { getAllDepartments, type DepartmentInfo } from '@/api/department'

// 状态定义
const list = ref<UserInfo[]>([])
const total = ref(0)
const loading = ref(false)

// 搜索表单
const searchForm = reactive<UserQuery>({
  username: '',
  nickname: '',
  pageNum: 1,
  pageSize: 10
})

// 角色对话框
const roleDialogVisible = ref(false)
const currentUser = ref<UserInfo>()
const selectedRoleIds = ref<number[]>([])
const allRoles = ref<RoleInfo[]>([])
const submitLoading = ref(false)

// 部门列表
const allDepartments = ref<DepartmentInfo[]>([])

// 部门树形数据
const departmentTree = ref<DepartmentInfo[]>([])

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 构建部门树
const buildDepartmentTree = (departments: DepartmentInfo[]): DepartmentInfo[] => {
  const map = new Map<number, DepartmentInfo>()
  const tree: DepartmentInfo[] = []

  // 先创建所有节点的映射
  departments.forEach(dept => {
    map.set(dept.id, { ...dept, children: [] })
  })

  // 构建树形结构
  departments.forEach(dept => {
    const node = map.get(dept.id)!
    if (dept.parentId === 0) {
      tree.push(node)
    } else {
      const parent = map.get(dept.parentId)
      if (parent) {
        if (!parent.children) parent.children = []
        parent.children.push(node)
      }
    }
  })

  return tree
}

// 编辑对话框
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const isEdit = ref(false)
const editForm = reactive({
  id: 0,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  departmentId: undefined as number | undefined,
  status: 'NORMAL' as 'NORMAL' | 'DISABLED'
})

// 重置密码对话框
const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref<FormInstance>()
const resetPasswordUser = ref<UserInfo>()
const resetPasswordForm = reactive({
  userId: 0,
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const editFormRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 重置密码表单验证规则
const resetPasswordFormRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 生命周期
onMounted(async () => {
  fetchData()
  // 加载部门列表并构建树形结构
  try {
    const res = await getAllDepartments()
    allDepartments.value = res.list || []
    departmentTree.value = buildDepartmentTree(allDepartments.value)
  } catch (error) {
    console.error('加载部门列表失败:', error)
  }
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserList(searchForm)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
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
  searchForm.username = ''
  searchForm.nickname = ''
  searchForm.pageNum = 1
  fetchData()
}

// 分配角色
const handleAssignRoles = async (row: UserInfo) => {
  currentUser.value = row

  // 加载所有角色
  try {
    const res = await getRoleList({ pageNum: 1, pageSize: 1000 })
    allRoles.value = res.list || []

    // 加载用户已有角色
    try {
      const roleIds = await getUserRoles(row.id)
      selectedRoleIds.value = roleIds
    } catch (error) {
      console.error('加载用户角色失败:', error)
      selectedRoleIds.value = []
    }

    roleDialogVisible.value = true
  } catch (error) {
    console.error('加载角色失败:', error)
    ElMessage.error('加载角色失败')
  }
}

// 提交角色分配
const handleAssignRolesSubmit = async () => {
  if (!currentUser.value) return

  try {
    submitLoading.value = true
    await assignUserRoles(currentUser.value.id, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    console.error('角色分配失败:', error)
    ElMessage.error(error.message || '角色分配失败')
  } finally {
    submitLoading.value = false
  }
}

// 新建用户
const handleCreate = () => {
  isEdit.value = false
  editForm.id = 0
  editForm.username = ''
  editForm.password = ''
  editForm.nickname = ''
  editForm.email = ''
  editForm.phone = ''
  editForm.departmentId = undefined
  editForm.status = 'NORMAL'
  editDialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: UserInfo) => {
  isEdit.value = true
  editForm.id = row.id
  editForm.username = row.username
  editForm.nickname = row.nickname || ''
  editForm.email = row.email || ''
  editForm.phone = row.phone || ''
  editForm.departmentId = row.departmentId
  editForm.status = row.status
  editDialogVisible.value = true
}

// 切换用户状态（启用/禁用）
const handleToggleStatus = async (row: UserInfo) => {
  const newStatus = row.status === 'NORMAL' ? 'DISABLED' : 'NORMAL'
  const statusText = newStatus === 'NORMAL' ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定要${statusText}用户"${row.username}"吗？`,
      '提示',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )

    await updateUser({
      id: row.id,
      status: newStatus
    })

    ElMessage.success(`${statusText}成功`)
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${statusText}失败:`, error)
      ElMessage.error(error.message || `${statusText}失败`)
    }
  }
}

// 提交编辑/新建
const handleSubmitEdit = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      // 编辑用户
      await updateUser({
        id: editForm.id,
        nickname: editForm.nickname,
        email: editForm.email || undefined,
        phone: editForm.phone || undefined,
        departmentId: editForm.departmentId,
        status: editForm.status
      })
      ElMessage.success('用户更新成功')
    } else {
      // 新建用户
      await createUser({
        username: editForm.username,
        password: editForm.password,
        nickname: editForm.nickname,
        email: editForm.email || undefined,
        phone: editForm.phone || undefined,
        departmentId: editForm.departmentId,
        status: editForm.status
      })
      ElMessage.success('用户创建成功')
    }

    editDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    if (error !== false) { // 表单验证失败时error为false
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 删除用户
const handleDelete = async (row: UserInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 关闭角色对话框
const handleRoleDialogClose = () => {
  currentUser.value = undefined
  selectedRoleIds.value = []
}

// 关闭编辑对话框
const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
  editForm.id = 0
  editForm.username = ''
  editForm.password = ''
  editForm.nickname = ''
  editForm.email = ''
  editForm.phone = ''
  editForm.departmentId = undefined
  editForm.status = 'NORMAL'
}

// 重置密码
const handleResetPassword = (row: UserInfo) => {
  resetPasswordUser.value = row
  resetPasswordForm.userId = row.id
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

// 提交重置密码
const handleSubmitResetPassword = async () => {
  if (!resetPasswordFormRef.value) return

  try {
    await resetPasswordFormRef.value.validate()
    submitLoading.value = true

    await resetPassword({
      userId: resetPasswordForm.userId,
      newPassword: resetPasswordForm.newPassword
    })

    ElMessage.success('密码重置成功')
    resetPasswordDialogVisible.value = false
  } catch (error: any) {
    if (error !== false) {
      console.error('重置密码失败:', error)
      ElMessage.error(error.message || '重置密码失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 关闭重置密码对话框
const handleResetPasswordDialogClose = () => {
  resetPasswordFormRef.value?.resetFields()
  resetPasswordUser.value = undefined
  resetPasswordForm.userId = 0
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
}

// 工具函数
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
/* ========== 用户管理特定样式 ========== */

/* 树形选择器下拉框样式优化 */
:deep(.el-tree-select__popper) {
  z-index: 9999 !important;
}
</style>
