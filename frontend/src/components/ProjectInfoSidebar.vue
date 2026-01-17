<template>
  <el-drawer
    v-model="visible"
    title="项目信息"
    direction="rtl"
    size="500px"
    :close-on-click-modal="true"
    @close="handleClose"
  >
    <div v-if="project" class="project-info-sidebar">
      <!-- 基本信息 -->
      <div class="info-section">
        <h4 class="section-title">基本信息</h4>
        <div class="info-item">
          <span class="label">项目名称：</span>
          <span class="value">{{ project.name }}</span>
        </div>
        <div class="info-item">
          <span class="label">项目代码：</span>
          <span class="value">{{ project.code }}</span>
        </div>
        <div class="info-item">
          <span class="label">项目类型：</span>
          <el-tag :type="getProjectTypeTag(project.projectType)" size="small">
            {{ getProjectTypeText(project.projectType) }}
          </el-tag>
        </div>
        <div class="info-item">
          <span class="label">项目状态：</span>
          <el-tag :type="getProjectStatusType(project.status)" size="small">
            {{ getProjectStatusText(project.status) }}
          </el-tag>
        </div>
        <div class="info-item">
          <span class="label">项目经理：</span>
          <span class="value">{{ project.managerName || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">项目描述：</span>
          <p class="value description">{{ project.description || '暂无描述' }}</p>
        </div>
      </div>

      <!-- 时间信息 -->
      <div class="info-section">
        <h4 class="section-title">时间信息</h4>
        <div class="info-item">
          <span class="label">计划开始：</span>
          <span class="value">{{ project.planStartDate || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">计划结束：</span>
          <span class="value">{{ project.planEndDate || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">实际开始：</span>
          <span class="value">{{ project.actualStartDate || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">实际结束：</span>
          <span class="value">{{ project.actualEndDate || '-' }}</span>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="info-section">
        <h4 class="section-title">统计信息</h4>
        <div class="info-item">
          <span class="label">成员数量：</span>
          <span class="value">{{ memberCount }} 人</span>
        </div>
        <div class="info-item">
          <span class="label">迭代数量：</span>
          <span class="value">{{ iterationCount }} 个</span>
        </div>
        <div class="info-item">
          <span class="label">任务数量：</span>
          <span class="value">{{ taskCount }} 个</span>
        </div>
      </div>

      <!-- 创建信息 -->
      <div class="info-section">
        <div class="section-header">
          <h4 class="section-title">项目成员 ({{ memberCount }})</h4>
          <el-button type="primary" size="small" :icon="Plus" @click="handleAddMember">
            添加成员
          </el-button>
        </div>
        <div v-if="members && members.length > 0" class="member-list">
          <div v-for="member in members" :key="member.id" class="member-item">
            <el-avatar :size="32">{{ (member.nickname || 'U').charAt(0) }}</el-avatar>
            <div class="member-info">
              <div class="member-name">{{ member.nickname }}</div>
              <el-tag :type="getRoleType(member.roleType)" size="small">
                {{ member.roleName }}
              </el-tag>
            </div>
            <el-button
              v-if="canManageMembers"
              link
              type="danger"
              size="small"
              @click="handleRemoveMember(member)"
            >
              移除
            </el-button>
          </div>
        </div>
        <el-empty
          v-else
          description="暂无成员"
          :image-size="60"
        />
      </div>

      <!-- 创建信息 -->
      <div class="info-section">
        <h4 class="section-title">创建信息</h4>
        <div class="info-item">
          <span class="label">创建人：</span>
          <span class="value">{{ project.createUserName || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ project.createTime || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="label">更新时间：</span>
          <span class="value">{{ project.updateTime || '-' }}</span>
        </div>
      </div>
    </div>

    <div v-else class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addMemberDialogVisible"
      title="添加项目成员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="memberFormData" label-width="100px">
        <el-form-item label="选择用户">
          <el-select
            v-model="memberFormData.userId"
            placeholder="请选择用户"
            filterable
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
        <el-form-item label="角色类型">
          <el-radio-group v-model="memberFormData.roleType">
            <el-radio :value="1">项目管理员</el-radio>
            <el-radio :value="2">普通成员</el-radio>
            <el-radio :value="3">只读访客</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMemberSubmit" :loading="addMemberLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getProjectStatusInfo } from '@/utils/statusMapping'

// Props
const props = defineProps<{
  visible: boolean
  project: any
  members?: any[]
  availableUsers?: any[]
  memberCount?: number
  iterationCount?: number
  taskCount?: number
  canManageMembers?: boolean
}>()

// Emits
const emit = defineEmits<{
  'update:visible': [value: boolean]
  'addMember': [userId: number, roleType: number]
  'removeMember': [member: any]
}>()

// 本地可见状态
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 添加成员对话框
const addMemberDialogVisible = ref(false)
const addMemberLoading = ref(false)
const memberFormData = ref({
  userId: undefined as number | undefined,
  roleType: 2 // 默认为普通成员
})

// 关闭侧边栏
const handleClose = () => {
  emit('update:visible', false)
}

// 打开添加成员对话框
const handleAddMember = () => {
  memberFormData.value = {
    userId: undefined,
    roleType: 2
  }
  addMemberDialogVisible.value = true
}

// 提交添加成员
const handleAddMemberSubmit = () => {
  if (!memberFormData.value.userId) {
    return
  }
  emit('addMember', memberFormData.value.userId, memberFormData.value.roleType)
  addMemberDialogVisible.value = false
}

// 移除成员
const handleRemoveMember = (member: any) => {
  emit('removeMember', member)
}

// 监听 ESC 键关闭
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && props.visible) {
    handleClose()
  }
}

// 挂载和卸载键盘监听
watch(() => props.visible, (newValue) => {
  if (newValue) {
    document.addEventListener('keydown', handleKeydown)
  } else {
    document.removeEventListener('keydown', handleKeydown)
  }
})

// 获取项目类型标签
const getProjectTypeTag = (type: string) => {
  const tags: Record<string, string> = {
    'SCHEDULE': 'info',
    'LARGE_SCALE': 'warning'
  }
  return tags[type] || 'info'
}

// 获取项目类型文本
const getProjectTypeText = (type: string) => {
  const texts: Record<string, string> = {
    'SCHEDULE': '常规型项目',
    'LARGE_SCALE': '中大型项目'
  }
  return texts[type] || '未知'
}

// 获取项目状态信息
const getProjectStatusType = (status: string) => getProjectStatusInfo(status).type
const getProjectStatusText = (status: string) => getProjectStatusInfo(status).text

// 获取角色类型
const getRoleType = (roleType: number) => {
  const types: Record<number, string> = {
    1: 'danger',      // 项目管理员
    2: '',           // 项目成员
    3: 'info'        // 只读访客
  }
  return types[roleType] || 'info'
}
</script>

<style scoped>
.project-info-sidebar {
  padding: 0;
}

.info-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.info-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

/* 成员列表 */
.member-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 4px;
  transition: all 0.3s;
}

.member-item:hover {
  background: #f0f0f0;
}

.member-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
  align-items: flex-start;
}

.info-item:last-child {
  margin-bottom: 0;
}

.label {
  flex-shrink: 0;
  width: 100px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.value {
  flex: 1;
  font-size: 14px;
  color: #303133;
  word-break: break-word;
}

.value.description {
  line-height: 1.6;
  white-space: pre-wrap;
  margin: 0;
}

.loading-container {
  padding: 20px;
}

/* Drawer 样式调整 */
:deep(.el-drawer__header) {
  margin-bottom: 20px;
  padding: 20px 20px 10px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-drawer__body) {
  padding: 20px;
}
</style>
