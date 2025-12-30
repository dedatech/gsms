-- 添加性能优化索引
-- 为高频查询字段添加索引以提升查询性能

-- ============================================
-- 任务表索引优化
-- ============================================

-- 任务状态索引（用于任务列表筛选）
CREATE INDEX `idx_task_status` ON `gsms_task`(`status`);

-- 任务类型和优先级组合索引（用于任务看板）
CREATE INDEX `idx_task_type_priority` ON `gsms_task`(`type`, `priority`);

-- 任务创建时间索引（用于时间排序）
CREATE INDEX `idx_task_create_time` ON `gsms_task`(`create_time`);

-- ============================================
-- 工时表索引优化
-- ============================================

-- 工时状态和日期组合索引（用于工时统计）
CREATE INDEX `idx_workhour_status_date` ON `gsms_work_hour`(`status`, `work_date`);

-- 工时用户和日期组合索引（用于个人工时查询）
CREATE INDEX `idx_workhour_user_date` ON `gsms_work_hour`(`user_id`, `work_date`);

-- ============================================
-- 项目表索引优化
-- ============================================

-- 项目状态索引（用于项目列表筛选）
CREATE INDEX `idx_project_status` ON `gsms_project`(`status`);

-- 项目创建时间索引（用于项目排序）
CREATE INDEX `idx_project_create_time` ON `gsms_project`(`create_time`);

-- ============================================
-- 迭代表索引优化
-- ============================================

-- 迭代状态索引（用于迭代列表筛选）
CREATE INDEX `idx_iteration_status` ON `gsms_iteration`(`status`);

-- 迭代计划开始时间索引（用于时间排序）
CREATE INDEX `idx_iteration_plan_start_date` ON `gsms_iteration`(`plan_start_date`);

-- ============================================
-- 用户表索引优化
-- ============================================

-- 用户状态索引（用于用户列表筛选）
CREATE INDEX `idx_user_status` ON `sys_user`(`status`);

-- 用户部门索引（用于部门成员查询）
CREATE INDEX `idx_user_department_id` ON `sys_user`(`department_id`);

-- ============================================
-- 部门表索引优化
-- ============================================

-- 部门父级ID索引（用于部门树查询）
CREATE INDEX `idx_department_parent_id` ON `sys_department`(`parent_id`);

-- 部门层级和排序索引（用于部门列表排序）
CREATE INDEX `idx_department_level_sort` ON `sys_department`(`level`, `sort`);
