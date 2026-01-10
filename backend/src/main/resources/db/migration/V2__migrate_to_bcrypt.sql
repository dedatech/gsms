-- V2__migrate_to_bcrypt.sql
-- 从MD5迁移到BCrypt密码哈希算法
-- 执行此脚本后，所有用户需要使用默认密码 Admin123 登录并立即修改密码
--
-- 重要说明：
-- 1. 此脚本将所有用户密码重置为统一的BCrypt哈希值
-- 2. BCrypt哈希值是对密码 "Admin123" 使用强度12加密的结果
-- 3. BCrypt特点：每次加密相同密码会生成不同哈希值（随机盐）
-- 4. 但验证时可以正确匹配原密码

-- 1. 添加密码重置标志字段（如果不存在则添加）
-- 注意：MySQL 5.7+ 不支持 IF NOT EXISTS 语法，使用存储过程方式忽略错误
SET @column_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                       WHERE TABLE_SCHEMA = DATABASE()
                       AND TABLE_NAME = 'sys_user'
                       AND COLUMN_NAME = 'password_reset_required');

SET @sql = IF(@column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN password_reset_required TINYINT(1) DEFAULT 0 COMMENT ''是否需要重置密码（0-否，1-是）''',
    'SELECT ''Column already exists''');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 重置所有用户密码为BCrypt哈希
-- 密码: Admin123
-- BCrypt强度: 12
-- 哈希值: $2a$12$CQpisUhdgrlmuz.76H79Y.jJGFR3dGribbfDyrCASJzkUsJkWVXfG
-- 验证: PasswordUtil.verify("Admin123", "$2a$12$CQpisUhdgrlmuz.76H79Y.jJGFR3dGribbfDyrCASJzkUsJkWVXfG") == true
UPDATE sys_user
SET password = '$2a$12$CQpisUhdgrlmuz.76H79Y.jJGFR3dGribbfDyrCASJzkUsJkWVXfG',
    password_reset_required = 1
WHERE id > 0;

-- 说明：
-- - 执行此脚本后，所有用户需要使用密码 "Admin123" 登录
-- - 首次登录后，建议用户立即使用 PUT /api/users/password 修改密码
-- - 新用户注册时会自动使用BCrypt加密密码
-- - BCrypt比MD5更安全，自动加盐，抗彩虹表攻击
