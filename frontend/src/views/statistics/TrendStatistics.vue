<template>
  <div class="page-root">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">工时趋势分析</h2>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="项目">
          <el-select
            v-model="searchForm.projectId"
            placeholder="全部项目"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="用户">
          <el-select
            v-model="searchForm.userId"
            placeholder="全部用户"
            clearable
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.username"
              :value="user.id"
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
      <el-col :span="24">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">趋势总览</span>
            </div>
          </template>

          <el-row :gutter="16">
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-label">统计周期</div>
                <div class="summary-value">
                  {{ statisticsData.startDate }} 至 {{ statisticsData.endDate }}
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-label">总工时</div>
                <div class="summary-value highlight">{{ statisticsData.totalHours || 0 }} 小时</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="summary-item">
                <div class="summary-label">平均每日工时</div>
                <div class="summary-value">
                  {{ averageHoursPerDay.toFixed(1) }} 小时
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-card class="chart-card" v-if="statisticsData && trendData.length > 0">
      <template #header>
        <div class="card-header">
          <span class="card-title">工时趋势图</span>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="table">表格视图</el-radio-button>
            <el-radio-button label="chart">图表视图</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 表格视图 -->
      <el-table v-if="viewMode === 'table'" :data="trendData" stripe>
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column label="工时" width="300">
          <template #default="{ row }">
            <el-progress
              :percentage="calculatePercentage(row.hours, maxHours)"
              :color="getProgressColor(row.hours, maxHours)"
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

      <!-- 图表视图 -->
      <div v-else class="chart-container">
        <div class="chart-bars">
          <div
            v-for="(item, index) in trendData"
            :key="index"
            class="chart-bar-item"
          >
            <div class="bar-wrapper">
              <div
                class="bar"
                :style="{
                  height: calculateBarHeight(item.hours, maxHours) + '%',
                  backgroundColor: getBarColor(item.hours)
                }"
              >
                <span class="bar-label">{{ item.hours }}h</span>
              </div>
            </div>
            <div class="bar-date">{{ formatDate(item.date) }}</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 暂无数据提示 -->
    <el-empty v-if="!loading && !statisticsData" description="请选择日期范围查看趋势数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWorkHourTrendStatistics } from '@/api/statistics'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'

interface TrendDataItem {
  date: string
  hours: number
}

interface StatisticsData {
  totalHours: number
  startDate: string
  endDate: string
  trendData: TrendDataItem[]
}

const loading = ref(false)
const statisticsData = ref<StatisticsData | null>(null)
const trendData = ref<TrendDataItem[]>([])
const projects = ref<any[]>([])
const users = ref<any[]>([])
const viewMode = ref<'table' | 'chart'>('chart')

const searchForm = reactive({
  projectId: undefined as number | undefined,
  userId: undefined as number | undefined,
  startDate: '',
  endDate: ''
})

const dateRange = ref<string[]>([])

// 计算平均每日工时
const averageHoursPerDay = computed(() => {
  if (!statisticsData.value || !trendData.value.length) return 0
  return statisticsData.value.totalHours / trendData.value.length
})

// 计算最大工时值
const maxHours = computed(() => {
  if (!trendData.value.length) return 0
  return Math.max(...trendData.value.map(item => item.hours))
})

// 获取项目列表
const fetchProjects = async () => {
  try {
    const res = await getProjectList({ pageNum: 1, pageSize: 1000 })
    projects.value = res.list || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    const res = await getUserList({ pageNum: 1, pageSize: 1000 })
    users.value = res.list || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  if (!searchForm.startDate || !searchForm.endDate) {
    ElMessage.warning('请选择日期范围')
    return
  }

  loading.value = true
  try {
    const data = await getWorkHourTrendStatistics({
      projectId: searchForm.projectId,
      userId: searchForm.userId,
      startDate: searchForm.startDate,
      endDate: searchForm.endDate
    })

    statisticsData.value = data
    trendData.value = data.trendData || []
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }

  searchForm.startDate = dateRange.value[0]
  searchForm.endDate = dateRange.value[1]
  fetchStatistics()
}

// 重置
const handleReset = () => {
  searchForm.projectId = undefined
  searchForm.userId = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  dateRange.value = []
  statisticsData.value = null
  trendData.value = []
}

// 计算百分比
const calculatePercentage = (value: number, total: number): number => {
  if (!total) return 0
  return (value / total) * 100
}

// 计算柱状图高度
const calculateBarHeight = (value: number, max: number): number => {
  if (!max) return 0
  return (value / max) * 100
}

// 获取进度条颜色
const getProgressColor = (value: number, total: number): string => {
  const percentage = calculatePercentage(value, total)
  if (percentage >= 50) return '#409eff'
  if (percentage >= 30) return '#67c23a'
  if (percentage >= 10) return '#e6a23c'
  return '#f56c6c'
}

// 获取柱状图颜色
const getBarColor = (hours: number): string => {
  if (hours >= 8) return '#67c23a'
  if (hours >= 4) return '#409eff'
  if (hours >= 2) return '#e6a23c'
  return '#f56c6c'
}

// 格式化日期
const formatDate = (dateStr: string): string => {
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}/${day}`
}

onMounted(() => {
  fetchProjects()
  fetchUsers()

  // 默认选择最近7天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 6)

  const formatDate = (date: Date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }

  dateRange.value = [formatDate(startDate), formatDate(endDate)]
  searchForm.startDate = dateRange.value[0]
  searchForm.endDate = dateRange.value[1]

  // 自动加载最近7天的数据
  fetchStatistics()
})
</script>

<style scoped>
/* ========== 趋势统计特定样式 ========== */

.summary-item {
  padding: 16px;
  text-align: center;
}

.summary-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.summary-value.highlight {
  font-size: 24px;
  color: #409eff;
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

.chart-container {
  padding: 20px 0;
}

.chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
  height: 300px;
}

.chart-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-wrapper {
  width: 100%;
  height: 250px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  position: relative;
}

.bar {
  width: 100%;
  min-height: 2px;
  border-radius: 4px 4px 0 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
  transition: all 0.3s;
  position: relative;
}

.bar:hover {
  opacity: 0.8;
  transform: scaleY(1.02);
  transform-origin: bottom;
}

.bar-label {
  font-size: 12px;
  color: #fff;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.bar-date {
  font-size: 12px;
  color: #666;
  text-align: center;
}
</style>
