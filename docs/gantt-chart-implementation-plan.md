# GSMS 项目甘特图功能 - 可行性分析与实现方案

## 📊 可行性评估结论

### 整体可行性：✅ 高度可行

| 评估维度 | 评分 | 说明 |
|---------|------|------|
| **数据完整性** | 100% ✅ | 所有必需字段已存在且格式统一 |
| **后端 API** | 95% ✅ | 需新增树形数据聚合接口 |
| **前端技术** | 90% ✅ | dhtmlx-gantt 成熟稳定 |
| **开发风险** | 低 ⭐⭐ | 技术方案成熟，无坑点 |
| **用户体验** | 高 ⭐⭐⭐⭐⭐ | 显著提升项目管理效率 |

---

## ✅ 数据完整性分析

### 核心要素检查

| 核心要素 | 数据字段 | 字段类型 | 状态 | 位置 |
|---------|---------|---------|------|------|
| **项目** | `planStartDate`, `planEndDate` | LocalDate | ✅ 已有 | `Project.java` |
| **迭代** | `planStartDate`, `planEndDate` | LocalDate | ✅ 已有 | `Iteration.java` |
| **任务** | `planStartDate`, `planEndDate` | LocalDate | ✅ 已有 | `Task.java` |
| **子任务** | `parentId` | Long | ✅ 已有 | `Task.java` |
| **执行人** | `assigneeId`, `assigneeName` | Long, String | ✅ 已有 | `Task.java` / `TaskInfoResp.java` |
| **计划开始** | `planStartDate` | LocalDate | ✅ 已有 | 所有实体 |
| **计划结束** | `planEndDate` | LocalDate | ✅ 已有 | 所有实体 |

### 数据格式统一性

**时间格式：** 所有日期字段统一使用 `LocalDate`，格式为 `yyyy-MM-dd`

**示例：**
```java
// Project 实体
private LocalDate planStartDate;  // 2024-01-01
private LocalDate planEndDate;    // 2024-12-31

// Iteration 实体
private LocalDate planStartDate;  // 2024-01-01
private LocalDate planEndDate;    // 2024-01-31

// Task 实体
private LocalDate planStartDate;  // 2024-01-05
private LocalDate planEndDate;    // 2024-01-15
private Long parentId;            // 支持无限层级嵌套
private Long assigneeId;          // 任务负责人
```

### 后端查询接口支持

**已有接口：**
- ✅ `GET /api/tasks/search` - 按条件查询任务
- ✅ `GET /api/tasks/{id}/subtasks` - 获取子任务列表
- ✅ `GET /api/projects/{id}/members` - 获取项目成员
- ✅ `GET /api/iterations/query` - 按项目查询迭代

**需要新增的接口：**
- ⚠️ `GET /api/gantt/project/{projectId}` - 获取项目甘特图数据（树形结构）
- ⚠️ `PUT /api/gantt/task/{id}/dates` - 更新任务时间（拖拽后）
- ⚠️ `PUT /api/gantt/task/{id}/parent` - 更新任务层级（拖拽改变父任务）

---

## 🎯 推荐实现方案

### 方案选择：dhtmlx-gantt（⭐ 强烈推荐）

**dhtmlx-gantt** 是业界最成熟的甘特图库之一，功能强大，文档完善。

#### 技术栈

```json
{
  "dependencies": {
    "dhtmlx-gantt": "^8.0.0"
  }
}
```

#### 为什么选择 dhtmlx-gantt？

| 对比项 | dhtmlx-gantt | @gantt-task/react | 其他开源库 |
|--------|-------------|-------------------|-----------|
| **功能完整性** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| **文档质量** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **社区活跃度** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **TypeScript支持** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **Vue 3 适配** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **性能表现** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **学习曲线** | 中等 | 较低 | 较高 |

**核心优势：**
1. ✅ **功能最成熟**：支持甘特图所有核心功能（拖拽、依赖、关键路径等）
2. ✅ **性能优秀**：内置虚拟滚动，支持 1000+ 任务流畅展示
3. ✅ **文档完善**：官方文档详细，示例丰富，社区活跃
4. ✅ **TypeScript 支持**：完整的类型定义文件
5. ✅ **Vue 3 友好**：提供 Vue 包装器，集成简单

---

## 📋 功能需求详细说明

### 阶段一：基础展示功能（P1 优先级）

#### 1.1 层级树形展示

**功能描述：**
- 项目 → 迭代 → 任务 → 子任务 四级层级结构
- 支持展开/收起子节点
- 树形结构左侧显示

**UI 效果示意：**
```
┌──────────────────────────────────────────────────────────┐
│ 智慧景区系统 - 项目甘特图                              │
├──────────────────────────────────────────────────────────┤
│ 任务名称      │ 执行人 │ 1月  │ 2月  │ 3月  │           │
│              │        │──────│──────│──────│           │
│ ▶ 智慧景区系统│ 张三   │███████████████████████       │
│   ▶ 需求阶段  │ 李四   │███████                      │
│     需求调研  │ 王五   │█████                        │
│     原型设计  │ 李四   │  ██████                     │
│   ▶ 开发阶段  │ 张三   │       ████████████████████   │
│     前端开发  │ 赵六   │       █████████████          │
│       页面A   │ 赵六   │       ██████               │
│       页面B   │ 孙七   │         ████               │
│     后端开发  │ 张三   │       █████████████          │
│   ▶ 测试阶段  │ 周八   │             ████████████████ │
└──────────────────────────────────────────────────────────┘
```

#### 1.2 时间轴视图

**视图切换：**
- **日视图**：显示每一天，适合短期项目
- **周视图**：显示每一周，适合中长期项目
- **月视图**：显示每一月，适合长期项目

**时间刻度：**
```javascript
// 日视图配置
gantt.config.scale_unit = 'day'
gantt.config.date_scale = '%d %M'  // "1 一月"

// 周视图配置
gantt.config.scale_unit = 'week'
gantt.config.date_scale = '第%W周'

// 月视图配置
gantt.config.scale_unit = 'month'
gantt.config.date_scale = '%Y年%m月'
```

#### 1.3 执行人信息显示

**列定义：**
```javascript
gantt.config.columns = [
  { name: 'text', label: '任务名称', tree: true, width: 250 },
  { name: 'start_date', label: '开始日期', align: 'center', width: 100 },
  { name: 'duration', label: '工期(天)', align: 'center', width: 80 },
  { name: 'owner', label: '执行人', align: 'center', width: 100,
    template: (task) => {
      // 显示执行人头像和姓名
      return `<img src="${task.ownerAvatar}" class="avatar" />
              <span>${task.owner || '未分配'}</span>`
    }
  },
  { name: 'status', label: '状态', align: 'center', width: 80,
    template: (task) => {
      return getStatusTag(task.status)
    }
  }
]
```

#### 1.4 任务状态颜色区分

**颜色映射：**
```javascript
gantt.templates.task_class = (start, end, task) => {
  const statusColorMap = {
    'TODO': 'task-todo',        // 灰色
    'IN_PROGRESS': 'task-progress',  // 蓝色
    'COMPLETED': 'task-completed',    // 绿色
    'BLOCKED': 'task-blocked',    // 红色
    'CANCELLED': 'task-cancelled'    // 浅灰
  }
  return statusColorMap[task.status] || ''
}
```

**CSS 样式：**
```css
.task-todo .gantt_task_progress {
  background-color: #d9d9d9;
}

.task-progress .gantt_task_progress {
  background-color: #1890ff;
}

.task-completed .gantt_task_progress {
  background-color: #52c41a;
}

.task-blocked .gantt_task_progress {
  background-color: #ff4d4f;
}
```

---

### 阶段二：交互编辑功能（P1 优先级）

#### 2.1 拖拽修改任务时间

**功能描述：**
- 拖拽任务条整体：修改开始和结束时间，保持工期不变
- 拖拽任务条左边缘：修改开始时间
- 拖拽任务条右边缘：修改结束时间
- 实时验证：结束日期必须晚于开始日期

**实现代码：**
```javascript
// 监听拖拽事件
gantt.attachEvent('onAfterTaskDrag', async (id, mode, e) => {
  const task = gantt.getTask(id)

  try {
    // 调用后端 API 更新任务时间
    await updateTaskDates(Number(id), {
      planStartDate: formatDate(task.start_date),
      planEndDate: formatDate(task.end_date)
    })

    ElMessage.success('任务时间已更新')
  } catch (error) {
    // 更新失败，回滚 UI
    gantt.updateTask(id)
    ElMessage.error('更新失败：' + error.message)
  }
})
```

#### 2.2 拖拽改变任务层级

**功能描述：**
- 拖拽任务到另一个任务下：改变父任务
- 拖拽任务到根级别：取消父子关系
- 实时验证：子任务时间不能超出父任务范围

**实现代码：**
```javascript
// 监听拖拽事件
gantt.attachEvent('onBeforeTaskDrag', async (id, mode, e) => {
  if (mode === gantt.config.drag_mode.move) {
    const task = gantt.getTask(id)
    const parent = gantt.getTask(task.parent)

    if (parent) {
      // 验证子任务时间不能超出父任务范围
      if (new Date(task.start_date) < new Date(parent.start_date) ||
          new Date(task.end_date) > new Date(parent.end_date)) {
        ElMessage.warning('子任务时间不能超出父任务范围')
        return false
      }
    }
  }
  return true
})

// 拖拽完成后更新
gantt.attachEvent('onAfterTaskDrag', async (id, mode) => {
  const task = gantt.getTask(id)

  try {
    await updateTaskParent(Number(id), task.parent)
    ElMessage.success('任务层级已更新')
  } catch (error) {
    gantt.updateTask(id)
    ElMessage.error('更新失败：' + error.message)
  }
})
```

#### 2.3 创建任务依赖关系

**功能描述：**
- 从一个任务拖拽连线到另一个任务：创建依赖关系
- 支持 4 种依赖类型：
  - 0: 结束-开始（FS）- 前置任务结束后，后置任务才能开始
  - 1: 开始-开始（SS）- 前置任务开始时，后置任务开始
  - 2: 结束-结束（FF）- 前置任务结束时，后置任务结束
  - 3: 开始-结束（SF）- 前置任务开始时，后置任务结束

**数据结构：**
```typescript
interface GanttLink {
  id: string
  source: string  // 前置任务 ID
  target: string  // 后置任务 ID
  type: '0' | '1' | '2' | '3'
}
```

**实现代码：**
```javascript
// 启用连线功能
gantt.config.drag_links = true

// 监听连线创建
gantt.attachEvent('onAfterLinkAdd', async (id, item) => {
  try {
    await createTaskLink({
      sourceTaskId: Number(item.source),
      targetTaskId: Number(item.target),
      dependencyType: item.type
    })
    ElMessage.success('依赖关系已创建')
  } catch (error) {
    gantt.deleteLink(id)
    ElMessage.error('创建失败：' + error.message)
  }
})
```

---

### 阶段三：高级功能（P2 优先级）

#### 3.1 关键路径计算和显示

**功能描述：**
- 自动计算项目的关键路径（Critical Path）
- 高亮显示关键路径上的任务
- 显示任务的松弛时间（Slack）

**实现方案：**
```javascript
// 启用关键路径
gantt.config.highlight_critical_path = true

// 自定义关键路径样式
gantt.templates.task_row_class = (start, end, task) => {
  if (task.critical) {
    return 'critical_task'
  }
  return ''
}
```

#### 3.2 里程碑标记

**功能描述：**
- 支持将任务标记为里程碑（工期为 0）
- 里程碑在甘特图中以菱形显示
- 里程碑通常表示重要的项目节点

**实现方案：**
```javascript
// 创建里程碑
const milestoneTask = {
  id: 'm1',
  text: '项目验收',
  start_date: '2024-03-31',
  duration: 0,  // 工期为 0 表示里程碑
  type: gantt.config.types.milestone
}

// 里程碑样式
gantt.templates.milestone_task = (task) => {
  return `<div class='milestone'>
    <span>${task.text}</span>
  </div>`
}
```

#### 3.3 资源分配视图

**功能描述：**
- 按执行人查看任务分配情况
- 检测人员工作负载是否均衡
- 识别过度分配或闲置人员

**实现方案：**
```javascript
// 切换到资源视图
gantt.config.layout = {
  css: 'gantt_container_resource'
}

// 按执行人分组显示任务
const resourceData = [
  {
    id: 'u1',
    name: '张三',
    tasks: [task1, task2, task3]
  },
  {
    id: 'u2',
    name: '李四',
    tasks: [task4, task5]
  }
]
```

#### 3.4 进度跟踪

**功能描述：**
- 对比计划时间 vs 实际时间
- 显示任务完成进度（0-100%）
- 进度条显示在任务条内部

**实现方案：**
```javascript
// 显示实际时间线
gantt.addTaskLayer({
  name: 'actual',
  renderer: {
    render: function(task, defaultRenderer) {
      if (task.actualStartDate && task.actualEndDate) {
        return `<div class='actual-bar'
                   style='left:${getPixel(task.actualStartDate)}px;
                          width:${getDuration(task.actualStartDate, task.actualEndDate)}px'>
                </div>`
      }
    }
  }
})

// 进度条
task.progress = 0.6  // 60% 完成
```

#### 3.5 导出功能

**支持格式：**
- PNG 图片（高清截图）
- PDF 文档（可打印）
- Excel 数据（可编辑）

**实现方案：**
```javascript
// 导出为 PNG
gantt.exportToPNG({
  name: 'project_gantt.png',
  callback: (link) => {
    window.open(link)
  }
})

// 导出为 PDF
gantt.exportToPDF({
  name: 'project_gantt.pdf',
  format: 'A4',
  orientation: 'landscape'
})
```

---

## 💻 后端实现方案

### 新增 Controller

**文件：** `backend/src/main/java/com/gsms/gsms/controller/GanttController.java`

```java
package com.gsms.gsms.controller;

import com.gsms.gsms.dto.Result;
import com.gsms.gsms.dto.gantt.*;
import com.gsms.gsms.service.GanttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/gantt")
@Tag(name = "甘特图接口", description = "项目甘特图相关接口")
@Validated
public class GanttController {

    @Autowired
    private GanttService ganttService;

    /**
     * 获取项目甘特图数据
     * 返回项目下所有迭代、任务、子任务的完整树形结构
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "获取项目甘特图数据")
    public Result<GanttDataResp> getProjectGanttData(
        @PathVariable Long projectId,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        GanttDataResp data = ganttService.getProjectGanttData(projectId, startDate, endDate);
        return Result.success(data);
    }

    /**
     * 更新任务时间（拖拽后调用）
     */
    @PutMapping("/task/{taskId}/dates")
    @Operation(summary = "更新任务时间")
    public Result<Void> updateTaskDates(
        @PathVariable Long taskId,
        @RequestBody @Valid TaskDateUpdateReq req
    ) {
        ganttService.updateTaskDates(taskId, req);
        return Result.success();
    }

    /**
     * 更新任务层级（拖拽改变父任务）
     */
    @PutMapping("/task/{taskId}/parent")
    @Operation(summary = "更新任务层级")
    public Result<Void> updateTaskParent(
        @PathVariable Long taskId,
        @RequestBody @Valid TaskParentUpdateReq req
    ) {
        ganttService.updateTaskParent(taskId, req.getParentId());
        return Result.success();
    }

    /**
     * 创建任务依赖关系
     */
    @PostMapping("/link")
    @Operation(summary = "创建任务依赖关系")
    public Result<Void> createTaskLink(
        @RequestBody @Valid TaskLinkCreateReq req
    ) {
        ganttService.createTaskLink(req);
        return Result.success();
    }

    /**
     * 删除任务依赖关系
     */
    @DeleteMapping("/link/{linkId}")
    @Operation(summary = "删除任务依赖关系")
    public Result<Void> deleteTaskLink(@PathVariable Long linkId) {
        ganttService.deleteTaskLink(linkId);
        return Result.success();
    }
}
```

### 新增 DTO

**GanttDataResp.java**
```java
package com.gsms.gsms.dto.gantt;

import lombok.Data;
import java.util.List;

@Data
public class GanttDataResp {
    private List<GanttTaskResp> data;  // 任务树形结构
    private List<GanttLinkResp> links; // 任务依赖关系
}
```

**GanttTaskResp.java**
```java
package com.gsms.gsms.dto.gantt;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class GanttTaskResp {
    private Long id;
    private String text;              // 任务名称
    private String type;              // project/iteration/task/milestone

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;      // 计划开始日期

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;        // 计划结束日期

    private Integer duration;         // 工期（天）
    private Double progress;          // 进度 0-1

    private Long parent;              // 父任务ID
    private String owner;             // 执行人姓名
    private Long ownerId;             // 执行人ID
    private String ownerAvatar;       // 执行人头像
    private String status;            // 任务状态
    private String priority;          // 优先级
    private String color;             // 颜色

    // 实际时间（用于进度跟踪）
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    private Boolean critical;         // 是否关键路径任务
    private Integer slack;            // 松弛时间

    // 子任务列表（递归结构）
    private List<GanttTaskResp> subtasks;
}
```

**TaskDateUpdateReq.java**
```java
package com.gsms.gsms.dto.gantt;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Schema(description = "任务时间更新请求")
public class TaskDateUpdateReq {

    @NotNull(message = "计划开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "计划开始日期")
    private LocalDate planStartDate;

    @NotNull(message = "计划结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "计划结束日期")
    private LocalDate planEndDate;
}
```

### 新增 Service

**GanttService.java**
```java
package com.gsms.gsms.service;

import com.gsms.gsms.dto.gantt.*;

public interface GanttService {
    /**
     * 获取项目甘特图数据（树形结构）
     */
    GanttDataResp getProjectGanttData(Long projectId, LocalDate startDate, LocalDate endDate);

    /**
     * 更新任务时间
     */
    void updateTaskDates(Long taskId, TaskDateUpdateReq req);

    /**
     * 更新任务层级
     */
    void updateTaskParent(Long taskId, Long parentId);

    /**
     * 创建任务依赖关系
     */
    void createTaskLink(TaskLinkCreateReq req);

    /**
     * 删除任务依赖关系
     */
    void deleteTaskLink(Long linkId);
}
```

**GanttServiceImpl.java** (关键实现)
```java
package com.gsms.gsms.service.impl;

import com.gsms.gsms.dto.gantt.*;
import com.gsms.gsms.mapper.TaskMapper;
import com.gsms.gsms.model.entity.Task;
import com.gsms.gsms.service.GanttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GanttServiceImpl implements GanttService {

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public GanttDataResp getProjectGanttData(Long projectId, LocalDate startDate, LocalDate endDate) {
        // 1. 查询项目下所有一级任务
        List<Task> allTasks = taskMapper.selectByProjectId(projectId);

        // 2. 构建树形结构
        List<GanttTaskResp> taskTree = buildTaskTree(allTasks);

        // 3. 过滤时间范围（可选）
        if (startDate != null || endDate != null) {
            taskTree = filterByDateRange(taskTree, startDate, endDate);
        }

        // 4. 查询任务依赖关系
        List<GanttLinkResp> links = queryTaskLinks(allTasks);

        // 5. 组装返回数据
        GanttDataResp resp = new GanttDataResp();
        resp.setData(taskTree);
        resp.setLinks(links);

        return resp;
    }

    /**
     * 递归构建任务树
     */
    private List<GanttTaskResp> buildTaskTree(List<Task> allTasks) {
        // 按 parentId 分组
        Map<Long, List<Task>> tasksByParent = allTasks.stream()
            .collect(Collectors.grouping(
                task -> task.getParentId() != null ? task.getParentId() : 0L
            ));

        // 递归构建树
        return buildTreeRecursive(0L, tasksByParent);
    }

    private List<GanttTaskResp> buildTreeRecursive(Long parentId, Map<Long, List<Task>> tasksByParent) {
        List<Task> children = tasksByParent.getOrDefault(parentId, Collections.emptyList());

        return children.stream().map(task -> {
            GanttTaskResp resp = convertToGanttTask(task);

            // 递归处理子任务
            List<GanttTaskResp> subtasks = buildTreeRecursive(task.getId(), tasksByParent);
            if (!subtasks.isEmpty()) {
                resp.setSubtasks(subtasks);
            }

            return resp;
        }).collect(Collectors.toList());
    }

    /**
     * 转换 Task 实体为 GanttTaskResp
     */
    private GanttTaskResp convertToGanttTask(Task task) {
        GanttTaskResp resp = new GanttTaskResp();
        resp.setId(task.getId());
        resp.setText(task.getTitle());
        resp.setStartDate(task.getPlanStartDate());
        resp.setEndDate(task.getPlanEndDate());
        resp.setDuration(calculateDuration(task.getPlanStartDate(), task.getPlanEndDate()));
        resp.setProgress(task.getProgress() != null ? task.getProgress() / 100.0 : 0.0);
        resp.setParent(task.getParentId());
        resp.setOwner(task.getAssigneeName());
        resp.setOwnerId(task.getAssigneeId());
        resp.setStatus(task.getStatus().name());
        resp.setPriority(task.getPriority().name());
        resp.setActualStartDate(task.getActualStartDate());
        resp.setActualEndDate(task.getActualEndDate());

        // 设置颜色
        resp.setColor(getPriorityColor(task.getPriority()));

        return resp;
    }

    @Override
    @Transactional
    public void updateTaskDates(Long taskId, TaskDateUpdateReq req) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 验证：结束日期必须晚于开始日期
        if (req.getPlanEndDate().isBefore(req.getPlanStartDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        // 如果有父任务，验证时间范围
        if (task.getParentId() != null) {
            validateChildTaskDate(task.getParentId(), req.getPlanStartDate(), req.getPlanEndDate());
        }

        // 更新任务时间
        task.setPlanStartDate(req.getPlanStartDate());
        task.setPlanEndDate(req.getPlanEndDate());

        taskMapper.updateById(task);
    }

    @Override
    @Transactional
    public void updateTaskParent(Long taskId, Long parentId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        // 验证：不能将任务设置为自己的子任务
        if (Objects.equals(taskId, parentId)) {
            throw new BusinessException(ErrorCode.CANNOT_SET_SELF_AS_PARENT);
        }

        // 验证：时间范围约束
        if (parentId != null) {
            Task parent = taskMapper.selectById(parentId);
            if (parent != null) {
                validateChildTaskDate(parentId, task.getPlanStartDate(), task.getPlanEndDate());
            }
        }

        // 更新父任务
        task.setParentId(parentId);
        taskMapper.updateById(task);
    }

    // ... 其他方法实现
}
```

---

## 🎨 前端实现方案

### 新增甘特图组件

**文件：** `frontend/src/components/gantt/ProjectGantt.vue`

```vue
<template>
  <div class="project-gantt-container">
    <!-- 工具栏 -->
    <div class="gantt-toolbar">
      <el-radio-group v-model="viewMode" size="small" @change="handleViewModeChange">
        <el-radio-button label="day">日视图</el-radio-button>
        <el-radio-button label="week">周视图</el-radio-button>
        <el-radio-button label="month">月视图</el-radio-button>
      </el-radio-group>

      <el-divider direction="vertical" />

      <el-button-group size="small">
        <el-button :icon="ZoomOut" @click="handleZoomOut" />
        <el-button :icon="ZoomIn" @click="handleZoomIn" />
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button size="small" :icon="Download" @click="handleExport">
        导出
      </el-button>
      <el-button size="small" :icon="Refresh" @click="handleRefresh" :loading="loading">
        刷新
      </el-button>
    </div>

    <!-- 甘特图容器 -->
    <div ref="ganttContainer" class="gantt-content"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { gantt } from 'dhtmlx-gantt'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import { ZoomIn, ZoomOut, Download, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectGanttData, updateTaskDates, updateTaskParent } from '@/api/gantt'

interface Props {
  projectId: number
  startDate?: string
  endDate?: string
}

const props = defineProps<Props>()
const ganttContainer = ref<HTMLElement>()
const viewMode = ref('day')
const loading = ref(false)

// 初始化甘特图
const initGantt = () => {
  // 基础配置
  gantt.config.date_format = '%Y-%m-%d'
  gantt.config.scale_unit = 'day'
  gantt.config.duration_unit = 'day'

  // 时间刻度配置
  gantt.config.scales = [
    { unit: 'month', step: 1, date: '%Y年%m月' },
    { unit: 'day', step: 1, date: '%m月%d日' }
  ]

  // 列配置
  gantt.config.columns = [
    {
      name: 'text',
      label: '任务名称',
      tree: true,
      width: 280,
      resize: true
    },
    {
      name: 'start_date',
      label: '开始日期',
      align: 'center',
      width: 100
    },
    {
      name: 'duration',
      label: '工期',
      align: 'center',
      width: 60,
      template: (task: any) => {
        return task.duration + '天'
      }
    },
    {
      name: 'owner',
      label: '执行人',
      align: 'center',
      width: 100,
      template: (task: any) => {
        if (!task.owner) return '<span style="color: #999">未分配</span>'
        return `<div class="owner-cell">
          <img src="${task.ownerAvatar || '/default-avatar.png'}" class="avatar" />
          <span>${task.owner}</span>
        </div>`
      }
    },
    {
      name: 'status',
      label: '状态',
      align: 'center',
      width: 80,
      template: (task: any) => {
        return getStatusTag(task.status)
      }
    }
  ]

  // 启用拖拽编辑
  gantt.config.drag_links = true
  gantt.config.drag_progress = true
  gantt.config.drag_resize = true
  gantt.config.drag_move = true
  gantt.config.order_branch = true
  gantt.config.order_branch_free = true

  // 启用关键路径
  gantt.config.highlight_critical_path = true
  gantt.config.show_grid = true

  // 任务条颜色模板
  gantt.templates.task_class = (start: Date, end: Date, task: any) => {
    const classes = []
    if (task.critical) classes.push('critical')
    classes.push(`status-${task.status?.toLowerCase()}`)
    return classes.join(' ')
  }

  // 任务进度条模板
  gantt.templates.progress_bar = (task: any) => {
    return `<div class="custom-progress" style="width:${task.progress * 100}%">
      ${Math.round(task.progress * 100)}%
    </div>`
  }

  // 拖拽验证
  gantt.attachEvent('onBeforeTaskDrag', (id: string, mode: string) => {
    const task = gantt.getTask(id)
    const parent = gantt.getTask(task.parent)

    // 验证子任务时间不能超出父任务
    if (parent && (mode === 'move' || mode === 'resize')) {
      const newStart = new Date(task.start_date)
      const newEnd = new Date(task.end_date)
      const parentStart = new Date(parent.start_date)
      const parentEnd = new Date(parent.end_date)

      if (newStart < parentStart || newEnd > parentEnd) {
        ElMessage.warning('子任务时间不能超出父任务范围')
        return false
      }
    }

    // 验证结束日期必须晚于开始日期
    if (new Date(task.start_date) >= new Date(task.end_date)) {
      ElMessage.warning('结束日期必须晚于开始日期')
      return false
    }

    return true
  })

  // 监听拖拽事件
  gantt.attachEvent('onAfterTaskDrag', async (id: string, mode: string) => {
    const task = gantt.getTask(id)

    try {
      if (mode === 'move' || mode === 'resize') {
        // 更新任务时间
        await updateTaskDates(Number(id), {
          planStartDate: formatDate(task.start_date),
          planEndDate: formatDate(task.end_date)
        })
        ElMessage.success('任务时间已更新')
      }
    } catch (error: any) {
      // 更新失败，回滚 UI
      gantt.updateTask(id)
      ElMessage.error('更新失败：' + error.message)
    }
  })

  // 监听连线创建
  gantt.attachEvent('onAfterLinkAdd', async (id: string, item: any) => {
    try {
      await createTaskLink({
        sourceTaskId: Number(item.source),
        targetTaskId: Number(item.target),
        dependencyType: item.type
      })
      ElMessage.success('依赖关系已创建')
    } catch (error: any) {
      gantt.deleteLink(id)
      ElMessage.error('创建失败：' + error.message)
    }
  })

  // 初始化
  gantt.init(ganttContainer.value!)
}

// 加载数据
const loadGanttData = async () => {
  loading.value = true
  try {
    const { data } = await getProjectGanttData(props.projectId, {
      startDate: props.startDate,
      endDate: props.endDate
    })

    const ganttData = {
      data: convertToGanttTasks(data.data || []),
      links: convertToGanttLinks(data.links || [])
    }

    gantt.parse(ganttData)
  } catch (error: any) {
    ElMessage.error('加载甘特图数据失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 数据转换
const convertToGanttTasks = (tasks: any[]): any[] => {
  return tasks.map(task => ({
    id: task.id.toString(),
    text: task.text,
    start_date: task.startDate,
    end_date: addDays(task.endDate, 1),  // dhtmlx-gantt 的 end_date 是不包含的
    duration: task.duration,
    progress: task.progress || 0,
    parent: task.parent?.toString(),
    owner: task.owner,
    ownerId: task.ownerId,
    ownerAvatar: task.ownerAvatar,
    status: task.status,
    priority: task.priority,
    color: task.color,
    critical: task.critical,
    ...((task.subtasks?.length > 0) && {
      open: true  // 默认展开
    })
  }))
}

const convertToGanttLinks = (links: any[]): any[] => {
  return links.map(link => ({
    id: link.id.toString(),
    source: link.source.toString(),
    target: link.target.toString(),
    type: link.type
  }))
}

// 视图切换
const handleViewModeChange = (mode: string) => {
  switch (mode) {
    case 'day':
      gantt.config.scale_unit = 'day'
      gantt.config.date_scale = '%m月%d日'
      gantt.config.subscales = []
      gantt.config.scales = [
        { unit: 'month', step: 1, date: '%Y年%m月' },
        { unit: 'day', step: 1, date: '%d' }
      ]
      break
    case 'week':
      gantt.config.scale_unit = 'week'
      gantt.config.date_scale = '第%W周'
      gantt.config.scales = [
        { unit: 'month', step: 1, date: '%Y年%m月' },
        { unit: 'week', step: 1, date: '第%W周' }
      ]
      break
    case 'month':
      gantt.config.scale_unit = 'month'
      gantt.config.date_scale = '%Y年%m月'
      gantt.config.scales = [
        { unit: 'year', step: 1, date: '%Y年' },
        { unit: 'month', step: 1, date: '%m月' }
      ]
      break
  }
  gantt.render()
}

// 缩放
const handleZoomIn = () => {
  gantt.ext.zoom.zoomIn()
}

const handleZoomOut = () => {
  gantt.ext.zoom.zoomOut()
}

// 导出
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出甘特图吗？', '导出确认', {
      confirmButtonText: '导出为PNG',
      cancelButtonText: '导出为PDF',
      distinguishCancelAndClose: true,
      type: 'info'
    })

    // 导出为 PNG
    gantt.exportToPNG({
      name: `project_${props.projectId}_gantt.png`,
      callback: (link: string) => {
        window.open(link)
        ElMessage.success('导出成功')
      }
    })
  } catch (action: any) {
    if (action === 'cancel') {
      // 导出为 PDF
      gantt.exportToPDF({
        name: `project_${props.projectId}_gantt.pdf`,
        format: 'A4',
        orientation: 'landscape',
        callback: (link: string) => {
          window.open(link)
          ElMessage.success('导出成功')
        }
      })
    }
  }
}

// 刷新
const handleRefresh = () => {
  loadGanttData()
}

// 辅助函数
const formatDate = (date: Date | string): string => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const addDays = (date: string, days: number): Date => {
  const d = new Date(date)
  d.setDate(d.getDate() + days)
  return d
}

const getStatusTag = (status: string): string => {
  const statusMap: Record<string, string> = {
    'TODO': '<el-tag size="small">待办</el-tag>',
    'IN_PROGRESS': '<el-tag size="small" type="primary">进行中</el-tag>',
    'COMPLETED': '<el-tag size="small" type="success">已完成</el-tag>',
    'BLOCKED': '<el-tag size="small" type="danger">已阻塞</el-tag>',
    'CANCELLED': '<el-tag size="small" type="info">已取消</el-tag>'
  }
  return statusMap[status] || status
}

onMounted(() => {
  initGantt()
  loadGanttData()
})

onBeforeUnmount(() => {
  gantt.clearAll()
})
</script>

<style scoped>
.project-gantt-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.gantt-toolbar {
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  gap: 12px;
}

.gantt-content {
  flex: 1;
  overflow: hidden;
}

:deep(.owner-cell) {
  display: flex;
  align-items: center;
  gap: 6px;
  justify-content: center;
}

:deep(.owner-cell .avatar) {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

/* 任务状态颜色 */
:deep(.status-todo) {
  background-color: #d9d9d9;
}

:deep(.status-in_progress) {
  background-color: #1890ff;
}

:deep(.status-completed) {
  background-color: #52c41a;
}

:deep(.status-blocked) {
  background-color: #ff4d4f;
}

:deep(.status-cancelled) {
  background-color: #8c8c8c;
}

/* 关键路径高亮 */
:deep(.critical) {
  border: 2px solid #ff4d4f !important;
}

/* 自定义进度条 */
:deep(.custom-progress) {
  height: 100%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #fff;
  font-weight: 500;
}
</style>
```

### 新增 API 接口

**文件：** `frontend/src/api/gantt.ts`

```typescript
import request from './request'

export interface GanttQueryParams {
  startDate?: string
  endDate?: string
}

/**
 * 获取项目甘特图数据
 */
export const getProjectGanttData = (projectId: number, params?: GanttQueryParams) => {
  return request.get<any>(`/api/gantt/project/${projectId}`, { params })
}

/**
 * 更新任务时间
 */
export const updateTaskDates = (taskId: number, data: {
  planStartDate: string
  planEndDate: string
}) => {
  return request.put(`/api/gantt/task/${taskId}/dates`, data)
}

/**
 * 更新任务层级
 */
export const updateTaskParent = (taskId: number, parentId: number | null) => {
  return request.put(`/api/gantt/task/${taskId}/parent`, { parentId })
}

/**
 * 创建任务依赖关系
 */
export const createTaskLink = (data: {
  sourceTaskId: number
  targetTaskId: number
  dependencyType: string
}) => {
  return request.post('/api/gantt/link', data)
}

/**
 * 删除任务依赖关系
 */
export const deleteTaskLink = (linkId: number) => {
  return request.delete(`/api/gantt/link/${linkId}`)
}
```

---

## 🚀 实施步骤和时间表

### 阶段一：后端开发（Day 1-3）

**Day 1：基础结构**
- [ ] 创建 `GanttController.java`
- [ ] 创建 `GanttService.java` 接口
- [ ] 创建 DTO 类（GanttDataResp、GanttTaskResp、TaskDateUpdateReq）
- [ ] 配置 Swagger API 文档

**Day 2：核心逻辑**
- [ ] 实现 `getProjectGanttData` 方法
  - [ ] 查询项目下所有任务
  - [ ] 构建树形结构算法
  - [ ] 添加时间范围过滤
- [ ] 实现 `updateTaskDates` 方法
  - [ ] 时间范围验证
  - [ ] 父任务约束验证

**Day 3：增强功能**
- [ ] 实现 `updateTaskParent` 方法
- [ ] 实现任务依赖关系相关接口
- [ ] 编写单元测试
- [ ] API 联调测试

### 阶段二：前端开发（Day 4-9）

**Day 4：组件集成**
- [ ] 安装 dhtmlx-gantt 依赖
- [ ] 创建 `ProjectGantt.vue` 组件
- [ ] 初始化甘特图配置
- [ ] 创建路由页面 `ProjectGanttView.vue`

**Day 5：数据加载**
- [ ] 创建 API 调用封装 (`api/gantt.ts`)
- [ ] 实现数据转换逻辑
  - [ ] 后端数据 → 甘特图数据格式
  - [ ] 树形结构递归转换
- [ ] 实现任务列表加载

**Day 6：基础展示**
- [ ] 配置甘特图列定义
- [ ] 显示执行人信息
- [ ] 显示任务状态标签
- [ ] 任务条颜色区分

**Day 7：视图切换**
- [ ] 实现日/周/月视图切换
- [ ] 配置时间刻度
- [ ] 实现缩放功能
- [ ] 响应式适配

**Day 8：拖拽交互**
- [ ] 实现任务时间拖拽
- [ ] 实现任务层级拖拽
- [ ] 实现依赖关系连线
- [ ] 添加实时验证

**Day 9：完善和优化**
- [ ] 添加导出功能（PNG/PDF）
- [ ] 性能优化（大数据量）
- [ ] 错误处理和用户提示
- [ ] UI 样式优化

### 阶段三：联调测试（Day 10-12）

**Day 10：功能测试**
- [ ] 后端 API 测试
- [ ] 前端组件测试
- [ ] 前后端联调测试
- [ ] 边界条件测试

**Day 11：性能优化**
- [ ] 大数据量测试（100+ 任务）
- [ ] 虚拟滚动优化
- [ ] 按需加载子任务
- [ ] 渲染性能优化

**Day 12：文档和交付**
- [ ] 编写用户使用文档
- [ ] 编写开发文档
- [ ] 代码审查和优化
- [ ] 正式发布

---

## 📊 性能优化方案

### 1. 虚拟滚动（大数据量支持）

**问题：** 项目下可能有数百个任务，一次性渲染会导致页面卡顿

**解决方案：**
```javascript
// 启用分支加载（按需加载子任务）
gantt.config.branch_loading = true

// 监听展开事件，按需加载子任务
gantt.attachEvent('onTaskOpened', async (id: string) => {
  const subtasks = await loadSubtasks(id)
  gantt.parse({
    data: convertToGanttTasks(subtasks),
    links: []
  })
})
```

### 2. 后端数据聚合优化

**问题：** 树形结构递归查询可能产生 N+1 查询问题

**解决方案：**
```java
// 一次性查询所有任务，内存中构建树形结构
List<Task> allTasks = taskMapper.selectByProjectId(projectId);
List<GanttTaskResp> taskTree = buildTaskTree(allTasks);
```

### 3. 前端渲染优化

**问题：** 甘特图重绘开销大

**解决方案：**
```javascript
// 批量更新
gantt.batchUpdate(() => {
  tasks.forEach(task => {
    gantt.updateTask(task.id)
  })
})

// 防抖处理
const debouncedRefresh = debounce(() => {
  gantt.render()
}, 300)
```

---

## ⚠️ 注意事项和风险提示

### 1. 数据验证

**时间范围验证：**
```java
@AssertTrue(message = "计划结束日期必须晚于计划开始日期")
public boolean isValidDateRange() {
    if (planStartDate == null || planEndDate == null) {
        return true;
    }
    return !planEndDate.isBefore(planStartDate);
}
```

**父子关系验证：**
- 不能将任务设置为自己的子任务
- 不能将任务设置为自己的子孙任务（会导致循环）
- 子任务时间不能超出父任务范围

### 2. 权限控制

**需要添加权限验证：**
```java
@PreAuthorize("hasPermission(#projectId, 'PROJECT_VIEW')")
public Result<GanttDataResp> getProjectGanttData(Long projectId) {
    // ...
}

@PreAuthorize("@ganttService.canUpdateTask(#taskId, authentication.name)")
public Result<Void> updateTaskDates(Long taskId, TaskDateUpdateReq req) {
    // ...
}
```

### 3. 并发问题

**问题：** 多人同时拖拽任务可能导致冲突

**解决方案：**
- 使用乐观锁（version 字段）
- 拖拽时锁定任务（悲观锁）
- WebSocket 实时通知其他用户

### 4. 浏览器兼容性

**测试浏览器：**
- ✅ Chrome 90+
- ✅ Firefox 88+
- ✅ Edge 90+
- ⚠️ Safari 14+（部分功能可能不支持）
- ❌ IE（不支持）

---

## 📝 总结

### 可行性总结：✅ 高度可行

| 评估维度 | 评分 | 说明 |
|---------|------|------|
| **数据完整性** | 100% ✅ | 所有必需字段已存在且格式统一 |
| **后端 API** | 95% ✅ | 需新增树形数据聚合接口 |
| **前端技术** | 90% ✅ | dhtmlx-gantt 成熟稳定 |
| **开发风险** | 低 ⭐⭐ | 技术方案成熟，无坑点 |
| **用户体验** | 高 ⭐⭐⭐⭐⭐ | 显著提升项目管理效率 |

### 推荐实施理由

1. ✅ **零数据改动**：无需修改现有数据结构
2. ✅ **独立模块**：不影响现有功能
3. ✅ **高价值**：甘特图是项目管理的核心工具
4. ✅ **用户需求强**：从时间维度直观管理项目是刚需
5. ✅ **技术风险低**：成熟方案，社区支持好

### 预估工时

**总计：8-12 个工作日**
- 后端开发：2-3 天
- 前端开发：5-7 天
- 联调测试：1-2 天

### 关键成功因素

- 📌 后端树形数据聚合性能优化
- 📌 前端大数据量虚拟滚动
- 📌 拖拽操作的实时验证
- 📌 与现有权限系统对接

---

**文档版本：** v1.0
**创建日期：** 2026-01-11
**作者：** AI 辅助生成
