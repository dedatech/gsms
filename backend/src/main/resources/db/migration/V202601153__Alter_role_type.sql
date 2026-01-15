-- ============================================
-- 角色类型改造 - role_level 改为 role_type
-- 创建时间: 2026-01-15
-- 描述: 将角色从 role_level（系统级/项目级）改造为 role_type（SYSTEM/CUSTOM）
--       SYSTEM: 系统预置角色（不可删除）如 SYS_ADMIN, EMPLOYEE
--       CUSTOM: 用户自定义角色（可删除）
-- ============================================

-- 1. 添加 role_type 字段
ALTER TABLE sys_role
ADD COLUMN role_type ENUM('SYSTEM', 'CUSTOM') NOT NULL DEFAULT 'CUSTOM'
COMMENT '角色类型 SYSTEM:系统预置(不可删除) CUSTOM:用户自定义(可删除)';

-- 2. 迁移现有数据
-- SYS_ADMIN 和 EMPLOYEE 标记为系统角色
UPDATE sys_role
SET role_type = 'SYSTEM'
WHERE code IN ('SYS_ADMIN', 'EMPLOYEE');

-- 其他角色标记为自定义角色
UPDATE sys_role
SET role_type = 'CUSTOM'
WHERE code NOT IN ('SYS_ADMIN', 'EMPLOYEE');

-- 3. 删除 role_level 字段
ALTER TABLE sys_role DROP COLUMN role_level;

-- ============================================
-- 迁移完成
-- ============================================
-- 验证SQL:
-- SELECT id, name, code, role_type FROM sys_role;
-- 预期结果:
-- id | name          | code        | role_type
-- 1  | 系统管理员    | SYS_ADMIN   | SYSTEM
-- 2  | 业务经理      | ...         | CUSTOM
-- 3  | 项目经理      | ...         | CUSTOM
-- 4  | 员工          | EMPLOYEE    | SYSTEM
