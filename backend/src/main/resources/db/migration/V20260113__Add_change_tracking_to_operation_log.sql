-- 为操作日志表添加数据变更追踪字段
-- 作者: Claude Code
-- 日期: 2026-01-13
-- 说明: 添加 business_type, business_id, old_value, new_value 字段，支持记录操作前后的完整数据变更

-- 检查并添加 business_type 字段
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_operation_log'
    AND COLUMN_NAME = 'business_type'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `sys_operation_log` ADD COLUMN `business_type` VARCHAR(50) COMMENT ''业务类型：USER, PROJECT, TASK, WORK_HOUR等'' AFTER `module`',
    'SELECT ''business_type column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 business_id 字段
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_operation_log'
    AND COLUMN_NAME = 'business_id'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `sys_operation_log` ADD COLUMN `business_id` BIGINT COMMENT ''业务ID（对应实体主键ID）'' AFTER `business_type`',
    'SELECT ''business_id column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 old_value 字段
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_operation_log'
    AND COLUMN_NAME = 'old_value'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `sys_operation_log` ADD COLUMN `old_value` TEXT COMMENT ''变更前数据（JSON格式）'' AFTER `business_id`',
    'SELECT ''old_value column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 new_value 字段
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_operation_log'
    AND COLUMN_NAME = 'new_value'
);

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `sys_operation_log` ADD COLUMN `new_value` TEXT COMMENT ''变更后数据（JSON格式）'' AFTER `old_value`',
    'SELECT ''new_value column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 创建复合索引（提升按业务类型和ID查询的性能）
SET @index_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_operation_log'
    AND INDEX_NAME = 'idx_business'
);

SET @sql = IF(@index_exists = 0,
    'CREATE INDEX `idx_business` ON `sys_operation_log`(`business_type`, `business_id`)',
    'SELECT ''idx_business index already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证字段创建
SELECT
    CONCAT('字段 ', COLUMN_NAME, ' 已添加') AS result
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 'sys_operation_log'
AND COLUMN_NAME IN ('business_type', 'business_id', 'old_value', 'new_value')
ORDER BY ORDINAL_POSITION;
