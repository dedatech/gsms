

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT COMMENT '操作人ID',
  `username` VARCHAR(50) COMMENT '操作人用户名',
  `operation_type` INT COMMENT '操作类型：1-CREATE,2-UPDATE,3-DELETE,4-ASSIGN,5-REMOVE,6-LOGIN,7-LOGOUT,8-QUERY',
  `module` INT COMMENT '操作模块：1-USER,2-ROLE,3-PERMISSION,4-PROJECT,5-TASK,6-WORK_HOUR,7-DEPARTMENT,8-ITERATION,9-SYSTEM',
  `operation_content` VARCHAR(500) COMMENT '操作内容描述',
  `ip_address` VARCHAR(50) COMMENT 'IP地址',
  `status` INT DEFAULT 1 COMMENT '操作状态：1-SUCCESS,2-FAILED',
  `error_message` VARCHAR(1000) COMMENT '错误信息',
  `operation_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
