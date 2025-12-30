-- 添加外键约束以确保数据引用完整性
-- 外键命名规范: fk_表名_引用字段名

-- ============================================
-- 系统表外键约束
-- ============================================

-- 1. sys_user.department_id -> sys_department.id
ALTER TABLE `sys_user`
    ADD CONSTRAINT `fk_user_department`
    FOREIGN KEY (`department_id`)
    REFERENCES `sys_department`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 2. sys_user_role.user_id -> sys_user.id
ALTER TABLE `sys_user_role`
    ADD CONSTRAINT `fk_user_role_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- 3. sys_user_role.role_id -> role.id
ALTER TABLE `sys_user_role`
    ADD CONSTRAINT `fk_user_role_role`
    FOREIGN KEY (`role_id`)
    REFERENCES `role`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- 4. role_permission.role_id -> role.id
ALTER TABLE `role_permission`
    ADD CONSTRAINT `fk_role_permission_role`
    FOREIGN KEY (`role_id`)
    REFERENCES `role`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- 5. role_permission.permission_id -> permission.id
ALTER TABLE `role_permission`
    ADD CONSTRAINT `fk_role_permission_permission`
    FOREIGN KEY (`permission_id`)
    REFERENCES `permission`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- ============================================
-- 业务表外键约束
-- ============================================

-- 6. gsms_project.manager_id -> sys_user.id
ALTER TABLE `gsms_project`
    ADD CONSTRAINT `fk_project_manager`
    FOREIGN KEY (`manager_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

-- 7. gsms_project.create_user_id -> sys_user.id
ALTER TABLE `gsms_project`
    ADD CONSTRAINT `fk_project_creator`
    FOREIGN KEY (`create_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 8. gsms_project.update_user_id -> sys_user.id
ALTER TABLE `gsms_project`
    ADD CONSTRAINT `fk_project_updater`
    FOREIGN KEY (`update_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 9. gsms_project_member.project_id -> gsms_project.id
ALTER TABLE `gsms_project_member`
    ADD CONSTRAINT `fk_project_member_project`
    FOREIGN KEY (`project_id`)
    REFERENCES `gsms_project`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- 10. gsms_project_member.user_id -> sys_user.id
ALTER TABLE `gsms_project_member`
    ADD CONSTRAINT `fk_project_member_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- 11. gsms_project_member.create_user_id -> sys_user.id
ALTER TABLE `gsms_project_member`
    ADD CONSTRAINT `fk_project_member_creator`
    FOREIGN KEY (`create_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 12. gsms_project_member.update_user_id -> sys_user.id
ALTER TABLE `gsms_project_member`
    ADD CONSTRAINT `fk_project_member_updater`
    FOREIGN KEY (`update_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 13. gsms_iteration.project_id -> gsms_project.id
ALTER TABLE `gsms_iteration`
    ADD CONSTRAINT `fk_iteration_project`
    FOREIGN KEY (`project_id`)
    REFERENCES `gsms_project`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- 14. gsms_iteration.create_user_id -> sys_user.id
ALTER TABLE `gsms_iteration`
    ADD CONSTRAINT `fk_iteration_creator`
    FOREIGN KEY (`create_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 15. gsms_iteration.update_user_id -> sys_user.id
ALTER TABLE `gsms_iteration`
    ADD CONSTRAINT `fk_iteration_updater`
    FOREIGN KEY (`update_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 16. gsms_task.project_id -> gsms_project.id
ALTER TABLE `gsms_task`
    ADD CONSTRAINT `fk_task_project`
    FOREIGN KEY (`project_id`)
    REFERENCES `gsms_project`(`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;

-- 17. gsms_task.iteration_id -> gsms_iteration.id
ALTER TABLE `gsms_task`
    ADD CONSTRAINT `fk_task_iteration`
    FOREIGN KEY (`iteration_id`)
    REFERENCES `gsms_iteration`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 18. gsms_task.assignee_id -> sys_user.id
ALTER TABLE `gsms_task`
    ADD CONSTRAINT `fk_task_assignee`
    FOREIGN KEY (`assignee_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 19. gsms_task.create_user_id -> sys_user.id
ALTER TABLE `gsms_task`
    ADD CONSTRAINT `fk_task_creator`
    FOREIGN KEY (`create_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 20. gsms_task.update_user_id -> sys_user.id
ALTER TABLE `gsms_task`
    ADD CONSTRAINT `fk_task_updater`
    FOREIGN KEY (`update_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 21. gsms_work_hour.user_id -> sys_user.id
ALTER TABLE `gsms_work_hour`
    ADD CONSTRAINT `fk_work_hour_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

-- 22. gsms_work_hour.project_id -> gsms_project.id
ALTER TABLE `gsms_work_hour`
    ADD CONSTRAINT `fk_work_hour_project`
    FOREIGN KEY (`project_id`)
    REFERENCES `gsms_project`(`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

-- 23. gsms_work_hour.task_id -> gsms_task.id
ALTER TABLE `gsms_work_hour`
    ADD CONSTRAINT `fk_work_hour_task`
    FOREIGN KEY (`task_id`)
    REFERENCES `gsms_task`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 24. gsms_work_hour.create_user_id -> sys_user.id
ALTER TABLE `gsms_work_hour`
    ADD CONSTRAINT `fk_work_hour_creator`
    FOREIGN KEY (`create_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 25. gsms_work_hour.update_user_id -> sys_user.id
ALTER TABLE `gsms_work_hour`
    ADD CONSTRAINT `fk_work_hour_updater`
    FOREIGN KEY (`update_user_id`)
    REFERENCES `sys_user`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- ============================================
-- 性能优化索引
-- ============================================

-- 为外键字段添加索引以提升关联查询性能
CREATE INDEX `idx_user_department_id` ON `sys_user`(`department_id`);
CREATE INDEX `idx_user_role_user_id` ON `sys_user_role`(`user_id`);
CREATE INDEX `idx_user_role_role_id` ON `sys_user_role`(`role_id`);
CREATE INDEX `idx_role_permission_role_id` ON `role_permission`(`role_id`);
CREATE INDEX `idx_role_permission_permission_id` ON `role_permission`(`permission_id`);
CREATE INDEX `idx_project_manager_id` ON `gsms_project`(`manager_id`);
CREATE INDEX `idx_project_create_user_id` ON `gsms_project`(`create_user_id`);
CREATE INDEX `idx_project_member_project_id` ON `gsms_project_member`(`project_id`);
CREATE INDEX `idx_project_member_user_id` ON `gsms_project_member`(`user_id`);
CREATE INDEX `idx_iteration_project_id` ON `gsms_iteration`(`project_id`);
CREATE INDEX `idx_task_project_id` ON `gsms_task`(`project_id`);
CREATE INDEX `idx_task_iteration_id` ON `gsms_task`(`iteration_id`);
CREATE INDEX `idx_task_assignee_id` ON `gsms_task`(`assignee_id`);
CREATE INDEX `idx_work_hour_project_id` ON `gsms_work_hour`(`project_id`);
CREATE INDEX `idx_work_hour_task_id` ON `gsms_work_hour`(`task_id`);
