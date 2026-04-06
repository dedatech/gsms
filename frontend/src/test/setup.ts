import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// 全局 mock Element Plus 组件
config.global.stubs = {
  'el-icon': true,
  'el-button': true,
  'el-input': true,
  'el-select': true,
  'el-option': true,
  'el-tag': true,
  'el-avatar': true,
  'el-table': true,
  'el-table-column': true,
  'el-pagination': true,
  'el-dialog': true,
  'el-form': true,
  'el-form-item': true,
  'el-date-picker': true,
  'el-progress': true,
  'el-timeline': true,
  'el-timeline-item': true,
  'el-skeleton': true,
  'el-empty': true,
  'el-dropdown': true,
  'el-dropdown-menu': true,
  'el-dropdown-item': true,
  'el-checkbox': true,
  'el-radio-group': true,
  'el-radio-button': true,
  'el-radio': true,
  'el-input-number': true,
  'el-message': true,
  'el-message-box': true,
  'el-link': true,
  'el-row': true,
  'el-col': true,
  'el-alert': true,
  'el-descriptions': true,
  'el-descriptions-item': true,
  'el-collapse': true,
  'el-collapse-item': true,
}

// mock vue-router
vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '1' } }),
  useRouter: () => ({ push: vi.fn() }),
}))

// mock pinia stores
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    getCurrentUserId: () => 1,
    isAuthenticated: true,
    currentUser: { id: 1, username: 'test' },
  }),
}))

vi.mock('@/stores/project', () => ({
  useProjectStore: () => ({
    currentProject: null,
    setCurrentProject: vi.fn(),
  }),
}))
