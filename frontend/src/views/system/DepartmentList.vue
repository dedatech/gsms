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

    <!-- 左树右表布局 -->
    <div class="content-container">
      <!-- 左侧部门树 -->
      <el-card class="tree-card" shadow="never">
        <template #header>
          <div class="tree-header">
            <span>组织架构</span>
            <el-button text type="primary" size="small" @click="refreshTree">
              刷新
            </el-button>
          </div>
        </template>
        <el-tree
          ref="treeRef"
          :data="departmentTree"
          :props="treeProps"
          :highlight-current="true"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <span class="node-label">{{ node.label }}</span>
              <span class="node-count">({{ data.children?.length || 0 }})</span>
            </div>
          </template>
        </el-tree>
      </el-card>

      <!-- 右侧部门表格 -->
      <el-card class="table-card" shadow="never">
        <template #header>
          <div class="table-header">
            <span>{{ currentTableTitle }}</span>
          </div>
        </template>
        <el-table :data="list" stripe v-loading="loading" border row-key="id">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="部门名称" width="200" />
          <el-table-column label="父部门" width="150">
            <template #default="{ row }">
              {{ getParentDepartmentName(row.parentId) }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" width="200" show-overflow-tooltip />
          <el-table-column label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row, $index }">
              <el-button link type="success" size="small" @click="handleAddChild(row)">
                添加子部门
              </el-button>
              <el-button link type="primary" size="small" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button
                link
                type="warning"
                size="small"
                @click="handleMoveUp(row, $index)"
                :disabled="$index === 0"
              >
                上移
              </el-button>
              <el-button
                link
                type="warning"
                size="small"
                @click="handleMoveDown(row, $index)"
                :disabled="$index === list.length - 1"
              >
                下移
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
    </div>

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
          <el-tree-select
            v-model="editForm.parentId"
            :data="availableParentTree"
            :props="treeProps"
            value-key="id"
            :render-after-expand="false"
            check-strictly
            default-expand-all
            placeholder="请选择父部门（不选择则为顶级部门）"
            clearable
            :disabled="isChildDepartment"
            style="width: 100%"
            @change="handleParentChange"
          />
          <div v-if="isChildDepartment" class="form-tip">
            当前为添加子部门，父部门已固定
          </div>
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
  children?: DepartmentInfo[]
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

// 树形数据
const treeRef = ref()
const departmentTree = ref<DepartmentInfo[]>([])
const selectedDepartmentId = ref<number | undefined>(undefined)

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 当前表格标题
const currentTableTitle = computed(() => {
  if (selectedDepartmentId.value === undefined) {
    return '所有部门'
  }
  const dept = allDepartures.value.find(d => d.id === selectedDepartmentId.value)
  return dept ? `${dept.name} - 子部门` : '子部门'
})

// 可选择的父部门树（排除自己和子孙部门）
const availableParentTree = computed(() => {
  // 深拷贝树数据，避免影响原树
  const deepClone = (nodes: DepartmentInfo[]): DepartmentInfo[] => {
    return nodes.map(node => ({
      ...node,
      children: node.children ? deepClone(node.children) : undefined
    }))
  }

  if (isEdit.value && !isChildDepartment.value) {
    // 编辑模式：排除自己和子孙部门
    const excludeIds = new Set<number>([editForm.id])

    // 递归查找所有子孙部门ID
    const findChildrenIds = (dept: DepartmentInfo) => {
      if (dept.children) {
        dept.children.forEach(child => {
          excludeIds.add(child.id)
          findChildrenIds(child)
        })
      }
    }

    // 找到当前部门节点并排除
    const findAndExcludeChildren = (nodes: DepartmentInfo[]): DepartmentInfo[] => {
      const result: DepartmentInfo[] = []

      for (const node of nodes) {
        // 如果是当前部门，跳过并记录所有子孙
        if (node.id === editForm.id) {
          findChildrenIds(node)
          continue
        }

        // 如果在排除列表中，跳过
        if (excludeIds.has(node.id)) {
          continue
        }

        // 递归处理子节点
        const newNode = { ...node }
        if (node.children) {
          newNode.children = findAndExcludeChildren(node.children)
        }

        result.push(newNode)
      }

      return result
    }

    return findAndExcludeChildren(deepClone(departmentTree.value))
  } else if (isChildDepartment.value) {
    // 添加子部门模式：显示完整树
    return deepClone(departmentTree.value)
  }

  // 新建模式：显示完整树
  return deepClone(departmentTree.value)
})

// 搜索表单
const searchForm = reactive<DepartmentQuery>({
  name: '',
  parentId: undefined,
  pageNum: 1,
  pageSize: 10
})

// 所有部门列表（用于父部门选择）
const allDepartures = ref<DepartmentInfo[]>([])

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
const isChildDepartment = ref(false) // 是否添加子部门
const editForm = reactive({
  id: 0,
  name: '',
  parentId: undefined as number | undefined, // 改为 undefined，el-tree-select 兼容性更好
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
const calculateLevel = (parentId: number | undefined): number => {
  if (!parentId || parentId === 0) return 0
  const parent = allDepartures.value.find(d => d.id === parentId)
  return parent ? parent.level + 1 : 0
}

// 父部门改变时自动计算层级
const handleParentChange = (parentId: number | undefined) => {
  editForm.level = calculateLevel(parentId)
}

// 生命周期
onMounted(async () => {
  // 加载树形数据
  await loadDepartmentTree()

  // 默认选中第一个顶级部门
  if (departmentTree.value.length > 0) {
    const firstTopDept = departmentTree.value[0]
    treeRef.value?.setCurrentKey(firstTopDept.id)
    selectedDepartmentId.value = firstTopDept.id
    searchForm.parentId = firstTopDept.id
  }

  // 加载表格数据
  fetchData()
})

// 加载部门树
const loadDepartmentTree = async () => {
  try {
    const res = await getAllDepartments()
    allDepartures.value = res.list || []
    departmentTree.value = buildDepartmentTree(allDepartures.value)
  } catch (error) {
    console.error('加载部门树失败:', error)
    ElMessage.error('加载部门树失败')
  }
}

// 刷新树
const refreshTree = async () => {
  await loadDepartmentTree()
  ElMessage.success('刷新成功')
}

// 树节点点击
const handleNodeClick = (data: DepartmentInfo) => {
  selectedDepartmentId.value = data.id
  searchForm.parentId = data.id
  searchForm.pageNum = 1
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDepartmentList(searchForm)
    // 按sort字段排序
    const sortedList = (res.list || []).sort((a, b) => a.sort - b.sort)
    list.value = sortedList
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
  searchForm.parentId = undefined
  selectedDepartmentId.value = undefined
  searchForm.pageNum = 1
  // 清除树的选中状态
  treeRef.value?.setCurrentKey(null)
  fetchData()
}

// 新建部门
const handleCreate = () => {
  isEdit.value = false
  isChildDepartment.value = false
  editForm.id = 0
  editForm.name = ''
  editForm.parentId = undefined  // undefined 表示顶级部门
  editForm.level = 0

  // 计算同级部门的最大sort值
  const siblings = allDepartures.value.filter(d => d.parentId === 0)
  const maxSort = siblings.length > 0 ? Math.max(...siblings.map(d => d.sort)) : -1
  editForm.sort = maxSort + 1

  editForm.remark = ''
  editDialogVisible.value = true
}

// 添加子部门
const handleAddChild = (row: DepartmentInfo) => {
  isEdit.value = false
  isChildDepartment.value = true
  editForm.id = 0
  editForm.name = ''
  editForm.parentId = row.id  // 设置父部门ID
  editForm.level = row.level + 1 // 自动计算层级

  // 计算同级部门的最大sort值
  const siblings = allDepartures.value.filter(d => d.parentId === row.id)
  const maxSort = siblings.length > 0 ? Math.max(...siblings.map(d => d.sort)) : -1
  editForm.sort = maxSort + 1

  editForm.remark = ''
  editDialogVisible.value = true
}

// 编辑部门
const handleEdit = (row: DepartmentInfo) => {
  isEdit.value = true
  isChildDepartment.value = false
  editForm.id = row.id
  editForm.name = row.name
  editForm.parentId = row.parentId === 0 ? undefined : row.parentId  // 0 转换为 undefined
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

    // 将 undefined 转换为 0（后端需要）
    const parentId = editForm.parentId === undefined ? 0 : editForm.parentId

    // 创建或更新时都重新计算层级
    const calculatedLevel = calculateLevel(parentId)

    if (isEdit.value && !isChildDepartment.value) {
      // 编辑部门
      await updateDepartment({
        id: editForm.id,
        name: editForm.name,
        parentId: parentId,
        level: calculatedLevel,
        sort: editForm.sort,
        remark: editForm.remark || undefined
      })
      ElMessage.success('部门更新成功')
    } else {
      // 新建部门或添加子部门
      await createDepartment({
        name: editForm.name,
        parentId: parentId,
        level: calculatedLevel,
        sort: editForm.sort,
        remark: editForm.remark || undefined
      })
      ElMessage.success('部门创建成功')
    }

    editDialogVisible.value = false
    fetchData()
    // 重新加载部门树
    await loadDepartmentTree()
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
    // 重新加载部门树
    await loadDepartmentTree()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 上移部门
const handleMoveUp = async (row: DepartmentInfo, index: number) => {
  if (index === 0) return

  try {
    // 获取上一个部门
    const prevRow = list.value[index - 1]

    // 交换排序值
    const currentSort = row.sort
    const prevSort = prevRow.sort

    // 更新当前部门排序
    await updateDepartment({
      id: row.id,
      sort: prevSort
    })

    // 更新上一个部门排序
    await updateDepartment({
      id: prevRow.id,
      sort: currentSort
    })

    ElMessage.success('上移成功')
    fetchData()
    // 重新加载部门树
    await loadDepartmentTree()
  } catch (error: any) {
    console.error('上移失败:', error)
    ElMessage.error(error.message || '上移失败')
  }
}

// 下移部门
const handleMoveDown = async (row: DepartmentInfo, index: number) => {
  if (index === list.value.length - 1) return

  try {
    // 获取下一个部门
    const nextRow = list.value[index + 1]

    // 交换排序值
    const currentSort = row.sort
    const nextSort = nextRow.sort

    // 更新当前部门排序
    await updateDepartment({
      id: row.id,
      sort: nextSort
    })

    // 更新下一个部门排序
    await updateDepartment({
      id: nextRow.id,
      sort: currentSort
    })

    ElMessage.success('下移成功')
    fetchData()
    // 重新加载部门树
    await loadDepartmentTree()
  } catch (error: any) {
    console.error('下移失败:', error)
    ElMessage.error(error.message || '下移失败')
  }
}

// 关闭编辑对话框
const handleEditDialogClose = () => {
  editFormRef.value?.resetFields()
  editForm.id = 0
  editForm.name = ''
  editForm.parentId = undefined
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

/* 左树右表布局 */
.content-container {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

/* 左侧树形卡片 */
.tree-card {
  width: 280px;
  position: sticky;
  top: 0;
  flex-shrink: 0;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.tree-header span {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  padding-right: 8px;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-count {
  font-size: 12px;
  color: #909399;
}

/* 右侧表格卡片 */
.table-card {
  flex: 1;
  margin-bottom: 0;
}

.table-header {
  font-size: 14px;
  font-weight: 500;
  color: #333;
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

/* 树形组件样式优化 */
:deep(.el-tree-node__content) {
  height: 36px;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: var(--theme-primary-light);
  color: var(--theme-primary);
}

:deep(.el-tree-node:focus > .el-tree-node__content) {
  background-color: var(--theme-primary-light);
  color: var(--theme-primary);
}

/* 树形选择器下拉框样式优化 */
:deep(.el-select-dropdown) {
  z-index: 9999 !important;
}

:deep(.el-tree-select__popper) {
  z-index: 9999 !important;
}
</style>
