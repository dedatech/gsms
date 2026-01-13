<template>
  <div class="project-statistics">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">项目工时统计</h2>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="项目">
          <el-select
            v-model="searchForm.projectId"
            placeholder="请选择项目"
            clearable
            style="width: 200px"
            @change="handleSearch"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row" v-if="statisticsData">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总工时</div>
              <div class="stat-value">{{ statisticsData.totalHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">工时记录数</div>
              <div class="stat-value">{{ statisticsData.totalRecords || 0 }} 条</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">参与人数</div>
              <div class="stat-value">{{ statisticsData.userCount || 0 }} 人</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户工时分布 -->
    <el-card class="chart-card" v-if="statisticsData && userDistribution.length > 0">
      <template #header>
        <div class="card-header">
          <span class="card-title">用户工时分布</span>
        </div>
      </template>

      <el-table :data="userDistribution" stripe>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="userName" label="用户名称" width="150" />
        <el-table-column label="工时" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="calculatePercentage(row.hours, statisticsData.totalHours)"
              :color="getProgressColor(row.hours, statisticsData.totalHours)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="hours" label="工时数（小时）" width="150" />
        <el-table-column label="占比" width="120">
          <template #default="{ row }">
            {{ calculatePercentage(row.hours, statisticsData.totalHours).toFixed(1) }}%
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 暂无数据提示 -->
    <el-empty v-if="!loading && !statisticsData" description="请选择项目查看统计数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Document, User } from '@element-plus/icons-vue'
import { getProjectWorkHourStatistics } from '@/api/statistics'
import { getProjectList } from '@/api/project'

interface StatisticsData {
  totalHours: number
  totalRecords: number
  userCount: number
  userHoursDistribution: Record<number, number>
  startDate?: string
  endDate?: string
}

interface UserDistributionItem {
  userId: number
  userName: string
  hours: number
}

const loading = ref(false)
const statisticsData = ref<StatisticsData | null>(null)
const userDistribution = ref<UserDistributionItem[]>([])
const projects = ref<any[]>([])

const searchForm = reactive({
  projectId: undefined as number | undefined,
  startDate: '',
  endDate: ''
})

const dateRange = ref<string[]>([])

// 获取项目列表
const fetchProjects = async () => {
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 1000 })
    projects.value = res.list || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  if (!searchForm.projectId) {
    statisticsData.value = null
    userDistribution.value = []
    return
  }

  loading.value = true
  try {
    const data = await getProjectWorkHourStatistics(
      searchForm.projectId,
      searchForm.startDate || undefined,
      searchForm.endDate || undefined
    )

    statisticsData.value = data

    // 转换用户工时分布数据
    if (data.userHoursDistribution && typeof data.userHoursDistribution === 'object') {
      const distribution: UserDistributionItem[] = []

      // 需要获取用户信息，这里先用userId显示
      for (const [userId, hours] of Object.entries(data.userHoursDistribution)) {
        distribution.push({
          userId: parseInt(userId),
          userName: `用户${userId}`, // 实际应该从用户列表获取
          hours: hours as number
        })
      }

      // 按工时降序排序
      distribution.sort((a, b) => b.hours - a.hours)
      userDistribution.value = distribution
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  fetchStatistics()
}

// 重置
const handleReset = () => {
  searchForm.projectId = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  dateRange.value = []
  statisticsData.value = null
  userDistribution.value = []
}

// 日期范围变化
const handleDateChange = (value: string[]) => {
  if (value && value.length === 2) {
    searchForm.startDate = value[0]
    searchForm.endDate = value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
  if (searchForm.projectId) {
    fetchStatistics()
  }
}

// 计算百分比
const calculatePercentage = (value: number, total: number): number => {
  if (!total) return 0
  return (value / total) * 100
}

// 获取进度条颜色
const getProgressColor = (value: number, total: number): string => {
  const percentage = calculatePercentage(value, total)
  if (percentage >= 50) return '#409eff'
  if (percentage >= 30) return '#67c23a'
  if (percentage >= 10) return '#e6a23c'
  return '#f56c6c'
}

onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.project-statistics {
  min-height: calc(100vh - 160px);
}

.page-header {
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.filter-card {
  margin-bottom: 16px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.chart-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #f5f5f5;
  color: #333;
  font-weight: 500;
}
</style>
