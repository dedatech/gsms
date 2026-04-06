import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useThemeStore } from './stores/theme'
import permissionDirective from './directives/permission'

// 全局样式规范
import '@/styles/page-common.css'
// ONES 风格样式
import '@/styles/ones-theme.css'
import '@/styles/ones-common.css'

const app = createApp(App)
const pinia = createPinia()

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})
app.use(permissionDirective) // 注册权限指令

// 全局错误处理器
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误:', err)
  console.error('错误信息:', info)
  // 可以在这里添加错误上报逻辑
}

// 捕获未处理的Promise错误
window.addEventListener('unhandledrejection', (event) => {
  console.error('未处理的Promise错误:', event.reason)
  // 可以在这里添加错误上报逻辑
})

// 在应用挂载前恢复认证信息
const authStore = useAuthStore()
authStore.restoreAuth()

// 在应用挂载前恢复主题
const themeStore = useThemeStore()
themeStore.restoreTheme()

// 开发环境：自动验证色彩对比度
if (import.meta.env.DEV) {
  import('./utils/colorContrast').then(({ autoValidateThemeInDev }) => {
    autoValidateThemeInDev()
  })
}

app.mount('#app')
