-- ============================================
-- 为EMPLOYEE角色添加项目管理菜单权限
-- 创建时间: 2026-01-15
-- 描述: 为了支持团队协作，EMPLOYEE角色需要能够查看参与的项目和项目中的所有任务
-- ============================================

-- 为EMPLOYEE角色(ID=4)分配项目管理菜单权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 4, id, NOW()
FROM `sys_permission`
WHERE `code` = 'MENU_PROJECT';

-- ============================================
-- 迁移完成
-- ============================================
-- 验证SQL:
-- SELECT rp.*, p.code, p.name
-- FROM sys_role_permission rp
-- INNER JOIN sys_permission p ON rp.permission_id = p.id
-- WHERE rp.role_id = 4 AND p.code = 'MENU_PROJECT';
