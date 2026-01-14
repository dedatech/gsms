/**
 * 主题配置
 * 定义多个配色方案供用户选择
 */

export interface ThemeConfig {
  id: string
  name: string
  primaryColor: string // 主题色
  primaryLight: string // 浅色背景（用于按钮未激活状态等）
  primaryHover: string // 悬停背景色（半透明）
  primaryShadow: string // 阴影颜色（半透明）
}

export const themes: Record<string, ThemeConfig> = {
  default: {
    id: 'default',
    name: '默认蓝',
    primaryColor: '#409eff',
    primaryLight: '#ecf5ff',
    primaryHover: 'rgba(64, 158, 255, 0.15)',
    primaryShadow: 'rgba(64, 158, 255, 0.4)'
  },
  antdesign: {
    id: 'antdesign',
    name: 'Ant Design 蓝',
    primaryColor: '#1890ff',
    primaryLight: '#e6f7ff',
    primaryHover: 'rgba(24, 144, 255, 0.15)',
    primaryShadow: 'rgba(24, 144, 255, 0.4)'
  },
  柔和蓝: {
    id: '柔和蓝',
    name: '柔和蓝',
    primaryColor: '#5b8ff9',
    primaryLight: '#f0f5ff',
    primaryHover: 'rgba(91, 143, 249, 0.15)',
    primaryShadow: 'rgba(91, 143, 249, 0.4)'
  },
  天蓝: {
    id: '天蓝',
    name: '天蓝',
    primaryColor: '#4facfe',
    primaryLight: '#e0f2fe',
    primaryHover: 'rgba(79, 172, 254, 0.15)',
    primaryShadow: 'rgba(79, 172, 254, 0.4)'
  },
  深宝蓝: {
    id: '深宝蓝',
    name: '深宝蓝',
    primaryColor: '#3b82f6',
    primaryLight: '#eff6ff',
    primaryHover: 'rgba(59, 130, 246, 0.15)',
    primaryShadow: 'rgba(59, 130, 246, 0.4)'
  },
  紫罗兰: {
    id: '紫罗兰',
    name: '紫罗兰',
    primaryColor: '#6366f1',
    primaryLight: '#eef2ff',
    primaryHover: 'rgba(99, 102, 241, 0.15)',
    primaryShadow: 'rgba(99, 102, 241, 0.4)'
  },
  青蓝: {
    id: '青蓝',
    name: '青蓝',
    primaryColor: '#0ea5e9',
    primaryLight: '#e0f2fe',
    primaryHover: 'rgba(14, 165, 233, 0.15)',
    primaryShadow: 'rgba(14, 165, 233, 0.4)'
  }
}

export default themes
