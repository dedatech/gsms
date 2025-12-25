-- 为核心业务表增加逻辑删除标记字段 is_deleted

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
