<template>
  <div class="layout">
    <el-container>
      <!-- 侧边栏菜单 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
        <div class="logo-container">
          <img src="@/assets/logo/logo-tm-letters.svg" alt="TeamMaster" class="logo-icon" :width="isCollapse ? 28 : 32" />
          <span v-if="!isCollapse" class="logo-text">TeamMaster</span>
        </div>

        <!-- 动态菜单 -->
        <el-menu
          v-if="menuTree.length > 0"
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          class="sidebar-menu"
        >
          <template v-for="menu in menuTree" :key="menu.id">
            <!-- 目录类型（有子菜单） -->
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="String(menu.id)">
              <template #title>
                <el-icon v-if="menu.icon" :size="18" class="menu-icon">
                  <component :is="iconMap[menu.icon]" />
                </el-icon>
                <span>{{ menu.name }}</span>
              </template>

              <!-- 递归渲染子菜单 -->
              <template v-for="subMenu in menu.children" :key="subMenu.id">
                <el-menu-item v-if="!subMenu.children || subMenu.children.length === 0"
                              :index="subMenu.path">
                  <el-icon v-if="subMenu.icon" :size="18" class="menu-icon">
                    <component :is="iconMap[subMenu.icon]" />
                  </el-icon>
                  <template #title>{{ subMenu.name }}</template>
                </el-menu-item>

                <!-- 三级菜单（如需要） -->
                <el-sub-menu v-else :index="String(subMenu.id)">
                  <template #title>
                    <el-icon v-if="subMenu.icon" :size="18" class="menu-icon">
                      <component :is="iconMap[subMenu.icon]" />
                    </el-icon>
                    <span>{{ subMenu.name }}</span>
                  </template>
                  <el-menu-item v-for="child in subMenu.children"
                                :key="child.id"
                                :index="child.path">
                    <el-icon v-if="child.icon" :size="18" class="menu-icon">
                      <component :is="iconMap[child.icon]" />
                    </el-icon>
                    <template #title>{{ child.name }}</template>
                  </el-menu-item>
                </el-sub-menu>
              </template>
            </el-sub-menu>

            <!-- 菜单项（无子菜单） -->
            <el-menu-item v-else :index="menu.path">
              <el-icon v-if="menu.icon" :size="18" class="menu-icon">
                <component :is="iconMap[menu.icon]" />
              </el-icon>
              <template #title>{{ menu.name }}</template>
            </el-menu-item>
          </template>
        </el-menu>

        <!-- 加载状态 -->
        <div v-else class="menu-loading">
          <el-skeleton :rows="3" animated />
        </div>
      </el-aside>

      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-tooltip
              :content="isCollapse ? '展开菜单' : '收起菜单'"
              placement="bottom"
              :show-after="300"
              :hide-after="100"
              effect="dark"
            >
              <div class="collapse-btn" @click="toggleCollapse">
                <el-icon :size="20">
                  <component :is="iconMap.Fold" v-if="!isCollapse" />
                  <component :is="iconMap.Expand" v-else />
                </el-icon>
              </div>
            </el-tooltip>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item
                v-for="(item, index) in breadcrumbs"
                :key="index"
                :to="index < breadcrumbs.length - 1 ? item.path : undefined"
              >
                {{ item.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <!-- 主题切换 -->
            <el-dropdown trigger="click" @command="handleThemeChange" class="theme-dropdown">
              <div class="theme-selector">
                <el-icon><component :is="iconMap.Brush" /></el-icon>
                <span class="theme-name">{{ themeStore.currentTheme.name }}</span>
                <el-icon><component :is="iconMap.ArrowDown" /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="(theme, key) in themeStore.themes"
                    :key="key"
                    :command="key"
                    :class="{ 'is-active': themeStore.currentThemeId === key }"
                  >
                    <div class="theme-option">
                      <div class="theme-color-preview" :style="{ backgroundColor: theme.primaryColor }"></div>
                      <span>{{ theme.name }}</span>
                    </div>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-dropdown trigger="click" @command="handleUserAction">
              <div class="user-info">
                <el-avatar :size="32" :src="userAvatar">
                  {{ username.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ username }}</span>
                <el-icon><component :is="iconMap.ArrowDown" /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><component :is="iconMap.User" /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><component :is="iconMap.Setting" /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><component :is="iconMap.SwitchButton" /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as ElementPlusIcons from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'
import { getUserMenuTree, type MenuInfo } from '@/api/menu'

const router = useRouter()
const route = useRoute()
const themeStore = useThemeStore()

// 动态菜单树
const menuTree = ref<MenuInfo[]>([])

// 图标映射对象
const iconMap = ElementPlusIcons

// 调试：检查 Setting 图标
console.log('Setting 图标:', iconMap.Setting)
console.log('Operation 图标:', iconMap.Operation)

// 加载用户菜单
const fetchUserMenus = async () => {
  try {
    const menus = await getUserMenuTree()
    menuTree.value = menus
    console.log('菜单加载成功:', menus)
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('加载菜单失败')
  }
}

// 用户名（从 localStorage 获取）
const username = ref(localStorage.getItem('username') || '用户')
const userAvatar = ref('')

// 侧边栏折叠状态（从 localStorage 读取，默认 false）
const isCollapse = ref(localStorage.getItem('sidebarCollapsed') === 'true')

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前路由标题
const currentRouteTitle = computed(() => route.meta.title as string || '')

// 面包屑导航
const breadcrumbs = computed(() => {
  const path = route.path
  const items: Array<{ title: string; path: string }> = []

  // 始终添加首页
  items.push({ title: '首页', path: '/dashboard' })

  // 根据路径生成面包屑
  if (path === '/dashboard') {
    // 首页不添加额外项
    return items
  }

  // 项目管理
  if (path.startsWith('/projects')) {
    if (path === '/projects') {
      items.push({ title: '项目管理', path: '/projects' })
    } else if (path.includes('/gantt')) {
      items.push({ title: '项目管理', path: '/projects' })
      items.push({ title: '项目甘特图', path: '' })
    } else {
      items.push({ title: '项目管理', path: '/projects' })
      items.push({ title: '项目详情', path: '' })
    }
  }
  // 任务中心
  else if (path.startsWith('/tasks')) {
    if (path === '/tasks') {
      items.push({ title: '任务中心', path: '/tasks' })
    } else {
      items.push({ title: '任务中心', path: '/tasks' })
      items.push({ title: '任务详情', path: '' })
    }
  }
  // 工时管理
  else if (path.startsWith('/workhours')) {
    items.push({ title: '工时管理', path: '/workhours/calendar' })
    if (path.includes('calendar')) {
      items.push({ title: '工时日历', path: '' })
    } else if (path.includes('list')) {
      items.push({ title: '工时列表', path: '' })
    }
  }
  // 统计分析
  else if (path.startsWith('/statistics')) {
    items.push({ title: '统计分析', path: '/statistics/project' })
    if (path.includes('project')) {
      items.push({ title: '项目工时统计', path: '' })
    } else if (path.includes('user')) {
      items.push({ title: '用户工时统计', path: '' })
    } else if (path.includes('trend')) {
      items.push({ title: '工时趋势分析', path: '' })
    }
  }
  // 系统管理
  else if (path.startsWith('/system')) {
    items.push({ title: '系统管理', path: '/system/users' })
    if (path.includes('users')) {
      items.push({ title: '用户管理', path: '' })
    } else if (path.includes('departments')) {
      items.push({ title: '部门管理', path: '' })
    } else if (path.includes('roles')) {
      items.push({ title: '角色管理', path: '' })
    } else if (path.includes('permissions')) {
      items.push({ title: '权限管理', path: '' })
    } else if (path.includes('operation-logs')) {
      items.push({ title: '操作日志', path: '' })
    }
  }

  return items
})

// 切换侧边栏
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
  // 保存到 localStorage
  localStorage.setItem('sidebarCollapsed', String(isCollapse.value))
}

// 切换主题
const handleThemeChange = (themeId: string) => {
  themeStore.setTheme(themeId)
  ElMessage.success(`已切换到${themeStore.currentTheme.name}主题`)
}

// 用户操作
const handleUserAction = (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中')
      break
    case 'settings':
      ElMessage.info('系统设置功能开发中')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          ElMessage.success('退出成功')
          router.push('/login')
        })
        .catch(() => {})
      break
  }
}

// 组件挂载时恢复主题并加载菜单
onMounted(async () => {
  await fetchUserMenus()
  themeStore.restoreTheme()
})
</script>

<style scoped>
.layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

/* 侧边栏样式 */
.sidebar {
  background: linear-gradient(180deg, var(--sidebar-gradient-start) 0%, var(--sidebar-gradient-end) 100%);
  transition: width 0.3s, background 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  line-height: 1;
}

.logo-icon {
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
  margin-right: 6px;
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.6)) drop-shadow(0 0 12px var(--theme-primary-shadow));
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}

/* 顶部导航栏样式 */
.header {
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--theme-primary);
  background-color: var(--theme-primary-light);
  border-radius: 6px;
  transition: all 0.3s;
  border: 1px solid var(--theme-primary);
  font-weight: 500;
}

.collapse-btn:hover {
  background-color: var(--theme-primary);
  color: #fff;
  transform: scale(1.05);
  box-shadow: 0 2px 8px var(--theme-primary-shadow);
}

.collapse-btn:active {
  transform: scale(0.95);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.theme-selector {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 12px;
  height: 40px;
  transition: background-color 0.3s;
  border-radius: 6px;
  color: #595959;
  font-size: 14px;
}

.theme-selector:hover {
  background-color: #f5f5f5;
}

.theme-name {
  font-size: 14px;
  color: #595959;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.theme-color-preview {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

:deep(.el-dropdown-menu__item.is-active) {
  background-color: var(--theme-primary-light);
  color: var(--theme-primary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 0 12px;
  height: 48px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #595959;
}

/* 主内容区样式 */
.main-content {
  background-color: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}

/* 路由切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

/* Element Plus 菜单项样式覆盖 */
:deep(.el-menu) {
  background: transparent;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.75);
  transition: all 0.3s;
}

:deep(.el-menu-item:hover) {
  background-color: var(--theme-primary-hover) !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--theme-primary) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px var(--theme-primary-shadow);
}

:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.75);
  transition: all 0.3s;
}

:deep(.el-sub-menu__title:hover) {
  background-color: var(--theme-primary-hover) !important;
  color: #fff !important;
}

/* 子菜单样式 */
:deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.2) !important;
}

/* 菜单图标样式 */
.menu-icon {
  color: #fff;
}

/* 菜单加载状态 */
.menu-loading {
  padding: 20px;
}
</style>
