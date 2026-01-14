# 待办事项

**最后更新：** 2026-01-13

---

## ✅ 最近完成的功能（2026-01-12 ~ 2026-01-14）

### 1. 统计分析模块 ✅

**完成内容：**
- ✅ 统计分析菜单（项目工时统计、用户工时统计、工时趋势分析）
- ✅ 项目工时统计页面（/statistics/project）
- ✅ 用户工时统计页面（/statistics/user）
- ✅ 工时趋势分析页面（/statistics/trend）
- ✅ 首页看板数据统计
- ✅ 7个统计API接口完整实现

**技术文档：** 见功能描述

---

### 2. 工时批量填报功能 ✅

**完成内容：**
- ✅ 首页"工时填报"入口
- ✅ 多行工时填报表单
- ✅ 后端批量创建API（/api/work-hours/batch）
- ✅ 中大型项目任务创建强制选择迭代

**估计工作量：** 已完成

---

### 3. RBAC 用户、角色、权限管理系统 ✅

**完成内容：**
- ✅ 用户管理 - CRUD、角色分配、启用/禁用
- ✅ 角色管理 - CRUD、权限分配
- ✅ 权限管理 - CRUD、权限查询
- ✅ 用户-角色关联 API
- ✅ 角色-权限关联 API
- ✅ 用户注册功能 - 默认禁用状态，需管理员审核
- ✅ 用户管理页面（UserList.vue）
- ✅ 角色管理页面（RoleList.vue）
- ✅ 权限管理页面（PermissionList.vue）
- ✅ 注册页面（RegisterView.vue）
- ✅ 三级权限控制（路由级 + 按钮级 + 数据级）
- ✅ 系统管理菜单（用户管理、角色管理、权限管理）

**技术文档：** `docs/RBAC_IMPLEMENTATION.md`

---

### 2. 操作日志功能 ✅

**完成内容：**
- ✅ 操作日志实体和表结构（sys_operation_log）
- ✅ 操作类型枚举（CREATE/UPDATE/DELETE/ASSIGN/REMOVE/LOGIN/LOGOUT/QUERY）
- ✅ 操作模块枚举（USER/ROLE/PERMISSION/PROJECT/TASK/WORK_HOUR/DEPARTMENT/ITERATION/SYSTEM）
- ✅ 操作日志查询 API（支持多条件过滤）
- ✅ 操作日志记录功能（成功/失败、IP地址、错误信息）
- ✅ 操作日志查询页面（OperationLogList.vue）
- ✅ 操作日志菜单和路由
- ✅ **数据变更追踪功能**（old_value/new_value JSON 字段）
  - CREATE 操作：记录新创建的完整实体
  - UPDATE 操作：记录更新前后的完整实体
  - DELETE 操作：记录删除前的完整实体
  - 支持日期时间格式化（yyyy-MM-dd HH:mm:ss）
  - 前端可视化展示（折叠面板 + JSON 格式化）

**性能影响：** 仅增加 6-13ms/操作，无额外数据库查询

**技术文档：** `docs/RBAC_IMPLEMENTATION.md` - 操作日志部分

---

### 3. 项目-迭代-任务关系重构 ✅

**完成内容：**
- ✅ 双项目类型（常规型 vs 中大型）
  - **常规型项目**：项目 → 任务（无迭代层）
  - **中大型项目**：项目 → 迭代 → 任务
- ✅ 数据库迁移（V20260114）- 添加 project_type 字段
- ✅ ProjectType 枚举（SCHEDULE/LARGE_SCALE）
- ✅ 智能数据迁移（有迭代的项目自动设为中大型）
- ✅ 项目创建时选择项目类型
- ✅ 项目详情页动态布局（根据项目类型）
- ✅ 迭代管理嵌入项目详情页
- ✅ 迭代 CRUD 功能（创建/编辑/删除/查看）
- ✅ 任务创建时的项目类型校验
- ✅ 菜单结构调整（移除独立迭代菜单）
- ✅ ProjectMapper.xml 修复（project_type 字段映射）

**技术决策：**
- 项目类型创建后固定，不可修改
- 中大型项目的任务必须关联迭代
- 常规型项目的任务不能关联迭代
- 枚举序列化使用字符串名（@JsonValue on toString()）

**技术文档：** `docs/project-iteration-task-redesign.md`

---

### 4. 甘特图功能 ✅

**完成内容：**
- ✅ 甘特图后端接口（任务时间范围查询）
- ✅ 甘特图前端页面（基于 ECharts）
- ✅ 任务时间轴展示
- ✅ 任务依赖关系展示

**技术文档：** `docs/gantt-chart-implementation-plan.md`

---

## 📋 待完成的功能

### 优先级 P0（高优先级）

#### 1. 工时填报功能优化 ⚠️ **NEW**

**当前状态：** 基础功能已实现，待优化体验

**改进需求：**
1. **文案优化**
   - [ ] 将"批量填报工时"改为"工时填报"（去掉"批量"概念）
   - [ ] 统一称呼，避免用户困惑

2. **统一日期选择**
   - [ ] 去掉每行的日期选项
   - [ ] 对话框标题旁显示日期（默认今天）
   - [ ] 标题旁紧邻日期选择器，可修改日期
   - [ ] 所有行共用一个填报日期

3. **工时日历集成**
   - [ ] 工时日历点击具体某天，触发工时填报对话框
   - [ ] 点击日期自动填充到填报日期字段

4. **剩余工时显示** ✅
   - [ ] 添加"剩余工时"列（可选显示）
   - [ ] 选中项目后显示项目剩余工时
   - [ ] 选中任务后显示任务剩余工时
   - [ ] 后端API：获取任务/项目预估工时和已用工时
   - [ ] 允许实际填报超过预估工时（不做限制）

5. **智能默认规则**
   - [ ] 当用户只有一个活跃项目时，自动默认选中
   - [ ] 当用户只有一个活跃任务时，自动默认选中
   - [ ] 多个项目/任务时，不自动默认，让用户选择
   - [ ] 活跃判断：进行中或未开始的项目和任务

**估计工作量：** 1 天

---

#### 2. 统计模块完善 ⚠️

**当前状态：** 已完成 ✅

**已完成接口：**
- [x] GET /api/statistics/dashboard - 仪表盘统计数据
- [x] GET /api/statistics/project/{projectId} - 项目工时统计
- [x] GET /api/statistics/user/{userId} - 人员工时统计
- [x] GET /api/statistics/department/{deptId} - 部门工时统计
- [x] GET /api/statistics/trend - 工时趋势统计

**状态：** 已完成（2026-01-14）

---

#### 3. Controller 层测试补充 ⚠️

**待创建测试：**

**待实现接口：**
- [ ] GET /api/statistics/dashboard - 仪表盘统计数据
- [ ] GET /api/statistics/project/{projectId} - 项目工时统计
- [ ] GET /api/statistics/user/{userId} - 人员工时统计
- [ ] GET /api/statistics/department/{deptId} - 部门工时统计
- [ ] GET /api/statistics/iteration/{iterationId} - 迭代进度统计

**估计工作量：** 3-5 天

---

#### 2. Controller 层测试补充 ⚠️

**待创建测试：**
- [ ] DepartmentControllerTest.java
- [ ] ProjectControllerTest.java
- [ ] IterationControllerTest.java
- [ ] TaskControllerTest.java

**当前覆盖率：** 33% (2/6) - 已完成 User 和 WorkHour

**估计工作量：** 2-3 天

---

#### 3. 数据库优化 ⚠️

**索引优化：**
- [ ] 添加外键约束（保证数据完整性）
- [ ] 添加查询性能索引（迭代ID、任务状态、工时日期等）

**估计工作量：** 0.5 天

---

### 优先级 P1（中优先级）

#### 4. 权限模板功能 📋

**功能描述：**
- 预置角色模板（系统管理员、项目经理、普通员工）
- 快速基于模板创建角色
- 模板管理页面

**估计工作量：** 2-3 天

---

#### 5. 数据级权限控制 📋

**功能描述：**
- 部门级数据权限过滤
- 项目级数据权限过滤
- 自定义数据权限规则

**估计工作量：** 3-5 天

---

#### 6. Service 层单元测试 📋

**待创建测试：**
- [ ] UserServiceTest.java
- [ ] ProjectServiceTest.java
- [ ] TaskServiceTest.java
- [ ] WorkHourServiceTest.java

**当前覆盖率：** 0%

**估计工作量：** 3-4 天

---

### 优先级 P2（低优先级）

#### 7. 性能优化 📋

**优化内容：**
- [ ] 权限缓存优化（前端 + 后端）
- [ ] 批量权限检查接口
- [ ] 表格虚拟滚动（大数据量）
- [ ] Redis 缓存引入

**估计工作量：** 3-5 天

---

#### 8. 配置文件优化 📋

**优化内容：**
- [ ] 移除硬编码密码（使用环境变量）
- [ ] 添加 HikariCP 连接池配置
- [ ] 移除重复配置

**估计工作量：** 0.5 天

---

## 📊 项目完成度统计

**总体完成度：** 92%

### 核心模块完成情况

| 模块 | 完成度 | 说明 |
|------|--------|------|
| 用户模块 | 100% | ✅ CRUD + 认证 + 权限 |
| 部门模块 | 95% | ⚠️ 缺少 Controller 测试 |
| 项目模块 | 100% | ✅ CRUD + 成员管理 + 看板视图 + 项目类型 |
| 迭代模块 | 100% | ✅ CRUD + 嵌入项目详情 |
| 任务模块 | 95% | ✅ CRUD + 看板拖拽 + 智能状态更新 |
| 工时模块 | 100% | ✅ CRUD + 审批流程 |
| 统计模块 | 30% | ❌ 未实现 |
| 权限模块 | 100% | ✅ RBAC 完整实现 |
| 操作日志 | 100% | ✅ 日志记录 + 变更追踪 |

### 基础设施完成情况

| 模块 | 完成度 | 说明 |
|------|--------|------|
| 认证授权 | 100% | ✅ JWT + 权限控制 |
| 异常处理 | 100% | ✅ 全局异常处理 |
| 数据库迁移 | 100% | ✅ Flyway + 8个版本 |
| 测试基础设施 | 80% | ⚠️ Controller 测试 33%，Service 测试 0% |

---

## 🎯 下一步计划

### 本周计划（P0 优先级）

1. **实现统计模块** (3-5 天)
   - 仪表盘数据接口
   - 项目工时统计
   - 人员工时统计

2. **补充 Controller 测试** (2-3 天)
   - DepartmentControllerTest
   - ProjectControllerTest
   - IterationControllerTest
   - TaskControllerTest

### 下周计划（P1 优先级）

1. **数据库优化** (0.5 天)
   - 添加外键约束
   - 添加性能索引

2. **Service 层测试** (3-4 天)
   - 核心业务逻辑测试
   - 权限检查测试

---

## 📝 技术债务清单

### 代码质量
- [ ] Mapper XML 包路径引用（已完成 90%）
- [ ] SQL 注释不足
- [ ] 代码注释不完整

### 测试覆盖
- [ ] Controller 测试覆盖率提升到 100%
- [ ] Service 测试覆盖率提升到 80%

### 性能优化
- [ ] 缺少数据库索引
- [ ] 无缓存机制
- [ ] 无连接池优化配置

### 安全性
- [ ] 密码硬编码（应使用环境变量）
- [ ] HTTPS 配置（生产环境）

---

## 📚 相关文档

### 技术文档
- `CLAUDE.md` - 项目开发指南
- `COLLABORATION.md` - 前后端协作沟通板
- `docs/RBAC_IMPLEMENTATION.md` - RBAC 系统实现文档
- `docs/project-iteration-task-redesign.md` - 项目-迭代-任务重构方案
- `docs/gantt-chart-implementation-plan.md` - 甘特图实现计划
- `docs/task-status-update-feature.md` - 任务状态更新功能
- `docs/DATABASE_OPTIMIZATION.md` - 数据库优化指南
- `docs/development/frontend-architecture.md` - 前端架构文档

### 需要清理的文档
- `docs/enum-type-standardization-2026-01-03.md` - 过期（枚举已标准化）
- `docs/dev-plan.md` - 过期（已更新到 PROJECT_STATUS_AND_PLAN.md）
- `docs/req.md` - 过期（需求已实现）
- `docs/refactoring-principles.md` - 空文件

---

**最后更新：** 2026-01-13
