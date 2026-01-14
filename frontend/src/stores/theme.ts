import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { themes, type ThemeConfig } from '@/config/theme'

const THEME_KEY = 'teamMaster_theme'

export const useThemeStore = defineStore('theme', () => {
  // 当前主题ID
  const currentThemeId = ref<string>('antdesign')

  // 从 localStorage 恢复主题
  const restoreTheme = () => {
    const saved = localStorage.getItem(THEME_KEY)
    if (saved && themes[saved]) {
      currentThemeId.value = saved
    }
  }

  // 获取当前主题配置
  const currentTheme = ref<ThemeConfig>(themes[currentThemeId.value])

  // 应用主题到 CSS 变量
  const applyTheme = (theme: ThemeConfig) => {
    const root = document.documentElement
    root.style.setProperty('--theme-primary', theme.primaryColor)
    root.style.setProperty('--theme-primary-light', theme.primaryLight)
    root.style.setProperty('--theme-primary-hover', theme.primaryHover)
    root.style.setProperty('--theme-primary-shadow', theme.primaryShadow)

    // 同时更新 Element Plus 的主色变量
    root.style.setProperty('--el-color-primary', theme.primaryColor)
    root.style.setProperty('--el-color-primary-light-9', theme.primaryLight)
    root.style.setProperty('--el-color-primary-light-8', theme.primaryLight)

    // 更新 Element Plus 的其他色阶（基于主题色计算）
    const primaryColor = theme.primaryColor
    root.style.setProperty('--el-color-primary-dark-2', primaryColor)
  }

  // 监听主题变化
  watch(currentThemeId, (newId) => {
    if (themes[newId]) {
      currentTheme.value = themes[newId]
      localStorage.setItem(THEME_KEY, newId)
      applyTheme(themes[newId])
    }
  }, { immediate: true })

  // 切换主题
  const setTheme = (themeId: string) => {
    if (themes[themeId]) {
      currentThemeId.value = themeId
    }
  }

  return {
    currentThemeId,
    currentTheme,
    themes,
    setTheme,
    restoreTheme,
    applyTheme
  }
})
