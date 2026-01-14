import request from './request'

// 部门查询参数
export interface DepartmentQuery {
  name?: string
  parentId?: number
  pageNum?: number
  pageSize?: number
}

// 部门信息
export interface DepartmentInfo {
  id: number
  name: string
  parentId: number
  level: number
  sort: number
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 获取部门列表
 */
export const getDepartmentList = (params: DepartmentQuery) => {
  return request.get('/departments', { params })
}

/**
 * 获取所有部门（用于下拉选择，不分页）
 */
export const getAllDepartments = () => {
  return request.get('/departments', { params: { pageNum: 1, pageSize: 1000 } })
}
