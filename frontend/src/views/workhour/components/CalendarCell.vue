<template>
  <div
    class="calendar-cell"
    :class="{
      'today': isToday,
      'weekend': isWeekend,
      'other-month': isOtherMonth,
      'has-data': hasData
    }"
    @dblclick="handleDblClick"
  >
    <div class="cell-header">
      <span class="date-number">{{ dayNumber }}</span>
      <span v-if="totalHours > 0" class="total-hours">
        <el-icon><Clock /></el-icon>
        {{ totalHours }}h
      </span>
    </div>

    <div class="cell-body">
      <div v-if="workHours.length === 0" class="empty-state">
        <el-icon><Plus /></el-icon>
      </div>
      <div v-else class="work-hours-list">
        <div
          v-for="wh in workHours"
          :key="wh.id"
          class="work-hour-item"
          :title="`${wh.hours}小时${wh.taskName ? ' - ' + wh.taskName : ''}`"
        >
          <div class="wh-info">
            <span class="wh-hours">{{ wh.hours }}h {{ wh.taskName || '' }}</span>
            <el-tag v-if="showStatus" :type="getStatusType(wh.status)" size="small">
              {{ getStatusText(wh.status) }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Clock, Plus } from '@element-plus/icons-vue'
import { isToday as checkIsToday, isWeekend as checkIsWeekend } from '@/utils/dateUtils'
import { getWorkHourStatusInfo } from '@/utils/statusMapping'
import type { WorkHourInfo } from '@/api/workhour'

const props = defineProps<{
  date: string // YYYY-MM-DD
  isOtherMonth: boolean
  workHours: WorkHourInfo[]
  showStatus?: boolean
}>()

const emit = defineEmits<{
  (e: 'dblclick'): void
}>()

const dayNumber = computed(() => {
  const date = new Date(props.date)
  return date.getDate()
})

const isToday = computed(() => checkIsToday(props.date))

const isWeekend = computed(() => checkIsWeekend(props.date))

const totalHours = computed(() => {
  return props.workHours.reduce((sum, wh) => sum + wh.hours, 0)
})

const hasData = computed(() => props.workHours.length > 0)

const getStatusType = (status: string) => getWorkHourStatusInfo(status).type
const getStatusText = (status: string) => getWorkHourStatusInfo(status).text

const handleDblClick = () => {
  if (!props.isOtherMonth) {
    emit('dblclick')
  }
}
</script>

<style scoped>
.calendar-cell {
  min-height: 120px;
  padding: 8px;
  background: #fff;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 4px;
}

.calendar-cell:hover {
  box-shadow: inset 0 0 0 2px #1890ff;
}

.calendar-cell.today {
  background: #e6f7ff;
}

.calendar-cell.weekend {
  background: #fafafa;
}

.calendar-cell.other-month {
  opacity: 0.3;
}

.calendar-cell.has-data {
  background: #f6ffed;
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
  overflow: hidden;
}

.wh-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
}

.wh-hours {
  color: #333;
  font-weight: 500;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
