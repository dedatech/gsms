-- ============================================
-- V9: 数据库设计完整优化
-- ============================================
-- 本版本整合了以下优化：
-- 1. 审计字段标准化（核心表）
-- 2. 关联表简化设计
-- 3. 外键约束（25个）
-- 4. 性能优化索引（20+个）
--
-- 设计原则：
-- - 核心业务表：完整审计字段（create_user_id, create_time, update_user_id, update_time, is_deleted）
-- - 纯关联表：只保留create_time（审计通过操作日志表）
-- - 半业务表：保留create_user_id和create_time
-- ============================================

-- ============================================
-- 第一部分：审计字段优化
-- ============================================

-- 1. sys_user 表添加审计字段
ALTER TABLE `sys_user`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 2. sys_department 表添加审计字段
ALTER TABLE `sys_department`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 3. gsms_project 表添加审计字段
ALTER TABLE `gsms_project`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 4. gsms_project_member 表添加审计字段（半业务表，保留create_user_id）
ALTER TABLE `gsms_project_member`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 5. gsms_iteration 表添加审计字段
ALTER TABLE `gsms_iteration`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 6. gsms_task 表添加审计字段
ALTER TABLE `gsms_task`
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 7. gsms_work_hour 表添加审计字段
ALTER TABLE `gsms_work_hour`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 8. role 表添加审计字段
ALTER TABLE `role`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 9. permission 表添加审计字段
ALTER TABLE `permission`
    ADD COLUMN `create_user_id` BIGINT COMMENT '创建人ID',
    ADD COLUMN `update_user_id` BIGINT COMMENT '更新人ID',
    ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '逻辑删除标记 0:正常 1:已删除',
    ADD INDEX `idx_is_deleted` (`is_deleted`);

-- 10. sys_user_role 纯关联表：只保留create_time（已有，不需要额外字段）

-- 11. role_permission 纯关联表：确保有create_time（已有，不需要额外字段）

-- ============================================
-- 第二部分：为历史数据设置默认值
-- ============================================

UPDATE `sys_user` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;
UPDATE `sys_department` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;
UPDATE `gsms_project` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_project_member` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_iteration` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_task` SET `update_user_id` = `create_user_id` WHERE `update_user_id` IS NULL;
UPDATE `gsms_work_hour` SET `create_user_id` = `user_id`, `update_user_id` = `user_id` WHERE `create_user_id` IS NULL;
UPDATE `role` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;
UPDATE `permission` SET `create_user_id` = 1, `update_user_id` = 1 WHERE `create_user_id` IS NULL;

-- ============================================
-- 第三部分：修改字段为 NOT NULL
-- ============================================

ALTER TABLE `sys_user` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `sys_user` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `sys_department` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `sys_department` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_project` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_project_member` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_iteration` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_task` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_work_hour` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `gsms_work_hour` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `role` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `role` MODIFY `update_user_id` BIGINT NOT NULL;
ALTER TABLE `permission` MODIFY `create_user_id` BIGINT NOT NULL;
ALTER TABLE `permission` MODIFY `update_user_id` BIGINT NOT NULL;

-- ============================================
-- 第四部分：外键约束（25个）
-- ============================================

-- 系统表外键约束（5个）

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

-- 业务表外键约束（20个）

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
-- 第五部分：性能优化索引（20+个）
-- ============================================

-- 外键字段索引（15个）
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

-- 高频查询索引（10个）

-- 任务表索引
CREATE INDEX `idx_task_status` ON `gsms_task`(`status`);
CREATE INDEX `idx_task_type_priority` ON `gsms_task`(`type`, `priority`);
CREATE INDEX `idx_task_create_time` ON `gsms_task`(`create_time`);

-- 工时表索引
CREATE INDEX `idx_workhour_status_date` ON `gsms_work_hour`(`status`, `work_date`);
CREATE INDEX `idx_workhour_user_date` ON `gsms_work_hour`(`user_id`, `work_date`);

-- 项目表索引
CREATE INDEX `idx_project_status` ON `gsms_project`(`status`);
CREATE INDEX `idx_project_create_time` ON `gsms_project`(`create_time`);

-- 迭代表索引
CREATE INDEX `idx_iteration_status` ON `gsms_iteration`(`status`);
CREATE INDEX `idx_iteration_plan_start_date` ON `gsms_iteration`(`plan_start_date`);

-- 用户表索引
CREATE INDEX `idx_user_status` ON `sys_user`(`status`);

-- 部门表索引
CREATE INDEX `idx_department_parent_id` ON `sys_department`(`parent_id`);
CREATE INDEX `idx_department_level_sort` ON `sys_department`(`level`, `sort`);

-- ============================================
-- 第六部分：创建操作日志表（审计中心）
-- ============================================

CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    `user_id` BIGINT NOT NULL COMMENT '操作人ID',
    `username` VARCHAR(50) NOT NULL COMMENT '操作人用户名',
    `operation` VARCHAR(100) NOT NULL COMMENT '操作类型（CREATE/UPDATE/DELETE/ASSIGN等）',
    `module` VARCHAR(50) NOT NULL COMMENT '模块名称（USER/ROLE/PROJECT等）',
    `business_type` VARCHAR(50) COMMENT '业务类型（ROLE_PERMISSION/PROJECT_MEMBER等）',
    `business_id` VARCHAR(200) COMMENT '业务ID（JSON格式或关键ID）',
    `old_value` TEXT COMMENT '旧值（JSON格式）',
    `new_value` TEXT COMMENT '新值（JSON格式）',
    `request_method` VARCHAR(10) COMMENT '请求方法（GET/POST/PUT/DELETE）',
    `request_params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `uri` VARCHAR(500) COMMENT '请求URI',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `execute_time` INT COMMENT '执行时长(ms)',
    `status` TINYINT DEFAULT 1 COMMENT '状态 1:成功 0:失败',
    `error_msg` VARCHAR(1000) COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    KEY `idx_user_id` (`user_id`),
    KEY `idx_module` (`module`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_operation` (`operation`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_module_business` (`module`, `business_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 优化总结
-- ============================================
--
-- 1. 审计字段：
--    - 核心业务表（9个）：完整审计字段
--    - 纯关联表（2个）：只有create_time（简化设计）
--    - 半业务表（1个）：create_user_id + create_time
--
-- 2. 外键约束：25个
--    - CASCADE: 从属关系自动删除
--    - RESTRICT: 防止误删重要数据
--    - SET NULL: 非必需关联置空
--
-- 3. 性能索引：25个
--    - 外键索引：15个
--    - 高频查询索引：10个
--
-- 4. 操作日志表：统一的审计中心
--
-- 设计原则：
-- "数据表应该专注于它的核心职责。关联表的核心职责是'表达关系'，
--  审计应该通过专门的日志系统来完成。"
-- ============================================
