-- ============================================
-- U9: 回滚数据库设计完整优化
-- ============================================
-- 本脚本回滚V9的所有更改：
-- 1. 删除操作日志表
-- 2. 删除所有性能优化索引
-- 3. 删除所有外键约束
-- 4. 删除审计字段
-- ============================================

-- ============================================
-- 第一部分：删除操作日志表
-- ============================================

DROP TABLE IF EXISTS `sys_operation_log`;

-- ============================================
-- 第二部分：删除所有索引
-- ============================================

-- 任务表索引
DROP INDEX IF EXISTS `idx_task_status` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_type_priority` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_create_time` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_project_id` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_iteration_id` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_assignee_id` ON `gsms_task`;

-- 工时表索引
DROP INDEX IF EXISTS `idx_workhour_status_date` ON `gsms_work_hour`;
DROP INDEX IF EXISTS `idx_workhour_user_date` ON `gsms_work_hour`;
DROP INDEX IF EXISTS `idx_work_hour_project_id` ON `gsms_work_hour`;
DROP INDEX IF EXISTS `idx_work_hour_task_id` ON `gsms_work_hour`;

-- 项目表索引
DROP INDEX IF EXISTS `idx_project_status` ON `gsms_project`;
DROP INDEX IF EXISTS `idx_project_create_time` ON `gsms_project`;
DROP INDEX IF EXISTS `idx_project_manager_id` ON `gsms_project`;
DROP INDEX IF EXISTS `idx_project_create_user_id` ON `gsms_project`;

-- 项目成员表索引
DROP INDEX IF EXISTS `idx_project_member_project_id` ON `gsms_project_member`;
DROP INDEX IF EXISTS `idx_project_member_user_id` ON `gsms_project_member`;

-- 迭代表索引
DROP INDEX IF EXISTS `idx_iteration_status` ON `gsms_iteration`;
DROP INDEX IF EXISTS `idx_iteration_plan_start_date` ON `gsms_iteration`;
DROP INDEX IF EXISTS `idx_iteration_project_id` ON `gsms_iteration`;

-- 用户表索引
DROP INDEX IF EXISTS `idx_user_status` ON `sys_user`;
DROP INDEX IF EXISTS `idx_user_department_id` ON `sys_user`;

-- 部门表索引
DROP INDEX IF EXISTS `idx_department_parent_id` ON `sys_department`;
DROP INDEX IF EXISTS `idx_department_level_sort` ON `sys_department`;

-- 用户角色关联表索引
DROP INDEX IF EXISTS `idx_user_role_user_id` ON `sys_user_role`;
DROP INDEX IF EXISTS `idx_user_role_role_id` ON `sys_user_role`;

-- 角色权限关联表索引
DROP INDEX IF EXISTS `idx_role_permission_role_id` ON `role_permission`;
DROP INDEX IF EXISTS `idx_role_permission_permission_id` ON `role_permission`;

-- ============================================
-- 第三部分：删除所有外键约束
-- ============================================

-- 系统表外键约束（5个）
ALTER TABLE `sys_user` DROP FOREIGN KEY IF EXISTS `fk_user_department`;
ALTER TABLE `sys_user_role` DROP FOREIGN KEY IF EXISTS `fk_user_role_user`;
ALTER TABLE `sys_user_role` DROP FOREIGN KEY IF EXISTS `fk_user_role_role`;
ALTER TABLE `role_permission` DROP FOREIGN KEY IF EXISTS `fk_role_permission_role`;
ALTER TABLE `role_permission` DROP FOREIGN KEY IF EXISTS `fk_role_permission_permission`;

-- 业务表外键约束（20个）
ALTER TABLE `gsms_project` DROP FOREIGN KEY IF EXISTS `fk_project_manager`;
ALTER TABLE `gsms_project` DROP FOREIGN KEY IF EXISTS `fk_project_creator`;
ALTER TABLE `gsms_project` DROP FOREIGN KEY IF EXISTS `fk_project_updater`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY IF EXISTS `fk_project_member_project`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY IF EXISTS `fk_project_member_user`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY IF EXISTS `fk_project_member_creator`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY IF EXISTS `fk_project_member_updater`;
ALTER TABLE `gsms_iteration` DROP FOREIGN KEY IF EXISTS `fk_iteration_project`;
ALTER TABLE `gsms_iteration` DROP FOREIGN KEY IF EXISTS `fk_iteration_creator`;
ALTER TABLE `gsms_iteration` DROP FOREIGN KEY IF EXISTS `fk_iteration_updater`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY IF EXISTS `fk_task_project`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY IF EXISTS `fk_task_iteration`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY IF EXISTS `fk_task_assignee`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY IF EXISTS `fk_task_creator`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY IF EXISTS `fk_task_updater`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY IF EXISTS `fk_work_hour_user`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY IF EXISTS `fk_work_hour_project`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY IF EXISTS `fk_work_hour_task`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY IF EXISTS `fk_work_hour_creator`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY IF EXISTS `fk_work_hour_updater`;

-- ============================================
-- 第四部分：删除审计字段
-- ============================================

-- 1. sys_user 表移除审计字段
ALTER TABLE `sys_user`
    DROP COLUMN IF EXISTS `create_user_id`,
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 2. sys_department 表移除审计字段
ALTER TABLE `sys_department`
    DROP COLUMN IF EXISTS `create_user_id`,
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 3. gsms_project 表移除审计字段
ALTER TABLE `gsms_project`
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 4. gsms_project_member 表移除审计字段
ALTER TABLE `gsms_project_member`
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 5. gsms_iteration 表移除审计字段
ALTER TABLE `gsms_iteration`
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 6. gsms_task 表移除审计字段
ALTER TABLE `gsms_task`
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 7. gsms_work_hour 表移除审计字段
ALTER TABLE `gsms_work_hour`
    DROP COLUMN IF EXISTS `create_user_id`,
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 8. role 表移除审计字段
ALTER TABLE `role`
    DROP COLUMN IF EXISTS `create_user_id`,
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 9. permission 表移除审计字段
ALTER TABLE `permission`
    DROP COLUMN IF EXISTS `create_user_id`,
    DROP COLUMN IF EXISTS `update_user_id`,
    DROP COLUMN IF EXISTS `is_deleted`;

-- 注意：sys_user_role 和 role_permission 是纯关联表，不需要删除额外字段
-- 因为V9没有给它们添加审计字段（只保留了原有的create_time）

-- ============================================
-- 回滚完成
-- ============================================
-- 数据库已恢复到V8状态
-- ============================================
