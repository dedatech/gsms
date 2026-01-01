import request from './request'

// 用户登录
export interface LoginReq {
  username: string
  password: string
}

export interface LoginResp {
  token: string
  userInfo: any
}

export const login = (data: LoginReq) => {
  return request.post<any, LoginResp>('/users/login', data)
}

// 用户注册
export interface RegisterReq {
  username: string
  password: string
  nickname: string
  email: string
  phone: string
}

export const register = (data: RegisterReq) => {
  return request.post('/users/register', data)
}

// 获取用户信息
export const getUserInfo = () => {
  return request.get('/users/info')
}
