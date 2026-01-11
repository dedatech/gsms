<template>
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
      <el-button-group>
        <el-button :type="viewMode === 'month' ? 'primary' : ''" @click="handleSwitchMonthView">
          <el-icon><Calendar /></el-icon>
          月视图
        </el-button>
        <el-button :type="viewMode === 'list' ? 'primary' : ''" @click="handleSwitchList">
          <el-icon><List /></el-icon>
          列表视图
        </el-button>
      </el-button-group>
      <el-button type="primary" :icon="Plus" @click="handleCreate">登记工时</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowLeft, ArrowRight, Calendar, List, Plus } from '@element-plus/icons-vue'

const props = defineProps<{
  currentYear: number
  currentMonth: number
  viewMode: 'month' | 'list'
}>()

const emit = defineEmits<{
  (e: 'update:year', value: number): void
  (e: 'update:month', value: number): void
  (e: 'update:viewMode', value: 'month' | 'list'): void
  (e: 'switchToList'): void
  (e: 'today'): void
  (e: 'create'): void
}>()

const currentMonthText = computed(() => {
  return `${props.currentYear}年${props.currentMonth}月`
})

const handlePrevMonth = () => {
  const newYear = props.currentMonth === 1 ? props.currentYear - 1 : props.currentYear
  const newMonth = props.currentMonth === 1 ? 12 : props.currentMonth - 1
  emit('update:year', newYear)
  emit('update:month', newMonth)
}

const handleNextMonth = () => {
  const newYear = props.currentMonth === 12 ? props.currentYear + 1 : props.currentYear
  const newMonth = props.currentMonth === 12 ? 1 : props.currentMonth + 1
  emit('update:year', newYear)
  emit('update:month', newMonth)
}

const handleToday = () => {
  emit('today')
}

const handleSwitchMonthView = () => {
  emit('update:viewMode', 'month')
}

const handleSwitchList = () => {
  emit('switchToList')
}

const handleCreate = () => {
  emit('create')
}
</script>

<style scoped>
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
</style>
