# 前端模块联动功能梳理

**创建时间**: 2026-01-11 12:25
**分析范围**: 所有前端模块的联动功能和未实现功能

---

## 一、模块概览

### 1.1 已实现的模块

| 模块 | 页面 | 状态 | 说明 |
|------|------|------|------|
| **认证** | LoginView, RegisterView | ✅ 完成 | 登录注册功能 |
| **Dashboard** | Dashboard | ✅ 完成 | 首页看板（含联动优化） |
| **项目管理** | ProjectList, ProjectDetail | ⚠️ 部分完成 | 列表完成，详情联动未完成 |
| **任务管理** | TaskList, TaskDetail | ⚠️ 部分完成 | 列表完成，详情联动未完成 |
| **迭代管理** | IterationList, IterationDetail | ⚠️ 部分完成 | 列表完成，详情联动未完成 |
| **工时管理** | WorkHourList | ⚠️ 待验证 | 基本功能，联动待检查 |

---

## 二、联动功能现状分析

### 2.1 已实现的联动 ✅

#### Dashboard → 其他模块
- ✅ 点击项目名称 → 项目详情页 `/projects/{id}`
- ✅ 点击"查看"任务 → 任务详情页 `/tasks/{id}`
- ✅ 点击"查看全部" → 任务列表页 `/tasks?status=TODO`

#### 任务详情 → 项目
- ✅ 点击"所属项目"链接 → 项目详情页

#### 迭代详情 → 项目
- ✅ 点击"所属项目"链接 → 项目详情页

---

### 2.2 未实现的联动 ❌

#### 2.2.1 项目详情页（ProjectDetail.vue）

**问题1：新建任务功能未实现**
```vue
<!-- 位置：第114行 -->
<el-button type="primary" :icon="Plus" @click="handleCreateTask">
  新建任务
</el-button>

// 当前实现（第502-505行）
const handleCreateTask = () => {
  // TODO: 打开新建任务对话框
  ElMessage.info('新建任务功能开发中')
}
```

**需要实现：**
1. 打开新建任务对话框
2. 自动填充当前项目ID
3. 创建成功后刷新任务列表

**期望行为：**
```typescript
const handleCreateTask = () => {
  // 1. 打开对话框，传递项目ID
  router.push({
    path: '/tasks/create',
    query: { projectId: project.id, projectName: project.name }
  })

  // 或者打开对话框
  taskDialogVisible = true
  taskForm.projectId = project.id
}
```

---

**问题2：查看任务详情未实现**
```vue
<!-- 位置：第144、166行 -->
<el-link type="primary" @click="handleViewTask(row)">{{ row.title }}</el-link>
<el-button link type="primary" :icon="View" @click="handleViewTask(row)">查看</el-button>

// 当前实现（第508-511行）
const handleViewTask = (task: TaskInfo) => {
  ElMessage.info('查看任务功能开发中')
  console.log('查看任务:', task)
}
```

**需要实现：**
```typescript
const handleViewTask = (task: TaskInfo) => {
  router.push(`/tasks/${task.id}`)
}
```

---

**问题3：编辑/删除任务未实现**
```typescript
// 当前实现（第513-520行）
const handleEditTask = (task: TaskInfo) => {
  ElMessage.info('编辑任务功能开发中')
}

const handleDeleteTask = (task: TaskInfo) => {
  ElMessage.info('删除任务功能开发中')
}
```

---

#### 2.2.2 任务详情页（TaskDetail.vue）

**问题1：工时记录标签页未实现**
```vue
<!-- 位置：第126-136行 -->
<el-tab-pane name="workhours">
  <template #label>
    <span>
      <el-icon><Clock /></el-icon>
      工时记录
    </span>
  </template>
  <div class="tab-content">
    <el-empty description="工时记录功能开发中" :image-size="100" />
  </div>
</el-tab-pane>
```

**需要实现：**
1. 显示该任务的工时记录列表
2. 添加"登记工时"按钮
3. 工时记录关联到该任务
4. 显示总工时统计

**期望内容：**
```vue
<div class="tab-content">
  <div class="content-header">
    <div class="header-title">
      <h3>工时记录</h3>
      <span class="subtitle">总工时: {{ totalHours }} 小时</span>
    </div>
    <el-button type="primary" :icon="Plus" @click="handleAddWorkHour">
      登记工时
    </el-button>
  </div>

  <el-table :data="workHours" stripe>
    <el-table-column prop="workDate" label="日期" width="110" />
    <el-table-column prop="hours" label="工时数" width="90" />
    <el-table-column prop="description" label="说明" min-width="200" />
    <el-table-column prop="createTime" label="创建时间" width="160" />
    <el-table-column label="操作" width="100">
      <template #default="{ row }">
        <el-button link type="primary" @click="handleEditWorkHour(row)">编辑</el-button>
        <el-button link type="danger" @click="handleDeleteWorkHour(row)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
</div>
```

---

**问题2：子任务标签页未实现**
```vue
<!-- 位置：第139-149行 -->
<el-tab-pane name="subtasks">
  <template #label>
    <span>
      <el-icon><Files /></el-icon>
      子任务
    </span>
  </template>
  <div class="tab-content">
    <el-empty description="子任务功能开发中" :image-size="100" />
  </div>
</el-tab-pane>
```

**需要实现：**
1. 显示该任务的子任务列表
2. 添加"添加子任务"按钮
3. 树形结构展示父子关系
4. 支持拖拽调整顺序

---

**问题3：迭代字段不完整**
```vue
<!-- 位置：第75-77行 -->
<el-descriptions-item label="迭代">
  {{ task?.iterationId || '-' }}
</el-descriptions-item>
```

**当前问题：**
- 只显示迭代ID，不显示迭代名称
- 无法点击跳转到迭代详情

**需要修改为：**
```vue
<el-descriptions-item label="所属迭代">
  <el-link
    v-if="task?.iterationId"
    type="primary"
    @click="goToIteration"
  >
    {{ task?.iterationName || `迭代${task?.iterationId}` }}
  </el-link>
  <span v-else>-</span>
</el-descriptions-item>
```

---

#### 2.2.3 迭代详情页（IterationDetail.vue）

**问题：关联任务标签页未实现**
```vue
<!-- 位置：第82-92行 -->
<el-tab-pane name="tasks">
  <template #label>
    <span>
      <el-icon><Files /></el-icon>
      关联任务
    </span>
  </template>
  <div class="tab-content">
    <el-empty description="关联任务功能开发中" :image-size="100" />
  </div>
</el-tab-pane>
```

**需要实现：**
1. 显示该迭代的任务列表
2. 添加"添加任务"按钮（自动关联到该迭代）
3. 任务统计（待办/进行中/已完成数量）
4. 点击任务跳转到任务详情

**期望内容：**
```vue
<div class="tab-content">
  <div class="content-header">
    <div class="header-title">
      <h3>关联任务</h3>
      <span class="subtitle">共 {{ taskTotal }} 个任务</span>
    </div>
    <el-button type="primary" :icon="Plus" @click="handleCreateTask">
      添加任务
    </el-button>
  </div>

  <!-- 任务统计 -->
  <div class="task-stats">
    <div class="stat-item">
      <div class="stat-label">全部</div>
      <div class="stat-value">{{ taskStats.total }}</div>
    </div>
    <div class="stat-item">
      <div class="stat-label">待办</div>
      <div class="stat-value todo">{{ taskStats.todo }}</div>
    </div>
    <div class="stat-item">
      <div class="stat-label">进行中</div>
      <div class="stat-value inProgress">{{ taskStats.inProgress }}</div>
    </div>
    <div class="stat-item">
      <div class="stat-label">已完成</div>
      <div class="stat-value done">{{ taskStats.done }}</div>
    </div>
  </div>

  <!-- 任务列表 -->
  <el-table :data="tasks" stripe>
    <el-table-column prop="id" label="ID" width="70" />
    <el-table-column prop="title" label="任务标题" min-width="200">
      <template #default="{ row }">
        <el-link type="primary" @click="handleViewTask(row)">{{ row.title }}</el-link>
      </template>
    </el-table-column>
    <el-table-column prop="assigneeName" label="负责人" width="110" />
    <el-table-column prop="status" label="状态" width="90">
      <template #default="{ row }">
        <el-tag :type="getTaskStatusType(row.status)" size="small">
          {{ getTaskStatusText(row.status) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="priority" label="优先级" width="90">
      <template #default="{ row }">
        <el-tag :type="getPriorityType(row.priority)" size="small">
          {{ getPriorityText(row.priority) }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="planEndDate" label="计划结束" width="110" />
    <el-table-column label="操作" width="100" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="handleViewTask(row)">查看</el-button>
      </template>
    </el-table-column>
  </el-table>
</div>
```

---

#### 2.2.4 工时列表页（WorkHourList.vue）

**需要验证的功能：**
1. "登记工时"按钮是否已实现
2. 是否可以关联任务
3. 是否可以关联项目
4. 点击任务/项目是否可以跳转

**待检查项：**
- [ ] handleCreate 方法实现情况
- [ ] 工时表单是否有任务/项目关联字段
- [ ] 工时列表是否显示关联的任务/项目名称

---

## 三、优先级分类

### 3.1 高优先级（P0）- 核心业务流程

**1. 项目详情页 - 新建任务**
- **影响**: 用户无法从项目页面创建任务
- **工作量**: 中等（需要对话框或跳转到创建页面）
- **依赖**: 任务创建API

**2. 项目详情页 - 查看任务详情**
- **影响**: 用户无法查看任务详细信息
- **工作量**: 简单（只需要添加跳转逻辑）
- **依赖**: 无

**3. 任务详情页 - 工时记录**
- **影响**: 用户无法在任务页查看和管理工时
- **工作量**: 中等（需要显示工时列表和添加功能）
- **依赖**: 工时查询API（按taskId过滤）

---

### 3.2 中优先级（P1）- 重要增强功能

**4. 迭代详情页 - 关联任务**
- **影响**: 用户无法在迭代页面管理任务
- **工作量**: 中等（类似于项目详情页的任务列表）
- **依赖**: 任务查询API（按iterationId过滤）

**5. 任务详情页 - 子任务**
- **影响**: 无法管理任务层级关系
- **工作量**: 较大（需要树形结构和拖拽功能）
- **依赖**: 子任务API（可能需要后端支持）

**6. 任务详情页 - 迭代字段完善**
- **影响**: 迭代信息不明确，无法跳转
- **工作量**: 简单（显示名称+添加链接）
- **依赖**: 任务详情API需返回iterationName

---

### 3.3 低优先级（P2）- 辅助功能

**7. 项目详情页 - 编辑/删除任务**
- **影响**: 用户体验，但可以通过任务列表操作
- **工作量**: 简单
- **依赖**: 无

---

## 四、实施建议

### 4.1 第一阶段（立即实施）

**目标**: 完成核心联动功能，提升用户体验

1. **项目详情页 - 查看任务详情** ✅ 最简单，立即见效
   - 修改 `handleViewTask` 方法
   - 添加跳转到任务详情页的逻辑
   - 预计时间：5分钟

2. **任务详情页 - 迭代字段完善** ✅ 简单但有价值
   - 显示迭代名称而不是ID
   - 添加点击跳转功能
   - 预计时间：10分钟

3. **项目详情页 - 新建任务** ⚠️ 核心功能
   - 实现新建任务对话框或跳转
   - 自动填充项目ID
   - 预计时间：30分钟

---

### 4.2 第二阶段（近期实施）

**目标**: 完善重要模块的联动

4. **任务详情页 - 工时记录**
   - 显示任务相关的工时记录
   - 添加登记工时功能
   - 预计时间：1小时

5. **迭代详情页 - 关联任务**
   - 显示迭代相关的任务列表
   - 添加任务统计
   - 预计时间：1小时

---

### 4.3 第三阶段（长期规划）

**目标**: 实现高级功能

6. **任务详情页 - 子任务管理**
   - 需要后端API支持
   - 树形结构展示
   - 拖拽排序功能
   - 预计时间：2-3小时

---

## 五、技术实施要点

### 5.1 路由跳转模式

**推荐方式：使用router.push**
```typescript
// 跳转到详情页
router.push(`/projects/${projectId}`)
router.push(`/tasks/${taskId}`)
router.push(`/iterations/${iterationId}`)

// 跳转到列表页（带查询参数）
router.push({
  path: '/tasks',
  query: { projectId: 1, status: 'TODO' }
})

// 跳转到创建页（带预填充参数）
router.push({
  path: '/tasks/create',
  query: { projectId: 1, iterationId: 2 }
})
```

---

### 5.2 对话框模式 vs 跳转模式

**对话框模式（适合简单创建）：**
- 优点：上下文不丢失，快速返回
- 缺点：表单复杂时页面拥挤
- 适用：添加成员、简单编辑

**跳转模式（适合复杂创建）：**
- 优点：页面空间大，表单复杂
- 缺点：需要返回导航
- 适用：新建任务、新建项目

**建议：**
- 项目详情页 → 新建任务：使用**跳转模式**（任务表单复杂）
- 迭代详情页 → 添加任务：使用**跳转模式**
- 任务详情页 → 登记工时：使用**对话框模式**（工时表单简单）

---

### 5.3 数据获取策略

**列表页需要支持查询参数过滤：**
```typescript
// TaskList.vue 需要支持
onMounted(() => {
  const route = useRoute()
  const projectId = route.query.projectId
  const status = route.query.status
  const iterationId = route.query.iterationId

  // 使用查询参数加载数据
  fetchTasks({
    projectId: projectId as string,
    status: status as string,
    iterationId: iterationId as string
  })
})
```

---

### 5.4 后端API需求

**需要确认/添加的API：**

1. **任务查询API** 需要支持：
   - 按 `projectId` 过滤 ✅ 已有
   - 按 `iterationId` 过滤 ❓ 需确认
   - 按 `status` 过滤 ✅ 已有

2. **工时查询API** 需要支持：
   - 按 `taskId` 过滤 ❓ 需确认
   - 返回时包含 `taskName` 和 `projectName` ❓ 需确认

3. **任务详情API** 需要返回：
   - `iterationName` ❌ 当前只返回 `iterationId`
   - `projectName` ✅ 已有

---

## 六、总结

### 当前状态
- ✅ **基础导航**: 模块间基础跳转已实现
- ⚠️ **核心联动**: 约50%完成度
- ❌ **高级功能**: 大部分未实现

### 重点改进方向
1. **项目 → 任务**: 完善新建和查看功能
2. **任务 → 项目/迭代**: 完善关联信息展示
3. **任务 → 工时**: 实现工时记录管理
4. **迭代 → 任务**: 实现任务列表展示

### 预计工作量
- **第一阶段（P0）**: 45分钟
- **第二阶段（P1）**: 2小时
- **第三阶段（P2）**: 2-3小时
- **总计**: 约5-6小时

---

## 附录：检查清单

### 立即修复（第一阶段）
- [ ] 项目详情页 - 查看任务详情跳转
- [ ] 项目详情页 - 新建任务功能
- [ ] 任务详情页 - 迭代字段显示名称和链接

### 近期完成（第二阶段）
- [ ] 任务详情页 - 工时记录列表
- [ ] 迭代详情页 - 关联任务列表

### 长期规划（第三阶段）
- [ ] 任务详情页 - 子任务管理
- [ ] 项目详情页 - 任务编辑/删除快捷操作
