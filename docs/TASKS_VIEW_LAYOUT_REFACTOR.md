# 任务模块三栏布局重构总结

> **完成时间：** 2026-02-07
> **重构目标：** 优化左侧迭代区域，简化中栏任务列表

---

## ✅ 已完成的修改

### 1. 左栏重构（220px固定宽度）

#### 新增功能：
- ✅ **迭代+需求两级导航树**
  - 显示所有迭代
  - 每个迭代下显示需求列表
  - 未分配任务单独显示

- ✅ **迭代状态标识**
  - 🔄 进行中（蓝色）
  - ✅ 已完成（绿色）
  - 📅 未开始（灰色）

- ✅ **工具栏**
  - 全部展开按钮
  - 全部折叠按钮
  - 显示任务数量徽章

#### 视觉优化：
- ✅ 左边框颜色区分状态
- ✅ 状态图标显示
- ✅ 需求图标（📝 文档图标）
- ✅ 展开/折叠动画

---

### 2. 中栏简化

#### 移除内容：
- ❌ 删除了标签页设计
- ❌ 删除了迭代概要卡片

#### 保留功能：
- ✅ 搜索和筛选工具栏
- ✅ 视图切换（列表/看板/树形）
- ✅ 任务列表展示
- ✅ 新建工作项按钮

---

### 3. 右栏保持不变

- ✅ 任务详情面板
- ✅ 内联编辑功能
- ✅ 工时记录显示

---

## 📐 新的布局结构

```
┌──────────┬───────────────────┬──────────┐
│ 迭代需求  │   任务列表         │ 任务详情 │
│  (220px) │    (自适应)        │ (400px)  │
├──────────┼───────────────────┼──────────┤
│ 🔄 一期  │ 🔍 [搜索] [筛选]  │ ⚪ 任务1 │
│  ├─ 需1 │ [+新建] [视图]     │          │
│  └─ 需2 │ ⚪ #123 任务1      │ 详情...  │
│          │ ⚪ #124 任务2      │          │
│ 🔄 二期  │ ⚪ #125 任务3      │          │
│  ├─ 需3 │                   │          │
│  └─ 需4 │                   │          │
│ 📁 未分配│                   │          │
└──────────┴───────────────────┴──────────┘
```

---

## 🎯 交互流程

### 左栏操作
1. **点击迭代** → 中栏显示该迭代的所有任务
2. **点击需求** → 中栏显示该需求的任务
3. **点击"未分配任务"** → 中栏显示所有未分配的任务
4. **展开/折叠图标** → 显示/隐藏需求列表

### 中栏操作
1. **搜索** → 根据关键词筛选任务
2. **类型筛选** → 筛选任务/需求/缺陷
3. **视图切换** → 切换列表/看板/树形视图
4. **点击任务** → 右栏显示任务详情

### 右栏操作
1. **查看详情** → 显示任务的完整信息
2. **内联编辑** → 直接修改状态、负责人等
3. **关闭面板** → 点击 ✕ 按钮

---

## 📊 数据结构

### 迭代节点结构
```typescript
interface IterationNode {
  id: number              // 迭代ID
  name: string            // 迭代名称
  status: string          // 迭代状态
  taskCount: number       // 任务数量
  expanded: boolean       // 是否展开
  requirements: {         // 需求列表
    id: number            // 需求ID
    title: string         // 需求标题
    taskCount: number     // 任务数量
  }[]
}
```

### 筛选逻辑
```typescript
// 根据选中的迭代/需求筛选任务
const filteredTasks = computed(() => {
  let result = tasks.value

  // 关键词搜索
  if (searchKeyword.value) {
    result = result.filter(t =>
      t.title.includes(searchKeyword.value) ||
      t.id.toString().includes(searchKeyword.value)
    )
  }

  // 类型筛选
  if (selectedTaskType.value) {
    result = result.filter(t => t.type === selectedTaskType.value)
  }

  // 迭代筛选
  if (selectedIterationId.value) {
    result = result.filter(t => t.iterationId === selectedIterationId.value)
  }

  // 需求筛选
  if (selectedRequirementId.value) {
    result = result.filter(t =>
      t.parentId === selectedRequirementId.value ||
      t.id === selectedRequirementId.value
    )
  }

  return result
})
```

---

## 🎨 样式特点

### 左栏样式
- **固定宽度**：220px（最小/最大都固定）
- **边框**：右边框分隔
- **背景**：白色
- **内边距**：8px（树容器）

### 迭代节点样式
- **状态边框**：左边框3px，颜色根据状态变化
- **悬停效果**：背景变为#f5f5f5
- **激活状态**：背景变为#e6f7ff

### 需求节点样式
- **图标**：绿色文档图标
- **缩进**：padding-left: 20px
- **字体大小**：13px
- **颜色**：默认#666，激活时#1890ff

---

## ✨ 核心优势

### 1. 信息层次清晰
- 左栏：导航和筛选（迭代+需求）
- 中栏：主要工作区（任务列表）
- 右栏：详情查看（任务详情）

### 2. 操作效率提升
- 左栏导航 → 快速定位
- 中栏列表 → 批量操作
- 右栏详情 → 查看编辑

### 3. 视觉负担减轻
- 固定宽度，布局稳定
- 简化结构，信息聚焦
- 状态标识，一目了然

---

## 🔧 技术实现

### 新增方法
```typescript
// 选择未分配任务
const handleSelectUnassigned = () => {
  selectedIterationId.value = null
  selectedRequirementId.value = null
}

// 选择迭代
const handleSelectIteration = (iterationId: number) => {
  selectedIterationId.value = iterationId
  selectedRequirementId.value = null
}

// 选择需求
const handleSelectRequirement = (requirement: { id: number; title: string }) => {
  selectedRequirementId.value = requirement.id
}

// 全部展开/折叠
const expandAll = () => { /* ... */ }
const collapseAll = () => { /* ... */ }

// 获取迭代状态相关方法
const getIterationStatusIcon = (status: string) => { /* ... */ }
const getIterationStatusTagType = (status: string) => { /* ... */ }
const getIterationStatusText = (status: string) => { /* ... */ }
```

### 数据计算属性
```typescript
// 迭代列表（包含需求）
const iterationList = computed<IterationNode[]>(() => {
  return iterations.value.map(iteration => {
    const requirements = tasks.value
      .filter(t => t.iterationId === iteration.id && t.type === 'REQUIREMENT')
      .map(req => ({
        id: req.id,
        title: req.title,
        taskCount: /* 计算任务数量 */
      }))

    return {
      id: iteration.id,
      name: iteration.name,
      status: iteration.status,
      taskCount: /* 计算任务数量 */,
      expanded: false,
      requirements
    }
  })
})

// 未分配的任务
const unassignedTasks = computed(() => {
  return tasks.value.filter(t => !t.iterationId)
})
```

---

## 📝 使用示例

### 场景1：查看某个迭代的任务
1. 在左栏点击"项目一期"
2. 中栏自动显示该迭代的所有任务
3. 可以继续筛选（按类型/状态）

### 场景2：查看某个需求的任务
1. 在左栏展开"项目一期"
2. 点击需求"用户登录功能"
3. 中栏显示该需求的任务列表

### 场景3：查看未分配的任务
1. 在左栏点击"未分配任务"
2. 中栏显示所有未分配的任务
3. 可以拖拽任务到迭代或需求

---

## 🎉 总结

本次重构成功实现了：

1. ✅ **左栏优化**
   - 固定220px宽度
   - 迭代+需求两级导航
   - 状态图标和标签
   - 全部展开/折叠功能

2. ✅ **中栏简化**
   - 移除标签页
   - 纯任务列表
   - 保留筛选和视图切换

3. ✅ **右栏保留**
   - 任务详情面板
   - 完整的查看和编辑功能

**核心价值：**
- 信息层次更清晰
- 操作流程更简洁
- 视觉负担更轻

**文件修改：**
- `frontend/src/views/project/TasksView.vue` - 完整重构

---

**版本：** v2.0
**完成日期：** 2026-02-07
