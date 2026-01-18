# ONES 风格完整重构规划（方案 B）

> **文档版本**：v1.0
> **创建日期**：2026-01-18
> **状态**：后续规划
> **当前方案**：采用方案 A（最小改动方案）渐进式改造

---

## 一、改造背景

### 1.1 ONES 产品核心逻辑

**核心概念**：工作项（WorkItem）是所有工作的基本单位，相当于当前系统的"任务"。

**敏捷开发全流程**：
```
需求（需求池） → 规划（创建迭代） → 迭代（分解子任务） → 执行 → 发布
```

**五个关键阶段**：

| 阶段 | 功能说明 | 工作项状态 |
|-----|---------|-----------|
| **需求** | 需求池管理 | 待规划、已发布、关闭 |
| **规划** | 迭代规划 | 已规划、待规划 |
| **迭代** | 泳道看板执行 | 待办、进行中、已完成、关闭 |
| **缺陷** | Bug 管理 | 待办、进行中、已解决、已关闭 |
| **发布** | 发布管理 | 待发布、已发布、已归档 |

**核心流程**：
1. 在"需求"模块创建工作项（需求池）
2. 在"规划"模块创建迭代
3. 将需求关联到迭代
4. 在迭代中分解子任务
5. 子任务状态流转（待办→进行中→已完成→关闭）
6. 子任务全部完成后，需求状态变为"已发布"

### 1.2 当前 GSMS 系统架构

**当前数据模型**：
- `gsms_project` - 项目表
- `gsms_iteration` - 迭代表（仅中大型项目使用）
- `gsms_task` - 任务表（支持父子关系）
- `gsms_work_hour` - 工时表

**当前功能**：
- 项目管理（列表、详情、CRUD）
- 迭代管理（列表、CRUD）
- 任务管理（支持父子任务、四列状态看板）
- 工时管理

**存在的问题**：
1. 任务和迭代耦合紧密（仅中大型项目有迭代）
2. 缺少"需求池"概念
3. 缺少"规划"视图（迭代列表+待规划区）
4. 任务状态流转不完整（缺少"已发布"状态）
5. 页面布局不符合 ONES 风格

---

## 二、完整重构方案（方案 B）

### 2.1 数据模型重构

#### 2.1.1 统一工作项模型

**目标**：将 `gsms_task` 改造为统一的工作项模型

**新建表**：`gsms_work_item`

```sql
CREATE TABLE gsms_work_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工作项ID',

  -- 基础信息
  serial_number VARCHAR(50) UNIQUE NOT NULL COMMENT '工作项编号（如 #REQ-2024-001）',
  title VARCHAR(200) NOT NULL COMMENT '标题',
  description TEXT COMMENT '描述',

  -- 类型分类
  type ENUM('REQUIREMENT', 'TASK', 'DEFECT', 'SUBTASK') NOT NULL DEFAULT 'TASK' COMMENT '工作项类型',

  -- 状态管理
  status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'RELEASED')
    NOT NULL DEFAULT 'NOT_STARTED' COMMENT '状态',

  -- 关联关系
  project_id BIGINT NOT NULL COMMENT '所属项目',
  iteration_id BIGINT COMMENT '所属迭代（可为空）',
  parent_id BIGINT COMMENT '父工作项ID（支持多级分解）',

  -- 属性
  priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM' COMMENT '优先级',

  -- 负责人
  assignee_id BIGINT COMMENT '负责人ID',

  -- 时间
  plan_start_date DATE COMMENT '计划开始时间',
  plan_end_date DATE COMMENT '计划结束时间',
  actual_start_date DATE COMMENT '实际开始时间',
  actual_end_date DATE COMMENT '实际结束时间',

  -- 工时
  estimate_hours DECIMAL(5,1) COMMENT '预估工时（小时）',
  actual_hours DECIMAL(5,1) COMMENT '实际工时（小时）',
  remaining_hours DECIMAL(5,1) COMMENT '剩余工时（小时）',

  -- 审计字段
  create_user_id BIGINT NOT NULL COMMENT '创建人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_user_id BIGINT COMMENT '更新人',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志',

  UNIQUE KEY uk_project_serial (project_id, serial_number),
  KEY idx_project (project_id),
  KEY idx_iteration (iteration_id),
  KEY idx_parent (parent_id),
  KEY idx_type_status (type, status),
  KEY idx_assignee (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作项表';
```

#### 2.1.2 迭代表增强

**改造 `gsms_iteration` 表**：

```sql
ALTER TABLE gsms_iteration
ADD COLUMN work_item_count INT DEFAULT 0 COMMENT '工作项数量',
ADD COLUMN total_estimate_hours DECIMAL(5,1) DEFAULT 0 COMMENT '总预估工时',
ADD COLUMN total_actual_hours DECIMAL(5,1) DEFAULT 0 COMMENT '总实际工时',
ADD COLUMN progress DECIMAL(5,2) DEFAULT 0 COMMENT '进度百分比（0-100）',
ADD COLUMN start_date DATE COMMENT '开始日期',
ADD COLUMN end_date DATE COMMENT '结束日期';
```

#### 2.1.3 项目类型调整

**建议**：取消项目类型的"常规项目"和"中大型项目"区分，所有项目都支持：
- 创建迭代
- 需求池
- 规划
- 泳道看板

### 2.2 后端 API 重构

#### 2.2.1 工作项 API

**新建控制器**：`WorkItemController`

```java
@RestController
@RequestMapping("/api/work-items")
public class WorkItemController {

    // 获取工作项列表（支持筛选、分页、树状结构）
    @GetMapping
    public Result<PageResult<WorkItemResp>> getWorkItems(WorkItemQueryReq req) {
        // 支持按类型（需求/任务/缺陷）、状态、迭代筛选
        // 支持树状结构返回（父子关系）
    }

    // 创建工作项
    @PostMapping
    public Result<WorkItemResp> create(@Valid @RequestBody WorkItemCreateReq req) {
        // 自动生成编号（如 #REQ-2024-001）
        // 类型自动识别（根据所属模块）
    }

    // 更新工作项
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody WorkItemUpdateReq req) {
        // 状态流转校验（如：子任务未完成时，父任务不能发布）
    }

    // 删除工作项（软删除）
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 检查是否有子任务
        // 检查是否有关联工时
    }

    // 获取工作项详情（包含子任务列表）
    @GetMapping("/{id}")
    public Result<WorkItemDetailResp> getDetail(@PathVariable Long id) {
        // 包含子任务列表、工时记录、操作日志
    }

    // 分解子任务
    @PostMapping("/{id}/subtasks")
    public Result<List<WorkItemResp>> createSubtasks(
        @PathVariable Long id,
        @Valid @RequestBody List<WorkItemCreateReq> reqs
    ) {
        // 批量创建子任务
        // 自动关联父工作项和项目
    }

    // 状态流转
    @PostMapping("/{id}/status")
    public Result<Void> changeStatus(
        @PathVariable Long id,
        @RequestBody @Valid StatusChangeReq req
    ) {
        // 状态机校验（如：TODO → IN_PROGRESS → DONE → RELEASED）
        // 自动更新父工作项状态
        // 记录状态变更日志
    }

    // 获取需求池列表
    @GetMapping("/pool/requirements")
    public Result<List<WorkItemResp>> getRequirementPool(
        @RequestParam Long projectId,
        @RequestParam(required = false) Long iterationId
    ) {
        // 获取未规划或已规划的需求
        // 支持按迭代筛选
    }

    // 关联到迭代
    @PostMapping("/{id}/iteration")
    public Result<Void> linkToIteration(
        @PathVariable Long id,
        @RequestParam Long iterationId
    ) {
        // 将工作项关联到迭代
        // 更新状态为"已规划"
    }

    // 批量操作
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchOperate(
        @RequestBody @Valid BatchOperateReq req
    ) {
        // 支持批量分配负责人、批量修改状态、批量删除等
    }
}
```

#### 2.2.2 迭代 API 增强

**新增方法**：`IterationController`

```java
@RestController
@RequestMapping("/api/iterations")
public class IterationController {

    // 获取项目所有迭代（规划视图）
    @GetMapping("/project/{projectId}/all")
    public Result<List<IterationWithStatsResp>> getProjectIterations(
        @PathVariable Long projectId
    ) {
        // 返回迭代列表及统计信息（工作项数、工时、进度）
    }

    // 获取迭代工作项列表
    @GetMapping("/{iterationId}/work-items")
    public Result<List<WorkItemResp>> getIterationWorkItems(
        @PathVariable Long iterationId
    ) {
        // 返回该迭代的所有工作项（包括子任务）
    }

    // 开始迭代
    @PostMapping("/{id}/start")
    public Result<Void> startIteration(@PathVariable Long id) {
        // 检查迭代是否有工作项
        // 更新状态为"进行中"
        // 发送通知给团队成员
    }

    // 完成迭代
    @PostMapping("/{id}/complete")
    public Result<Void> completeIteration(@PathVariable Long id) {
        // 检查所有工作项是否完成
        // 更新状态为"已完成"
        // 自动发布关联的需求
    }

    // 迭代燃尽图数据
    @GetMapping("/{id}/burndown")
    public Result<BurndownDataResp> getBurndownData(@PathVariable Long id) {
        // 返回燃尽图数据（日期、理想工时、实际工时）
    }
}
```

### 2.3 前端页面重构

#### 2.3.1 需求管理页面

**路由**：`/projects/:projectId/requirements`

**页面组件**：`RequirementPool.vue`

**功能**：
- 表格视图（默认）：显示所有工作项
- 树状视图：显示父子任务层级
- 筛选：按状态、负责人、优先级筛选
- 批量操作：批量分配、批量修改状态、批量关联迭代
- 拖拽：拖拽到迭代进行规划

**核心字段**：
- 编号（如 #REQ-2024-001）
- 标题
- 类型（需求=橙色图标、任务=蓝色图标、缺陷=红色图标）
- 状态（待规划、已发布、关闭）
- 优先级（高、中、低）
- 负责人
- 预估工时
- 所属迭代

#### 2.3.2 规划管理页面

**路由**：`/projects/:projectId/planning`

**页面组件**：`PlanningView.vue`

**布局结构**：
```
┌─────────────────────────────────────────────────────────┐
│ [迭代列表区]              │ [待规划区]                  │
│                          │                              │
│ ▼ Sprint1 (7)             │  待规划工作项                │
│   工作项1                  │  - 带规划工作项              │
│   工作项2                  │  - 用户管理                  │
│   工作项3                  │  - 测试用例设计              │
│                          │                              │
│ ▼ Sprint2 (0)             │  [+ 新建工作项]              │
│                          │                              │
│ ▼ Sprint3 (0)             │                              │
│                          │                              │
│ [+ 新建迭代]              │                              │
└─────────────────────────────────────────────────────────┘
```

**功能**：
- 左侧：迭代列表（可展开/收起）
  - 显示迭代名称、工作项数量、时间、预估工时
  - 展开/收起切换
  - 拖拽工作项到迭代
- 右侧：待规划工作项池
  - 显示未关联到迭代的工作项
  - 支持快速创建工作项
  - 拖拽到迭代进行规划
- 支持三种规划视角：
  - 按迭代规划
  - 按发布规划
  - 按史诗规划

#### 2.3.3 迭代详情页面

**路由**：`/projects/:projectId/iterations/:iterationId`

**页面组件**：`IterationDetail.vue`

**布局结构**：
```
┌─────────────────────────────────────────────────────────┐
│ [迭代 sprint1] 开始迭代 | 燃尽图 | 成员剩余工时 | 设置   │
├─────────────────────────────────────────────────────────┤
│ [概览] [敏捷看板] [筛选器]                              │
├─────────────────────────────────────────────────────────┤
│ [未开始 (0)]  [进行中 (0)]  [已完成 (0)]               │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐                │
│ │          │ │          │ │          │                │
│ │  任务卡片 │ │  任务卡片 │ │  任务卡片 │                │
│ │          │ │          │ │          │                │
│ └──────────┘ └──────────┘ └──────────┘                │
└─────────────────────────────────────────────────────────┘
```

**功能**：
- 迭代信息展示（名称、时间、进度）
- 操作按钮（开始迭代、燃尽图、成员剩余工时、看板设置）
- 视图切换（概览、敏捷看板、筛选器）
- 三列看板（未开始、进行中、已完成）
- 泳道展示：按任务分组，每个任务一行，子任务在泳道中显示
- 拖拽：拖拽任务到不同列改变状态
- 状态流转：自动记录状态变更日志

#### 2.3.4 工作项详情抽屉

**组件**：`WorkItemDrawer.vue`

**功能**：
- 侧边抽屉式详情页
- Tab 切换：
  - 概览：基本信息、状态流转记录
  - 子任务：子任务列表（支持拖拽排序）
  - 工时：工时记录列表、工时统计
  - 评论：@提及、讨论
  - 附件：文件上传
  - 操作日志：变更历史

### 2.4 状态机设计

#### 2.4.1 工作项状态流转

**需求状态流转**：
```
NOT_STARTED (待规划)
    ↓
PLANNED (已规划) - 关联到迭代后自动更新
    ↓
IN_PROGRESS (进行中) - 迭代开始后自动更新
    ↓
COMPLETED (已完成) - 所有子任务完成
    ↓
RELEASED (已发布) - 发布时更新
    ↓
CLOSED (已关闭)
```

**任务状态流转**：
```
NOT_STARTED (待办)
    ↓
IN_PROGRESS (进行中)
    ↓
COMPLETED (已完成)
    ↓
CLOSED (已关闭)
```

**缺陷状态流转**：
```
OPEN (待处理)
    ↓
IN_PROGRESS (处理中)
    ↓
RESOLVED (已解决)
    ↓
CLOSED (已关闭)
    ↓
REOPENED (重新打开) - 循环
```

#### 2.4.2 状态流转规则

**自动更新规则**：
1. 工作项关联到迭代 → 状态变为"已规划"
2. 迭代开始 → 该迭代所有工作项变为"进行中"
3. 所有子任务完成 → 父工作项自动变为"已完成"
4. 工作项发布 → 关联的父需求变为"已发布"

**阻止规则**：
1. 有未完成的子任务 → 父工作项不能完成/发布
2. 迭代中有工作项 → 迭代不能完成
3. 已发布/关闭的工作项 → 不能编辑

### 2.5 通知系统

#### 2.5.1 通知触发场景

1. **工作项分配**：分配负责人时通知
2. **状态变更**：状态流转时通知相关人员
3. **评论/@提及**：评论或@提及时通知
4. **迭代开始/完成**：通知团队成员
5. **即将到期**：任务快到期时提醒

#### 2.5.2 通知方式

- 站内通知（铃铛图标）
- 邮件通知（可选）
- 浏览器通知（可选）

### 2.6 权限系统增强

#### 2.6.1 新增权限

| 权限码 | 权限名称 | 说明 |
|-------|---------|------|
| `workitem:create` | 创建工作项 | 在项目中创建工作项 |
| `workitem:edit` | 编辑工作项 | 编辑工作项内容 |
| `workitem:delete` | 删除工作项 | 删除工作项 |
| `workitem:assign` | 分配工作项 | 分配负责人 |
| `workitem:link` | 关联迭代 | 将工作项关联到迭代 |
| `iteration:start` | 开始迭代 | 启动迭代 |
| `iteration:complete` | 完成迭代 | 完成迭代 |
| `planning:manage` | 规划管理 | 管理迭代规划 |

---

## 三、实施计划

### 3.1 第一阶段：数据模型改造（1-2周）

**任务**：
1. 创建 `gsms_work_item` 表
2. 数据迁移（从 `gsms_task` 迁移到 `gsms_work_item`）
3. 增强 `gsms_iteration` 表
4. 更新 MyBatis Mapper
5. 编写迁移脚本

**验收**：
- 新表创建成功
- 历史数据完整迁移
- 单元测试通过

### 3.2 第二阶段：后端 API 开发（2-3周）

**任务**：
1. 创建 `WorkItemController`
2. 创建 `WorkItemService`
3. 实现状态机逻辑
4. 实现 DTO 和 Converter
5. 编写单元测试和集成测试

**验收**：
- 所有 API 接口实现
- 状态流转逻辑正确
- 测试覆盖率 > 80%

### 3.3 第三阶段：前端页面开发（3-4周）

**任务**：
1. 创建需求管理页面
2. 创建规划管理页面
3. 创建迭代详情页面
4. 创建工作项详情抽屉
5. 集成拖拽功能
6. 实现状态流转动画

**验收**：
- 所有页面功能完整
- 拖拽功能正常
- 用户体验流畅

### 3.4 第四阶段：通知和权限（1周）

**任务**：
1. 实现站内通知
2. 邮件通知（可选）
3. 权限控制增强
4. 功能开关

**验收**：
- 通知及时准确
- 权限控制正确

### 3.5 第五阶段：测试和上线（1周）

**任务**：
1. 功能测试
2. 性能测试
3. 用户验收测试
4. 灰度发布
5. 文档更新

**验收**：
- 所有测试通过
- 性能指标达标
- 用户满意

---

## 四、风险评估

| 风险项 | 风险等级 | 影响 | 应对措施 |
|-------|---------|------|---------|
| 数据迁移失败 | 🔴 高 | 历史数据丢失 | 1. 备份原表<br>2. 测试迁移<br>3. 回滚方案 |
| 状态机逻辑错误 | 🟡 中 | 状态流转异常 | 1. 状态机图审查<br>2. 单元测试覆盖<br>3. 边界条件测试 |
| 用户学习成本 | 🟡 中 | 用户不适应 | 1. 用户培训<br>2. 操作手册<br>3. 引导提示 |
| 性能下降 | 🟢 低 | 页面加载慢 | 1. SQL 优化<br>2. 索引优化<br>3. 分页加载 |
| 功能回归 | 🟡 中 | 原有功能失效 | 1. 充分回归测试<br>2. 保持兼容 |

---

## 五、成功指标

### 5.1 功能指标
- ✅ 支持工作项全生命周期管理
- ✅ 支持需求池和规划视图
- ✅ 支持迭代泳道看板
- ✅ 支持父子任务分解
- ✅ 支持状态自动流转

### 5.2 性能指标
- ✅ 页面加载时间 < 2s
- ✅ 状态流转响应 < 500ms
- ✅ 看板拖拽流畅（60fps）

### 5.3 用户体验指标
- ✅ 用户满意度 > 4.0/5.0
- ✅ 操作错误率 < 5%
- ✅ 功能使用率 > 70%

---

## 六、附录

### 6.1 数据库迁移脚本

```sql
-- 创建新表
CREATE TABLE gsms_work_item (
  -- 见 2.1.1 节
);

-- 数据迁移：从 gsms_task 迁移到 gsms_work_item
INSERT INTO gsms_work_item (
  id, serial_number, title, description, type, status,
  project_id, iteration_id, parent_id, priority,
  assignee_id, plan_start_date, plan_end_date,
  estimate_hours, actual_hours,
  create_user_id, create_time, update_user_id, update_time
)
SELECT
  id,
  CONCAT('#TASK-', id) as serial_number,
  title,
  description,
  CASE
    WHEN parent_id IS NULL THEN 'TASK'
    ELSE 'SUBTASK'
  END as type,
  status,
  project_id,
  iteration_id,
  parent_id,
  priority,
  assignee_id,
  plan_start_date,
  plan_end_date,
  estimate_hours,
  actual_hours,
  create_user_id,
  create_time,
  update_user_id,
  update_time
FROM gsms_task
WHERE is_deleted = 0;

-- 重命名原表（备份）
RENAME TABLE gsms_task TO gsms_task_backup_20260118;
```

### 6.2 参考资料
- ONES 官网：https://ones.ai/
- 敏捷开发最佳实践
- 状态机设计模式
- 看板方法论文档

---

**文档维护者**：Claude
**最后更新**：2026-01-18
**下次评审**：实施方案 A 后
