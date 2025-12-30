-- 优化关联表设计 - 简化审计字段
-- 原则：关联表专注于表达关系，审计通过操作日志完成

-- ============================================
-- 1. 简化 sys_role_permission 表
-- ============================================

-- 移除多余的审计字段
ALTER TABLE `role_permission`
    DROP COLUMN `create_user_id`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `update_time`,
    DROP COLUMN `is_deleted`;

-- 添加创建时间字段（如果还没有）
ALTER TABLE `role_permission`
    ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间'
    AFTER `permission_id`;

-- 添加创建时间索引（便于查询最近授权）
ALTER TABLE `role_permission`
    ADD INDEX `idx_create_time` (`create_time`);

-- ============================================
-- 2. 简化 sys_user_role 表
-- ============================================

-- 移除多余的字段
ALTER TABLE `sys_user_role`
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 添加创建时间字段（如果还没有）
ALTER TABLE `sys_user_role`
    ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间'
    AFTER `role_id`;

-- 添加创建时间索引
ALTER TABLE `sys_user_role`
    ADD INDEX `idx_create_time` (`create_time`);

-- ============================================
-- 3. 优化 gsms_project_member 表
-- ============================================

-- 移除多余的字段，只保留 create_time
ALTER TABLE `gsms_project_member`
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 添加创建时间索引（如果还没有）
ALTER TABLE `gsms_project_member`
    ADD INDEX `idx_create_time` (`create_time`);

-- ============================================
-- 4. 创建/优化操作日志表
-- ============================================

-- 如果表不存在则创建，存在则优化
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '操作人ID',
    `username` VARCHAR(50) NOT NULL COMMENT '操作人用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型（CREATE/UPDATE/DELETE/ASSIGN等）',
    `module` VARCHAR(50) NOT NULL COMMENT '模块名称（USER/ROLE/PROJECT等）',
    `business_type` VARCHAR(50) COMMENT '业务类型（ROLE_PERMISSION/PROJECT_MEMBER等）',
    `business_id` VARCHAR(200) COMMENT '业务ID（JSON格式或关键ID）',
    `old_value` TEXT COMMENT '旧值（JSON格式）',
    `new_value` TEXT COMMENT '新值（JSON格式）',
    `request_method` VARCHAR(10) COMMENT '请求方法（GET/POST/PUT/DELETE）',
    `request_params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `uri` VARCHAR(500) COMMENT '请求URI',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `execute_time` INT COMMENT '执行时长(ms)',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1:成功 0:失败',
    `error_msg` VARCHAR(1000) COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_operation` (`operation`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_module_business` (`module`, `business_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 5. 为历史数据设置默认创建时间
-- ============================================

-- 为 role_permission 的现有数据设置创建时间
UPDATE `role_permission`
SET `create_time` = NOW()
WHERE `create_time` IS NULL;

-- 为 sys_user_role 的现有数据设置创建时间
UPDATE `sys_user_role`
SET `create_time` = NOW()
WHERE `create_time` IS NULL;

-- ============================================
-- 6. 修改字段为 NOT NULL
-- ============================================

ALTER TABLE `role_permission`
    MODIFY `create_time` DATETIME NOT NULL;

ALTER TABLE `sys_user_role`
    MODIFY `create_time` DATETIME NOT NULL;

-- ============================================
-- 说明
-- ============================================

-- 优化后的关联表设计：
--
-- sys_role_permission / sys_user_role:
--   - id: 主键
--   - role_id + permission_id/user_id: 关联关系
--   - create_time: 授权时间（用于查询最近变更）
--   - 不需要 update_time/is_deleted 等字段
--
-- gsms_project_member:
--   - id: 主键
--   - project_id + user_id + role_type: 成员关系
--   - create_time: 加入时间（用于查询最近加入）
--   - create_user_id: 创建人ID（保留，因为是业务表）
--
-- 审计追踪方式：
--   - 所有 CRUD 操作通过 AOP 切面自动记录到 sys_operation_log
--   - 日志包含：操作人、操作类型、业务模块、新旧值、IP等
--   - 可以通过日志表追溯完整的操作历史
