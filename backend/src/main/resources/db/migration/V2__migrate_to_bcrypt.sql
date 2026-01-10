-- V2__migrate_to_bcrypt.sql
-- 从MD5迁移到BCrypt密码哈希算法
-- 执行此脚本后，所有用户需要使用默认密码 Admin123 登录并立即修改密码

-- 1. 添加密码重置标志字段
ALTER TABLE sys_user
ADD COLUMN password_reset_required TINYINT(1) DEFAULT 0 COMMENT '是否需要重置密码（0-否，1-是）';

-- 2. 将所有现有用户标记为需要重置密码
UPDATE sys_user
SET password_reset_required = 1
WHERE id > 0;

-- 3. 重置所有用户密码为统一临时密码：Admin123
-- 注意：这里的BCrypt哈希是对 "Admin123" 进行BCrypt(12)加密的结果
-- 由于BCrypt每次生成结果不同，这里提供一个示例哈希值
-- 实际部署时，应用启动时会自动使用 PasswordUtil.encrypt("Admin123") 生成新的哈希值
UPDATE sys_user
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzpLaEmc3i',
    password_reset_required = 1
WHERE id > 0;

-- 说明：
-- - 执行此脚本前，请确保已备份现有用户数据
-- - 脚本执行后，所有用户需要使用密码 "Admin123" 登录
-- - 首次登录后，系统会要求用户修改密码
-- - 新用户注册时会自动使用BCrypt加密密码
