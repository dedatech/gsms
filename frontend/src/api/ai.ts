import request, { type AxiosRequestConfig } from './request'

/**
 * 需求拆分请求
 */
export interface RequirementBreakdownReq {
  requirement: string      // 需求描述
  projectType?: string     // 项目类型
  teamSize?: number        // 团队规模
  expectedDays?: number    // 期望完成时间（天）
  estimateHours?: number   // 期望工时（小时）
}

/**
 * 子任务估算
 */
export interface SubTaskEstimate {
  sequence: number
  title: string
  description: string
  estimatedDays: number
  taskType: string
  priority: string
  dependsOn?: number
  notes?: string
}

/**
 * 需求拆分响应
 */
export interface RequirementBreakdownResp {
  summary: string
  subTasks: SubTaskEstimate[]
  totalEstimatedDays: number
  suggestedTeamSize: number
  suggestedIterationDays: number
  risks: string[]
  suggestions: string[]
  notes?: string
}

/**
 * API 响应结果包装
 */
interface Result<T> {
  code: number
  message: string
  data: T
}

/**
 * 拆分需求并预估工时
 * AI 接口需要较长超时时间（最多 3 分钟）
 */
export const breakdownRequirement = (data: RequirementBreakdownReq) => {
  const config: AxiosRequestConfig = {
    timeout: 180000 // 3 分钟超时
  }
  return request.post<Result<RequirementBreakdownResp>>('/ai/breakdown-requirement', data, config)
}

/**
 * 检查 AI 服务状态
 */
export const checkAiStatus = () => {
  return request.get<Result<boolean>>('/ai/status')
}
