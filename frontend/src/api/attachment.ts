import request from './request'

// 附件信息响应
export interface AttachmentInfo {
  id: number
  fileName: string
  displayName: string
  fileSize: number
  fileSizeFormatted: string
  fileType: string
  mimeType: string
  storageType: string
  targetType: string
  targetId: number
  uploaderId: number
  uploaderName: string
  url: string
  canPreview: boolean
  createTime: string
  taskId?: number
  taskNumber?: string
  taskTitle?: string
}

// 附件查询参数
export interface AttachmentQuery {
  targetType: string  // 'project' | 'task'
  targetId: number
}

// 上传附件
export const uploadAttachment = (targetType: string, targetId: number, file: File) => {
  const formData = new FormData()
  formData.append('targetType', targetType)
  formData.append('targetId', targetId.toString())
  formData.append('file', file)

  return request.post('/attachments/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取附件列表
export const getAttachmentList = (targetType: string, targetId: number) => {
  return request.get<AttachmentInfo[]>('/attachments/list', {
    params: { targetType, targetId }
  })
}

// 获取项目的所有附件（包括项目附件和任务附件）
export const getProjectAttachments = (projectId: number) => {
  return request.get<AttachmentInfo[]>(`/attachments/project/${projectId}`)
}

// 获取附件详情
export const getAttachmentDetail = (id: number) => {
  return request.get<AttachmentInfo>(`/attachments/${id}`)
}

// 下载附件
export const downloadAttachment = (id: number) => {
  return request.get(`/attachments/${id}/download`, {
    responseType: 'blob'
  })
}

// 预览附件
export const previewAttachment = (id: number) => {
  return request.get(`/attachments/${id}/preview`, {
    responseType: 'blob'
  })
}

// 重命名附件
export interface AttachmentRenameReq {
  id: number
  displayName: string
}

export const renameAttachment = (data: AttachmentRenameReq) => {
  return request.put('/attachments/rename', data)
}

// 删除附件
export const deleteAttachment = (id: number) => {
  return request.delete(`/attachments/${id}`)
}

// 批量删除附件
export const batchDeleteAttachments = (ids: number[]) => {
  return request.delete('/attachments/batch', { data: ids })
}

// 获取文件访问 URL
export const getFileUrl = (filePath: string) => {
  return `/api/attachments/file/${filePath}`
}
