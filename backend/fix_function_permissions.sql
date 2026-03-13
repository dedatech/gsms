-- ============================================
-- 功能权限修复脚本
-- 为 admin 用户分配完整的功能权限
-- ============================================

-- 1. 插入功能权限（项目管理相关）
INSERT IGNORE INTO `sys_permission` (`name`, `code`, `description`, `permission_type`, `create_user_id`, `update_user_id`) VALUES
-- 项目管理权限 (ID 100-109)
('查看项目', 'PROJECT_VIEW', '可查看项目列表和详情', 1, 1, 1),
('创建项目', 'PROJECT_CREATE', '可创建新项目', 1, 1, 1),
('编辑项目', 'PROJECT_EDIT', '可编辑项目信息', 1, 1, 1),
('删除项目', 'PROJECT_DELETE', '可删除项目', 1, 1, 1),

-- 任务管理权限 (ID 110-119)
('查看任务', 'TASK_VIEW', '可查看任务列表和详情', 1, 1, 1),
('创建任务', 'TASK_CREATE', '可创建新任务', 1, 1, 1),
('编辑任务', 'TASK_EDIT', '可编辑任务信息', 1, 1, 1),
('删除任务', 'TASK_DELETE', '可删除任务', 1, 1, 1),

-- 迭代管理权限 (ID 120-129)
('查看迭代', 'ITERATION_VIEW', '可查看迭代列表和详情', 1, 1, 1),
('创建迭代', 'ITERATION_CREATE', '可创建新迭代', 1, 1, 1),
('编辑迭代', 'ITERATION_EDIT', '可编辑迭代信息', 1, 1, 1),
('删除迭代', 'ITERATION_DELETE', '可删除迭代', 1, 1, 1),

-- 工时管理权限 (ID 130-139)
('查看工时', 'WORKHOUR_VIEW', '可查看工时记录', 1, 1, 1),
('创建工时', 'WORKHOUR_CREATE', '可创建工时记录', 1, 1, 1),
('编辑工时', 'WORKHOUR_EDIT', '可编辑工时记录', 1, 1, 1),
('删除工时', 'WORKHOUR_DELETE', '可删除工时记录', 1, 1, 1),

-- 统计分析权限 (ID 140-149)
('项目统计', 'STATISTICS_PROJECT', '可查看项目工时统计', 1, 1, 1),
('用户统计', 'STATISTICS_USER', '可查看用户工时统计', 1, 1, 1),
('查看统计', 'STATISTICS_VIEW', '可查看统计分析', 1, 1, 1),

-- 用户管理权限 (ID 150-159)
('查看用户', 'USER_VIEW', '可查看用户列表', 1, 1, 1),
('创建用户', 'USER_CREATE', '可创建新用户', 1, 1, 1),
('编辑用户', 'USER_EDIT', '可编辑用户信息', 1, 1, 1),
('删除用户', 'USER_DELETE', '可删除用户', 1, 1, 1),

-- 角色管理权限 (ID 160-169)
('查看角色', 'ROLE_VIEW', '可查看角色列表', 1, 1, 1),
('创建角色', 'ROLE_CREATE', '可创建新角色', 1, 1, 1),
('编辑角色', 'ROLE_EDIT', '可编辑角色信息', 1, 1, 1),
('删除角色', 'ROLE_DELETE', '可删除角色', 1, 1, 1),

-- 权限管理权限 (ID 170-179)
('查看权限', 'PERMISSION_VIEW', '可查看权限列表', 1, 1, 1),
('分配权限', 'PERMISSION_ASSIGN', '可分配角色权限', 1, 1, 1);

-- 2. 为 SYS_ADMIN (role_id=1) 分配所有功能权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 1, id, NOW()
FROM `sys_permission`
WHERE `code` IN (
    'PROJECT_VIEW', 'PROJECT_CREATE', 'PROJECT_EDIT', 'PROJECT_DELETE',
    'TASK_VIEW', 'TASK_CREATE', 'TASK_EDIT', 'TASK_DELETE',
    'ITERATION_VIEW', 'ITERATION_CREATE', 'ITERATION_EDIT', 'ITERATION_DELETE',
    'WORKHOUR_VIEW', 'WORKHOUR_CREATE', 'WORKHOUR_EDIT', 'WORKHOUR_DELETE',
    'STATISTICS_PROJECT', 'STATISTICS_USER', 'STATISTICS_VIEW',
    'USER_VIEW', 'USER_CREATE', 'USER_EDIT', 'USER_DELETE',
    'ROLE_VIEW', 'ROLE_CREATE', 'ROLE_EDIT', 'ROLE_DELETE',
    'PERMISSION_VIEW', 'PERMISSION_ASSIGN'
);

-- 3. 为 BUSINESS_MANAGER (role_id=2) 分配权限（只读+统计）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 2, id, NOW()
FROM `sys_permission`
WHERE `code` IN (
    'PROJECT_VIEW', 'TASK_VIEW', 'WORKHOUR_VIEW',
    'STATISTICS_PROJECT', 'STATISTICS_USER', 'STATISTICS_VIEW'
);

-- 4. 为 PROJECT_MANAGER (role_id=3) 分配权限（项目+任务+工时）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 3, id, NOW()
FROM `sys_permission`
WHERE `code` IN (
    'PROJECT_VIEW', 'PROJECT_CREATE', 'PROJECT_EDIT',
    'TASK_VIEW', 'TASK_CREATE', 'TASK_EDIT',
    'WORKHOUR_VIEW', 'WORKHOUR_CREATE', 'WORKHOUR_EDIT'
);

-- 5. 为 EMPLOYEE (role_id=4) 分配权限（任务+工时）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 4, id, NOW()
FROM `sys_permission`
WHERE `code` IN (
    'TASK_VIEW', 'WORKHOUR_VIEW'
);

-- ============================================
-- 验证 SQL（执行后检查）
-- ============================================
-- 查看 admin 用户的所有权限
-- SELECT DISTINCT p.code, p.name
-- FROM sys_permission p
-- INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
-- INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id
-- WHERE ur.user_id = 1
-- ORDER BY p.code;

-- 预期结果：应该包含所有功能权限（PROJECT_VIEW, TASK_VIEW 等）
