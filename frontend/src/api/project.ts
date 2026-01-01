import request from './request'

// 获取项目列表
export interface ProjectQuery {
  name?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

export const getProjectList = (params: ProjectQuery) => {
  return request.get('/projects', { params })
}

// 创建项目
export interface ProjectCreateReq {
  name: string
  code: string
  description?: string
  managerId: number
  status: number
}

export const createProject = (data: ProjectCreateReq) => {
  return request.post('/projects', data)
}

// 更新项目
export interface ProjectUpdateReq {
  id: number
  name?: string
  description?: string
  status?: number
}

export const updateProject = (data: ProjectUpdateReq) => {
  return request.put('/projects', data)
}

// 删除项目
export const deleteProject = (id: number) => {
  return request.delete(`/projects/${id}`)
}
