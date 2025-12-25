-- ============================================
-- 文件名：update_ddl_20241225.sql
-- 说明：表重命名（加前缀）+ 系统表逻辑删除字段 + 补充字段
-- 注意：执行前请备份数据库
-- 依赖：需先执行 update_ddl_20241224.sql
-- ============================================

-- 第一步：表重命名（业务表加 gsms_ 前缀，系统表加 sys_ 前缀）
RENAME TABLE `user` TO `gsms_user`;
RENAME TABLE `department` TO `gsms_department`;
RENAME TABLE `project` TO `gsms_project`;
RENAME TABLE `project_member` TO `gsms_project_member`;
RENAME TABLE `iteration` TO `gsms_iteration`;
RENAME TABLE `task` TO `gsms_task`;
RENAME TABLE `work_hour` TO `gsms_work_hour`;
RENAME TABLE `role` TO `sys_role`;
RENAME TABLE `user_role` TO `sys_user_role`;
RENAME TABLE `permission` TO `sys_permission`;
RENAME TABLE `role_permission` TO `sys_role_permission`;
RENAME TABLE `operation_log` TO `sys_operation_log`;

-- 第二步：系统表增加逻辑删除字段
ALTER TABLE `sys_role`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

ALTER TABLE `sys_permission`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

ALTER TABLE `sys_user_role`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `create_time`;

ALTER TABLE `sys_role_permission`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `create_time`;

ALTER TABLE `gsms_project_member`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

-- 第三步：补充 update_user_id 字段
ALTER TABLE `gsms_project`
    ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `create_user_id`;

ALTER TABLE `gsms_task`
    ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `create_user_id`;

ALTER TABLE `gsms_work_hour`
    ADD COLUMN `create_user_id` bigint(20) NOT NULL COMMENT '创建人ID' AFTER `status`,
    ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `create_user_id`;
