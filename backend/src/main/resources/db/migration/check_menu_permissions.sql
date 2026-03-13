-- ============================================
-- 菜单权限调试脚本
-- 用于诊断侧边栏不渲染问题
-- ============================================

-- 1. 检查 Flyway 迁移历史
-- 如果 V20260115__Create_menu_tables 不在列表中，说明迁移未执行
SELECT * FROM flyway_schema_history WHERE version LIKE 'V20260115%' ORDER BY installed_rank DESC LIMIT 10;

-- 2. 检查菜单表是否存在且有数据
SELECT COUNT(*) as menu_count FROM sys_menu WHERE is_deleted = 0;
-- 预期结果：17（如果没有数据，说明迁移未执行）

-- 3. 检查菜单权限是否存在
SELECT COUNT(*) as menu_permission_count FROM sys_permission WHERE code LIKE 'MENU_%';
-- 预期结果：6（MENU_DASHBOARD, MENU_PROJECT, MENU_TASK, MENU_WORKHOUR, MENU_STATISTICS, MENU_SYSTEM）

-- 4. 检查菜单-权限关联
SELECT COUNT(*) as menu_permission_link_count FROM sys_menu_permission WHERE is_deleted = 0;
-- 预期结果：17（每个菜单都有一个权限关联）

-- 5. 检查角色是否有菜单权限
SELECT r.name, rp.permission_id, p.code
FROM sys_role r
INNER JOIN sys_role_permission rp ON r.id = rp.role_id
INNER JOIN sys_permission p ON rp.permission_id = p.id
WHERE p.code LIKE 'MENU_%'
ORDER BY r.id, p.code;
-- 预期结果：每个角色应该有不同的菜单权限

-- 6. 检查用户 ID=1（admin）的角色分配
SELECT u.username, r.name, r.id as role_id
FROM sys_user u
INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.is_deleted = 0
INNER JOIN sys_role r ON ur.role_id = r.id
WHERE u.id = 1;
-- 预期结果：admin -> SYS_ADMIN (role_id=1)

-- 7. 检查用户 ID=1 可访问的菜单（实际查询）
SELECT DISTINCT m.id, m.name, m.path, m.parent_id, m.sort
FROM sys_menu m
INNER JOIN sys_menu_permission mp ON m.id = mp.menu_id AND mp.is_deleted = 0
INNER JOIN sys_role_permission rp ON mp.permission_id = rp.permission_id
INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id AND ur.is_deleted = 0
WHERE ur.user_id = 1
  AND m.is_deleted = 0
  AND m.status = 1
  AND m.visible = 1
ORDER BY m.parent_id, m.sort;
-- 预期结果：应该返回菜单列表，而不是空结果

-- ============================================
-- 如果以上检查发现数据缺失，执行以下修复：
-- ============================================

-- 修复方案 1：重新运行 Flyway 迁移（推荐）
-- 在 backend 目录执行：
-- mvn flyway:repair
-- mvn flyway:migrate

-- 修复方案 2：手动执行迁移脚本（如果 Flyway 失败）
-- source E:/codes/gsms/backend/src/main/resources/db/migration/V20260115__Create_menu_tables.sql;
