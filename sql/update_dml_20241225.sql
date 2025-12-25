-- ============================================
-- 文件名：update_dml_20241225.sql
-- 说明：基于新表名的角色权限初始化数据
-- 注意：需先执行 update_ddl_20241225.sql
-- ============================================

-- 1. 预置系统角色（sys_role）
INSERT INTO sys_role (id, name, code, description, role_level, create_time, update_time)
VALUES 
  (1, '系统管理员',  'SYS_ADMIN',        '系统级管理员，拥有所有权限',              1, NOW(), NOW()),
  (2, '业务经理',    'BUSINESS_MANAGER', '业务分析/统计角色，全局只读',            1, NOW(), NOW()),
  (3, '项目经理',    'PROJECT_MANAGER',  '项目级管理者，管理参与项目范围内数据',  2, NOW(), NOW()),
  (4, '员工',        'EMPLOYEE',         '普通员工，管理个人任务与工时',          2, NOW(), NOW());

-- 2. 预置权限点（sys_permission）
INSERT INTO sys_permission (id, name, code, description, create_time, update_time)
VALUES
  (1, '查看所有项目', 'PROJECT_VIEW_ALL',  '可查看系统中所有项目',          NOW(), NOW()),
  (2, '查看所有任务', 'TASK_VIEW_ALL',     '可查看系统中所有任务',          NOW(), NOW()),
  (3, '查看所有工时', 'WORKHOUR_VIEW_ALL', '可查看系统中所有工时记录',      NOW(), NOW());

-- 3. 角色-权限关联（sys_role_permission）
-- 说明：
--   SYS_ADMIN (id=1)：拥有所有全局查看权限
--   BUSINESS_MANAGER (id=2)：也拥有所有全局查看权限（全局只读）
--   PROJECT_MANAGER / EMPLOYEE 目前不赋全局查看权限，走项目成员范围控制

INSERT INTO sys_role_permission (id, role_id, permission_id, create_user_id, create_time)
VALUES
  (1, 1, 1, 1, NOW()),   -- SYS_ADMIN -> PROJECT_VIEW_ALL
  (2, 1, 2, 1, NOW()),   -- SYS_ADMIN -> TASK_VIEW_ALL
  (3, 1, 3, 1, NOW()),   -- SYS_ADMIN -> WORKHOUR_VIEW_ALL
  (4, 2, 1, 1, NOW()),   -- BUSINESS_MANAGER -> PROJECT_VIEW_ALL
  (5, 2, 2, 1, NOW()),   -- BUSINESS_MANAGER -> TASK_VIEW_ALL
  (6, 2, 3, 1, NOW());   -- BUSINESS_MANAGER -> WORKHOUR_VIEW_ALL

-- 4. 示例：给具体用户绑定角色（按需启用）
-- INSERT INTO sys_user_role (id, user_id, role_id, create_user_id, create_time)
-- VALUES
--   (1, 1, 1, 1, NOW()),  -- 用户1为SYS_ADMIN
--   (2, 2, 2, 1, NOW());  -- 用户2为BUSINESS_MANAGER
