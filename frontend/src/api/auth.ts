import request from './request'
import type { ApiResponse, LoginRequest, LoginResponse, UnknownError } from '@/types'

// 用户登录
export interface LoginReq {
  username: string
  password: string
  captchaCode: string
  captchaUuid: string
}

export interface LoginResp {
  token: string
  userInfo: {
    id: number
    username: string
    nickname?: string
  }
}

export const login = (data: LoginReq): Promise<ApiResponse<LoginResp>> => {
  return request.post('/users/login', data)
}

// 获取验证码
export interface CaptchaResp {
  uuid: string
  image: string
}

export const getCaptcha = (): Promise<ApiResponse<CaptchaResp>> => {
  return request.get('/captcha')
}

// 用户注册
export interface RegisterReq {
  username: string
  password: string
  nickname: string
  email: string
  phone: string
}

export const register = (data: RegisterReq): Promise<ApiResponse<string>> => {
  return request.post('/users/register', data)
}

// 获取用户信息
export const getUserInfo = (): Promise<ApiResponse<unknown>> => {
  return request.get('/users/info')
}

/**
 * 获取当前用户权限码列表
 */
export const getUserPermissions = (): Promise<string[]> => {
  return request.get('/auth/permissions')
}

/**
 * 获取当前用户角色编码列表
 */
export const getUserRoles = (): Promise<string[]> => {
  return request.get('/auth/roles')
}
