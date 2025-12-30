-- 回滚 V9: 移除添加的审计字段和逻辑删除字段

-- 1. sys_user 表移除审计字段
ALTER TABLE `sys_user`
    DROP COLUMN `create_user_id`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 2. sys_department 表移除审计字段
ALTER TABLE `sys_department`
    DROP COLUMN `create_user_id`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 3. gsms_project 表移除审计字段
ALTER TABLE `gsms_project`
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 4. gsms_project_member 表移除审计字段
ALTER TABLE `gsms_project_member`
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 5. gsms_iteration 表移除审计字段
ALTER TABLE `gsms_iteration`
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 6. gsms_task 表移除审计字段
ALTER TABLE `gsms_task`
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 7. gsms_work_hour 表移除审计字段
ALTER TABLE `gsms_work_hour`
    DROP COLUMN `create_user_id`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 8. role 表移除审计字段
ALTER TABLE `role`
    DROP COLUMN `create_user_id`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 9. permission 表移除审计字段
ALTER TABLE `permission`
    DROP COLUMN `create_user_id`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 10. sys_user_role 表移除审计字段
ALTER TABLE `sys_user_role`
    DROP COLUMN `update_time`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;

-- 11. role_permission 表移除审计字段
ALTER TABLE `role_permission`
    DROP COLUMN `update_time`,
    DROP COLUMN `update_user_id`,
    DROP COLUMN `is_deleted`;
