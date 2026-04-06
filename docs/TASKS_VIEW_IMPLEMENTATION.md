# 项目详情页 - 任务模块实现文档

## 概述

任务模块采用**三栏布局设计**，提供全方位的工作项管理功能，支持列表、看板、树形三种视图模式。

## 文件结构

```
frontend/src/views/project/
├── ProjectDetail.vue      # 项目详情页（主容器）
└── TasksView.vue          # 任务模块（三栏布局）
```

## 核心功能

### 1. 左栏：迭代树

**功能特性：**
- 显示所有迭代（包括"全部"节点）
- 显示每个迭代的任务数量
- 展开子节点，按类型分组（需求、任务、缺陷）
- 支持拖拽工作项到迭代
- 点击迭代筛选中栏任务列表

**交互：**
- 点击迭代节点：筛选该迭代的工作项
- 点击展开图标：展开/收起类型分组
- 拖拽工作项：移动工作项到该迭代

### 2. 中栏：工作项列表

**视图切换：**
- **列表视图**（默认）：表格形式，支持分页
- **看板视图**：按状态分列（待办、进行中、已完成）
- **树形视图**：显示层级关系（需求→任务）

**筛选功能：**
- 关键词搜索（编号、标题）
- 类型筛选（全部、任务、需求、缺陷）
- 迭代筛选（通过左栏选择）

**操作：**
- 点击行：查看详情
- 编辑按钮：打开编辑对话框
- 删除按钮：删除工作项
- 拖拽（看板视图）：拖拽卡片到不同状态列

### 3. 右栏：详情面板

**显示内容：**
- 工作项编号和标题
- 状态标签（可点击修改）
- 负责人头像和姓名
- 完整描述
- 关联关系（父级需求）
- 工时记录（预估 vs 实际）
- 元数据（创建时间、更新时间）

**交互：**
- 状态下拉菜单：快捷修改状态
- 点击父级需求：跳转到父级需求

### 4. 快速操作

**工具栏：**
- 搜索框：关键词搜索
- 类型筛选：下拉选择
- 视图切换：列表/看板/树形
- 新建按钮：创建工作项

**快捷键：**
- `N` - 新建工作项
- `F` - 聚焦搜索框
- `Esc` - 关闭详情面板

## 对话框

### 新建/编辑工作项对话框

**表单字段：**
- 工作项类型：需求/任务/缺陷
- 所属迭代：下拉选择（可选）
- 父级任务：下拉选择（仅需求可作为父级）
- 标题：必填
- 描述：多行文本
- 优先级：低/中/高
- 状态：待办/进行中/已完成
- 负责人：下拉选择
- 预估工时：数字输入（小时）

### 新建迭代对话框

**表单字段：**
- 迭代名称：必填
- 迭代描述：多行文本
- 计划开始时间：日期选择
- 计划结束时间：日期选择

## 技术实现

### API 调用

**任务相关：**
```typescript
import {
  getTasksByProjectId,    // 获取项目任务列表
  createTask,             // 创建任务
  updateTask,             // 更新任务
  deleteTask,             // 删除任务
  updateTaskStatus,       // 更新任务状态（拖拽）
  updateTaskIterationId   // 更新任务迭代ID（拖拽）
} from '@/api/task'
```

**迭代相关：**
```typescript
import {
  getIterationList,       // 获取迭代列表
  createIteration         // 创建迭代
} from '@/api/iteration'
```

**用户相关：**
```typescript
import { getAllUsers } from '@/api/user'
import { getProjectMembers } from '@/api/project'
```

### 状态管理

**响应式数据：**
```typescript
const tasks = ref<TaskInfo[]>([])              // 任务列表
const iterations = ref<IterationInfo[]>([])    // 迭代列表
const viewMode = ref<'list' | 'kanban' | 'tree'>('list')  // 视图模式
const searchKeyword = ref('')                  // 搜索关键词
const selectedTaskType = ref<string>()         // 选中的任务类型
const selectedIterationId = ref<number>()      // 选中的迭代ID
const selectedTask = ref<TaskInfo | null>(null)  // 选中的任务
```

**计算属性：**
```typescript
const iterationTree = computed(() => {
  // 构建迭代树结构（按类型分组）
})

const filteredTasks = computed(() => {
  // 根据筛选条件过滤任务
})

const kanbanColumns = computed(() => {
  // 看板列数据（按状态分组）
})

const treeTasks = computed(() => {
  // 树形视图数据（需求→子任务）
})
```

### 拖拽功能

**HTML5 Drag and Drop API：**

1. **开始拖拽：**
```typescript
const handleDragStart = (task: TaskInfo, event: DragEvent) => {
  draggedTask.value = task
  if (event.dataTransfer) {
    event.dataTransfer.setData('text/plain', task.id.toString())
    event.dataTransfer.effectAllowed = 'move'
  }
}
```

2. **拖拽到状态列（看板视图）：**
```typescript
const handleDropToStatus = async (event: DragEvent, status: string) => {
  if (draggedTask.value && draggedTask.value.status !== status) {
    await updateTaskStatus({ id: draggedTask.value.id, status })
    fetchTasks() // 刷新列表
  }
}
```

3. **拖拽到迭代（左栏）：**
```typescript
const handleDropToIteration = async (event: DragEvent, iterationId: number) => {
  if (draggedTask.value) {
    await updateTaskIterationId(draggedTask.value.id, iterationId)
    fetchTasks() // 刷新列表
  }
}
```

### 样式设计

**布局：**
- 三栏自适应布局（flexbox）
- 左栏固定宽度：280px
- 右栏固定宽度：400px
- 中栏自适应：flex: 1

**颜色规范：**
- 主色调：#1890ff（蓝色）
- 成功色：#52c41a（绿色）
- 警告色：#faad14（橙色）
- 危险色：#f5222d（红色）
- 信息色：#999999（灰色）

**交互反馈：**
- hover 状态：背景色变化、阴影提升
- active 状态：高亮背景色
- 拖拽状态：边框高亮、背景色变化

## 使用说明

### 基本流程

1. **查看工作项列表**
   - 默认显示列表视图
   - 使用搜索和筛选功能快速定位
   - 切换到看板或树形视图查看不同视角

2. **管理工作项**
   - 点击"新建工作项"按钮创建
   - 点击工作项查看详情
   - 在详情面板中编辑状态或点击"编辑"按钮

3. **组织工作项**
   - 在左栏选择或创建迭代
   - 拖拽工作项到不同迭代
   - 在看板视图中拖拽工作项到不同状态列

### 高级技巧

1. **快捷键操作**
   - 按 `N` 快速新建工作项
   - 按 `F` 聚焦搜索框
   - 按 `Esc` 关闭详情面板

2. **批量管理**
   - 在列表视图中勾选多个工作项
   - 使用批量操作功能（待实现）

3. **层级关系**
   - 创建子任务时选择父级需求
   - 在树形视图中查看层级结构
   - 点击父级需求链接快速跳转

## 待优化功能

### 短期优化

1. **评论系统**
   - 添加评论输入框
   - 显示评论列表（时间倒序）
   - 支持 @提及功能

2. **工时管理**
   - 从工时记录表获取实际工时
   - 显示剩余工时（预估 - 已用）
   - 支持工时录入快捷入口

3. **批量操作**
   - 批量修改状态
   - 批量分配负责人
   - 批量移动到迭代

### 长期优化

1. **高级筛选**
   - 多条件组合筛选
   - 自定义筛选器保存
   - 筛选条件分享

2. **视图自定义**
   - 自定义列显示
   - 自定义列排序
   - 保存视图配置

3. **协作功能**
   - 实时协作编辑
   - 工作项变更通知
   - @提及消息推送

## 性能优化

1. **数据分页**
   - 列表视图支持分页加载
   - 每页 10-50 条可配置

2. **虚拟滚动**
   - 大量数据时使用虚拟滚动
   - 提升渲染性能

3. **缓存策略**
   - 缓存任务列表数据
   - 减少重复请求

## 测试建议

1. **功能测试**
   - 创建、编辑、删除工作项
   - 拖拽功能（状态、迭代）
   - 视图切换
   - 搜索和筛选

2. **交互测试**
   - 快捷键操作
   - 详情面板展开/收起
   - 对话框表单验证

3. **性能测试**
   - 大量数据加载（1000+ 工作项）
   - 拖拽操作流畅度
   - 视图切换响应速度

## 相关文档

- [前端开发规范](../CLAUDE.md#前端开发规范)
- [项目详情页重构](./PROJECT_DETAIL_REDESIGN.md)
- [模块联动分析](./frontend-module-linkage-analysis.md)

## 更新日志

### 2026-02-07

- ✨ 新增任务模块（TasksView.vue）
- ✅ 实现三栏布局设计
- ✅ 支持列表、看板、树形三种视图
- ✅ 实现拖拽功能（状态、迭代）
- ✅ 添加快捷键支持
- ✅ 集成到项目详情页（"任务"标签）

### 待办事项

- [ ] 添加评论系统
- [ ] 完善工时管理
- [ ] 实现批量操作
- [ ] 优化性能（虚拟滚动）
- [ ] 添加更多筛选条件
