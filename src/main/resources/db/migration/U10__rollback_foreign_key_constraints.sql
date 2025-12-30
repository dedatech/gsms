-- 回滚 V10: 移除外键约束和性能优化索引

-- ============================================
-- 移除性能优化索引
-- ============================================

DROP INDEX IF EXISTS `idx_user_department_id` ON `sys_user`;
DROP INDEX IF EXISTS `idx_user_role_user_id` ON `sys_user_role`;
DROP INDEX IF EXISTS `idx_user_role_role_id` ON `sys_user_role`;
DROP INDEX IF EXISTS `idx_role_permission_role_id` ON `role_permission`;
DROP INDEX IF EXISTS `idx_role_permission_permission_id` ON `role_permission`;
DROP INDEX IF EXISTS `idx_project_manager_id` ON `gsms_project`;
DROP INDEX IF EXISTS `idx_project_create_user_id` ON `gsms_project`;
DROP INDEX IF EXISTS `idx_project_member_project_id` ON `gsms_project_member`;
DROP INDEX IF EXISTS `idx_project_member_user_id` ON `gsms_project_member`;
DROP INDEX IF EXISTS `idx_iteration_project_id` ON `gsms_iteration`;
DROP INDEX IF EXISTS `idx_task_project_id` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_iteration_id` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_task_assignee_id` ON `gsms_task`;
DROP INDEX IF EXISTS `idx_work_hour_project_id` ON `gsms_work_hour`;
DROP INDEX IF EXISTS `idx_work_hour_task_id` ON `gsms_work_hour`;

-- ============================================
-- 移除外键约束
-- ============================================

-- 系统表外键约束
ALTER TABLE `sys_user` DROP FOREIGN KEY `fk_user_department`;
ALTER TABLE `sys_user_role` DROP FOREIGN KEY `fk_user_role_user`;
ALTER TABLE `sys_user_role` DROP FOREIGN KEY `fk_user_role_role`;
ALTER TABLE `role_permission` DROP FOREIGN KEY `fk_role_permission_role`;
ALTER TABLE `role_permission` DROP FOREIGN KEY `fk_role_permission_permission`;

-- 业务表外键约束
ALTER TABLE `gsms_project` DROP FOREIGN KEY `fk_project_manager`;
ALTER TABLE `gsms_project` DROP FOREIGN KEY `fk_project_creator`;
ALTER TABLE `gsms_project` DROP FOREIGN KEY `fk_project_updater`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY `fk_project_member_project`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY `fk_project_member_user`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY `fk_project_member_creator`;
ALTER TABLE `gsms_project_member` DROP FOREIGN KEY `fk_project_member_updater`;
ALTER TABLE `gsms_iteration` DROP FOREIGN KEY `fk_iteration_project`;
ALTER TABLE `gsms_iteration` DROP FOREIGN KEY `fk_iteration_creator`;
ALTER TABLE `gsms_iteration` DROP FOREIGN KEY `fk_iteration_updater`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY `fk_task_project`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY `fk_task_iteration`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY `fk_task_assignee`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY `fk_task_creator`;
ALTER TABLE `gsms_task` DROP FOREIGN KEY `fk_task_updater`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY `fk_work_hour_user`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY `fk_work_hour_project`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY `fk_work_hour_task`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY `fk_work_hour_creator`;
ALTER TABLE `gsms_work_hour` DROP FOREIGN KEY `fk_work_hour_updater`;
