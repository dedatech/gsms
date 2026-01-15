-- ============================================
-- 清理菜单 type 字段注释 - 移除 type=3 说明
-- 创建时间: 2026-01-15
-- 描述: 按钮权限已通过 sys_permission 实现，不再需要在 sys_menu 中保留 type=3 的定义
--       将 type 字段注释从 "1:目录 2:菜单 3:按钮" 改为 "1:目录 2:菜单"
-- ============================================

-- 修改 type 字段注释，移除按钮类型说明
ALTER TABLE sys_menu
MODIFY COLUMN type TINYINT NOT NULL DEFAULT 1
COMMENT '菜单类型 1:目录 2:菜单';

-- ============================================
-- 迁移完成
-- ============================================
-- 验证SQL:
-- SHOW FULL COLUMNS FROM sys_menu WHERE Field = 'type';
-- 预期结果: Type 列的 Comment 应为 "菜单类型 1:目录 2:菜单"
