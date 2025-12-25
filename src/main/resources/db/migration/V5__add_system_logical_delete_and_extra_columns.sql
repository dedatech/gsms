-- 系统表增加逻辑删除字段 + 补充缺失字段

-- 系统表逻辑删除字段
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

-- 补充 create_user_id 和 update_user_id 字段
ALTER TABLE `gsms_work_hour`
    ADD COLUMN `create_user_id` bigint(20) NOT NULL COMMENT '创建人ID' AFTER `status`;
