-- ============================================
-- 权限分类 - 添加 permission_type 字段
-- 创建时间: 2026-01-15
-- 描述: 为权限添加类型分类，便于管理和查询
--       permission_type: 1=功能权限 2=菜单权限 3=数据权限
-- ============================================

-- 1. 添加 permission_type 字段
ALTER TABLE sys_permission
ADD COLUMN permission_type TINYINT NOT NULL DEFAULT 1
COMMENT '权限类型 1:功能权限 2:菜单权限 3:数据权限';

-- 2. 更新现有权限类型
-- 菜单权限
UPDATE sys_permission
SET permission_type = 2
WHERE code LIKE 'MENU_%';

-- 数据权限
UPDATE sys_permission
SET permission_type = 3
WHERE code LIKE '%_VIEW_ALL';

-- 功能权限（按钮权限）
UPDATE sys_permission
SET permission_type = 1
WHERE code LIKE '%_CREATE'
   OR code LIKE '%_EDIT'
   OR code LIKE '%_DELETE';

-- ============================================
-- 迁移完成
-- ============================================
-- 验证SQL:
-- SELECT id, name, code, permission_type FROM sys_permission ORDER BY permission_type, code;
-- 预期结果:
-- permission_type = 1 (功能权限): PROJECT_CREATE, TASK_EDIT, WORKHOUR_DELETE 等
-- permission_type = 2 (菜单权限): MENU_PROJECT, MENU_TASK, MENU_WORKHOUR 等
-- permission_type = 3 (数据权限): PROJECT_VIEW_ALL, TASK_VIEW_ALL, WORKHOUR_VIEW_ALL 等
