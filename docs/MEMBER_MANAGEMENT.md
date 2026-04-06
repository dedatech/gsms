# 项目成员管理模块实现文档

## 概述

本文档描述了项目详情页的成员管理模块的完整实现，包括前端组件和后端 API。

## 前端实现

### 文件位置
- `frontend/src/views/project/MembersView.vue` - 成员管理视图组件

### 核心功能

#### 1. 成员列表展示
- **卡片式布局**：响应式网格布局，每张卡片显示一个成员的信息
- **成员信息**：
  - 头像（自动生成首字母头像）
  - 姓名和邮箱
  - 项目角色（项目管理员/普通成员/只读访客）
- **统计数据**：
  - 任务数量
  - 总工时
  - 本周工时
  - 本月工时
- **贡献度**：基于完成任务数的星级评分（⭐⭐⭐⭐⭐）

#### 2. 搜索和筛选
- **关键词搜索**：按姓名或邮箱搜索
- **角色筛选**：按项目角色筛选成员
- **排序方式**：
  - 默认排序
  - 按任务数量排序
  - 按工时总数排序

#### 3. 成员详情
- **基本信息**：头像、姓名、角色、邮箱、用户名
- **工时统计**：总工时、本周工时、本月工时（带颜色区分）
- **任务列表**：显示该成员在项目中参与的所有任务
- **贡献度分析**：
  - 完成任务数
  - 贡献度等级（五星制）

#### 4. 成员管理操作
- **添加成员**：
  - 用户选择器（支持搜索）
  - 角色选择（项目管理员/普通成员/只读访客）
- **移除成员**：二次确认对话框
- **更改角色**：
  - 显示当前角色
  - 选择新角色
  - 自动更新

### 技术实现

#### 使用技术
- Vue 3 Composition API (`<script setup>`)
- TypeScript 类型定义
- Element Plus UI 组件库
- 响应式设计（CSS Grid）

#### 状态管理
```typescript
// 成员列表
const members = ref<any[]>([])

// 任务列表
const tasks = ref<TaskInfo[]>([])

// 成员统计缓存
const memberStats = ref<Record<number, {
  totalHours: number
  weekHours: number
  monthHours: number
  taskCount: number
  completedCount: number
}>>({})
```

#### 关键方法
- `fetchMembers()` - 获取成员列表
- `fetchTasks()` - 获取任务列表
- `calculateMemberStats()` - 计算成员统计数据
- `getMemberTaskCount(userId)` - 获取成员任务数
- `getMemberTotalHours(userId)` - 获取成员总工时
- `getContributionLevel(userId)` - 计算贡献度等级

## 后端实现

### API 端点

#### 1. 查询项目成员列表
```
GET /api/projects/{projectId}/members
```
响应：`List<ProjectMemberResp>`

#### 2. 添加项目成员
```
POST /api/projects/{projectId}/members?roleType={roleType}
Body: [userId1, userId2, ...]
```

#### 3. 移除项目成员
```
DELETE /api/projects/{projectId}/members/{userId}
```

#### 4. 更新成员角色
```
PUT /api/projects/{projectId}/members/{userId}?roleType={roleType}
```

#### 5. 获取成员统计信息（新增）
```
GET /api/projects/{projectId}/members/{userId}/stats
```
响应：`ProjectMemberStatsResp`
```json
{
  "userId": 1,
  "totalTasks": 10,
  "completedTasks": 8,
  "totalHours": 80.5,
  "weekHours": 20.0,
  "monthHours": 60.0
}
```

### 数据库查询

#### 新增 Mapper 方法

**TaskMapper.java**
```java
List<Task> selectByProjectIdAndAssigneeId(
    @Param("projectId") Long projectId,
    @Param("assigneeId") Long assigneeId
);
```

**WorkHourMapper.java**
```java
// 查询项目成员的总工时
Double selectTotalHoursByProjectIdAndUserId(
    @Param("projectId") Long projectId,
    @Param("userId") Long userId
);

// 查询项目成员在指定日期范围内的工时
Double selectTotalHoursByDateRange(
    @Param("projectId") Long projectId,
    @Param("userId") Long userId,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
);
```

### 服务层实现

**ProjectMemberService.java**
```java
// 新增接口方法
ProjectMemberStatsResp getMemberStats(Long projectId, Long userId);
```

**ProjectMemberServiceImpl.java**
实现统计信息计算：
- 任务总数和已完成数
- 总工时、本周工时、本月工时
- 基于日期范围的时间计算逻辑

## 使用方式

### 1. 在项目详情页中集成

```vue
<!-- ProjectDetail.vue -->
<template>
  <div v-else-if="activeModule === 'member'" class="module-content">
    <MembersView
      ref="membersViewRef"
      :project-id="projectId"
      @refresh="fetchMembers"
    />
  </div>
</template>

<script setup lang="ts">
import MembersView from '@/views/project/MembersView.vue'

const membersViewRef = ref<InstanceType<typeof MembersView>>()
</script>
```

### 2. 导航到成员视图

点击项目详情页的"成员"标签即可进入成员管理视图。

## 样式设计

### 颜色方案
- **主色调**：#1890ff（蓝色）
- **成功色**：#52c41a（绿色）
- **警告色**：#faad14（橙色）
- **信息色**：#1890ff（蓝色）
- **危险色**：#ff4d4f（红色）

### 卡片设计
- 圆角：8px
- 阴影：0 1px 2px rgba(0, 0, 0, 0.03)
- 悬停效果：上移 2px，增强阴影
- 过渡动画：0.3s

### 响应式布局
```css
.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
}
```

## 数据流程

1. **初始化**：
   - 获取项目成员列表
   - 获取项目任务列表
   - 获取可用用户列表

2. **计算统计**：
   - 遍历每个成员
   - 查询任务统计（总数、已完成数）
   - 查询工时统计（总工时、本周工时、本月工时）
   - 缓存统计结果

3. **渲染**：
   - 使用计算属性过滤和排序成员
   - 绑定统计数据到卡片
   - 响应用户交互

## 性能优化

1. **统计缓存**：避免重复计算，将统计结果缓存在 `memberStats` 对象中
2. **计算属性**：使用 `computed` 自动处理搜索和筛选
3. **异步加载**：使用 `Promise.all` 并行请求数据
4. **懒加载**：统计信息按需计算

## 扩展功能建议

1. **活跃度趋势图**：使用 ECharts 绘制成员活跃度折线图
2. **成员技能标签**：展示成员的技能和专业领域
3. **工作时间分布**：可视化成员的工作时间分布
4. **成员绩效报告**：导出成员的详细绩效报告
5. **批量操作**：批量添加/移除成员

## 测试建议

### 单元测试
- 测试统计计算逻辑
- 测试搜索和筛选功能
- 测试排序功能

### 集成测试
- 测试添加成员流程
- 测试移除成员流程
- 测试更改角色流程

### UI 测试
- 测试响应式布局
- 测试对话框交互
- 测试表单验证

## 常见问题

### Q: 为什么贡献度是五星制？
A: 基于完成任务数自动计算：
- 1 星：1-4 个任务
- 2 星：5-9 个任务
- 3 星：10-14 个任务
- 4 星：15-19 个任务
- 5 星：20+ 个任务

### Q: 工时统计为什么有时为 0？
A: 可能原因：
- 成员未记录工时
- 时间范围内没有工时记录
- 数据同步延迟

### Q: 如何更改贡献度计算规则？
A: 修改 `MembersView.vue` 中的 `getContributionLevel` 方法：
```typescript
const getContributionLevel = (userId: number) => {
  const completedCount = getCompletedTaskCount(userId)
  // 自定义规则
  if (completedCount >= 50) return 5
  if (completedCount >= 30) return 4
  // ...
}
```

## 更新日志

### 2026-02-07
- ✅ 创建成员管理视图组件
- ✅ 实现成员列表展示
- ✅ 实现搜索和筛选功能
- ✅ 实现成员详情对话框
- ✅ 实现添加/移除成员功能
- ✅ 实现更改角色功能
- ✅ 添加后端统计 API
- ✅ 添加数据库查询方法
- ✅ 集成到项目详情页

## 相关文件

### 前端
- `frontend/src/views/project/MembersView.vue` - 成员管理视图
- `frontend/src/views/project/ProjectDetail.vue` - 项目详情页
- `frontend/src/api/project.ts` - 项目 API

### 后端
- `backend/src/main/java/com/gsms/gsms/controller/ProjectController.java` - 项目控制器
- `backend/src/main/java/com/gsms/gsms/service/ProjectMemberService.java` - 成员服务接口
- `backend/src/main/java/com/gsms/gsms/service/impl/ProjectMemberServiceImpl.java` - 成员服务实现
- `backend/src/main/java/com/gsms/gsms/dto/project/ProjectMemberStatsResp.java` - 统计响应 DTO
- `backend/src/main/java/com/gsms/gsms/repository/TaskMapper.java` - 任务 Mapper
- `backend/src/main/java/com/gsms/gsms/repository/WorkHourMapper.java` - 工时 Mapper
- `backend/src/main/resources/mapper/TaskMapper.xml` - 任务 SQL 映射
- `backend/src/main/resources/mapper/WorkHourMapper.xml` - 工时 SQL 映射
