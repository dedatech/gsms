<template>
  <div class="layout">
    <el-container>
      <!-- 侧边栏菜单 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
        <div class="logo-container">
          <el-icon class="logo-icon" :size="isCollapse ? 24 : 20"><FolderOpened /></el-icon>
          <span v-if="!isCollapse" class="logo-text">GSMS</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/projects">
            <el-icon><FolderOpened /></el-icon>
            <template #title>项目管理</template>
          </el-menu-item>
          <el-menu-item index="/tasks">
            <el-icon><List /></el-icon>
            <template #title>任务中心</template>
          </el-menu-item>
          <el-sub-menu index="workhours">
            <template #title>
              <el-icon><Clock /></el-icon>
              <span>工时管理</span>
            </template>
            <el-menu-item index="/workhours/calendar">工时日历</el-menu-item>
            <el-menu-item index="/workhours/list">工时列表</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Operation /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/users">用户管理</el-menu-item>
            <el-menu-item index="/system/roles">角色管理</el-menu-item>
            <el-menu-item index="/system/permissions">权限管理</el-menu-item>
            <el-menu-item index="/system/operation-logs">操作日志</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-icon" @click="toggleCollapse">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRouteTitle">{{ currentRouteTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <el-dropdown trigger="click" @command="handleUserAction">
              <div class="user-info">
                <el-avatar :size="32" :src="userAvatar">
                  {{ username.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人信息
                  </el-dropdown-item>
                  <el-dropdown-item command="settings">
                    <el-icon><Setting /></el-icon>
                    系统设置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
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
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Odometer,
  FolderOpened,
  List,
  Refresh,
  Clock,
  Fold,
  Expand,
  ArrowDown,
  User,
  Setting,
  SwitchButton,
  Operation
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 用户名（从 localStorage 获取）
const username = ref(localStorage.getItem('username') || '用户')
const userAvatar = ref('')

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 当前路由标题
const currentRouteTitle = computed(() => route.meta.title as string || '')

// 切换侧边栏
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
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
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #002140;
  color: #fff;
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.logo-icon {
  font-size: 24px;
  color: #fff;
  margin: 0 auto;
}

.sidebar-menu {
  border-right: none;
  background-color: #001529;
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

.collapse-icon {
  font-size: 20px;
  cursor: pointer;
  color: #595959;
  transition: color 0.3s;
}

.collapse-icon:hover {
  color: #1890ff;
}

.header-right {
  display: flex;
  align-items: center;
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
  background-color: #001529;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.el-menu-item:hover) {
  background-color: #1890ff !important;
  color: #fff !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
  color: #fff !important;
}

:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.el-sub-menu__title:hover) {
  background-color: #1890ff !important;
  color: #fff !important;
}
</style>
