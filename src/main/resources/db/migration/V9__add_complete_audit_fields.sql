-- 添加完整的审计字段和逻辑删除字段
-- 确保所有表都包含: create_user_id, create_time, update_user_id, update_time, is_deleted

-- 1. sys_user 表添加审计字段
ALTER TABLE `sys_user`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 2. sys_department 表添加审计字段
ALTER TABLE `sys_department`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 3. gsms_project 表添加审计字段
ALTER TABLE `gsms_project`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 4. gsms_project_member 表添加审计字段
ALTER TABLE `gsms_project_member`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 5. gsms_iteration 表添加审计字段
ALTER TABLE `gsms_iteration`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 6. gsms_task 表添加审计字段
ALTER TABLE `gsms_task`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 7. gsms_work_hour 表添加审计字段
ALTER TABLE `gsms_work_hour`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 8. role 表添加审计字段
ALTER TABLE `role`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 9. permission 表添加审计字段
ALTER TABLE `permission`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 10. sys_user_role 表添加审计字段
ALTER TABLE `sys_user_role`
    ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 11. role_permission 表添加审计字段
ALTER TABLE `role_permission`
    ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 12. 为已有数据设置默认的创建人和更新人（使用系统管理员ID=1）
UPDATE `sys_user` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;
UPDATE `sys_department` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;
UPDATE `gsms_project` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_project_member` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_iteration` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_task` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_work_hour` SET `create_user_id` = `user_id`, `update_user_id` = `user_id` WHERE `create_user_id` IS NULL;
UPDATE `role` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;
UPDATE `permission` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;
UPDATE `sys_user_role` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `role_permission` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;

-- 13. 修改字段为 NOT NULL（在设置完默认值后）
ALTER TABLE `sys_user` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `sys_user` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `sys_department` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `sys_department` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_project` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_project_member` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_iteration` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_task` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_work_hour` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_work_hour` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `role` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `role` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `permission` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `permission` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `sys_user_role` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `role_permission` MODIFY `update_user_id` BIGINT NOT NULL;
