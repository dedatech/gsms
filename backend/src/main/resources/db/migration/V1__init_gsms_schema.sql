-- ============================================
-- GSMS 数据库初始化脚本
-- ============================================
-- 描述：工时管理系统完整数据库结构，包含表结构、外键约束、索引和初始数据
-- 包含内容：
--   - 12张表（7个系统表 + 5个业务表）
--   - 25个外键约束
--   - 27个性能优化索引
--   - 初始数据（管理员用户、根部门、角色、权限）
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 第一部分：系统表
-- ============================================

-- 1. 用户表（系统基础表）
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
  `department_id` BIGINT DEFAULT NULL COMMENT '部门ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1:正常 2:禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_user_department_id` (`department_id`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 部门表（系统基础表）
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父部门ID',
  `level` INT DEFAULT 1 COMMENT '层级',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  KEY `idx_department_parent_id` (`parent_id`),
  KEY `idx_department_level_sort` (`level`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 3. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `role_level` TINYINT DEFAULT 1 COMMENT '角色级别 1:系统级 2:项目级',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 4. 权限表
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` VARCHAR(100) NOT NULL COMMENT '权限名称',
  `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '权限描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 5. 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_role_user_id` (`user_id`),
  KEY `idx_user_role_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 6. 角色权限关联表
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_permission_role_id` (`role_id`),
  KEY `idx_role_permission_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 7. 操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
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
  `execute_time` INT COMMENT '执行时长',
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
-- 第二部分：业务表
-- ============================================

-- 8. 项目表
DROP TABLE IF EXISTS `gsms_project`;
CREATE TABLE `gsms_project` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  `code` VARCHAR(50) NOT NULL COMMENT '项目编码',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
  `manager_id` BIGINT NOT NULL COMMENT '项目负责人ID',
  `status` TINYINT DEFAULT 1 COMMENT '项目状态 1:未开始 2:进行中 3:已挂起 4:已归档',
  `plan_start_date` DATE DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` DATE DEFAULT NULL COMMENT '计划结束日期',
  `actual_start_date` DATE DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` DATE DEFAULT NULL COMMENT '实际结束日期',
  `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_project_manager_id` (`manager_id`),
  KEY `idx_project_create_user_id` (`create_user_id`),
  KEY `idx_project_status` (`status`),
  KEY `idx_project_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 9. 项目成员表
DROP TABLE IF EXISTS `gsms_project_member`;
CREATE TABLE `gsms_project_member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_type` TINYINT NOT NULL DEFAULT 1 COMMENT '角色类型 1:项目管理员 2:项目成员 3:只读访客',
  `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
  KEY `idx_project_member_project_id` (`project_id`),
  KEY `idx_project_member_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目成员表';

-- 10. 迭代表
DROP TABLE IF EXISTS `gsms_iteration`;
CREATE TABLE `gsms_iteration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '迭代ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `name` VARCHAR(100) NOT NULL COMMENT '迭代名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '迭代描述',
  `status` TINYINT DEFAULT 1 COMMENT '迭代状态 1:未开始 2:进行中 3:已完成',
  `plan_start_date` DATE DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` DATE DEFAULT NULL COMMENT '计划结束日期',
  `actual_start_date` DATE DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` DATE DEFAULT NULL COMMENT '实际结束日期',
  `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  KEY `idx_iteration_project_id` (`project_id`),
  KEY `idx_iteration_status` (`status`),
  KEY `idx_iteration_plan_start_date` (`plan_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='迭代表';

-- 11. 任务表
DROP TABLE IF EXISTS `gsms_task`;
CREATE TABLE `gsms_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `iteration_id` BIGINT DEFAULT NULL COMMENT '迭代ID',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `description` TEXT COMMENT '任务描述',
  `type` TINYINT DEFAULT 1 COMMENT '任务类型 1:任务 2:需求 3:缺陷',
  `priority` TINYINT DEFAULT 2 COMMENT '优先级 1:高 2:中 3:低',
  `assignee_id` BIGINT DEFAULT NULL COMMENT '负责人ID',
  `status` TINYINT DEFAULT 1 COMMENT '任务状态 1:待处理 2:进行中 3:已完成',
  `plan_start_date` DATE DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` DATE DEFAULT NULL COMMENT '计划结束日期',
  `actual_start_date` DATE DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` DATE DEFAULT NULL COMMENT '实际结束日期',
  `estimate_hours` DECIMAL(5,2) DEFAULT 0.00 COMMENT '预估工时',
  `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` BIGINT NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  KEY `idx_task_project_id` (`project_id`),
  KEY `idx_task_iteration_id` (`iteration_id`),
  KEY `idx_task_assignee_id` (`assignee_id`),
  KEY `idx_task_status` (`status`),
  KEY `idx_task_type_priority` (`type`, `priority`),
  KEY `idx_task_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 12. 工时记录表
DROP TABLE IF EXISTS `gsms_work_hour`;
CREATE TABLE `gsms_work_hour` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工时记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `project_id` BIGINT NOT NULL COMMENT '项目ID',
  `task_id` BIGINT DEFAULT NULL COMMENT '任务ID',
  `work_date` DATE NOT NULL COMMENT '工作日期',
  `hours` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '工时数',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '工作内容描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态 1:已保存 2:已提交 3:已确认',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` BIGINT NOT NULL COMMENT '创建人ID',
  `update_user_id` BIGINT NOT NULL COMMENT '更新人ID',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_work_date` (`work_date`),
  KEY `idx_work_hour_project_id` (`project_id`),
  KEY `idx_work_hour_task_id` (`task_id`),
  KEY `idx_workhour_status_date` (`status`, `work_date`),
  KEY `idx_workhour_user_date` (`user_id`, `work_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工时记录表';

-- ============================================
-- 第三部分：外键约束（25个）
-- ============================================

-- 系统表外键约束（5个）
ALTER TABLE `sys_user` ADD CONSTRAINT `fk_user_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `sys_user_role` ADD CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `sys_user_role` ADD CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `sys_role_permission` ADD CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `sys_role_permission` ADD CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 业务表外键约束（20个）
ALTER TABLE `gsms_project` ADD CONSTRAINT `fk_project_manager` FOREIGN KEY (`manager_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_project` ADD CONSTRAINT `fk_project_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_project` ADD CONSTRAINT `fk_project_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `gsms_project_member` ADD CONSTRAINT `fk_project_member_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `gsms_project_member` ADD CONSTRAINT `fk_project_member_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `gsms_project_member` ADD CONSTRAINT `fk_project_member_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_project_member` ADD CONSTRAINT `fk_project_member_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `gsms_iteration` ADD CONSTRAINT `fk_iteration_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `gsms_iteration` ADD CONSTRAINT `fk_iteration_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_iteration` ADD CONSTRAINT `fk_iteration_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `gsms_task` ADD CONSTRAINT `fk_task_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project`(`id`) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE `gsms_task` ADD CONSTRAINT `fk_task_iteration` FOREIGN KEY (`iteration_id`) REFERENCES `gsms_iteration`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `gsms_task` ADD CONSTRAINT `fk_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `sys_user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `gsms_task` ADD CONSTRAINT `fk_task_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_task` ADD CONSTRAINT `fk_task_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `gsms_work_hour` ADD CONSTRAINT `fk_work_hour_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_work_hour` ADD CONSTRAINT `fk_work_hour_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_work_hour` ADD CONSTRAINT `fk_work_hour_task` FOREIGN KEY (`task_id`) REFERENCES `gsms_task`(`id`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `gsms_work_hour` ADD CONSTRAINT `fk_work_hour_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE `gsms_work_hour` ADD CONSTRAINT `fk_work_hour_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 第四部分：初始数据
-- ============================================
--
-- 注意：由于循环依赖（user.department_id → department, department.create_user_id → user）
-- 需要按以下顺序插入：
-- 1. 先插入管理员用户（department_id 设为 NULL）
-- 2. 再插入根部门（create_user_id = 1）
-- 3. 最后更新用户的 department_id
-- ============================================

-- 1. 插入初始管理员用户（department_id 暂时为 NULL）
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `phone`, `department_id`, `status`, `create_time`, `update_time`, `create_user_id`, `update_user_id`, `is_deleted`)
VALUES
  (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@gsms.com', '13800138000', NULL, 1, NOW(), NOW(), 1, 1, 0);
-- 密码: admin123 (BCrypt加密)

-- 2. 插入根部门（create_user_id = 1，指向刚创建的管理员）
INSERT INTO `sys_department` (`id`, `name`, `parent_id`, `level`, `sort`, `remark`, `create_time`, `update_time`, `create_user_id`, `update_user_id`, `is_deleted`)
VALUES
  (1, '总公司', 0, 1, 0, '系统根部门', NOW(), NOW(), 1, 1, 0);

-- 3. 更新管理员用户的部门（解决循环依赖）
UPDATE `sys_user` SET `department_id` = 1 WHERE `id` = 1;

-- 4. 预置系统角色
INSERT INTO `sys_role` (`id`, `name`, `code`, `description`, `role_level`, `create_time`, `update_time`, `create_user_id`, `update_user_id`)
VALUES
  (1, '系统管理员',  'SYS_ADMIN',        '系统级管理员，拥有所有权限',              1, NOW(), NOW(), 1, 1),
  (2, '业务经理',    'BUSINESS_MANAGER', '业务分析/统计角色，全局只读',            1, NOW(), NOW(), 1, 1),
  (3, '项目经理',    'PROJECT_MANAGER',  '项目级管理者，管理参与项目范围内数据',  2, NOW(), NOW(), 1, 1),
  (4, '员工',        'EMPLOYEE',         '普通员工，管理个人任务与工时',          2, NOW(), NOW(), 1, 1);

-- 5. 预置权限点
INSERT INTO `sys_permission` (`id`, `name`, `code`, `description`, `create_time`, `update_time`, `create_user_id`, `update_user_id`)
VALUES
  (1, '查看所有项目', 'PROJECT_VIEW_ALL',  '可查看系统中所有项目',          NOW(), NOW(), 1, 1),
  (2, '查看所有任务', 'TASK_VIEW_ALL',     '可查看系统中所有任务',          NOW(), NOW(), 1, 1),
  (3, '查看所有工时', 'WORKHOUR_VIEW_ALL', '可查看系统中所有工时记录',      NOW(), NOW(), 1, 1);

-- 6. 角色-权限关联
INSERT INTO `sys_role_permission` (`id`, `role_id`, `permission_id`, `create_time`)
VALUES
  (1, 1, 1, NOW()),   -- SYS_ADMIN -> PROJECT_VIEW_ALL
  (2, 1, 2, NOW()),   -- SYS_ADMIN -> TASK_VIEW_ALL
  (3, 1, 3, NOW()),   -- SYS_ADMIN -> WORKHOUR_VIEW_ALL
  (4, 2, 1, NOW()),   -- BUSINESS_MANAGER -> PROJECT_VIEW_ALL
  (5, 2, 2, NOW()),   -- BUSINESS_MANAGER -> TASK_VIEW_ALL
  (6, 2, 3, NOW());   -- BUSINESS_MANAGER -> WORKHOUR_VIEW_ALL

-- 7. 为管理员用户分配角色（admin -> SYS_ADMIN）
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`, `is_deleted`)
VALUES
  (1, 1, 1, NOW(), 0);

-- ============================================
-- 初始化完成
-- ============================================
