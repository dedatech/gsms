-- 回滚 V12: 恢复关联表的完整审计字段

-- ============================================
-- 1. 恢复 role_permission 的审计字段
-- ============================================

ALTER TABLE `role_permission`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记';

DROP INDEX `idx_create_time` ON `role_permission`;

-- ============================================
-- 2. 恢复 sys_user_role 的审计字段
-- ============================================

ALTER TABLE `sys_user_role`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记';

DROP INDEX `idx_create_time` ON `sys_user_role`;

-- ============================================
-- 3. 恢复 gsms_project_member 的审计字段
-- ============================================

ALTER TABLE `gsms_project_member`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记';

-- ============================================
-- 4. 删除操作日志表（如果是本次迁移创建的）
-- ============================================

DROP TABLE IF EXISTS `sys_operation_log`;

-- ============================================
-- 5. 为历史数据设置默认值
-- ============================================

UPDATE `role_permission`
SET `create_user_id` = 1, `update_user_id` = 1, `is_deleted` = 0
WHERE `create_user_id` IS NULL;

UPDATE `sys_user_role`
SET `update_user_id` = `create_user_id`, `is_deleted` = 0
WHERE `update_user_id` IS NULL;

UPDATE `gsms_project_member`
SET `update_user_id` = `create_user_id`, `is_deleted` = 0
WHERE `update_user_id` IS NULL;

-- ============================================
-- 6. 修改字段为 NOT NULL
-- ============================================

ALTER TABLE `role_permission`
    MODIFY `create_user_id` BIGINT NOT NULL,
    MODIFY `update_user_id` BIGINT NOT NULL;

ALTER TABLE `sys_user_role`
    MODIFY `update_user_id` BIGINT NOT NULL;

ALTER TABLE `gsms_project_member`
    MODIFY `update_user_id` BIGINT NOT NULL;

-- ============================================
-- 7. 添加 is_deleted 索引
-- ============================================

ALTER TABLE `role_permission`
    ADD INDEX `idx_is_deleted` (`is_deleted`);

ALTER TABLE `sys_user_role`
    ADD INDEX `idx_is_deleted` (`is_deleted`);

ALTER TABLE `gsms_project_member`
    ADD INDEX `idx_is_deleted` (`is_deleted`);
