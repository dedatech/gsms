/**
 * 岗位配置
 */

export interface PositionOption {
  label: string
  value: number
  name: string // 枚举名称，用于后端API
}

export const POSITION_OPTIONS: PositionOption[] = [
  { label: '全栈开发', value: 1, name: 'FULL_STACK' },
  { label: '后端开发', value: 2, name: 'BACKEND' },
  { label: '前端开发', value: 3, name: 'FRONTEND' },
  { label: '质量保证', value: 4, name: 'QA' },
  { label: '需求方', value: 5, name: 'PRODUCT_OWNER' }
]

/**
 * 根据枚举名称获取标签
 */
export const getPositionLabel = (name: string | undefined): string => {
  if (!name) return '未设置'
  const option = POSITION_OPTIONS.find(opt => opt.name === name)
  return option?.label || '未设置'
}

/**
 * 根据枚举名称获取值
 */
export const getPositionValue = (name: string | undefined): number => {
  if (!name) return 0
  const option = POSITION_OPTIONS.find(opt => opt.name === name)
  return option?.value || 0
}
