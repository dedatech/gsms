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
  sidebarGradientStart: string // 侧边栏渐变起始色
  sidebarGradientEnd: string // 侧边栏渐变结束色
}

export const themes: Record<string, ThemeConfig> = {
  default: {
    id: 'default',
    name: '默认蓝',
    primaryColor: '#409eff',
    primaryLight: '#ecf5ff',
    primaryHover: 'rgba(64, 158, 255, 0.15)',
    primaryShadow: 'rgba(64, 158, 255, 0.4)',
    sidebarGradientStart: '#003a8c',
    sidebarGradientEnd: '#001529'
  },
  antdesign: {
    id: 'antdesign',
    name: 'Ant Design 蓝',
    primaryColor: '#1890ff',
    primaryLight: '#e6f7ff',
    primaryHover: 'rgba(24, 144, 255, 0.15)',
    primaryShadow: 'rgba(24, 144, 255, 0.4)',
    sidebarGradientStart: '#002c66',
    sidebarGradientEnd: '#001529'
  },
  柔和蓝: {
    id: '柔和蓝',
    name: '柔和蓝',
    primaryColor: '#5b8ff9',
    primaryLight: '#f0f5ff',
    primaryHover: 'rgba(91, 143, 249, 0.15)',
    primaryShadow: 'rgba(91, 143, 249, 0.4)',
    sidebarGradientStart: '#1d39c4',
    sidebarGradientEnd: '#09204b'
  },
  天蓝: {
    id: '天蓝',
    name: '天蓝',
    primaryColor: '#4facfe',
    primaryLight: '#e0f2fe',
    primaryHover: 'rgba(79, 172, 254, 0.15)',
    primaryShadow: 'rgba(79, 172, 254, 0.4)',
    sidebarGradientStart: '#0050b3',
    sidebarGradientEnd: '#001529'
  },
  深宝蓝: {
    id: '深宝蓝',
    name: '深宝蓝',
    primaryColor: '#3b82f6',
    primaryLight: '#eff6ff',
    primaryHover: 'rgba(59, 130, 246, 0.15)',
    primaryShadow: 'rgba(59, 130, 246, 0.4)',
    sidebarGradientStart: '#1e3a8a',
    sidebarGradientEnd: '#0f172a'
  },
  紫罗兰: {
    id: '紫罗兰',
    name: '紫罗兰',
    primaryColor: '#6366f1',
    primaryLight: '#eef2ff',
    primaryHover: 'rgba(99, 102, 241, 0.15)',
    primaryShadow: 'rgba(99, 102, 241, 0.4)',
    sidebarGradientStart: '#312e81',
    sidebarGradientEnd: '#1e1b4b'
  },
  青蓝: {
    id: '青蓝',
    name: '青蓝',
    primaryColor: '#0ea5e9',
    primaryLight: '#e0f2fe',
    primaryHover: 'rgba(14, 165, 233, 0.15)',
    primaryShadow: 'rgba(14, 165, 233, 0.4)',
    sidebarGradientStart: '#0c4a6e',
    sidebarGradientEnd: '#082f49'
  }
}

export default themes
