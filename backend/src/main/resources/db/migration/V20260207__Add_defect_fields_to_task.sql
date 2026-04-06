-- 为缺陷跟踪添加必要的字段
ALTER TABLE `gsms_task`
ADD COLUMN `severity` INT DEFAULT NULL COMMENT '缺陷严重程度：1-轻微 2-次要 3-主要 4-严重 5-致命' AFTER `priority`,
ADD COLUMN `reproduction_steps` TEXT DEFAULT NULL COMMENT '缺陷复现步骤' AFTER `description`,
ADD COLUMN `attachments` VARCHAR(1000) DEFAULT NULL COMMENT '附件列表（JSON格式）' AFTER `reproduction_steps`,
ADD COLUMN `fix_version` VARCHAR(100) DEFAULT NULL COMMENT '修复版本' AFTER `attachments`;

-- 添加索引以提升查询性能
ALTER TABLE `gsms_task`
ADD KEY `idx_task_severity` (`severity`),
ADD KEY `idx_task_type_status` (`type`, `status`);
