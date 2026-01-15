import request from './request'

/**
 * 菜单信息响应接口
 */
export interface MenuInfo {
  id: number
  name: string
  path?: string
  component?: string
  icon?: string
  parentId: number
  sort: number
  type: number // 1:目录 2:菜单 3:按钮
  status: number // 1:启用 2:禁用
  visible: number // 1:可见 2:隐藏
  permissionIds?: number[]
  children?: MenuInfo[]
  createTime: string
  updateTime: string
}

/**
 * 菜单查询请求接口
 */
export interface MenuQuery {
  name?: string
  type?: number
  status?: number
  parentId?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 创建菜单请求接口
 */
export interface MenuCreateReq {
  name: string
  path?: string
  component?: string
  icon?: string
  parentId: number
  sort?: number
  type: number
  status?: number
  visible?: number
  permissionIds?: number[]
}

/**
 * 更新菜单请求接口
 */
export interface MenuUpdateReq {
  id: number
  name?: string
  path?: string
  component?: string
  icon?: string
  parentId?: number
  sort?: number
  type?: number
  status?: number
  visible?: number
  permissionIds?: number[]
}

/**
 * 获取当前用户可访问菜单树
 */
export function getUserMenuTree(): Promise<MenuInfo[]> {
  return request({
    url: '/menus/user/tree',
    method: 'get'
  })
}

/**
 * 获取所有菜单树
 */
export function getMenuTree(): Promise<MenuInfo[]> {
  return request({
    url: '/menus/tree',
    method: 'get'
  })
}

/**
 * 根据ID获取菜单
 */
export function getMenuById(id: number): Promise<MenuInfo> {
  return request({
    url: `/menus/${id}`,
    method: 'get'
  })
}

/**
 * 根据条件分页查询菜单
 */
export function getMenuList(params: MenuQuery): Promise<{ list: MenuInfo[]; total: number }> {
  return request({
    url: '/menus',
    method: 'get',
    params
  })
}

/**
 * 创建菜单
 */
export function createMenu(data: MenuCreateReq): Promise<MenuInfo> {
  return request({
    url: '/menus',
    method: 'post',
    data
  })
}

/**
 * 更新菜单
 */
export function updateMenu(data: MenuUpdateReq): Promise<MenuInfo> {
  return request({
    url: '/menus',
    method: 'put',
    data
  })
}

/**
 * 删除菜单
 */
export function deleteMenu(id: number): Promise<string> {
  return request({
    url: `/menus/${id}`,
    method: 'delete'
  })
}

/**
 * 为菜单分配权限
 */
export function assignMenuPermissions(menuId: number, permissionIds: number[]): Promise<string> {
  return request({
    url: `/menus/${menuId}/permissions`,
    method: 'post',
    data: permissionIds
  })
}

/**
 * 获取菜单的权限ID列表
 */
export function getMenuPermissions(menuId: number): Promise<number[]> {
  return request({
    url: `/menus/${menuId}/permissions`,
    method: 'get'
  })
}
