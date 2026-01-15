<template>
  <div class="page-root">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">工时日历</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon today">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">今日工时</div>
              <div class="stats-value">{{ statistics.todayHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon week">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">本周工时</div>
              <div class="stats-value">{{ statistics.weekHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon month">
              <el-icon><List /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">本月工时</div>
              <div class="stats-value">{{ statistics.monthHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">总计工时</div>
              <div class="stats-value">{{ statistics.totalHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工具栏 -->
    <div class="calendar-toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button :icon="ArrowLeft" @click="handlePrevMonth">上个月</el-button>
          <el-button @click="handleToday">今天</el-button>
          <el-button :icon="ArrowRight" @click="handleNextMonth">下个月</el-button>
        </el-button-group>
        <div class="current-month">{{ currentMonthText }}</div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" :icon="Plus" @click="handleBatchCreate">工时填报</el-button>
      </div>
    </div>

    <!-- 日历网格 -->
    <div class="month-view">
      <div class="weekdays">
        <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
      </div>
      <div class="calendar-grid">
        <div
          v-for="cell in calendarCells"
          :key="cell.date"
          class="calendar-cell"
          :class="{
            'today': cell.isToday,
            'weekend': cell.isWeekend,
            'other-month': cell.isOtherMonth,
            'has-data': cell.hasData
          }"
          @click="handleCellClick(cell)"
        >
          <div class="cell-header">
            <span class="date-number">{{ cell.dayNumber }}</span>
            <span v-if="cell.totalHours > 0" class="total-hours">
              <el-icon><Clock /></el-icon>
              {{ cell.totalHours }}h
            </span>
          </div>
          <div class="cell-body">
            <div v-if="cell.workHours.length === 0" class="empty-state">
              <el-icon><Plus /></el-icon>
            </div>
            <div v-else class="work-hours-list">
              <div
                v-for="wh in cell.workHours"
                :key="wh.id"
                class="work-hour-item"
              >
                <div class="wh-hours">{{ wh.hours }}h {{ wh.taskName || '' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 工时批量填报对话框 -->
    <WorkHourBatchDialog
      v-model:visible="batchDialogVisible"
      :date="selectedDate"
      @success="fetchMonthData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Calendar, List, Folder, ArrowLeft, ArrowRight, Plus } from '@element-plus/icons-vue'
import { getMonthRange, getCurrentYearMonth, getMonthText, formatDate, isToday, isWeekend } from '@/utils/dateUtils'
import { getWorkHourList, getUserWorkHourStatistics, type WorkHourInfo } from '@/api/workhour'
import WorkHourBatchDialog from '@/components/WorkHourBatchDialog.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 工时批量填报对话框
const batchDialogVisible = ref(false)
const selectedDate = ref<string>()

// 当前年月
const { year: initYear, month: initMonth } = getCurrentYearMonth()
const currentYear = ref(initYear)
const currentMonth = ref(initMonth)

// 当前月份文本
const currentMonthText = computed(() => getMonthText(currentYear.value, currentMonth.value))

// 星期标题
const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 加载状态
const loading = ref(false)

// 日历单元格接口
interface CalendarCell {
  date: string
  dayNumber: number
  isToday: boolean
  isWeekend: boolean
  isOtherMonth: boolean
  totalHours: number
  hasData: boolean
  workHours: WorkHourInfo[]
}

// 日历单元格
const calendarCells = computed<CalendarCell[]>(() => {
  const cells: CalendarCell[] = []
  const year = currentYear.value
  const month = currentMonth.value

  // 当月第一天和最后一天
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)

  // 获取第一天是周几（0-6，0是周日）
  let dayOfWeek = firstDay.getDay()
  // 转换为周一为1，周日为7
  dayOfWeek = dayOfWeek === 0 ? 7 : dayOfWeek

  // 填充上月剩余日期
  const prevMonthLastDay = new Date(year, month - 1, 0)
  for (let i = dayOfWeek - 1; i > 0; i--) {
    const date = new Date(prevMonthLastDay)
    date.setDate(prevMonthLastDay.getDate() - i + 1)
    const dateStr = formatDate(date)
    cells.push({
      date: dateStr,
      dayNumber: date.getDate(),
      isToday: isToday(dateStr),
      isWeekend: isWeekend(dateStr),
      isOtherMonth: true,
      totalHours: 0,
      hasData: false,
      workHours: []
    })
  }

  // 当月日期
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month - 1, day)
    const dateStr = formatDate(date)
    const dayData = monthData.value.get(dateStr)
    cells.push({
      date: dateStr,
      dayNumber: day,
      isToday: isToday(dateStr),
      isWeekend: isWeekend(dateStr),
      isOtherMonth: false,
      totalHours: dayData?.totalHours || 0,
      hasData: !!dayData?.workHours?.length,
      workHours: dayData?.workHours || []
    })
  }

  // 动态计算是否需要填充下月日期（确保完整显示当前月）
  const cellsCount = cells.length
  const remainder = cellsCount % 7

  // 如果当前月数据不满整行，补齐到完整行
  if (remainder !== 0) {
    const remaining = 7 - remainder
    for (let i = 1; i <= remaining; i++) {
      const date = new Date(year, month, i)
      const dateStr = formatDate(date)
      cells.push({
        date: dateStr,
        dayNumber: i,
        isToday: isToday(dateStr),
        isWeekend: isWeekend(dateStr),
        isOtherMonth: true,
        totalHours: 0,
        hasData: false,
        workHours: []
      })
    }
  }

  return cells
})

// 月度数据（按日期聚合的 Map）
interface DayWorkHours {
  date: string
  totalHours: number
  workHours: WorkHourInfo[]
}
const monthData = ref<Map<string, DayWorkHours>>(new Map())

// 统计数据
const statistics = reactive({
  todayHours: 0,
  weekHours: 0,
  monthHours: 0,
  totalHours: 0
})

// 获取月度数据
const fetchMonthData = async () => {
  loading.value = true
  try {
    const { start, end } = getMonthRange(currentYear.value, currentMonth.value)
    const res = await getWorkHourList({
      startDate: start,
      endDate: end,
      pageNum: 1,
      pageSize: 1000
    })

    // 聚合数据按日期
    const map = new Map<string, DayWorkHours>()
    const workHours = res?.list || []

    workHours.forEach(wh => {
      if (!map.has(wh.workDate)) {
        map.set(wh.workDate, {
          date: wh.workDate,
          totalHours: 0,
          workHours: []
        })
      }
      const day = map.get(wh.workDate)!
      day.totalHours += wh.hours
      day.workHours.push(wh)
    })

    monthData.value = map

    // 更新统计数据
    updateStatistics(workHours)
  } catch (error) {
    console.error('获取工时数据失败:', error)
    ElMessage.error('获取工时数据失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStatistics = async (allWorkHours: WorkHourInfo[]) => {
  try {
    const today = new Date()
    const todayStr = today.toISOString().split('T')[0]

    // 计算今日工时
    const todayData = allWorkHours.filter(wh => wh.workDate === todayStr)
    statistics.todayHours = todayData.reduce((sum, wh) => sum + wh.hours, 0)

    // 计算本周工时
    const weekStart = new Date(today)
    weekStart.setDate(today.getDate() - today.getDay() + 1) // 周一
    const weekStartStr = weekStart.toISOString().split('T')[0]
    const weekData = allWorkHours.filter(wh => wh.workDate >= weekStartStr && wh.workDate <= todayStr)
    statistics.weekHours = weekData.reduce((sum, wh) => sum + wh.hours, 0)

    // 计算本月工时
    const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
    const monthStartStr = monthStart.toISOString().split('T')[0]
    const monthData_filtered = allWorkHours.filter(wh => wh.workDate >= monthStartStr && wh.workDate <= todayStr)
    statistics.monthHours = monthData_filtered.reduce((sum, wh) => sum + wh.hours, 0)

    // 获取总工时
    const currentUserId = authStore.getCurrentUserId()
    const totalRes = await getUserWorkHourStatistics(currentUserId)
    statistics.totalHours = totalRes?.totalHours || 0
  } catch (error) {
    console.error('更新统计数据失败:', error)
  }
}

// 批量工时填报
const handleBatchCreate = () => {
  selectedDate.value = undefined // 不指定日期，使用今天
  batchDialogVisible.value = true
}

// 单元格点击
const handleCellClick = (cell: CalendarCell) => {
  if (cell.isOtherMonth) return

  // 点击单元格打开工时填报，并传入点击的日期
  selectedDate.value = cell.date
  batchDialogVisible.value = true
}

// 回到今天
const handleToday = () => {
  const { year, month } = getCurrentYearMonth()
  currentYear.value = year
  currentMonth.value = month
}

// 上个月
const handlePrevMonth = () => {
  if (currentMonth.value === 1) {
    currentYear.value--
    currentMonth.value = 12
  } else {
    currentMonth.value--
  }
}

// 下个月
const handleNextMonth = () => {
  if (currentMonth.value === 12) {
    currentYear.value++
    currentMonth.value = 1
  } else {
    currentMonth.value++
  }
}

// 监听年月变化
watch([currentYear, currentMonth], () => {
  fetchMonthData()
})

onMounted(() => {
  fetchMonthData()
})
</script>

<style scoped>
/* ========== 工时日历特定样式 ========== */

/* 日历工具栏 */
.calendar-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-month {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  min-width: 100px;
}

/* 月份视图 */
.month-view {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  padding: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  background: #f5f5f5;
  border-radius: 4px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-cell {
  min-height: 120px;
  padding: 8px;
  background: #fff; /* 当前月份默认白色 */
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.calendar-cell:hover {
  box-shadow: 0 0 0 2px #1890ff inset;
}

/* 当前月份的周末：浅蓝色 */
.calendar-cell.weekend:not(.other-month) {
  background: #e6f7ff;
}

/* 非当前月份：浅灰色 */
.calendar-cell.other-month {
  background: #f5f5f5;
  opacity: 0.6;
}

/* 非当前月份的周末：保持灰色（其他月份优先） */
.calendar-cell.other-month.weekend {
  background: #f5f5f5;
  opacity: 0.6;
}

/* 今天：特殊边框 */
.calendar-cell.today {
  border: 2px solid #1890ff;
  box-shadow: 0 0 0 1px #1890ff;
}

/* 有工时数据的日期：浅绿色背景 */
.calendar-cell.has-data:not(.other-month):not(.weekend) {
  background: #f6ffed;
}

.calendar-cell.has-data.weekend:not(.other-month) {
  background: #d9f7be;
}

.cell-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.date-number {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.total-hours {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

.cell-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: #d9d9d9;
  font-size: 24px;
}

.work-hours-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.work-hour-item {
  padding: 4px 6px;
  background: #f5f5f5;
  border-radius: 3px;
  font-size: 12px;
}

.wh-project {
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wh-hours {
  color: #666;
  font-weight: 500;
}
</style>
