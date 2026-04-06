# TasksView 使用示例

## 基本使用

### 在路由中使用

```typescript
// router/index.ts
{
  path: 'projects/:id',
  name: 'ProjectDetail',
  component: () => import('@/views/project/ProjectDetail.vue'),
  meta: { title: '项目详情', requiresAuth: true }
}
```

### 在组件中使用

```vue
<template>
  <div class="project-detail">
    <!-- 导航标签 -->
    <div class="module-tabs">
      <div
        v-for="tab in moduleTabs"
        :key="tab.key"
        class="module-tab"
        :class="{ active: activeModule === tab.key }"
        @click="activeModule = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="module-content">
      <!-- 任务模块 -->
      <TasksView
        v-if="activeModule === 'defect'"
        :project-id="projectId"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import TasksView from '@/views/project/TasksView.vue'

const route = useRoute()
const projectId = computed(() => Number(route.params.id))
const activeModule = ref('defect')
</script>
```

## 高级用法

### 自定义工具栏

```vue
<template>
  <TasksView
    :project-id="projectId"
    :show-toolbar="true"
    :default-view-mode="'kanban'"
    @task-created="handleTaskCreated"
    @task-updated="handleTaskUpdated"
    @task-deleted="handleTaskDeleted"
  />
</template>

<script setup lang="ts">
import TasksView from '@/views/project/TasksView.vue'

const handleTaskCreated = (task: TaskInfo) => {
  console.log('任务已创建:', task)
}

const handleTaskUpdated = (task: TaskInfo) => {
  console.log('任务已更新:', task)
}

const handleTaskDeleted = (taskId: number) => {
  console.log('任务已删除:', taskId)
}
</script>
```

### 与其他组件联动

```vue
<template>
  <div class="project-page">
    <!-- 左侧导航 -->
    <div class="sidebar">
      <el-menu>
        <el-menu-item @click="activeView = 'tasks'">任务</el-menu-item>
        <el-menu-item @click="activeView = 'iterations'">迭代</el-menu-item>
        <el-menu-item @click="activeView = 'members'">成员</el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <TasksView
        v-if="activeView === 'tasks'"
        :project-id="projectId"
        ref="tasksViewRef"
      />

      <IterationView
        v-if="activeView === 'iterations'"
        :project-id="projectId"
        @iteration-created="refreshTasks"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TasksView from '@/views/project/TasksView.vue'

const tasksViewRef = ref<InstanceType<typeof TasksView>>()
const activeView = ref('tasks')

const refreshTasks = () => {
  tasksViewRef.value?.refresh()
}
</script>
```

## 样式定制

### 覆盖默认样式

```vue
<style scoped>
/* 自定义左栏宽度 */
.tasks-view :deep(.left-column) {
  width: 320px;
  min-width: 320px;
}

/* 自定义右栏宽度 */
.tasks-view :deep(.right-column) {
  width: 480px;
  min-width: 480px;
}

/* 自定义主题色 */
.tasks-view :deep(.task-number) {
  color: #52c41a;
}

/* 自定义卡片样式 */
.tasks-view :deep(.task-card) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
```

## 数据交互示例

### 获取选中任务

```vue
<script setup lang="ts">
import { ref } from 'vue'
import TasksView from '@/views/project/TasksView.vue'

const tasksViewRef = ref<InstanceType<typeof TasksView>>()

// 获取当前选中的任务
const getSelectedTask = () => {
  const task = tasksViewRef.value?.selectedTask
  if (task) {
    console.log('选中的任务:', task)
  }
}
</script>
```

### 刷新任务列表

```vue
<script setup lang="ts">
import { ref } from 'vue'
import TasksView from '@/views/project/TasksView.vue'

const tasksViewRef = ref<InstanceType<typeof TasksView>>()

// 刷新任务列表
const refreshTasks = () => {
  tasksViewRef.value?.refresh()
}
</script>
```

### 切换视图模式

```vue
<script setup lang="ts">
import { ref } from 'vue'
import TasksView from '@/views/project/TasksView.vue'

const tasksViewRef = ref<InstanceType<typeof TasksView>>()

// 切换到看板视图
const switchToKanban = () => {
  if (tasksViewRef.value) {
    tasksViewRef.value.viewMode = 'kanban'
  }
}
</script>
```

## 测试用例

### 单元测试示例

```typescript
import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import TasksView from '@/views/project/TasksView.vue'

describe('TasksView', () => {
  it('renders correctly', () => {
    const wrapper = mount(TasksView, {
      props: {
        projectId: 1
      }
    })
    expect(wrapper.find('.tasks-view').exists()).toBe(true)
  })

  it('displays iterations in left column', async () => {
    const wrapper = mount(TasksView, {
      props: {
        projectId: 1
      }
    })

    // 等待数据加载
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.iteration-tree').exists()).toBe(true)
  })

  it('switches view modes', async () => {
    const wrapper = mount(TasksView, {
      props: {
        projectId: 1
      }
    })

    // 切换到看板视图
    await wrapper.vm.viewMode = 'kanban'
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.kanban-view').exists()).toBe(true)
  })
})
```

### E2E 测试示例

```typescript
import { test, expect } from '@playwright/test'

test('tasks view basic operations', async ({ page }) => {
  // 导航到项目详情页
  await page.goto('/projects/1')

  // 切换到任务标签
  await page.click('text=任务')

  // 验证左栏迭代树显示
  await expect(page.locator('.iteration-tree')).toBeVisible()

  // 验证中栏工作项列表显示
  await expect(page.locator('.list-view')).toBeVisible()

  // 切换到看板视图
  await page.click('button[aria-label="看板视图"]')
  await expect(page.locator('.kanban-view')).toBeVisible()

  // 点击工作项查看详情
  await page.click('.task-number:first')
  await expect(page.locator('.detail-panel')).toBeVisible()
})
```

## 常见场景

### 场景 1: 快速创建任务

```typescript
// 1. 按 N 键打开新建对话框
// 2. 选择类型（任务）
// 3. 输入标题
// 4. 选择负责人
// 5. 点击确定
```

### 场景 2: 使用看板管理任务

```typescript
// 1. 切换到看板视图
// 2. 查看三列（待办、进行中、已完成）
// 3. 拖拽任务卡片到不同列
// 4. 实时更新状态
```

### 场景 3: 按迭代组织任务

```typescript
// 1. 创建迭代
// 2. 在左栏选择迭代
// 3. 拖拽任务到迭代
// 4. 查看迭代任务分布
```

### 场景 4: 搜索和筛选

```typescript
// 1. 按 F 键聚焦搜索框
// 2. 输入关键词
// 3. 查看过滤结果
// 4. 使用类型筛选进一步过滤
```

## 性能优化建议

### 大数据量优化

```typescript
// 使用分页
const fetchTasks = async () => {
  const res = await getTasksByProjectId(
    projectId.value,
    pageNum.value,
    pageSize.value  // 每页 50 条
  )
  tasks.value = res.list
}

// 使用虚拟滚动
import { VirtualScroller } from 'vue-virtual-scroller'
```

### 缓存优化

```typescript
// 使用 computed 缓存计算结果
const filteredTasks = computed(() => {
  return tasks.value.filter(/* ... */)
})

// 使用 watch 防抖
import { debounce } from 'lodash-es'

watch(searchKeyword, debounce((keyword) => {
  // 执行搜索
}, 300))
```

## 故障排除

### 问题 1: 任务列表不显示

**解决方案：**
```typescript
// 检查 projectId 是否正确
console.log('projectId:', projectId.value)

// 检查 API 调用
fetchTasks().catch(error => {
  console.error('获取任务失败:', error)
})
```

### 问题 2: 拖拽不工作

**解决方案：**
```typescript
// 检查浏览器支持
if (!('draggable' in document.createElement('div'))) {
  console.error('浏览器不支持拖拽')
}

// 检查事件处理
const handleDragStart = (task: TaskInfo, event: DragEvent) => {
  console.log('开始拖拽:', task)
  // ...
}
```

### 问题 3: 样式错乱

**解决方案：**
```vue
<style>
/* 确保样式优先级 */
.tasks-view :deep(.left-column) {
  width: 280px !important;
}
</style>
```

## 更多资源

- [API 文档](../api-docs.md)
- [组件文档](./TASKS_VIEW_IMPLEMENTATION.md)
- [用户指南](./TASKS_VIEW_USER_GUIDE.md)
