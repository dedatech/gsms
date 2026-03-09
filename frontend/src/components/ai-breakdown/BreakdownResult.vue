<template>
  <div class="breakdown-result" v-loading="loading">
    <template v-if="result">
      <!-- 需求概述 -->
      <div class="result-section">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>需求概述</span>
        </div>
        <div class="summary-box">
          {{ result.summary }}
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="result-section">
        <div class="section-title">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计信息</span>
        </div>
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-icon">📦</div>
            <div class="stat-label">子任务数量</div>
            <div class="stat-value">{{ result.subTasks?.length || 0 }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">⏱️</div>
            <div class="stat-label">总预估工时</div>
            <div class="stat-value">{{ result.totalEstimatedDays }} 人天</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div class="stat-label">建议团队规模</div>
            <div class="stat-value">{{ result.suggestedTeamSize }} 人</div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">📅</div>
            <div class="stat-label">建议迭代周期</div>
            <div class="stat-value">{{ result.suggestedIterationDays }} 天</div>
          </div>
        </div>
      </div>

      <!-- 子任务列表 -->
      <div class="result-section">
        <div class="section-title">
          <el-icon><List /></el-icon>
          <span>子任务列表</span>
        </div>
        <div class="subtask-list">
          <div
            v-for="(task, index) in result.subTasks"
            :key="index"
            class="subtask-item"
          >
            <div class="task-header">
              <span class="task-sequence">{{ task.sequence }}</span>
              <span class="task-title">{{ task.title }}</span>
              <el-tag size="small" :type="getTaskTypeColor(task.taskType)">
                {{ task.taskType }}
              </el-tag>
              <el-tag
                size="small"
                :type="getPriorityColor(task.priority)"
                effect="plain"
              >
                {{ task.priority }}
              </el-tag>
              <span class="task-estimate">{{ task.estimatedDays }} 人天</span>
            </div>
            <div class="task-description">{{ task.description }}</div>
            <div v-if="task.notes" class="task-notes">
              <el-icon><InfoFilled /></el-icon>
              {{ task.notes }}
            </div>
            <div v-if="task.dependsOn" class="task-dependency">
              <el-icon><Link /></el-icon>
              依赖任务：{{ task.dependsOn }}
            </div>
          </div>
        </div>
      </div>

      <!-- 风险提示 -->
      <div class="result-section" v-if="result.risks && result.risks.length > 0">
        <div class="section-title warning">
          <el-icon><Warning /></el-icon>
          <span>风险提示</span>
        </div>
        <div class="risk-box">
          <ul>
            <li v-for="(risk, index) in result.risks" :key="index">
              {{ risk }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 技术建议 -->
      <div class="result-section" v-if="result.suggestions && result.suggestions.length > 0">
        <div class="section-title success">
          <el-icon><SuccessFilled /></el-icon>
          <span>技术建议</span>
        </div>
        <div class="suggestion-box">
          <ul>
            <li v-for="(suggestion, index) in result.suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>
      </div>

      <!-- 备注 -->
      <div class="result-section" v-if="result.notes">
        <div class="section-title">
          <el-icon><Memo /></el-icon>
          <span>备注</span>
        </div>
        <div class="notes-box">
          {{ result.notes }}
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="result-actions">
        <el-button type="primary" :icon="Select" @click="handleBatchCreate">
          批量创建任务
        </el-button>
        <el-button :icon="Refresh" @click="handleRetry">
          重新拆分
        </el-button>
        <el-button :icon="Close" @click="handleClose">
          关闭
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {
  Document,
  DataAnalysis,
  List,
  InfoFilled,
  Link,
  Warning,
  SuccessFilled,
  Memo,
  Select,
  Refresh,
  Close
} from '@element-plus/icons-vue'
import type { RequirementBreakdownResp } from '@/api/ai'

// Props
const props = defineProps<{
  result: RequirementBreakdownResp | null
  loading: boolean
}>()

// Emits
const emit = defineEmits<{
  batchCreate: []
  retry: []
  close: []
}>()

// 获取任务类型颜色
const getTaskTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    '需求分析': '',
    '系统设计': 'success',
    '前端开发': 'primary',
    '后端开发': 'warning',
    '数据库设计': 'info',
    '接口开发': 'warning',
    '测试': 'danger',
    '部署': 'success'
  }
  return colorMap[type] || ''
}

// 获取优先级颜色
const getPriorityColor = (priority: string) => {
  const colorMap: Record<string, string> = {
    '高': 'danger',
    '中': 'warning',
    '低': 'info'
  }
  return colorMap[priority] || ''
}

// 批量创建任务
const handleBatchCreate = () => {
  emit('batchCreate')
}

// 重新拆分
const handleRetry = () => {
  emit('retry')
}

// 关闭
const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.breakdown-result {
  padding: 10px 0;
}

.result-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
}

.section-title.warning {
  color: #e6a23c;
}

.section-title.success {
  color: #67c23a;
}

.summary-box {
  background: #f0f9ff;
  border-left: 4px solid #409eff;
  padding: 12px 16px;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.subtask-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding-right: 8px;
}

.subtask-item {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  transition: all 0.3s;
}

.subtask-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.task-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.task-sequence {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.task-title {
  flex: 1;
  font-weight: 500;
  color: #303133;
}

.task-estimate {
  font-size: 14px;
  color: #67c23a;
  font-weight: 600;
  flex-shrink: 0;
}

.task-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
  padding-left: 32px;
}

.task-notes {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
  padding-left: 32px;
}

.task-dependency {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #409eff;
  padding-left: 32px;
}

.risk-box {
  background: #fef0f0;
  border-left: 4px solid #f56c6c;
  padding: 12px 16px;
  border-radius: 4px;
}

.risk-box ul {
  margin: 0;
  padding-left: 20px;
}

.risk-box li {
  color: #606266;
  line-height: 1.8;
}

.suggestion-box {
  background: #f0f9ff;
  border-left: 4px solid #67c23a;
  padding: 12px 16px;
  border-radius: 4px;
}

.suggestion-box ul {
  margin: 0;
  padding-left: 20px;
}

.suggestion-box li {
  color: #606266;
  line-height: 1.8;
}

.notes-box {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 4px;
  color: #606266;
  line-height: 1.6;
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
}

/* 滚动条样式 */
.subtask-list::-webkit-scrollbar {
  width: 6px;
}

.subtask-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.subtask-list::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
</style>
