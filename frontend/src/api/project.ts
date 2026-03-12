import request from './request'

// 获取项目列表
export interface ProjectQuery {
  name?: string
  status?: string
  pageNum?: number
  pageSize?: number
}

export const getProjectList = (params: ProjectQuery) => {
  return request.get('/projects', { params })
}

// 获取项目详情
export const getProjectDetail = (id: number) => {
  return request.get(`/projects/${id}`)
}

// 创建项目
export interface ProjectCreateReq {
  name: string
  code: string
  description?: string
  managerId: number
  status: string
}

export const createProject = (data: ProjectCreateReq) => {
  return request.post('/projects', data)
}

// 更新项目
export interface ProjectUpdateReq {
  id: number
  name?: string
  description?: string
  status?: string
}

export const updateProject = (data: ProjectUpdateReq) => {
  return request.put('/projects', data)
}

// 删除项目
export const deleteProject = (id: number) => {
  return request.delete(`/projects/${id}`)
}

// 获取项目成员列表
export const getProjectMembers = (projectId: number, excludeReadOnly = true) => {
  return request.get(`/projects/${projectId}/members`, { params: { excludeReadOnly } })
}

// 添加项目成员
export const addProjectMember = (projectId: number, userId: number, roleType: number) => {
  return request.post(`/projects/${projectId}/members`, [userId], { params: { roleType } })
}

// 移除项目成员
export const removeProjectMember = (projectId: number, userId: number) => {
  return request.delete(`/projects/${projectId}/members/${userId}`)
}

// 批量添加项目成员
export interface AddProjectMembersReq {
  userIds: number[]
  roleType: number  // 1: 项目经理, 2: 普通成员, 3: 只读访客
}

export const addProjectMembers = (projectId: number, data: AddProjectMembersReq) => {
  return request.post(`/projects/${projectId}/members`, data.userIds, { params: { roleType: data.roleType } })
}

// 批量移除项目成员
export const batchRemoveProjectMembers = (projectId: number, userIds: number[]) => {
  return request.delete(`/projects/${projectId}/members/batch`, { data: userIds })
}

// 项目成员响应类型
export interface ProjectMember {
  id: number
  userId: number
  nickname: string
  roleType: number
  roleName: string
  createTime: string
}

// 项目信息响应类型
export interface ProjectInfoResp {
  id: number
  name: string
  code: string
  projectType: string
  description: string
  managerId: number
  status: string
  planStartDate: string
  planEndDate: string
  actualStartDate: string
  actualEndDate: string
  createUserId: number
  createUserName: string
  updateUserId: number
  updateUserName: string
  createTime: string
  updateTime: string
}
