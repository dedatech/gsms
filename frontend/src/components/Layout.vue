<template>
  <div class="layout">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header>
        <div class="header-content">
          <h1>GSMS 工时管理系统</h1>
          <div class="user-info">
            <span>欢迎，{{ username }}</span>
            <el-button @click="handleLogout" type="text">退出</el-button>
          </div>
        </div>
      </el-header>

      <el-container>
        <!-- 侧边栏菜单 -->
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            router
            class="el-menu-vertical"
          >
            <el-menu-item index="/projects">
              <span>项目管理</span>
            </el-menu-item>
            <el-menu-item index="/tasks">
              <span>任务管理</span>
            </el-menu-item>
            <el-menu-item index="/iterations">
              <span>迭代管理</span>
            </el-menu-item>
            <el-menu-item index="/work-hours">
              <span>工时管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>

        <!-- 主内容区 -->
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 用户名（从 localStorage 获取）
const username = ref(localStorage.getItem('username') || '用户')

// 当前激活的菜单
const activeMenu = computed(() => router.currentRoute.value.path)

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  ElMessage.success('退出成功')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.el-container {
  height: 100%;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.header-content h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.el-aside {
  background-color: #f5f5f5;
}

.el-main {
  background-color: #fff;
  padding: 20px;
}
</style>
