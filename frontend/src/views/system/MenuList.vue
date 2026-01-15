<template>
  <div class="menu-list">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">菜单管理</h2>
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建菜单
        </el-button>
      </div>
      <div class="header-right">
        <el-button :icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <div class="content-wrapper">
      <!-- 左侧：菜单树 -->
      <div class="tree-panel">
        <div class="panel-header">
          <span>菜单树</span>
        </div>
        <div class="tree-content">
          <el-tree
            ref="treeRef"
            :data="menuTreeWithRoot"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            :highlight-current="true"
            :default-expand-all="true"
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node" @dblclick.stop="handleNodeDblClick($event, data)">
                <el-icon v-if="data.icon">
                  <component :is="data.icon" />
                </el-icon>
                <span>{{ node.label }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </div>

      <!-- 右侧：菜单列表 -->
      <div class="table-panel">
        <div class="panel-header">
          <span>菜单列表</span>
          <el-input
            v-model="searchForm.name"
            placeholder="搜索菜单名称"
            clearable
            style="width: 200px"
            :prefix-icon="Search"
            @clear="fetchData"
            @keyup.enter="fetchData"
          />
        </div>
        <div class="table-content">
          <el-table :data="list" stripe border v-loading="loading">
            <el-table-column prop="name" label="菜单名称" min-width="120" />
            <el-table-column label="父菜单" min-width="100">
              <template #default="{ row }">
                {{ getParentMenuName(row.parentId) }}
              </template>
            </el-table-column>
            <el-table-column prop="path" label="路由路径" min-width="180" />
            <el-table-column prop="icon" label="图标" min-width="150" />
            <el-table-column label="类型" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.type === 1" type="info">目录</el-tag>
                <el-tag v-else-if="row.type === 2" type="success">菜单</el-tag>
                <el-tag v-else type="warning">按钮</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success">启用</el-tag>
                <el-tag v-else type="danger">禁用</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row, $index }">
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
                <el-button link type="primary" size="small" @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button link type="primary" size="small" @click="handleAssignPermissions(row)">
                  分配权限
                </el-button>
                <el-button link type="danger" size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination" v-if="total > 0">
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
        </div>
      </div>
    </div>

    <!-- 菜单编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="父菜单" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="menuTreeForSelect"
            :props="{ label: 'name', value: 'id' }"
            placeholder="选择父菜单（不选则为根菜单）"
            check-strictly
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="formData.type">
            <el-radio :label="1">目录</el-radio>
            <el-radio :label="2">菜单</el-radio>
            <el-radio :label="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="formData.type === 2">
          <el-input v-model="formData.path" placeholder="例如：/dashboard" />
        </el-form-item>
        <el-form-item label="组件路径" prop="component" v-if="formData.type === 2">
          <el-input v-model="formData.component" placeholder="例如：Dashboard" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input
            v-model="formData.icon"
            placeholder="点击右侧按钮选择图标"
            readonly
            style="cursor: pointer"
          >
            <template #append>
              <el-button @click="iconSelectorVisible = true">选择</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否可见" prop="visible">
          <el-radio-group v-model="formData.visible">
            <el-radio :label="1">可见</el-radio>
            <el-radio :label="2">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="权限">
          <el-select
            v-model="selectedPermissionIds"
            multiple
            placeholder="选择权限"
            style="width: 100%"
          >
            <el-option
              v-for="permission in allPermissions"
              :key="permission.id"
              :label="permission.name"
              :value="permission.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissionsSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 图标选择器对话框 -->
    <el-dialog
      v-model="iconSelectorVisible"
      title="选择图标"
      width="800px"
    >
      <div class="icon-selector">
        <el-input
          v-model="iconSearchKeyword"
          placeholder="搜索图标名称"
          clearable
          :prefix-icon="Search"
          style="margin-bottom: 16px"
        />
        <div class="icon-grid">
          <div
            v-for="icon in filteredIcons"
            :key="icon"
            class="icon-item"
            :class="{ selected: formData.icon === icon }"
            @click="handleSelectIcon(icon)"
          >
            <el-icon :size="24">
              <component :is="icon" />
            </el-icon>
            <span class="icon-name">{{ icon }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="iconSelectorVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmIcon">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  // 常用图标
  Odometer,
  FolderOpened,
  List,
  Clock,
  DataAnalysis,
  Operation,
  Folder,
  Document,
  User,
  Setting,
  SwitchButton,
  Brush,
  ArrowDown,
  Fold,
  Expand,
  HomeFilled,
  Menu as IconMenu,
  Grid,
  Management,
  TrendCharts,
  PieChart,
  DataLine,
  Calendar,
  Edit,
  Delete,
  View,
  Download,
  Upload,
  Share,
  Star,
  Lock,
  Unlock,
  Bell,
  Message,
  ChatDotRound,
  Phone,
  Location,
  Link,
  Paperclip,
  Tickets,
  Collection,
  Notebook,
  Memo,
  Printer,
  Tickets as Ticket,
  SoldOut,
  Goods,
  Box,
  ShoppingBag,
  ShoppingCart,
  Wallet,
  Coin,
  CreditCard,
  BankCard,
  Money,
  Histogram,
  Tools,
  SetUp,
  Notification,
  Warning,
  InfoFilled,
  SuccessFilled,
  CircleCheckFilled,
  CircleCloseFilled,
  WarningFilled
} from '@element-plus/icons-vue'
import {
  getMenuTree,
  getMenuList,
  createMenu,
  updateMenu,
  deleteMenu,
  assignMenuPermissions,
  getMenuPermissions,
  type MenuInfo,
  type MenuCreateReq,
  type MenuUpdateReq
} from '@/api/menu'
import { getAllPermissions } from '@/api/permission'

// 数据定义
const treeRef = ref<any>()
const menuTree = ref<MenuInfo[]>([])
const menuTreeWithRoot = computed(() => {
  return [{ id: 0, name: '根菜单', children: menuTree.value }]
})
const menuTreeForSelect = computed(() => {
  return [{ id: 0, name: '根菜单', children: menuTree.value }]
})
const list = ref<MenuInfo[]>([])
const total = ref(0)
const loading = ref(false)

const searchForm = reactive({
  name: '',
  type: undefined as number | undefined,
  status: undefined as number | undefined,
  parentId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const formData = reactive<MenuCreateReq & { id?: number }>({
  name: '',
  parentId: 0,
  type: 2,
  path: '',
  component: '',
  icon: '',
  sort: 0,
  status: 1,
  visible: 1,
  permissionIds: []
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请选择父菜单', trigger: 'change' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// 权限分配
const permissionDialogVisible = ref(false)
const currentMenuId = ref<number>(0)
const selectedPermissionIds = ref<number[]>([])
const allPermissions = ref<any[]>([])

// 图标选择器
const iconSelectorVisible = ref(false)
const iconSearchKeyword = ref('')
const availableIcons = ref([
  'Odometer',
  'FolderOpened',
  'List',
  'Clock',
  'DataAnalysis',
  'Operation',
  'Folder',
  'Document',
  'User',
  'Setting',
  'SwitchButton',
  'Brush',
  'HomeFilled',
  'IconMenu',
  'Grid',
  'Management',
  'TrendCharts',
  'PieChart',
  'DataLine',
  'Calendar',
  'Edit',
  'Delete',
  'View',
  'Download',
  'Upload',
  'Share',
  'Star',
  'Lock',
  'Unlock',
  'Bell',
  'Message',
  'ChatDotRound',
  'Phone',
  'Location',
  'Link',
  'Paperclip',
  'Tickets',
  'Collection',
  'Notebook',
  'Memo',
  'Printer',
  'Goods',
  'Box',
  'ShoppingBag',
  'ShoppingCart',
  'Wallet',
  'Coin',
  'CreditCard',
  'BankCard',
  'Money',
  'Histogram',
  'Tools',
  'SetUp',
  'Notification',
  'InfoFilled',
  'SuccessFilled',
  'CircleCheckFilled',
  'Warning',
  'WarningFilled'
])

// 过滤后的图标列表
const filteredIcons = computed(() => {
  if (!iconSearchKeyword.value) {
    return availableIcons.value
  }
  const keyword = iconSearchKeyword.value.toLowerCase()
  return availableIcons.value.filter(icon =>
    icon.toLowerCase().includes(keyword)
  )
})

// 选择图标
const handleSelectIcon = (icon: string) => {
  formData.icon = icon
}

// 确认选择图标
const handleConfirmIcon = () => {
  iconSelectorVisible.value = false
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMenuList(searchForm)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取菜单列表失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const fetchMenuTree = async () => {
  try {
    const tree = await getMenuTree()
    menuTree.value = tree
  } catch (error) {
    console.error('获取菜单树失败:', error)
  }
}

// 节点点击
const handleNodeClick = (data: MenuInfo) => {
  searchForm.parentId = data.id
  searchForm.pageNum = 1
  fetchData()
}

// 节点双击 - 切换展开/折叠
const handleNodeDblClick = (event: MouseEvent, data: MenuInfo) => {
  // 阻止默认文本选中行为
  event.preventDefault()

  // 清除可能已经选中的文本
  const selection = window.getSelection()
  if (selection) {
    selection.removeAllRanges()
  }

  if (!treeRef.value) return

  // 获取节点对象
  const node = treeRef.value.getNode(data.id)

  if (node) {
    // 切换展开/折叠状态
    if (node.expanded) {
      node.collapse()
    } else {
      node.expand()
    }
  }
}

// 获取父菜单名称
const getParentMenuName = (parentId: number): string => {
  if (parentId === 0) return '根菜单'

  // 在菜单树中递归查找父菜单
  const findMenu = (menus: MenuInfo[], id: number): MenuInfo | null => {
    for (const menu of menus) {
      if (menu.id === id) return menu
      if (menu.children) {
        const found = findMenu(menu.children, id)
        if (found) return found
      }
    }
    return null
  }

  const parent = findMenu(menuTree.value, parentId)
  return parent ? parent.name : '未知'
}

// 对话框操作
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    name: '',
    parentId: 0,
    type: 2,
    path: '',
    component: '',
    icon: '',
    sort: 0,
    status: 1,
    visible: 1,
    permissionIds: []
  })
  delete formData.id
}

const handleCreate = () => {
  dialogTitle.value = '新建菜单'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: MenuInfo) => {
  dialogTitle.value = '编辑菜单'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    parentId: row.parentId,
    type: row.type,
    path: row.path,
    component: row.component,
    icon: row.icon,
    sort: row.sort,
    status: row.status,
    visible: row.visible,
    permissionIds: row.permissionIds || []
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      if (formData.id) {
        // 更新
        await updateMenu(formData as MenuUpdateReq)
        ElMessage.success('菜单更新成功')
      } else {
        // 创建
        await createMenu(formData as MenuCreateReq)
        ElMessage.success('菜单创建成功')
      }
      dialogVisible.value = false
      fetchData()
      fetchMenuTree()
    } catch (error) {
      console.error('操作失败:', error)
    }
  })
}

const handleDelete = async (row: MenuInfo) => {
  try {
    await ElMessageBox.confirm(`确定要删除菜单"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteMenu(row.id)
    ElMessage.success('删除成功')
    fetchData()
    fetchMenuTree()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 上移菜单
const handleMoveUp = async (row: MenuInfo, index: number) => {
  if (index === 0) return

  try {
    // 获取上一个菜单
    const prevRow = list.value[index - 1]

    // 交换排序值
    const currentSort = row.sort
    const prevSort = prevRow.sort

    // 更新当前菜单排序
    await updateMenu({
      id: row.id,
      sort: prevSort
    })

    // 更新上一个菜单排序
    await updateMenu({
      id: prevRow.id,
      sort: currentSort
    })

    ElMessage.success('上移成功')
    fetchData()
    fetchMenuTree()
  } catch (error: any) {
    console.error('上移失败:', error)
    ElMessage.error(error.message || '上移失败')
  }
}

// 下移菜单
const handleMoveDown = async (row: MenuInfo, index: number) => {
  if (index === list.value.length - 1) return

  try {
    // 获取下一个菜单
    const nextRow = list.value[index + 1]

    // 交换排序值
    const currentSort = row.sort
    const nextSort = nextRow.sort

    // 更新当前菜单排序
    await updateMenu({
      id: row.id,
      sort: nextSort
    })

    // 更新下一个菜单排序
    await updateMenu({
      id: nextRow.id,
      sort: currentSort
    })

    ElMessage.success('下移成功')
    fetchData()
    fetchMenuTree()
  } catch (error: any) {
    console.error('下移失败:', error)
    ElMessage.error(error.message || '下移失败')
  }
}

// 权限分配
const handleAssignPermissions = async (row: MenuInfo) => {
  currentMenuId.value = row.id
  try {
    // 获取所有权限
    const permissions = await getAllPermissions()
    allPermissions.value = permissions

    // 获取当前菜单的权限
    const permissionIds = await getMenuPermissions(row.id)
    selectedPermissionIds.value = permissionIds

    permissionDialogVisible.value = true
  } catch (error) {
    console.error('获取权限失败:', error)
    ElMessage.error('获取权限失败')
  }
}

const handleAssignPermissionsSubmit = async () => {
  try {
    await assignMenuPermissions(currentMenuId.value, selectedPermissionIds.value)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('权限分配失败:', error)
    ElMessage.error('权限分配失败')
  }
}

// 生命周期
onMounted(async () => {
  await fetchMenuTree()
  // 默认选中根节点，显示所有一级菜单
  searchForm.parentId = 0

  // 等待树渲染完成后选中根节点
  await fetchData()
  await nextTick()
  if (treeRef.value) {
    treeRef.value.setCurrentKey(0)
  }
})
</script>

<style scoped>
.menu-list {
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

.content-wrapper {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.tree-panel {
  width: 300px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-content {
  padding: 16px;
  height: 500px;
  overflow-y: auto;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  padding: 4px 0;
  cursor: pointer;
}

.table-panel {
  flex: 1;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.table-content {
  padding: 16px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 图标选择器样式 */
.icon-selector {
  max-height: 500px;
  overflow-y: auto;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.icon-item:hover {
  border-color: var(--el-color-primary);
  background: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.icon-item.selected {
  border-color: var(--el-color-primary);
  background: #ecf5ff;
  box-shadow: 0 0 0 2px var(--el-color-primary-light-7);
}

.icon-name {
  font-size: 12px;
  color: #606266;
  text-align: center;
  word-break: break-all;
}
</style>
