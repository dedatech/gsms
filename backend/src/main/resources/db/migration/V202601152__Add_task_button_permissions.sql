-- ============================================
-- 添加任务管理按钮级权限
-- 创建时间: 2026-01-15
-- 描述: 为任务管理添加按钮级权限控制（创建、编辑、删除）
-- ============================================

-- 插入任务管理按钮权限
INSERT INTO `sys_permission` (`name`, `code`, `description`, `create_user_id`, `update_user_id`) VALUES
('创建任务', 'TASK_CREATE', '可创建任务', 1, 1),
('编辑任务', 'TASK_EDIT', '可编辑任务', 1, 1),
('删除任务', 'TASK_DELETE', '可删除任务', 1, 1);

-- 为角色分配任务按钮权限
-- SYS_ADMIN(ID=1): 所有权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 1, id, NOW()
FROM `sys_permission`
WHERE `code` IN ('TASK_CREATE', 'TASK_EDIT', 'TASK_DELETE');

-- PROJECT_MANAGER(ID=3): 可以创建、编辑、删除任务
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 3, id, NOW()
FROM `sys_permission`
WHERE `code` IN ('TASK_CREATE', 'TASK_EDIT', 'TASK_DELETE');

-- EMPLOYEE(ID=4): 默认没有任务管理权限（只能查看）

-- BUSINESS_MANAGER(ID=2): 可以创建、编辑、删除任务
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`)
SELECT 2, id, NOW()
FROM `sys_permission`
WHERE `code` IN ('TASK_CREATE', 'TASK_EDIT', 'TASK_DELETE');

-- ============================================
-- 迁移完成
-- ============================================
-- 验证SQL:
-- SELECT rp.*, p.code, p.name
-- FROM sys_role_permission rp
-- INNER JOIN sys_permission p ON rp.permission_id = p.id
-- WHERE p.code LIKE 'TASK_%'
-- ORDER BY rp.role_id, p.code;
