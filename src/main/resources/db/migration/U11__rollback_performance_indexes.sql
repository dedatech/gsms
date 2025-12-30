-- 回滚 V11: 移除性能优化索引

-- 任务表索引
DROP INDEX IF EXISTS `idx_task_status` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_type_priority` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_create_time` ON `gsms_task`;

-- 工时表索引
DROP INDEX IF EXISTS `idx_workhour_status_date` ON `gsms_work_hour`;
DROP INDEX IF EXISTS `idx_workhour_user_date` ON `gsms_work_hour`;

-- 项目表索引
DROP INDEX IF EXISTS `idx_project_status` ON `gsms_project`;
DROP INDEX IF EXISTS `idx_project_create_time` ON `gsms_project`;

-- 迭代表索引
DROP INDEX IF EXISTS `idx_iteration_status` ON `gsms_iteration`;
DROP INDEX IF EXISTS `idx_iteration_plan_start_date` ON `gsms_iteration`;

-- 用户表索引
DROP INDEX IF EXISTS `idx_user_status` ON `sys_user`;
DROP INDEX IF EXISTS `idx_user_department_id` ON `sys_user`;

-- 部门表索引
DROP INDEX IF EXISTS `idx_department_parent_id` ON `sys_department`;
DROP INDEX IF EXISTS `idx_department_level_sort` ON `sys_department`;
