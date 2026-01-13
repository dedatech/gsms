<template>
  <div class="change-diff-viewer">
    <!-- 差异摘要 -->
    <div class="diff-summary">
      <el-tag type="success" size="small">+{{ stats.added }} 新增</el-tag>
      <el-tag type="danger" size="small">-{{ stats.removed }} 删除</el-tag>
      <el-tag type="warning" size="small">~{{ stats.modified }} 修改</el-tag>
    </div>

    <!-- 变更对比表格 -->
    <el-table
      v-if="hasChanges"
      :data="diffItems"
      border
      stripe
      size="small"
      class="diff-table"
    >
      <el-table-column prop="field" label="字段" width="150" />
      <el-table-column label="变更前" min-width="200">
        <template #default="{ row }">
          <span :class="getValueClass(row, 'old')">
            {{ formatValue(row.oldValue) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="变更后" min-width="200">
        <template #default="{ row }">
          <span :class="getValueClass(row, 'new')">
            {{ formatValue(row.newValue) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getChangeType(row.changeType)" size="small">
            {{ getChangeTypeText(row.changeType) }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 无变更提示 -->
    <el-empty v-if="!hasChanges" description="无数据变更" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  oldValue?: string
  newValue?: string
}>()

// 字段映射
const getFieldLabel = (field: string): string => {
  const fieldMap: Record<string, string> = {
    id: 'ID',
    username: '用户名',
    nickname: '昵称',
    email: '邮箱',
    phone: '手机号',
    status: '状态',
    roleId: '角色ID',
    roleName: '角色名称',
    createTime: '创建时间',
    updateTime: '更新时间'
  }
  return fieldMap[field] || field
}

// 解析 JSON
const parseJson = (jsonStr?: string): any => {
  if (!jsonStr) return null
  try {
    return JSON.parse(jsonStr)
  } catch (e) {
    console.error('JSON 解析失败:', e)
    return null
  }
}

// 格式化值
const formatValue = (value: any): string => {
  if (value === null || value === undefined) return '-'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

// 变更项接口
interface DiffItem {
  field: string
  oldValue: any
  newValue: any
  changeType: 'added' | 'removed' | 'modified' | 'unchanged'
}

// 生成对比项列表
const diffItems = computed<DiffItem[]>(() => {
  const oldObj = parseJson(props.oldValue)
  const newObj = parseJson(props.newValue)

  if (!oldObj && !newObj) return []

  const items: DiffItem[] = []
  const allKeys = new Set([
    ...(oldObj ? Object.keys(oldObj) : []),
    ...(newObj ? Object.keys(newObj) : [])
  ])

  Array.from(allKeys).sort().forEach(key => {
    const oldVal = oldObj?.[key]
    const newVal = newObj?.[key]

    if (oldVal === undefined && newVal !== undefined) {
      // 新增字段
      items.push({
        field: getFieldLabel(key),
        oldValue: null,
        newValue: newVal,
        changeType: 'added'
      })
    } else if (oldVal !== undefined && newVal === undefined) {
      // 删除字段
      items.push({
        field: getFieldLabel(key),
        oldValue: oldVal,
        newValue: null,
        changeType: 'removed'
      })
    } else if (JSON.stringify(oldVal) !== JSON.stringify(newVal)) {
      // 修改字段
      items.push({
        field: getFieldLabel(key),
        oldValue: oldVal,
        newValue: newVal,
        changeType: 'modified'
      })
    }
  })

  return items
})

// 统计变更
const stats = computed(() => {
  return {
    added: diffItems.value.filter(item => item.changeType === 'added').length,
    removed: diffItems.value.filter(item => item.changeType === 'removed').length,
    modified: diffItems.value.filter(item => item.changeType === 'modified').length
  }
})

const hasChanges = computed(() => {
  return diffItems.value.length > 0
})

// 获取值样式类
const getValueClass = (row: DiffItem, type: 'old' | 'new'): string => {
  if (row.changeType === 'added' && type === 'new') return 'value-added'
  if (row.changeType === 'removed' && type === 'old') return 'value-removed'
  if (row.changeType === 'modified') {
    return type === 'old' ? 'value-removed' : 'value-added'
  }
  return ''
}

// 获取变更类型标签
const getChangeType = (changeType: string) => {
  const map: Record<string, any> = {
    added: 'success',
    removed: 'danger',
    modified: 'warning'
  }
  return map[changeType] || 'info'
}

// 获取变更类型文本
const getChangeTypeText = (changeType: string) => {
  const map: Record<string, string> = {
    added: '新增',
    removed: '删除',
    modified: '修改'
  }
  return map[changeType] || '未变化'
}
</script>

<style scoped>
.change-diff-viewer {
  margin-top: 16px;
}

.diff-summary {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.diff-table {
  margin-top: 12px;
}

.value-added {
  color: #67c23a;
  font-weight: 500;
}

.value-removed {
  color: #f56c6c;
  font-weight: 500;
  text-decoration: line-through;
}
</style>
