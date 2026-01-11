<template>
  <div class="month-view">
    <!-- 星期标题 -->
    <div class="weekdays">
      <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
    </div>

    <!-- 日历网格 -->
    <div class="calendar-grid" v-loading="loading">
      <CalendarCell
        v-for="cell in calendarCells"
        :key="cell.date"
        :date="cell.date"
        :is-other-month="cell.isOtherMonth"
        :work-hours="cell.workHours || []"
        :show-status="false"
        @dblclick="handleCellDblClick(cell)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import CalendarCell from './CalendarCell.vue'
import { formatDate } from '@/utils/dateUtils'
import type { WorkHourInfo } from '@/api/workhour'

const props = defineProps<{
  currentYear: number
  currentMonth: number
  monthData: Map<string, { date: string; totalHours: number; workHours: WorkHourInfo[] }>
  loading: boolean
}>()

const emit = defineEmits<{
  (e: 'cellDblClick', date: string, workHours: WorkHourInfo[]): void
}>()

const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 日历单元格接口
interface CalendarCell {
  date: string
  isOtherMonth: boolean
  workHours?: WorkHourInfo[]
}

// 计算日历单元格（42格：6行×7列）
const calendarCells = computed<CalendarCell[]>(() => {
  const cells: CalendarCell[] = []
  const year = props.currentYear
  const month = props.currentMonth

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
      isOtherMonth: true,
      workHours: []
    })
  }

  // 当月日期
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month - 1, day)
    const dateStr = formatDate(date)
    const dayData = props.monthData.get(dateStr)
    cells.push({
      date: dateStr,
      isOtherMonth: false,
      workHours: dayData?.workHours || []
    })
  }

  // 填充下月开始日期（补齐到42格）
  const remaining = 42 - cells.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month, i)
    const dateStr = formatDate(date)
    cells.push({
      date: dateStr,
      isOtherMonth: true,
      workHours: []
    })
  }

  return cells
})

// 处理单元格双击
const handleCellDblClick = (cell: CalendarCell) => {
  if (cell.isOtherMonth) return // 非本月日期不处理

  emit('cellDblClick', cell.date, cell.workHours || [])
}
</script>

<style scoped>
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
</style>
