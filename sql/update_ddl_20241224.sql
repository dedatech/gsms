-- ============================================
-- 文件名：update_ddl_20241224.sql
-- 说明：为核心业务表增加逻辑删除标记字段 is_deleted
-- 注意：执行前请确认表中暂不存在同名字段
-- ============================================

ALTER TABLE `user`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

ALTER TABLE `department`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

ALTER TABLE `project`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

ALTER TABLE `iteration`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

ALTER TABLE `task`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;

ALTER TABLE `work_hour`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `update_time`;
