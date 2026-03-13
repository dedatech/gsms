-- ============================================
-- 菜单权限修复脚本
-- 直接执行此脚本可修复侧边栏不显示问题
-- ============================================

-- 1. 插入菜单权限（使用 INSERT IGNORE 避免重复）
INSERT IGNORE INTO `sys_permission` (`name`, `code`, `description`, `permission_type`, `create_user_id`, `update_user_id`) VALUES
('访问首页', 'MENU_DASHBOARD', '可访问首页菜单', 2, 1, 1),
('访问项目管理', 'MENU_PROJECT', '可访问项目管理菜单', 2, 1, 1),
('访问任务中心', 'MENU_TASK', '可访问任务中心菜单', 2, 1, 1),
('访问工时管理', 'MENU_WORKHOUR', '可访问工时管理相关菜单', 2, 1, 1),
('访问统计分析', 'MENU_STATISTICS', '可访问统计分析相关菜单', 2, 1, 1),
('访问系统管理', 'MENU_SYSTEM', '可访问系统管理相关菜单', 2, 1, 1);

-- 2. 插入菜单数据（使用 INSERT IGNORE 避免重复）
INSERT IGNORE INTO `sys_menu` (`id`, `name`, `path`, `component`, `icon`, `parent_id`, `sort`, `type`, `status`, `visible`, `create_user_id`, `update_user_id`) VALUES
-- 一级菜单（目录）
(1, '首页', '/dashboard', 'Dashboard', 'Odometer', 0, 1, 2, 1, 1, 1, 1),
(2, '项目管理', '/projects', 'ProjectList', 'FolderOpened', 0, 2, 2, 1, 1, 1, 1),
(3, '任务中心', '/tasks', 'TaskList', 'List', 0, 3, 2, 1, 1, 1, 1),
(4, '工时管理', NULL, NULL, 'Clock', 0, 4, 1, 1, 1, 1, 1),
(5, '统计分析', NULL, NULL, 'DataAnalysis', 0, 5, 1, 1, 1, 1, 1),
(6, '系统管理', NULL, NULL, 'Operation', 0, 6, 1, 1, 1, 1, 1),
-- 工时管理子菜单
(7, '工时日历', '/workhours/calendar', 'WorkHourCalendar', NULL, 4, 1, 2, 1, 1, 1, 1),
(8, '工时列表', '/workhours/list', 'WorkHourList', NULL, 4, 2, 2, 1, 1, 1, 1),
-- 统计分析子菜单
(9, '项目工时统计', '/statistics/project', 'ProjectStatistics', NULL, 5, 1, 2, 1, 1, 1, 1),
(10, '用户工时统计', '/statistics/user', 'UserStatistics', NULL, 5, 2, 2, 1, 1, 1, 1),
(11, '工时趋势分析', '/statistics/trend', 'TrendStatistics', NULL, 5, 3, 2, 1, 1, 1, 1),
-- 系统管理子菜单
(12, '用户管理', '/system/users', 'UserList', NULL, 6, 1, 2, 1, 1, 1, 1),
(13, '部门管理', '/system/departments', 'DepartmentList', NULL, 6, 2, 2, 1, 1, 1, 1),
(14, '角色管理', '/system/roles', 'RoleList', NULL, 6, 3, 2, 1, 1, 1, 1),
(15, '权限管理', '/system/permissions', 'PermissionList', NULL, 6, 4, 2, 1, 1, 1, 1),
(16, '菜单管理', '/system/menus', 'MenuList', NULL, 6, 5, 2, 1, 1, 1, 1),
(17, '操作日志', '/system/operation-logs', 'OperationLogList', NULL, 6, 6, 2, 1, 1, 1, 1);

-- 3. 为菜单分配权限
INSERT IGNORE INTO `sys_menu_permission` (`menu_id`, `permission_id`, `update_user_id`)
SELECT 1, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_DASHBOARD'
UNION ALL
SELECT 2, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_PROJECT'
UNION ALL
SELECT 3, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_TASK'
UNION ALL
SELECT 4, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_WORKHOUR'
UNION ALL
SELECT 7, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_WORKHOUR'
UNION ALL
SELECT 8, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_WORKHOUR'
UNION ALL
SELECT 5, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_STATISTICS'
UNION ALL
SELECT 9, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_STATISTICS'
UNION ALL
SELECT 10, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_STATISTICS'
UNION ALL
SELECT 11, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_STATISTICS'
UNION ALL
SELECT 6, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_SYSTEM'
UNION ALL
SELECT 12, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_SYSTEM'
UNION ALL
SELECT 13, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_SYSTEM'
UNION ALL
SELECT 14, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_SYSTEM'
UNION ALL
SELECT 15, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_SYSTEM'
UNION ALL
SELECT 16, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_SYSTEM'
UNION ALL
SELECT 17, id, 1 FROM `sys_permission` WHERE `code` = 'MENU_SYSTEM';

-- 4. 为角色分配菜单权限
-- SYS_ADMIN(ID=1): 所有菜单权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 1, id, NOW()
FROM `sys_permission`
WHERE `code` IN ('MENU_DASHBOARD', 'MENU_PROJECT', 'MENU_TASK', 'MENU_WORKHOUR', 'MENU_STATISTICS', 'MENU_SYSTEM');

-- BUSINESS_MANAGER(ID=2): 首页、项目、任务、工时、统计
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 2, id, NOW()
FROM `sys_permission`
WHERE `code` IN ('MENU_DASHBOARD', 'MENU_PROJECT', 'MENU_TASK', 'MENU_WORKHOUR', 'MENU_STATISTICS');

-- PROJECT_MANAGER(ID=3): 首页、项目、任务、工时
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 3, id, NOW()
FROM `sys_permission`
WHERE `code` IN ('MENU_DASHBOARD', 'MENU_PROJECT', 'MENU_TASK', 'MENU_WORKHOUR');

-- EMPLOYEE(ID=4): 首页、任务、工时
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 4, id, NOW()
FROM `sys_permission`
WHERE `code` IN ('MENU_DASHBOARD', 'MENU_TASK', 'MENU_WORKHOUR');

-- ============================================
-- 执行完成后验证
-- ============================================
-- 检查菜单数量：应该返回 17
-- SELECT COUNT(*) FROM sys_menu WHERE is_deleted = 0;

-- 检查菜单权限数量：应该返回 6
-- SELECT COUNT(*) FROM sys_permission WHERE code LIKE 'MENU_%';

-- 检查菜单-权限关联：应该返回 17
-- SELECT COUNT(*) FROM sys_menu_permission WHERE is_deleted = 0;

-- 检查 admin 用户可访问菜单：应该返回菜单列表
-- SELECT DISTINCT m.* FROM sys_menu m
-- INNER JOIN sys_menu_permission mp ON m.id = mp.menu_id AND mp.is_deleted = 0
-- INNER JOIN sys_role_permission rp ON mp.permission_id = rp.permission_id
-- INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id AND ur.is_deleted = 0
-- WHERE ur.user_id = 1 AND m.is_deleted = 0 AND m.status = 1 AND m.visible = 1
-- ORDER BY m.parent_id, m.sort;
