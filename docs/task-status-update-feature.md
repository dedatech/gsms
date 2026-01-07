# 任务状态更新功能开发总结

**日期**: 2026-01-07
**功能**: 任务拖拽和快捷状态变更
**关键技术点**: MyBatis 动态 SQL、DTO 设计、状态转换逻辑

---

## 功能概述

实现了看板视图下的任务拖拽功能，以及任务详情页的快捷状态变更按钮（开始任务、完成任务、重新打开）。系统能够根据状态变化智能地自动设置或清空实际开始时间、实际结束时间。

---

## 用户更正与问题解决

### 更正 1：UpdateReq 参数约束问题

**问题描述**：
- 初始实现中，所有任务更新都使用 `TaskUpdateReq`，它继承自 `TaskBaseReq`
- `TaskBaseReq` 要求 `projectId` 和 `title` 为必填字段（`@NotNull`、`@NotBlank`）
- 在"开始任务"、"拖拽任务"等场景下，只需要更新 `status` 和实际时间字段
- 导致这些简单操作也需要传入 `projectId` 和 `title`，违反了最小必要原则

**用户指出**：
> "开始任务，和任务拖拽都更新接口，有一些必须要传入的参数约束导致更新失败，目前后台这块的约束有些合适它集成了TaskBaseReq。应该重新定义UpdateReq，严格意义必须id，其他的要跟进场景默认"

**解决方案**：
创建专门的轻量级 DTO `TaskStatusUpdateReq`：

```java
@Schema(description = "任务状态更新请求")
public class TaskStatusUpdateReq {
    @NotNull(message = "任务ID不能为空")
    private Long id;

    @NotNull(message = "任务状态不能为空")
    private TaskStatus status;

    // 可选：允许用户手动指定实际时间
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
}
```

- ✅ 只包含必需字段：`id` 和 `status`
- ✅ 实际时间字段为可选，系统会自动设置
- ✅ 添加新的 API 端点：`PUT /api/tasks/status`

---

### 更正 2：状态转换逻辑不完整（反向转换清空时间）

**问题描述**：
- 初始实现只考虑了正向状态转换（TODO → IN_PROGRESS → DONE）
- 只处理了自动**设置**实际时间的场景
- 忽略了反向状态转换（DONE → IN_PROGRESS → TODO）时需要**清空**实际时间的场景

**用户指出**：
> "根据状态变化计算实际时间的逻辑不完整，注意除了正向还会反向，反向就要把完成时间或开始时间清除掉。比如由完成到进行中，完成时间应该重新设置为空"

**完整的状态转换逻辑**：

#### 实际开始时间（actualStartDate）
| 状态转换 | 操作 | 说明 |
|---------|------|------|
| TODO → IN_PROGRESS | 设置为今天 | 正常开始任务 |
| DONE → IN_PROGRESS | 设置为今天 | 从完成重新回到进行中 |
| TODO → DONE | 设置为今天 | 跳跃转换 |
| **IN_PROGRESS → TODO** | **清空为 null** | **反向：撤销开始** |
| **DONE → TODO** | **清空为 null** | **反向跳跃** |
| 其他 | 保持原值 | 无状态变化 |

#### 实际结束时间（actualEndDate）
| 状态转换 | 操作 | 说明 |
|---------|------|------|
| TODO → DONE | 设置为今天 | 跳跃转换 |
| IN_PROGRESS → DONE | 设置为今天 | 正常完成任务 |
| **DONE → TODO** | **清空为 null** | **反向：重新打开** |
| **DONE → IN_PROGRESS** | **清空为 null** | **反向：从完成回到进行中** |
| 其他 | 保持原值 | 无状态变化 |

**实现代码**：
```java
// 处理实际开始时间
LocalDate actualStartDate = null;
if (updateReq.getActualStartDate() != null) {
    actualStartDate = updateReq.getActualStartDate();
} else {
    if (newStatus == TaskStatus.IN_PROGRESS && oldStatus != TaskStatus.IN_PROGRESS) {
        actualStartDate = LocalDate.now();
    } else if (newStatus == TaskStatus.DONE && oldStatus == TaskStatus.TODO) {
        actualStartDate = LocalDate.now();
    } else if (newStatus == TaskStatus.TODO && oldStatus == TaskStatus.IN_PROGRESS) {
        actualStartDate = null;  // 反向转换：清空
    } else if (newStatus == TaskStatus.TODO && oldStatus == TaskStatus.DONE) {
        actualStartDate = null;  // 反向转换：清空
    } else {
        actualStartDate = existTask.getActualStartDate();  // 保持原值
    }
}

// 处理实际结束时间
LocalDate actualEndDate = null;
if (updateReq.getActualEndDate() != null) {
    actualEndDate = updateReq.getActualEndDate();
} else {
    if (newStatus == TaskStatus.DONE && oldStatus != TaskStatus.DONE) {
        actualEndDate = LocalDate.now();
    } else if (newStatus != TaskStatus.DONE && oldStatus == TaskStatus.DONE) {
        actualEndDate = null;  // 反向转换：清空
    } else {
        actualEndDate = existTask.getActualEndDate();  // 保持原值
    }
}
```

---

### 更正 3：MyBatis 无法更新字段为 null

**问题描述**：
- MyBatis-Plus 默认会忽略 null 值字段，不将它们更新到数据库
- 即使添加 `@TableField(updateStrategy = FieldStrategy.IGNORED)` 注解
- **问题根源**：在 `TaskMapper.xml` 中使用了 `<if test="">` 条件判断

**用户指出**：
> "目前mybatis写法好像不能更新每个字段为 null"
> "<if test="planStartDate != null">plan_start_date = #{planStartDate},</if> 不好用Mapper代码是这样的，不会执行的"

**问题分析**：

原 XML 配置：
```xml
<update id="update" parameterType="com.gsms.gsms.model.entity.Task">
    UPDATE gsms_task
    <set>
        <if test="actualStartDate != null">actual_start_date = #{actualStartDate},</if>
        <if test="actualEndDate != null">actual_end_date = #{actualEndDate},</if>
        ...
    </set>
    WHERE id = #{id}
</update>
```

当 `actualStartDate = null` 时，`<if test="actualStartDate != null">` 条件为 false，不会生成对应的 SQL 语句。

**解决方案**：

创建专门的 `updateStatus` SQL 方法，**无条件**更新字段：

```xml
<!-- 更新任务状态（支持将 actualStartDate 和 actualEndDate 更新为 null） -->
<update id="updateStatus" parameterType="com.gsms.gsms.model.entity.Task">
    UPDATE gsms_task
    SET status = #{status, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler},
        actual_start_date = #{actualStartDate},
        actual_end_date = #{actualEndDate},
        update_user_id = #{updateUserId},
        update_time = CURRENT_TIMESTAMP
    WHERE id = #{id}
</update>
```

**关键点**：
- ✅ 不使用 `<if test="">` 条件判断
- ✅ 直接设置字段值，即使为 null 也会执行
- ✅ 在 Mapper 接口中添加对应方法：`int updateStatus(Task task)`
- ✅ 在 Service 层调用新方法：`taskMapper.updateStatus(task)` 而非 `taskMapper.update(task)`

---

## 技术实现总结

### 后端实现

#### 1. DTO 层
- 创建 `TaskStatusUpdateReq.java` 轻量级请求类
- 只包含状态更新必需的字段

#### 2. Controller 层
```java
@PutMapping("/status")
@Operation(summary = "更新任务状态")
public Result<TaskInfoResp> updateStatus(@RequestBody @Valid TaskStatusUpdateReq req)
```

#### 3. Service 层
```java
@Override
@Transactional(rollbackFor = Exception.class)
public Task updateStatus(TaskStatusUpdateReq updateReq) {
    // 1. 检查任务存在性
    // 2. 权限验证
    // 3. 根据状态转换自动计算实际时间
    // 4. 调用 updateStatus（不是 update）
    // 5. 返回更新后的任务
}
```

#### 4. Mapper 层
- 创建专门的 `updateStatus` SQL 方法
- 无条件更新 `status`、`actualStartDate`、`actualEndDate`

### 前端实现

#### 1. API 层（`frontend/src/api/task.ts`）
```typescript
export interface TaskStatusUpdateReq {
  id: number
  status: string
  actualStartDate?: string
  actualEndDate?: string
}

export const updateTaskStatus = (data: TaskStatusUpdateReq) => {
  return request.put('/tasks/status', data)
}
```

#### 2. 看板拖拽（`TaskList.vue`）
```typescript
const handleDrop = async (event: DragEvent) => {
  const targetStatus = (event.currentTarget as HTMLElement).getAttribute('data-status')
  await updateTaskStatus({
    id: draggedTask.value.id,
    status: targetStatus
  })
}
```

#### 3. 快捷按钮（`TaskDetail.vue`）
```typescript
// 开始任务
const handleStartTask = async () => {
  await updateTaskStatus({
    id: taskId.value,
    status: 'IN_PROGRESS'
  })
}

// 完成任务
const handleCompleteTask = async () => {
  await updateTaskStatus({
    id: taskId.value,
    status: 'DONE'
  })
}

// 重新打开
const handleReopenTask = async () => {
  await updateTaskStatus({
    id: taskId.value,
    status: 'TODO'
  })
}
```

---

## 关键经验教训

### 1. DTO 设计要遵循单一职责原则
- **问题**：一个 DTO 试图满足所有场景，导致不必要的字段约束
- **解决**：根据使用场景创建专门的 DTO
  - `TaskCreateReq`：创建任务
  - `TaskUpdateReq`：完整更新任务信息
  - `TaskStatusUpdateReq`：仅更新状态和实际时间

### 2. 状态转换逻辑要考虑双向性
- **问题**：只考虑正向流程，忽略反向操作
- **解决**：完整梳理所有可能的状态转换路径
  - 正向转换：自动设置时间戳
  - 反向转换：清空相应的时间戳
  - 跳跃转换：也要正确处理

### 3. MyBatis 动态 SQL 的陷阱
- **问题**：`<if test="">` 条件会跳过 null 值字段
- **解决**：对于需要支持 null 值的场景，使用专门的 SQL 方法
  - 常规更新：使用动态 SQL（`update`）
  - 状态更新：使用静态 SQL（`updateStatus`）

### 4. 字段更新策略的优先级
1. **Mapper XML 层**：最优先，直接控制 SQL 生成
2. **实体类注解**：`@TableField(updateStrategy)` 次优先
3. **Service 层逻辑**：补充业务规则

---

## 文件变更清单

### 后端新增/修改
- ✅ 新增：`TaskStatusUpdateReq.java`
- ✅ 修改：`TaskController.java`（添加 `/status` 端点）
- ✅ 修改：`TaskService.java`（添加 `updateStatus` 接口）
- ✅ 修改：`TaskServiceImpl.java`（实现状态转换逻辑）
- ✅ 修改：`TaskMapper.java`（添加 `updateStatus` 方法）
- ✅ 修改：`TaskMapper.xml`（添加 `updateStatus` SQL）
- ✅ 修改：`Task.java`（添加 `FieldStrategy.IGNORED` 注解）

### 前端新增/修改
- ✅ 修改：`task.ts`（添加 `updateTaskStatus` API）
- ✅ 修改：`TaskList.vue`（拖拽使用新 API）
- ✅ 修改：`TaskDetail.vue`（快捷按钮使用新 API）

---

## 测试场景

### 正向转换
- ✅ TODO → IN_PROGRESS：`actualStartDate` 自动设置为今天
- ✅ IN_PROGRESS → DONE：`actualEndDate` 自动设置为今天
- ✅ TODO → DONE（拖拽跳跃）：两个字段都设置为今天

### 反向转换
- ✅ DONE → IN_PROGRESS：`actualEndDate` 被清空为 null
- ✅ IN_PROGRESS → TODO：`actualStartDate` 被清空为 null
- ✅ DONE → TODO（拖拽跳跃）：两个字段都被清空

### 边界场景
- ✅ 同状态转换：时间保持不变
- ✅ 手动指定时间：优先使用用户提供的值
