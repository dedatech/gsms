-- ============================================
-- 动态菜单系统 - 数据库迁移脚本
-- 创建时间: 2026-01-15
-- 描述: 创建菜单表、菜单权限关联表,并初始化菜单数据和权限配置
-- ============================================

-- ============================================
-- 1. 创建菜单表（sys_menu）
-- ============================================
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径（菜单项才有，目录为空）',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径（前端路由组件）',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '菜单图标',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID，0表示根菜单',
  `sort` INT DEFAULT 0 COMMENT '排序号（同级菜单排序）',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '菜单类型 1:目录 2:菜单 3:按钮',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1:启用 2:禁用',
  `visible` TINYINT DEFAULT 1 COMMENT '是否可见 1:可见 2:隐藏',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  KEY `idx_menu_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`type`),
  KEY `idx_menu_status` (`status`),
  KEY `idx_menu_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ============================================
-- 2. 创建菜单权限关联表（sys_menu_permission）
-- ============================================
DROP TABLE IF EXISTS `sys_menu_permission`;
CREATE TABLE `sys_menu_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_permission` (`menu_id`, `permission_id`),
  KEY `idx_menu_permission_menu` (`menu_id`),
  KEY `idx_menu_permission_permission` (`permission_id`),
  CONSTRAINT `fk_menu_permission_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_menu_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限关联表';

-- ============================================
-- 3. 初始化菜单数据（从现有 Layout.vue 迁移）
-- ============================================
INSERT INTO `sys_menu` (`id`, `name`, `path`, `component`, `icon`, `parent_id`, `sort`, `type`, `status`, `visible`, `create_user_id`, `update_user_id`) VALUES
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

-- ============================================
-- 4. 初始化菜单权限码到 sys_permission 表
-- 使用 INSERT IGNORE 避免重复插入
-- ============================================
INSERT IGNORE INTO `sys_permission` (`name`, `code`, `description`, `create_user_id`, `update_user_id`) VALUES
('访问首页', 'MENU_DASHBOARD', '可访问首页菜单', 1, 1),
('访问项目管理', 'MENU_PROJECT', '可访问项目管理菜单', 1, 1),
('访问任务中心', 'MENU_TASK', '可访问任务中心菜单', 1, 1),
('访问工时管理', 'MENU_WORKHOUR', '可访问工时管理相关菜单', 1, 1),
('访问统计分析', 'MENU_STATISTICS', '可访问统计分析相关菜单', 1, 1),
('访问系统管理', 'MENU_SYSTEM', '可访问系统管理相关菜单', 1, 1);

-- ============================================
-- 5. 为菜单分配权限
-- 注意: 这里使用子查询动态获取权限ID,避免硬编码
-- ============================================
INSERT INTO `sys_menu_permission` (`menu_id`, `permission_id`, `update_user_id`)
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

-- ============================================
-- 6. 为角色分配菜单权限
-- 使用 INSERT IGNORE 避免重复插入
-- ============================================

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
-- 迁移完成
-- ============================================
-- 验证数据:
-- SELECT COUNT(*) FROM sys_menu;  -- 应返回 17
-- SELECT COUNT(*) FROM sys_menu_permission;  -- 应返回 17
-- SELECT COUNT(*) FROM sys_permission WHERE code LIKE 'MENU_%';  -- 应返回 6
