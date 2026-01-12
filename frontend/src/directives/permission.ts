import type { Directive, DirectiveBinding } from 'vue'
import { useAuthStore } from '@/stores/auth'

/**
 * 权限指令
 * 用法: v-permission="'USER_CREATE'" 或 v-permission="['USER_EDIT', 'USER_DELETE']"
 */
export const permission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    const { value } = binding
    const authStore = useAuthStore()

    if (value) {
      // 检查是否有权限
      let hasPermission = false

      if (typeof value === 'string') {
        // 单个权限
        hasPermission = authStore.hasPermission(value)
      } else if (Array.isArray(value) && value.length > 0) {
        // 多个权限（满足其一即可）
        hasPermission = authStore.hasAnyPermission(value)
      }

      if (!hasPermission) {
        // 移除元素
        el.parentNode?.removeChild(el)
      }
    } else {
      throw new Error('v-permission 指令需要权限码参数')
    }
  }
}

export default {
  install(app: any) {
    app.directive('permission', permission)
  }
}
