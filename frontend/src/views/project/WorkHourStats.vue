<template>
  <div class="workhour-stats">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 统计内容 -->
    <div v-else class="stats-content">
      <!-- 统计筛选器 -->
      <div class="stats-filters">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 300px"
          @change="handleFilterChange"
        />
        <el-select
          v-model="selectedMemberId"
          placeholder="全部成员"
          clearable
          style="width: 180px"
          @change="handleFilterChange"
        >
          <el-option label="全部成员" :value="undefined" />
          <el-option
            v-for="member in members"
            :key="member.userId"
            :label="member.nickname"
            :value="member.userId"
          >
            <div class="member-option">
              <el-avatar :size="20">{{ member.nickname?.charAt(0) }}</el-avatar>
              <span>{{ member.nickname }}</span>
            </div>
          </el-option>
        </el-select>
      </div>

      <!-- 统计概览卡片 -->
      <div class="summary-cards">
        <div class="summary-card">
          <div class="card-icon" style="background: #e6f7ff; color: #1890ff;">
            <el-icon :size="28"><Clock /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-value">{{ summary.totalHours }}</div>
            <div class="card-label">总工时</div>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon" style="background: #f6ffed; color: #52c41a;">
            <el-icon :size="28"><TrendCharts /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-value">{{ summary.avgHours }}</div>
            <div class="card-label">人均工时</div>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon" style="background: #fff7e6; color: #faad14;">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-value">{{ summary.activeMembers }}</div>
            <div class="card-label">活跃成员</div>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon" style="background: #fff0f6; color: #eb2f96;">
            <el-icon :size="28"><Calendar /></el-icon>
          </div>
          <div class="card-content">
            <div class="card-value">{{ summary.workDays }}</div>
            <div class="card-label">工作日数</div>
          </div>
        </div>
      </div>

      <!-- 成员工时排行 -->
      <div class="member-ranking">
        <h3 class="section-title">成员贡献度排行</h3>
        <div class="ranking-list">
          <div
            v-for="(item, index) in memberRanking"
            :key="item.userId"
            class="ranking-item"
          >
            <div class="ranking-number" :class="`rank-${index + 1}`">{{ index + 1 }}</div>
            <el-avatar :size="36">{{ item.nickname?.charAt(0) }}</el-avatar>
            <div class="member-info">
              <div class="member-name">{{ item.nickname }}</div>
              <div class="member-stats">
                <span>{{ item.taskCount }} 个任务</span>
                <span>{{ item.workDays }} 天工作</span>
              </div>
            </div>
            <div class="member-hours">
              <div class="hours-value">{{ item.totalHours }}</div>
              <div class="hours-unit">小时</div>
            </div>
            <!-- 进度条 -->
            <div class="hours-bar">
              <div
                class="hours-bar-fill"
                :style="{ width: (item.totalHours / maxHours * 100) + '%' }"
              ></div>
            </div>
          </div>
          <el-empty v-if="memberRanking.length === 0" description="暂无工时数据" :image-size="80" />
        </div>
      </div>

      <!-- 工时趋势图（简化版，使用柱状图表示） -->
      <div class="hour-trend">
        <h3 class="section-title">工时趋势（最近30天）</h3>
        <div class="trend-chart-container">
          <div class="trend-chart">
            <div
              v-for="item in dailyTrend"
              :key="item.date"
              class="trend-bar-wrapper"
            >
              <div class="trend-bar-container">
                <div
                  class="trend-bar"
                  :style="{ height: (item.hours / maxDailyHours * 100) + '%' }"
                  :title="`${item.date}: ${item.hours}h`"
                ></div>
              </div>
              <div class="trend-label">{{ formatTrendDate(item.date) }}</div>
              <div class="trend-value">{{ item.hours }}h</div>
            </div>
            <el-empty v-if="dailyTrend.length === 0" description="暂无趋势数据" :image-size="80" />
          </div>
        </div>
      </div>

      <!-- 任务工时分布 -->
      <div class="task-distribution">
        <h3 class="section-title">任务工时分布</h3>
        <div class="distribution-chart">
          <div
            v-for="(item, index) in taskDistribution"
            :key="item.taskId"
            class="distribution-item"
          >
            <div class="distribution-bar-wrapper">
              <div class="distribution-info">
                <span class="task-name">{{ item.taskName }}</span>
                <span class="task-hours">{{ item.totalHours }}h</span>
              </div>
              <div class="distribution-bar">
                <div
                  class="distribution-bar-fill"
                  :style="{
                    width: (item.totalHours / maxTaskHours * 100) + '%',
                    backgroundColor: getChartColor(index)
                  }"
                ></div>
              </div>
            </div>
          </div>
          <el-empty v-if="taskDistribution.length === 0" description="暂无任务分布数据" :image-size="80" />
        </div>
      </div>

      <!-- 工时明细列表 -->
      <div class="hour-details">
        <h3 class="section-title">工时明细</h3>
        <el-table
          :data="workHourList"
          stripe
          style="width: 100%"
          :height="400"
        >
          <el-table-column prop="workDate" label="日期" width="120" />
          <el-table-column label="成员" width="120">
            <template #default="{ row }">
              <div class="user-cell">
                <el-avatar :size="24">{{ (row.userName || 'U')?.charAt(0) }}</el-avatar>
                <span>{{ row.userName || '未知用户' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="taskName" label="关联任务" min-width="200">
            <template #default="{ row }">
              <span class="task-link">{{ row.taskName || '无关联任务' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="hours" label="工时数" width="100">
            <template #default="{ row }">
              <span class="hours-value">{{ row.hours }}h</span>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="工作内容" min-width="250" show-overflow-tooltip />
        </el-table>
        <div class="table-pagination">
          <el-pagination
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchWorkHours"
            @current-change="fetchWorkHours"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Clock,
  TrendCharts,
  User,
  Calendar
} from '@element-plus/icons-vue'
import { getProjectMembers, type ProjectMember } from '@/api/project'
import { getWorkHourList, type WorkHourInfo } from '@/api/workhour'

// Props
const props = defineProps<{
  projectId: number
}>()

// 数据定义
const loading = ref(false)
const members = ref<ProjectMember[]>([])
const workHourList = ref<WorkHourInfo[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

// 筛选条件
const dateRange = ref<[string, string]>([])
const selectedMemberId = ref<number | undefined>(undefined)

// 统计概览
const summary = reactive({
  totalHours: 0,
  avgHours: 0,
  activeMembers: 0,
  workDays: 0
})

// 成员排行
const memberRanking = ref<any[]>([])

// 每日趋势
const dailyTrend = ref<any[]>([])

// 任务工时分布
const taskDistribution = ref<any[]>([])

// 图表颜色
const chartColors = [
  '#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1',
  '#eb2f96', '#13c2c2', '#2f54eb', '#fa8c16', '#a0d911'
]

// 计算属性
const maxHours = computed(() => {
  return Math.max(...memberRanking.value.map(m => m.totalHours), 1)
})

const maxDailyHours = computed(() => {
  return Math.max(...dailyTrend.value.map(d => d.hours), 1)
})

const maxTaskHours = computed(() => {
  return Math.max(...taskDistribution.value.map(t => t.totalHours), 1)
})

// 获取项目成员
const fetchMembers = async () => {
  try {
    const res = await getProjectMembers(props.projectId)
    members.value = res || []
  } catch (error) {
    console.error('获取成员列表失败:', error)
  }
}

// 获取工时列表
const fetchWorkHours = async () => {
  loading.value = true
  try {
    const res = await getWorkHourList({
      projectId: props.projectId,
      userId: selectedMemberId.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    workHourList.value = res?.list || []
    total.value = res?.total || 0

    // 计算统计数据
    calculateStats()
  } catch (error) {
    console.error('获取工时数据失败:', error)
    ElMessage.error('获取工时数据失败')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStats = () => {
  const list = workHourList.value

  // 总工时
  summary.totalHours = list.reduce((sum, item) => sum + item.hours, 0)

  // 活跃成员数
  const uniqueUsers = new Set(list.map(item => item.userId))
  summary.activeMembers = uniqueUsers.size

  // 工作日数
  const uniqueDates = new Set(list.map(item => item.workDate))
  summary.workDays = uniqueDates.size

  // 人均工时
  summary.avgHours = summary.activeMembers > 0
    ? Math.round(summary.totalHours / summary.activeMembers)
    : 0

  // 成员排行
  const memberMap = new Map<number, any>()
  list.forEach(item => {
    if (!memberMap.has(item.userId)) {
      memberMap.set(item.userId, {
        userId: item.userId,
        nickname: item.userName,
        totalHours: 0,
        taskCount: 0,
        workDays: new Set<string>()
      })
    }
    const member = memberMap.get(item.userId)!
    member.totalHours += item.hours
    member.workDays.add(item.workDate)
  })

  // 统计每个成员的任务数（去重）
  const taskSet = new Map<number, Set<number>>()
  list.forEach(item => {
    if (!taskSet.has(item.userId)) {
      taskSet.set(item.userId, new Set())
    }
    taskSet.get(item.userId)!.add(item.taskId)
  })

  memberRanking.value = Array.from(memberMap.values())
    .map(member => ({
      ...member,
      taskCount: taskSet.get(member.userId)?.size || 0,
      workDays: member.workDays.size
    }))
    .sort((a, b) => b.totalHours - a.totalHours)

  // 每日趋势
  const dateMap = new Map<string, number>()
  list.forEach(item => {
    const hours = dateMap.get(item.workDate) || 0
    dateMap.set(item.workDate, hours + item.hours)
  })

  dailyTrend.value = Array.from(dateMap.entries())
    .map(([date, hours]) => ({ date, hours }))
    .sort((a, b) => a.date.localeCompare(b.date))
    .slice(-30) // 只显示最近30天

  // 任务工时分布
  const taskMap = new Map<number, any>()
  list.forEach(item => {
    if (!taskMap.has(item.taskId)) {
      taskMap.set(item.taskId, {
        taskId: item.taskId,
        taskName: item.taskName || `任务 #${item.taskId}`,
        totalHours: 0
      })
    }
    taskMap.get(item.taskId)!.totalHours += item.hours
  })

  taskDistribution.value = Array.from(taskMap.values())
    .sort((a, b) => b.totalHours - a.totalHours)
    .slice(0, 10) // 只显示前10个任务
}

// 筛选变更
const handleFilterChange = () => {
  pageNum.value = 1
  fetchWorkHours()
}

// 格式化趋势日期
const formatTrendDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 获取图表颜色
const getChartColor = (index: number) => {
  return chartColors[index % chartColors.length]
}

// 生命周期
onMounted(async () => {
  await fetchMembers()
  await fetchWorkHours()
})
</script>

<style scoped>
.workhour-stats {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background: #f5f5f5;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计筛选器 */
.stats-filters {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  gap: 16px;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.member-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计概览卡片 */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.summary-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
}

.card-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

/* 成员贡献度排行 */
.member-ranking {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  position: relative;
}

.ranking-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
  background: #d9d9d9;
  color: #666;
  flex-shrink: 0;
}

.ranking-number.rank-1 {
  background: #ffd700;
  color: #fff;
}

.ranking-number.rank-2 {
  background: #c0c0c0;
  color: #fff;
}

.ranking-number.rank-3 {
  background: #cd7f32;
  color: #fff;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.member-stats {
  font-size: 13px;
  color: #999;
  display: flex;
  gap: 12px;
}

.member-hours {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  margin-right: 12px;
}

.hours-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.hours-unit {
  font-size: 12px;
  color: #999;
}

.hours-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #f0f0f0;
  border-radius: 0 0 8px 8px;
  overflow: hidden;
}

.hours-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #1890ff 0%, #52c41a 100%);
  transition: width 0.5s;
}

/* 工时趋势 */
.hour-trend {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.trend-chart-container {
  margin-top: 16px;
}

.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 200px;
  padding-top: 20px;
  overflow-x: auto;
}

.trend-bar-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 40px;
}

.trend-bar-container {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
}

.trend-bar {
  width: 100%;
  background: linear-gradient(180deg, #1890ff 0%, #69c0ff 100%);
  border-radius: 4px 4px 0 0;
  transition: height 0.3s;
  min-height: 4px;
}

.trend-label {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.trend-value {
  margin-top: 4px;
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

/* 任务工时分布 */
.task-distribution {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.distribution-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.distribution-item {
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
}

.distribution-bar-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.distribution-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.task-hours {
  font-size: 16px;
  color: #1890ff;
  font-weight: 600;
}

.distribution-bar {
  width: 100%;
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.distribution-bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.5s;
}

/* 工时明细 */
.hour-details {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-link {
  color: #1890ff;
  cursor: pointer;
}

.task-link:hover {
  text-decoration: underline;
}

.table-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
