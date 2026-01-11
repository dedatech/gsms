<template>
  <el-row :gutter="16" class="stats-row">
    <el-col :span="6">
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon today">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-label">今日工时</div>
            <div class="stats-value">{{ todayHours || 0 }} 小时</div>
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
            <div class="stats-value">{{ weekHours || 0 }} 小时</div>
          </div>
        </div>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon month">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-label">本月工时</div>
            <div class="stats-value">{{ monthHours || 0 }} 小时</div>
          </div>
        </div>
      </el-card>
    </el-col>
    <el-col :span="6">
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon total">
            <el-icon><Sum /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-label">总计工时</div>
            <div class="stats-value">{{ totalHours || 0 }} 小时</div>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { Clock, Calendar, DataLine, Sum } from '@element-plus/icons-vue'

defineProps<{
  todayHours?: number
  weekHours?: number
  monthHours?: number
  totalHours?: number
}>()
</script>

<style scoped>
.stats-row {
  margin-bottom: 16px;
}

.stats-card {
  border-radius: 8px;
  overflow: hidden;
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stats-icon.today {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stats-icon.week {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.stats-icon.month {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.stats-icon.total {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
}

.stats-info {
  flex: 1;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}
</style>
