# 项目详情页 - 概览模块实现文档

## 概述

本文档描述了项目详情页"概览"模块的完整实现，该模块提供了一个直观的项目状态仪表板，展示项目的关键指标、迭代进度和最近活动。

## 功能特性

### 1. 顶部进度条区域

**功能描述：**
- 显示项目名称和状态标签
- 可视化完成进度条（蓝绿色 #52c41a）
- 显示剩余时间（< 3天显示橙色预警）
- 显示项目成员数和迭代数

**实现细节：**
- 状态标签：
  - 未开始 - 灰色
  - 进行中 - 蓝色
  - 已暂停 - 黄色
  - 已归档 - 灰色
  - 已完成 - 绿色
- 进度条颜色根据完成率动态变化：
  - < 30%: 红色 #ff4d4f
  - 30-70%: 蓝色 #1890ff
  - > 70%: 绿色 #52c41a
- 剩余时间计算：
  - 已延期：显示"已延期 X 天"
  - 今天截止：显示"今天截止"
  - 即将截止（≤3天）：橙色警告

### 2. 关键指标卡片

**功能描述：**
- 4个指标卡片，展示项目任务统计
- 点击卡片可跳转到任务列表（带筛选）
- 响应式布局（4列 → 2列 → 1列）

**指标卡片：**

1. **总任务数**
   - 图标：列表图标
   - 显示任务总数和趋势箭头
   - 点击：跳转到所有任务

2. **已完成**
   - 图标：完成图标
   - 显示已完成任务数
   - 显示完成百分比
   - 点击：筛选已完成任务

3. **进行中**
   - 图标：加载图标
   - 显示进行中任务数
   - 点击：筛选进行中任务

4. **已逾期**
   - 图标：警告图标
   - 显示逾期任务数
   - 有逾期任务时显示红色徽章 "!"
   - 点击：筛选逾期任务

**计算逻辑：**
```typescript
// 总任务数：所有关联任务的计数
taskMetrics.total = tasks.value.length

// 已完成：状态为 DONE 的任务
taskMetrics.completed = tasks.value.filter(t => t.status === 'DONE').length

// 进行中：状态为 IN_PROGRESS 的任务
taskMetrics.inProgress = tasks.value.filter(t => t.status === 'IN_PROGRESS').length

// 已逾期：未完成且计划结束时间 < 今天
taskMetrics.overdue = tasks.value.filter(t => {
  if (t.status === 'DONE') return false
  if (!t.planEndDate) return false
  const endDate = new Date(t.planEndDate)
  endDate.setHours(0, 0, 0, 0)
  return endDate < today
}).length
```

### 3. 迭代进度时间轴

**功能描述：**
- 横向滚动的迭代卡片列表
- 显示每个迭代的状态、日期和进度
- 点击跳转到迭代详情

**迭代卡片内容：**
- 状态图标：
  - ✅ 已完成 - 绿色
  - ⚡ 进行中 - 蓝色
  - 📅 未开始 - 灰色
- 迭代名称和状态标签
- 计划开始/结束日期
- 进度条（基于该迭代的任务完成率）
- 任务数量统计

**进度计算：**
```typescript
const calculateIterationProgress = (iteration: any) => {
  if (iteration.status === 'COMPLETED') return 100
  if (iteration.status === 'NOT_STARTED') return 0

  const iterationTasks = tasks.value.filter(t => t.iterationId === iteration.id)
  if (iterationTasks.length === 0) return 0

  const completed = iterationTasks.filter(t => t.status === 'DONE').length
  return Math.round((completed / iterationTasks.length) * 100)
}
```

**交互功能：**
- 点击卡片：跳转到迭代详情页
- 点击"查看全部"：跳转到规划视图

### 4. 最近活动流

**功能描述：**
- 时间线样式的活动记录
- 按日期分组（今天、昨天、具体日期）
- 支持按类型筛选（任务、迭代、成员）
- 点击活动可查看详情

**活动数据来源：**
- 从操作日志表获取
- 自动识别活动类型（任务/迭代/成员）
- 格式化日期显示（今天/昨天/月日）

**筛选选项：**
- 全部：显示所有活动
- 任务：只显示任务相关活动
- 迭代：只显示迭代相关活动
- 成员：只显示成员相关活动

**活动类型图标：**
- 任务：蓝色列表图标
- 迭代：绿色文件夹图标
- 成员：橙色用户图标

### 5. 快速操作区

**功能描述：**
- 提供常用操作的快捷入口
- 通过 emit 事件与父组件通信

**快速操作按钮：**
1. **新建任务** - 触发 `create-task` 事件
2. **新建需求** - 触发 `create-requirement` 事件
3. **新建迭代** - 触发 `create-iteration` 事件

## 技术实现

### 组件结构

```vue
<template>
  <div class="overview-view">
    <!-- 顶部进度条区域 -->
    <div class="overview-header">...</div>

    <!-- 关键指标卡片 -->
    <div class="metrics-section">...</div>

    <!-- 迭代进度时间轴 -->
    <div class="iterations-section">...</div>

    <!-- 最近活动流 -->
    <div class="activities-section">...</div>

    <!-- 快速操作区 -->
    <div class="quick-actions-section">...</div>
  </div>
</template>
```

### Props 和 Events

**Props:**
```typescript
interface Props {
  projectId: number  // 项目ID
}
```

**Events:**
```typescript
defineEmits<{
  'create-task': []
  'create-requirement': []
  'create-iteration': []
}>()
```

### API 调用

1. **项目详情**
```typescript
const res = await getProjectDetail(props.projectId)
project.value = res
```

2. **项目成员**
```typescript
const res = await getProjectMembers(props.projectId)
members.value = res || []
```

3. **任务列表**
```typescript
const res = await getTasksByProjectId(props.projectId, 1, 1000)
tasks.value = res.list || []
```

4. **迭代列表**
```typescript
const res = await getIterationList({
  projectId: props.projectId,
  pageNum: 1,
  pageSize: 100
})
iterations.value = res.list || []
```

5. **操作日志**
```typescript
const res = await getOperationLogList({
  pageNum: 1,
  pageSize: 20
})
activities.value = (res.list || [])
  .filter(log => log.module?.includes('项目') || log.module?.includes('任务') || log.module?.includes('迭代'))
  .map(log => ({
    id: log.id,
    type: getLogType(log.operationType),
    description: log.operationContent || log.operationType,
    date: formatLogDate(log.operationTime),
    time: formatLogTime(log.operationTime),
    entity: log
  }))
```

### 响应式设计

**断点策略：**
- 桌面端（≥992px）：4列布局
- 平板端（768px-991px）：2列布局
- 移动端（<768px）：1列布局

**关键媒体查询：**
```css
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-center {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
  }

  .timeline-scroll {
    flex-direction: column;
  }

  .iteration-item {
    width: 100%;
  }
}
```

### 样式规范

**颜色系统：**
- 主色：#1890ff（蓝色）
- 成功：#52c41a（绿色）
- 警告：#faad14（橙色）
- 危险：#ff4d4f（红色）
- 信息：#d9d9d9（灰色）

**间距规范：**
- 大间距：24px
- 中间距：16px
- 小间距：12px
- 微间距：8px

**圆角规范：**
- 大圆角：8px
- 中圆角：6px
- 小圆角：4px

**阴影规范：**
- 轻阴影：`0 1px 2px rgba(0, 0, 0, 0.03)`
- 中阴影：`0 2px 8px rgba(0, 0, 0, 0.08)`
- 重阴影：`0 4px 12px rgba(0, 0, 0, 0.1)`

## 集成说明

### 在 ProjectDetail.vue 中集成

```vue
<template>
  <!-- 概览 -->
  <div v-if="activeModule === 'overview'" class="module-content">
    <OverviewView
      :project-id="projectId"
      @create-task="handleCreateTask"
      @create-requirement="handleCreateRequirement"
      @create-iteration="handleCreateIteration"
    />
  </div>
</template>

<script setup lang="ts">
import OverviewView from '@/views/project/OverviewView.vue'

// 事件处理
const handleCreateTask = () => {
  // 打开新建任务对话框
}

const handleCreateRequirement = () => {
  // 打开新建需求对话框
}

const handleCreateIteration = () => {
  // 打开新建迭代对话框
}
</script>
```

## 导航跳转

### 跳转到任务列表
```typescript
const navigateToTasks = (filter: string) => {
  router.push({
    path: `/projects/${props.projectId}`,
    query: { tab: 'iteration', filter }
  })
}
```

### 跳转到规划视图
```typescript
const navigateToPlanning = () => {
  router.push({
    path: `/projects/${props.projectId}`,
    query: { tab: 'planning' }
  })
}
```

### 跳转到迭代详情
```typescript
const navigateToIteration = (iterationId: number) => {
  router.push(`/projects/${props.projectId}/iterations/${iterationId}`)
}
```

## 性能优化

1. **数据缓存**：使用 `ref` 缓存 API 响应，避免重复请求
2. **条件渲染**：使用 `v-if` 和 `v-else` 减少不必要的 DOM 节点
3. **虚拟滚动**：活动流时间线限制最大高度（400px），超出滚动
4. **计算属性**：使用 `computed` 缓存计算结果，避免重复计算

## 后续优化建议

1. **后端 API 优化**
   - 添加专门的概览统计接口（一次性返回所有统计数据）
   - 支持分页获取活动记录（当前取前20条）
   - 添加任务趋势计算接口

2. **功能增强**
   - 添加任务趋势图表（本周/本月完成情况）
   - 支持自定义时间范围筛选
   - 添加导出功能（导出概览报告）

3. **性能优化**
   - 实现数据轮询（定时刷新统计数据）
   - 添加骨架屏加载效果
   - 优化大量数据渲染（虚拟滚动）

4. **交互优化**
   - 添加工具提示（悬停显示详细信息）
   - 支持拖拽排序（迭代卡片）
   - 添加快捷键支持

## 文件位置

- 组件文件：`frontend/src/views/project/OverviewView.vue`
- 集成文件：`frontend/src/views/project/ProjectDetail.vue`
- API 文件：
  - `frontend/src/api/project.ts`
  - `frontend/src/api/task.ts`
  - `frontend/src/api/iteration.ts`
  - `frontend/src/api/operationLog.ts`

## 测试建议

1. **功能测试**
   - 验证各指标卡片数据准确性
   - 测试点击跳转功能
   - 验证筛选功能正常工作

2. **边界测试**
   - 无任务数据时的显示
   - 无迭代数据时的显示
   - 无活动记录时的显示

3. **响应式测试**
   - 不同屏幕尺寸下的布局
   - 移动端触摸交互

4. **性能测试**
   - 大量任务数据的渲染性能
   - 大量活动记录的渲染性能

## 总结

项目详情页概览模块提供了全面的项目状态视图，通过直观的可视化组件和交互式操作，帮助项目管理者快速了解项目进展和关键指标。该模块遵循 ONES 设计系统，具有良好的响应式布局和用户体验。
