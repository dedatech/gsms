-- 添加parent_id字段到任务表，支持父子任务关系
ALTER TABLE `gsms_task`
ADD COLUMN `parent_id` BIGINT DEFAULT NULL COMMENT '父任务ID' AFTER `iteration_id`,
ADD KEY `idx_task_parent_id` (`parent_id`);
