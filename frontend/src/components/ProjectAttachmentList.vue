<template>
  <div class="project-attachment-list">
    <div class="list-header">
      <div class="header-left">
        <h3 class="title">附件</h3>
        <span class="count">{{ attachments.length }}</span>
      </div>
      <div class="header-right">
        <el-button
          v-if="canUpload"
          type="primary"
          :icon="Upload"
          size="small"
          @click="handleUpload"
        >
          上传附件
        </el-button>
      </div>
    </div>

    <div class="list-body" v-loading="loading">
      <el-table
        :data="attachments"
        empty-text="暂无附件"
      >
        <el-table-column label="文件名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="file-name" @click="handlePreview(row)">
              <el-icon class="file-icon">
                <component :is="getFileIcon(row.fileType)" />
              </el-icon>
              <span>{{ row.displayName || row.fileName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="关联需求" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.taskNumber" class="task-link">
              {{ row.taskNumber }} {{ row.taskTitle }}
            </span>
            <span v-else class="no-task">未关联需求</span>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.fileSizeFormatted }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上传者" width="120" align="center">
          <template #default="{ row }">
            <span>{{ row.uploaderName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="180" align="center">
          <template #default="{ row }">
            <span>{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="Download"
              size="small"
              @click="handleDownload(row)"
            >
              下载
            </el-button>
            <el-button
              v-if="row.canPreview"
              type="primary"
              link
              :icon="View"
              size="small"
              @click="handlePreview(row)"
            >
              预览
            </el-button>
            <el-dropdown v-if="canEdit(row)" @command="(cmd) => handleCommand(cmd, row)">
              <el-button type="primary" link :icon="More" size="small">
                更多
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="rename" :icon="Edit">
                    重命名
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" :icon="Delete">
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 上传对话框 -->
    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileChange"
    />

    <!-- 重命名对话框 -->
    <el-dialog
      v-model="renameDialogVisible"
      title="重命名附件"
      width="500px"
    >
      <el-form :model="renameForm" :rules="renameRules" ref="renameFormRef">
        <el-form-item label="显示名称" prop="displayName">
          <el-input
            v-model="renameForm.displayName"
            placeholder="请输入显示名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRenameSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="previewAttachment?.displayName || previewAttachment?.fileName"
      width="80%"
      fullscreen
    >
      <div class="preview-container" v-if="previewAttachment">
        <img
          v-if="isImage(previewAttachment.fileType)"
          :src="previewAttachment.url"
          :alt="previewAttachment.fileName"
          class="preview-image"
        />
        <iframe
          v-else-if="isPdf(previewAttachment.fileType)"
          :src="previewAttachment.url"
          class="preview-pdf"
        />
        <div v-else class="preview-unsupported">
          <el-icon :size="64"><Document /></el-icon>
          <p>该文件类型不支持在线预览</p>
          <el-button type="primary" @click="handleDownload(previewAttachment)">
            下载文件
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Upload, Download, View, Delete, Edit, More, Document,
  FolderOpened, Picture, VideoCamera
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import {
  getProjectAttachments,
  uploadAttachment,
  downloadAttachment,
  renameAttachment,
  deleteAttachment,
  getFileUrl,
  type AttachmentInfo
} from '@/api/attachment'

interface Props {
  projectId: number
  canUpload?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  canUpload: true
})

const authStore = useAuthStore()
const currentUserId = computed(() => authStore.getCurrentUserId())

// 数据
const loading = ref(false)
const attachments = ref<AttachmentInfo[]>([])

// 上传
const fileInputRef = ref<HTMLInputElement>()
const uploading = ref(false)

// 重命名
const renameDialogVisible = ref(false)
const renameFormRef = ref<FormInstance>()
const renameForm = reactive({
  id: 0,
  displayName: ''
})
const renameRules: FormRules = {
  displayName: [
    { required: true, message: '请输入显示名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}
const renamingAttachment = ref<AttachmentInfo | null>(null)

// 预览
const previewDialogVisible = ref(false)
const previewAttachment = ref<AttachmentInfo | null>(null)

// 方法
const fetchData = async () => {
  loading.value = true
  try {
    const list = await getProjectAttachments(props.projectId)
    attachments.value = list.map((att: AttachmentInfo) => ({
      ...att,
      url: getFileUrl(att.url.split('/api/attachments/file/')[1] || '')
    }))
  } catch (error) {
    console.error('获取附件列表失败:', error)
  } finally {
    loading.value = false
  }
}

const canEdit = (attachment: AttachmentInfo) => {
  return attachment.uploaderId === currentUserId.value
}

const handleUpload = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    await uploadAttachment('project', props.projectId, file)
    ElMessage.success('上传成功')
    await fetchData()
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploading.value = false
    target.value = ''
  }
}

const handleDownload = async (attachment: AttachmentInfo) => {
  try {
    const blob = await downloadAttachment(attachment.id)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = attachment.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载失败:', error)
  }
}

const handlePreview = (attachment: AttachmentInfo) => {
  if (!attachment.canPreview) {
    handleDownload(attachment)
    return
  }
  previewAttachment.value = attachment
  previewDialogVisible.value = true
}

const isImage = (fileType: string) => {
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(fileType.toLowerCase())
}

const isPdf = (fileType: string) => {
  return fileType.toLowerCase() === 'pdf'
}

const handleCommand = (command: string, attachment: AttachmentInfo) => {
  if (command === 'rename') {
    handleRename(attachment)
  } else if (command === 'delete') {
    handleDelete(attachment)
  }
}

const handleRename = (attachment: AttachmentInfo) => {
  renamingAttachment.value = attachment
  renameForm.id = attachment.id
  renameForm.displayName = attachment.displayName || attachment.fileName
  renameDialogVisible.value = true
}

const handleRenameSubmit = async () => {
  if (!renameFormRef.value) return
  await renameFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await renameAttachment({
          id: renameForm.id,
          displayName: renameForm.displayName
        })
        ElMessage.success('重命名成功')
        renameDialogVisible.value = false
        await fetchData()
      } catch (error) {
        console.error('重命名失败:', error)
      }
    }
  })
}

const handleDelete = async (attachment: AttachmentInfo) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除附件 "${attachment.displayName || attachment.fileName}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteAttachment(attachment.id)
    ElMessage.success('删除成功')
    await fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const getFileIcon = (fileType: string) => {
  const type = fileType.toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(type)) {
    return Picture
  } else if (['mp4', 'avi', 'mov', 'wmv'].includes(type)) {
    return VideoCamera
  } else {
    return Document
  }
}

onMounted(() => {
  fetchData()
})

defineExpose({
  refresh: fetchData
})
</script>

<style scoped>
.project-attachment-list {
  background: #fff;
  border-radius: 4px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  background: #f0f0f0;
  border-radius: 12px;
  font-size: 12px;
  color: #666;
}

.header-right {
  display: flex;
  gap: 8px;
}

.list-body {
  padding: 0;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #409eff;
}

.file-name:hover {
  text-decoration: underline;
}

.file-icon {
  font-size: 18px;
  color: #909399;
}

.task-link {
  color: #409eff;
  font-size: 13px;
}

.no-task {
  color: #909399;
  font-size: 13px;
}

.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

.preview-pdf {
  width: 100%;
  height: 80vh;
  border: none;
}

.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #909399;
}
</style>
