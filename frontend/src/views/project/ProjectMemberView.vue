<template>
  <div class="project-member-view">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">项目成员</h2>
        <span class="member-count">共 {{ members.length }} 位成员</span>
      </div>
      <div class="header-right">
        <el-button
          type="primary"
          :icon="Plus"
          @click="openAddMemberDialog"
        >
          维护成员
        </el-button>
      </div>
    </div>

    <!-- 成员列表（按角色分组显示） -->
    <div class="member-list">
      <!-- 项目经理组 -->
      <div v-if="managerMembers.length > 0" class="member-group">
        <div class="group-header">
          <div class="group-title">
            <el-tag type="danger" size="small">项目经理</el-tag>
            <span class="group-count">{{ managerMembers.length }}</span>
          </div>
        </div>
        <div class="group-body">
          <div
            v-for="member in managerMembers"
            :key="member.id"
            class="member-card manager-card"
          >
            <div class="member-avatar">
              <el-avatar :size="40" :icon="UserFilled" />
            </div>
            <div class="member-info">
              <div class="member-name">{{ member.nickname }}</div>
              <div class="member-role">
                <el-tag type="danger" size="small">{{ member.roleName }}</el-tag>
              </div>
            </div>
            <div class="member-actions">
              <el-button
                text
                type="danger"
                :icon="Delete"
                size="small"
                @click="handleRemoveMember(member)"
              >
                移除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 普通成员组 -->
      <div v-if="normalMembers.length > 0" class="member-group">
        <div class="group-header">
          <div class="group-title">
            <el-tag type="" size="small">普通成员</el-tag>
            <span class="group-count">{{ normalMembers.length }}</span>
          </div>
        </div>
        <div class="group-body">
          <div
            v-for="member in normalMembers"
            :key="member.id"
            class="member-card"
          >
            <div class="member-avatar">
              <el-avatar :size="40" :icon="UserFilled" />
            </div>
            <div class="member-info">
              <div class="member-name">{{ member.nickname }}</div>
              <div class="member-role">
                <el-tag size="small">{{ member.roleName }}</el-tag>
              </div>
            </div>
            <div class="member-actions">
              <el-checkbox
                v-model="selectedMemberIds"
                :label="member.userId"
                @change="handleSelectionChange"
              >
                <span class="select-label">选择</span>
              </el-checkbox>
              <el-button
                text
                type="danger"
                :icon="Delete"
                size="small"
                @click="handleRemoveMember(member)"
              >
                移除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 只读访客组 -->
      <div v-if="readonlyMembers.length > 0" class="member-group">
        <div class="group-header">
          <div class="group-title">
            <el-tag type="info" size="small">只读访客</el-tag>
            <span class="group-count">{{ readonlyMembers.length }}</span>
          </div>
        </div>
        <div class="group-body">
          <div
            v-for="member in readonlyMembers"
            :key="member.id"
            class="member-card"
          >
            <div class="member-avatar">
              <el-avatar :size="40" :icon="UserFilled" />
            </div>
            <div class="member-info">
              <div class="member-name">{{ member.nickname }}</div>
              <div class="member-role">
                <el-tag type="info" size="small">{{ member.roleName }}</el-tag>
              </div>
            </div>
            <div class="member-actions">
              <el-checkbox
                v-model="selectedMemberIds"
                :label="member.userId"
                @change="handleSelectionChange"
              >
                <span class="select-label">选择</span>
              </el-checkbox>
              <el-button
                text
                type="danger"
                :icon="Delete"
                size="small"
                @click="handleRemoveMember(member)"
              >
                移除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="members.length === 0"
        description="暂无项目成员"
        :image-size="120"
      />

      <!-- 批量操作栏 -->
      <div v-if="selectedMemberIds.length > 0" class="batch-actions">
        <div class="batch-info">
          已选择 <strong>{{ selectedMemberIds.length }}</strong> 位成员
        </div>
        <el-button
          type="danger"
          :icon="Delete"
          @click="handleBatchRemove"
        >
          批量移除
        </el-button>
      </div>
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="维护项目成员"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="addFormRef" :model="addFormData" label-width="100px">
        <el-form-item label="角色类型">
          <el-radio-group v-model="addFormData.roleType">
            <el-radio :value="1">项目经理</el-radio>
            <el-radio :value="2">普通成员</el-radio>
            <el-radio :value="3">只读访客</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="选择成员">
          <el-select
            v-model="addFormData.userIds"
            multiple
            filterable
            placeholder="请选择要添加的成员"
            style="width: 100%"
            :multiple-limit="50"
          >
            <el-option
              v-for="user in availableUsers"
              :key="user.id"
              :label="user.nickname"
              :value="user.id"
              :disabled="isMemberExists(user.id)"
            >
              <span>{{ user.nickname }}</span>
              <span v-if="isMemberExists(user.id)" class="user-exist-tag">已添加</span>
            </el-option>
          </el-select>
          <div class="form-tip">已添加的成员不可重复选择</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          @click="handleAddMembers"
          :loading="addSubmitLoading"
          :disabled="addFormData.userIds.length === 0"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Delete, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import {
  getProjectMembers,
  addProjectMembers,
  removeProjectMember,
  batchRemoveProjectMembers,
  type ProjectMember
} from '@/api/project'
import { getAllUsers, type UserInfo } from '@/api/user'

const props = defineProps<{
  projectId: number
}>()

const authStore = useAuthStore()

// 成员列表
const members = ref<ProjectMember[]>([])

// 选中的成员ID列表（用于批量操作）
const selectedMemberIds = ref<number[]>([])

// 按角色分组的成员列表
const managerMembers = computed(() =>
  members.value.filter(m => m.roleType === 1)
)

const normalMembers = computed(() =>
  members.value.filter(m => m.roleType === 2)
)

const readonlyMembers = computed(() =>
  members.value.filter(m => m.roleType === 3)
)

// 可用用户列表
const availableUsers = ref<UserInfo[]>([])

// 添加成员对话框
const addDialogVisible = ref(false)
const addSubmitLoading = ref(false)
const addFormRef = ref<FormInstance>()
const addFormData = reactive({
  roleType: 2,  // 默认为普通成员
  userIds: [] as number[]
})

// 获取成员列表
const fetchMembers = async () => {
  try {
    const res = await getProjectMembers(props.projectId)
    members.value = res || []
  } catch (error) {
    console.error('获取成员列表失败:', error)
    ElMessage.error('获取成员列表失败')
  }
}

// 获取可用用户列表
const fetchAvailableUsers = async () => {
  try {
    const res = await getAllUsers()
    availableUsers.value = (res?.list || []).filter((u: UserInfo) => u.status === 'NORMAL')
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 判断用户是否已是项目成员
const isMemberExists = (userId: number) => {
  return members.value.some(m => m.userId === userId)
}

// 打开添加成员对话框
const openAddMemberDialog = () => {
  addFormData.roleType = 2
  addFormData.userIds = []
  addDialogVisible.value = true
}

// 添加成员
const handleAddMembers = async () => {
  if (addFormData.userIds.length === 0) {
    ElMessage.warning('请选择要添加的成员')
    return
  }

  addSubmitLoading.value = true
  try {
    await addProjectMembers(props.projectId, {
      userIds: addFormData.userIds,
      roleType: addFormData.roleType
    })
    ElMessage.success('添加成员成功')
    addDialogVisible.value = false
    await fetchMembers()
    selectedMemberIds.value = []
  } catch (error: any) {
    console.error('添加成员失败:', error)
    ElMessage.error(error.message || '添加成员失败')
  } finally {
    addSubmitLoading.value = false
  }
}

// 选择变化处理
const handleSelectionChange = () => {
  // 选择变化时自动更新
}

// 单个删除成员
const handleRemoveMember = async (member: ProjectMember) => {
  try {
    await ElMessageBox.confirm(
      `确定要将成员"${member.nickname}"从项目中移除吗？`,
      '确认移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    try {
      await removeProjectMember(props.projectId, member.userId)
      ElMessage.success('移除成员成功')
      await fetchMembers()
      // 清除选中状态
      selectedMemberIds.value = selectedMemberIds.value.filter(id => id !== member.userId)
    } catch (error: any) {
      console.error('移除成员失败:', error)
      ElMessage.error(error.message || '移除成员失败')
    }
  } catch {
    // 用户取消操作
  }
}

// 批量删除成员
const handleBatchRemove = async () => {
  if (selectedMemberIds.value.length === 0) {
    ElMessage.warning('请先选择要移除的成员')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要将选中的 ${selectedMemberIds.value.length} 位成员从项目中移除吗？`,
      '确认批量移除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    try {
      await batchRemoveProjectMembers(props.projectId, selectedMemberIds.value)
      ElMessage.success('批量移除完成')
      await fetchMembers()
      selectedMemberIds.value = []
    } catch (error: any) {
      console.error('批量移除失败:', error)
      ElMessage.error(error.message || '批量移除失败')
    }
  } catch {
    // 用户取消操作
  }
}

onMounted(() => {
  fetchMembers()
  fetchAvailableUsers()
})

// 暴露刷新方法供父组件调用
defineExpose({
  refresh: fetchMembers
})
</script>

<style scoped>
.project-member-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.member-count {
  font-size: 14px;
  color: #999;
}

/* 成员列表 */
.member-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}

.member-group {
  background: #fff;
  border-radius: 4px;
  margin-bottom: 16px;
  overflow: hidden;
}

.group-header {
  padding: 12px 16px;
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.group-count {
  font-size: 12px;
  color: #999;
  margin-left: 4px;
}

.group-body {
  padding: 8px 0;
}

.member-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.member-card:last-child {
  border-bottom: none;
}

.member-card:hover {
  background: #fafafa;
}

.member-card.manager-card {
  background: #fff7e6;
}

.member-card.manager-card:hover {
  background: #ffeccb;
}

.member-avatar {
  flex-shrink: 0;
  margin-right: 12px;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.member-role {
  font-size: 12px;
  color: #999;
}

.member-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.select-label {
  font-size: 12px;
  color: #666;
}

/* 批量操作栏 */
.batch-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-top: 1px solid #e8e8e8;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.batch-info {
  font-size: 14px;
  color: #333;
}

.batch-info strong {
  color: #1890ff;
  font-weight: 600;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 用户已存在标签 */
.user-exist-tag {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

/* 空状态 */
:deep(.el-empty) {
  padding: 40px 0;
}
</style>
