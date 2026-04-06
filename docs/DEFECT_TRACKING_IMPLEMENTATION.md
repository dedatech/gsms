# 缺陷跟踪模块实现文档

## 概述

本实现为 TeamMaster 系统添加了完整的缺陷跟踪功能。缺陷作为任务的一种特殊类型（`TaskType.BUG`），复用现有的任务管理基础设施，同时添加了缺陷特有字段和工作流。

## 核心特性

### 1. 缺陷类型

- **任务类型枚举**: `TaskType.BUG` (code=3)
- **状态扩展**: 为缺陷工作流添加了新状态
  - `TODO` - 待修复
  - `IN_PROGRESS` - 修复中
  - `TESTING` - 待验证（新增）
  - `REOPENED` - 重新打开（新增）
  - `DONE` - 已关闭

### 2. 缺陷工作流

```
[新建/待修复] → [修复中] → [待验证] → [已关闭]
      ↑                              ↓
      └──────── [重新打开] ──────────┘
```

**状态流转规则**:

- `TODO` → `IN_PROGRESS`（开始修复）
- `IN_PROGRESS` → `TESTING`（提交验证）
- `IN_PROGRESS` → `TODO`（暂停修复）
- `TESTING` → `DONE`（验证通过）
- `TESTING` → `IN_PROGRESS`（验证失败，继续修复）
- `TESTING` → `REOPENED`（重新打开）
- `REOPENED` → `TODO`（重新进入待修复）
- `DONE` → `REOPENED`（已关闭的缺陷重新打开）

### 3. 缺陷特有字段

| 字段名 | 类型 | 说明 | 可选性 |
|--------|------|------|--------|
| `severity` | DefectSeverity | 缺陷严重程度 | 必填 |
| `reproductionSteps` | String | 复现步骤 | 可选 |
| `attachments` | String | 附件列表（JSON格式） | 可选 |
| `fixVersion` | String | 修复版本 | 可选 |

### 4. 严重程度等级

- `TRIVIAL` (code=1) - 轻微
- `MINOR` (code=2) - 次要
- `MAJOR` (code=3) - 主要
- `CRITICAL` (code=4) - 严重
- `BLOCKER` (code=5) - 致命

## 数据库变更

### 迁移脚本

文件: `V20260207__Add_defect_fields_to_task.sql`

```sql
-- 添加缺陷特有字段
ALTER TABLE `gsms_task`
ADD COLUMN `severity` INT DEFAULT NULL COMMENT '缺陷严重程度',
ADD COLUMN `reproduction_steps` TEXT DEFAULT NULL COMMENT '缺陷复现步骤',
ADD COLUMN `attachments` VARCHAR(1000) DEFAULT NULL COMMENT '附件列表（JSON格式）',
ADD COLUMN `fix_version` VARCHAR(100) DEFAULT NULL COMMENT '修复版本';

-- 添加索引
ALTER TABLE `gsms_task`
ADD KEY `idx_task_severity` (`severity`),
ADD KEY `idx_task_type_status` (`type`, `status`);
```

## 后端实现

### 1. 枚举类

#### DefectSeverity.java
- **位置**: `backend/src/main/java/com/gsms/gsms/model/enums/DefectSeverity.java`
- **功能**: 定义缺陷严重程度枚举

#### TaskStatus.java (扩展)
- **变更**: 添加 `TESTING` 和 `REOPENED` 状态
- **位置**: `backend/src/main/java/com/gsms/gsms/model/enums/TaskStatus.java`

### 2. 实体类

#### Task.java (扩展)
- **新增字段**:
  - `severity` - 缺陷严重程度
  - `reproductionSteps` - 复现步骤
  - `attachments` - 附件列表
  - `fixVersion` - 修复版本
- **位置**: `backend/src/main/java/com/gsms/gsms/model/entity/Task.java`

### 3. DTO 类

#### TaskBaseReq.java (扩展)
- **新增字段**: 缺陷特有字段（与实体类对应）
- **位置**: `backend/src/main/java/com/gsms/gsms/dto/task/TaskBaseReq.java`

#### TaskInfoResp.java (扩展)
- **新增字段**: 缺陷特有字段（与实体类对应）
- **位置**: `backend/src/main/java/com/gsms/gsms/dto/task/TaskInfoResp.java`

### 4. Mapper 更新

#### TaskMapper.xml
- **位置**: `backend/src/main/resources/mapper/TaskMapper.xml`
- **变更**:
  - 扩展 `selectAllFields` SQL片段
  - 更新 `TaskResultMap` 包含新字段映射

## 前端实现

### 1. 组件

#### DefectsView.vue
- **位置**: `frontend/src/views/project/DefectsView.vue`
- **功能**:
  - 缺陷列表展示（表格视图）
  - 搜索和筛选（状态、优先级、严重程度、负责人）
  - 新建/编辑缺陷对话框
  - 缺陷详情查看
  - 状态流转操作
  - 评论功能（前端模拟）
  - 附件上传（UI实现）

**主要特性**:
- ✅ 分页显示
- ✅ 关键词搜索
- ✅ 多维度筛选
- ✅ 状态流转可视化
- ✅ 表单验证
- ✅ 响应式设计

### 2. API 集成

#### task.ts (扩展)
- **位置**: `frontend/src/api/task.ts`
- **变更**:
  - `TaskInfo` 接口添加缺陷特有字段
  - `TaskCreateReq` 接口添加缺陷特有字段

### 3. 集成到项目详情页

#### ProjectDetail.vue
- **位置**: `frontend/src/views/project/ProjectDetail.vue`
- **变更**:
  - 导入 `DefectsView` 组件
  - 在"缺陷"模块标签中使用 `DefectsView`
  - 添加组件引用 `defectsViewRef`

## API 接口

缺陷复用现有的任务管理 API，无需额外端点：

### 创建缺陷
```
POST /api/tasks
Content-Type: application/json

{
  "projectId": 1,
  "title": "用户登录失败",
  "description": "使用错误密码登录时未显示错误提示",
  "type": "BUG",
  "priority": "HIGH",
  "severity": "MAJOR",
  "reproductionSteps": "1. 打开登录页面\n2. 输入错误密码\n3. 点击登录",
  "assigneeId": 10,
  "status": "TODO"
}
```

### 查询项目缺陷
```
GET /api/tasks/search?projectId=1&pageNum=1&pageSize=20
```
返回结果中筛选 `type === "BUG"` 的任务

### 更新缺陷状态
```
PUT /api/tasks/status
Content-Type: application/json

{
  "id": 123,
  "status": "IN_PROGRESS"
}
```

### 更新缺陷信息
```
PUT /api/tasks
Content-Type: application/json

{
  "id": 123,
  "title": "更新后的标题",
  "severity": "CRITICAL",
  "priority": "HIGH"
}
```

## 使用指南

### 1. 创建缺陷

1. 进入项目详情页
2. 点击"缺陷"模块标签
3. 点击"新建缺陷"按钮
4. 填写缺陷信息：
   - 标题（必填）
   - 描述（必填）
   - 严重程度（必填）
   - 优先级（必填）
   - 复现步骤（可选）
   - 负责人（可选）
   - 所属迭代（可选）
   - 附件（可选）
5. 点击"确定"创建

### 2. 缺陷状态流转

在缺陷详情中，根据当前状态显示可流转的操作按钮：

- **待修复** → "开始修复"
- **修复中** → "提交验证"、"暂停修复"
- **待验证** → "验证通过"、"验证失败"、"重新打开"
- **已关闭** → "重新打开"
- **重新打开** → "设为待修复"

### 3. 筛选和搜索

- **关键词搜索**: 支持按缺陷编号或标题搜索
- **状态筛选**: 待修复、修复中、待验证、已关闭、重新打开
- **优先级筛选**: 低、中、高
- **严重程度筛选**: 轻微、次要、主要、严重、致命
- **负责人筛选**: 选择具体团队成员

### 4. 批量操作

当前实现支持单个缺陷的操作：
- 查看详情
- 编辑信息
- 删除缺陷
- 状态流转

## 未来扩展

### 计划功能

1. **附件管理**
   - 文件上传到云存储（OSS/S3）
   - 图片预览
   - 视频播放

2. **评论系统**
   - 后端评论接口
   - @提及功能
   - 评论通知

3. **关联功能**
   - 关联需求
   - 关联其他缺陷
   - 关联迭代

4. **高级筛选**
   - 创建日期范围
   - 更新日期范围
   - 多选筛选条件

5. **缺陷统计**
   - 按严重程度统计
   - 按状态统计
   - 趋势图表
   - 缺陷密度分析

6. **导出功能**
   - 导出为 Excel
   - 导出为 PDF
   - 自定义导出字段

## 技术亮点

1. **复用现有架构**: 缺陷作为任务的特殊类型，无需独立的表和服务
2. **灵活的状态机**: 支持复杂的状态流转规则
3. **完善的枚举系统**: 使用 MyBatis-Plus 枚举处理器，保证类型安全
4. **前端组件化**: 清晰的组件划分和状态管理
5. **数据库索引优化**: 为常用查询字段添加索引
6. **可扩展性**: 预留字段和扩展点，便于后续功能迭代

## 测试建议

### 单元测试

```java
@Test
public void testCreateDefect() {
    TaskCreateReq req = new TaskCreateReq();
    req.setProjectId(1L);
    req.setTitle("测试缺陷");
    req.setType(TaskType.BUG);
    req.setSeverity(DefectSeverity.MAJOR);
    req.setStatus(TaskStatus.TODO);

    TaskInfoResp defect = taskService.create(req);

    assertNotNull(defect);
    assertEquals(TaskType.BUG, defect.getType());
    assertEquals(DefectSeverity.MAJOR, defect.getSeverity());
}
```

### 集成测试

1. 创建缺陷
2. 更新缺陷状态
3. 查询项目缺陷列表
4. 筛选缺陷（按状态、严重程度）
5. 删除缺陷

### UI 测试

1. 打开项目详情页
2. 切换到"缺陷"标签
3. 创建新缺陷
4. 验证表单验证
5. 测试筛选功能
6. 测试状态流转
7. 查看缺陷详情

## 注意事项

1. **数据库迁移**: 确保执行最新的 Flyway 迁移脚本
2. **枚举序列化**: 前端使用枚举名称（如 "MAJOR"），后端存储整数（如 3）
3. **权限检查**: 复用现有的项目成员权限系统
4. **附件存储**: 当前附件字段为字符串，后续需要实现文件上传
5. **评论功能**: 前端已实现 UI，后端接口待实现

## 版本历史

- **v1.0** (2026-02-07)
  - 初始实现
  - 支持缺陷 CRUD
  - 实现基本工作流
  - 添加筛选和搜索
  - 集成到项目详情页

---

**文档维护**: 本文档应随功能迭代同步更新
**最后更新**: 2026-02-07
