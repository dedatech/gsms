<template>
  <div class="page-root">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">权限管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">新建权限</el-button>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchForm.name"
          placeholder="搜索权限名称"
          :prefix-icon="Search"
          clearable
          style="width: 240px; margin-right: 16px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-input
          v-model="searchForm.code"
          placeholder="搜索权限编码"
          clearable
          style="width: 200px; margin-right: 16px"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 权限表格 -->
    <el-card class="table-card" shadow="never">
      <!-- 权限类型筛选 -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="permission-tabs">
        <el-tab-pane label="全部权限" name="0" />
        <el-tab-pane label="功能权限" name="1" />
        <el-tab-pane label="菜单权限" name="2" />
        <el-tab-pane label="数据权限" name="3" />
      </el-tabs>

      <el-table :data="list" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="module" label="模块" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ getModuleFromCode(row.code) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permissionType" label="权限类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getPermissionTypeTag(row.permissionType)" size="small">
              {{ getPermissionTypeName(row.permissionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="权限名称" min-width="120" />
        <el-table-column prop="code" label="权限编码" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button link type="primary" size="small" @click="handleViewRoles(row)">
              查看角色
            </el-button>
            <el-button
              v-if="row.permissionType === 2"
              link
              type="primary"
              size="small"
              @click="handleAssignMenus(row)">
              设置菜单权限
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

    <!-- 新建/编辑权限对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入权限名称" clearable />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="请输入权限编码，如：PROJECT_VIEW_ALL"
            :disabled="isEdit"
            clearable
          />
          <div class="form-tip">只能包含大写字母和下划线</div>
        </el-form-item>
        <el-form-item label="权限描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入权限描述"
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

    <!-- 查看角色对话框 -->
    <el-dialog
      v-model="roleDialogVisible"
      title="拥有该权限的角色"
      width="600px"
    >
      <div class="role-list">
        <el-table :data="permissionRoles" stripe border max-height="400">
          <el-table-column prop="id" label="角色ID" width="80" />
          <el-table-column prop="name" label="角色名称" width="150" />
          <el-table-column prop="code" label="角色编码" width="180" />
          <el-table-column prop="description" label="描述" min-width="150" />
        </el-table>
        <el-empty v-if="permissionRoles.length === 0" description="暂无角色" />
      </div>
    </el-dialog>

    <!-- 设置菜单权限对话框 -->
    <el-dialog
      v-model="menuDialogVisible"
      title="设置菜单权限"
      width="600px"
    >
      <el-form label-width="80px">
        <el-form-item label="菜单">
          <el-select
            v-model="selectedMenuIds"
            multiple
            placeholder="选择菜单"
            style="width: 100%"
          >
            <el-option
              v-for="menu in allMenus"
              :key="menu.id"
              :label="menu.name"
              :value="menu.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignMenusSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getPermissionList,
  createPermission,
  updatePermission,
  deletePermission,
  assignPermissionMenus,
  getPermissionMenus,
  type PermissionInfo,
  type PermissionCreateReq,
  type PermissionUpdateReq
} from '@/api/permission'
import { getMenuTree, type MenuInfo } from '@/api/menu'

// 状态定义
const list = ref<PermissionInfo[]>([])
const total = ref(0)
const loading = ref(false)
const activeTab = ref('0') // '0':全部 '1':功能 '2':菜单 '3':数据

// 搜索表单
const searchForm = reactive({
  name: '',
  code: '',
  permissionType: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新建权限')
const isEdit = ref(false)
const submitLoading = ref(false)

// 表单
const formRef = ref<FormInstance>()
const formData = reactive<PermissionCreateReq & { id?: number }>({
  name: '',
  code: '',
  description: ''
})

// 表单验证规则
const formRules: FormRules = {
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 角色对话框
const roleDialogVisible = ref(false)
const permissionRoles = ref<{ id: number; name: string; code: string; description?: string }[]>([])

// 菜单对话框
const menuDialogVisible = ref(false)
const currentPermissionId = ref<number>(0)
const allMenus = ref<MenuInfo[]>([])
const selectedMenuIds = ref<number[]>([])

// 生命周期
onMounted(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPermissionList(searchForm)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败')
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
  searchForm.permissionType = undefined
  searchForm.pageNum = 1
  activeTab.value = 0
  fetchData()
}

// 新建权限
const handleCreate = () => {
  dialogTitle.value = '新建权限'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑权限
const handleEdit = (row: PermissionInfo) => {
  dialogTitle.value = '编辑权限'
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description
  })
  dialogVisible.value = true
}

// 删除权限
const handleDelete = async (row: PermissionInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除权限"${row.name}"吗？`, '提示', {
      type: 'warning'
    })

    await deletePermission(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除权限失败:', error)
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
      await updatePermission(formData as PermissionUpdateReq)
      ElMessage.success('更新成功')
    } else {
      await createPermission(formData as PermissionCreateReq)
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
  formRef.value?.clearValidate()
}

// 关闭对话框
const handleDialogClose = () => {
  resetForm()
}

// 查看角色
const handleViewRoles = async (row: PermissionInfo) => {
  // TODO: 根据permission.roleIds获取角色详情
  // 临时显示空列表
  permissionRoles.value = []
  roleDialogVisible.value = true
}

// 设置菜单权限
const handleAssignMenus = async (row: PermissionInfo) => {
  currentPermissionId.value = row.id
  try {
    // 获取所有菜单（扁平化处理）
    const menuTree = await getMenuTree()
    allMenus.value = flattenMenuTree(menuTree)

    // 获取当前权限的菜单
    const menuIds = await getPermissionMenus(row.id)
    selectedMenuIds.value = menuIds

    menuDialogVisible.value = true
  } catch (error) {
    console.error('获取菜单失败:', error)
    ElMessage.error('获取菜单失败')
  }
}

const handleAssignMenusSubmit = async () => {
  try {
    await assignPermissionMenus(currentPermissionId.value, selectedMenuIds.value)
    ElMessage.success('菜单权限设置成功')
    menuDialogVisible.value = false
  } catch (error) {
    console.error('菜单权限设置失败:', error)
    ElMessage.error('菜单权限设置失败')
  }
}

// 扁平化菜单树
const flattenMenuTree = (menus: MenuInfo[]): MenuInfo[] => {
  const result: MenuInfo[] = []
  const flatten = (list: MenuInfo[]) => {
    for (const menu of list) {
      result.push(menu)
      if (menu.children && menu.children.length > 0) {
        flatten(menu.children)
      }
    }
  }
  flatten(menus)
  return result
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
  if (code.startsWith('PERMISSION_')) return '权限管理'
  return '其他'
}

// 权限类型名称
const getPermissionTypeName = (type: number): string => {
  const map: Record<number, string> = {
    1: '功能权限',
    2: '菜单权限',
    3: '数据权限'
  }
  return map[type] || '未知'
}

// 权限类型标签样式
const getPermissionTypeTag = (type: number): string => {
  const map: Record<number, string> = {
    1: 'primary',   // 功能权限 - 蓝色
    2: 'success',   // 菜单权限 - 绿色
    3: 'warning'    // 数据权限 - 橙色
  }
  return map[type] || 'info'
}

// 标签切换处理
const handleTabChange = (value: string) => {
  const typeNum = parseInt(value)
  if (typeNum === 0) {
    searchForm.permissionType = undefined
  } else {
    searchForm.permissionType = typeNum
  }
  searchForm.pageNum = 1
  fetchData()
}
</script>

<style scoped>
/* ========== 权限管理页面样式 ========== */

.permission-tabs {
  margin-bottom: 16px;
}

.page-root {
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

.table-card {
  margin-bottom: 24px;
}

.role-list {
  margin-top: 16px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
