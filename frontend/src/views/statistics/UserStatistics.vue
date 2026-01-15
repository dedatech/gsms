<template>
  <div class="page-root">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">用户工时统计</h2>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="用户">
          <el-select
            v-model="searchForm.userId"
            placeholder="请选择用户"
            clearable
            filterable
            style="width: 200px"
            @change="handleSearch"
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
              <el-icon :size="32"><Folder /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">参与项目</div>
              <div class="stat-value">{{ statisticsData.projectCount || 0 }} 个</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 项目工时分布 -->
    <el-card class="chart-card" v-if="statisticsData && projectDistribution.length > 0">
      <template #header>
        <div class="card-header">
          <span class="card-title">项目工时分布</span>
        </div>
      </template>

      <el-table :data="projectDistribution" stripe>
        <el-table-column type="index" label="排名" width="80" />
        <el-table-column prop="projectName" label="项目名称" width="200" />
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
    <el-empty v-if="!loading && !statisticsData" description="请选择用户查看统计数据" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Document, Folder } from '@element-plus/icons-vue'
import { getUserWorkHourStatistics } from '@/api/statistics'
import { getUserList } from '@/api/user'

interface StatisticsData {
  totalHours: number
  totalRecords: number
  projectCount: number
  projectHoursDistribution: Record<number, number>
  startDate?: string
  endDate?: string
}

interface ProjectDistributionItem {
  projectId: number
  projectName: string
  hours: number
}

const loading = ref(false)
const statisticsData = ref<StatisticsData | null>(null)
const projectDistribution = ref<ProjectDistributionItem[]>([])
const users = ref<any[]>([])

const searchForm = reactive({
  userId: undefined as number | undefined,
  startDate: '',
  endDate: ''
})

const dateRange = ref<string[]>([])

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
  if (!searchForm.userId) {
    statisticsData.value = null
    projectDistribution.value = []
    return
  }

  loading.value = true
  try {
    const data = await getUserWorkHourStatistics(
      searchForm.userId,
      searchForm.startDate || undefined,
      searchForm.endDate || undefined
    )

    statisticsData.value = data

    // 转换项目工时分布数据
    if (data.projectHoursDistribution && typeof data.projectHoursDistribution === 'object') {
      const distribution: ProjectDistributionItem[] = []

      for (const [projectId, hours] of Object.entries(data.projectHoursDistribution)) {
        distribution.push({
          projectId: parseInt(projectId),
          projectName: `项目${projectId}`, // 实际应该从项目列表获取
          hours: hours as number
        })
      }

      // 按工时降序排序
      distribution.sort((a, b) => b.hours - a.hours)
      projectDistribution.value = distribution
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
  searchForm.userId = undefined
  searchForm.startDate = ''
  searchForm.endDate = ''
  dateRange.value = []
  statisticsData.value = null
  projectDistribution.value = []
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
  if (searchForm.userId) {
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
  fetchUsers()
})
</script>

<style scoped>
/* ========== 用户统计特定样式 ========== */

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
</style>
