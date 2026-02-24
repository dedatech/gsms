/*
 Navicat Premium Dump SQL

 Source Server         : mysql8docker
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3307
 Source Schema         : gsms

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 24/02/2026 18:34:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for flyway_schema_history
-- ----------------------------
DROP TABLE IF EXISTS `flyway_schema_history`;
CREATE TABLE `flyway_schema_history`  (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `checksum` int NULL DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`) USING BTREE,
  INDEX `flyway_schema_history_s_idx`(`success` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of flyway_schema_history
-- ----------------------------
INSERT INTO `flyway_schema_history` VALUES (1, '1', 'init gsms schema', 'SQL', 'V1__init_gsms_schema.sql', 362408730, 'root', '2026-02-24 10:17:17', 1568, 1);
INSERT INTO `flyway_schema_history` VALUES (2, '2', 'migrate to bcrypt', 'SQL', 'V2__migrate_to_bcrypt.sql', -192220491, 'root', '2026-02-24 10:17:17', 56, 1);
INSERT INTO `flyway_schema_history` VALUES (3, '3', 'add parent id to task', 'SQL', 'V3__add_parent_id_to_task.sql', -1600888675, 'root', '2026-02-24 10:17:18', 127, 1);
INSERT INTO `flyway_schema_history` VALUES (4, '20260112', 'Create operation log table', 'SQL', 'V20260112__Create_operation_log_table.sql', 148124373, 'root', '2026-02-24 10:17:18', 14, 1);
INSERT INTO `flyway_schema_history` VALUES (5, '20260113', 'Add change tracking to operation log', 'SQL', 'V20260113__Add_change_tracking_to_operation_log.sql', -1910115446, 'root', '2026-02-24 10:17:18', 67, 1);
INSERT INTO `flyway_schema_history` VALUES (6, '20260114', 'Add project type', 'SQL', 'V20260114__Add_project_type.sql', -493367146, 'root', '2026-02-24 10:17:18', 168, 1);
INSERT INTO `flyway_schema_history` VALUES (7, '20260115', 'Create menu tables', 'SQL', 'V20260115__Create_menu_tables.sql', 1250250783, 'root', '2026-02-24 10:17:18', 290, 1);
INSERT INTO `flyway_schema_history` VALUES (8, '202601151', 'Add project menu to employee', 'SQL', 'V202601151__Add_project_menu_to_employee.sql', 445723970, 'root', '2026-02-24 10:17:18', 21, 1);
INSERT INTO `flyway_schema_history` VALUES (9, '202601152', 'Add task button permissions', 'SQL', 'V202601152__Add_task_button_permissions.sql', 796964266, 'root', '2026-02-24 10:17:18', 13, 1);
INSERT INTO `flyway_schema_history` VALUES (10, '202601153', 'Alter role type', 'SQL', 'V202601153__Alter_role_type.sql', 1260022699, 'root', '2026-02-24 10:17:19', 72, 1);
INSERT INTO `flyway_schema_history` VALUES (11, '202601154', 'Add permission type', 'SQL', 'V202601154__Add_permission_type.sql', 1987566623, 'root', '2026-02-24 10:17:19', 40, 1);
INSERT INTO `flyway_schema_history` VALUES (12, '202601155', 'Clean menu type comment', 'SQL', 'V202601155__Clean_menu_type_comment.sql', -154529841, 'root', '2026-02-24 10:17:19', 20, 1);

-- ----------------------------
-- Table structure for gsms_iteration
-- ----------------------------
DROP TABLE IF EXISTS `gsms_iteration`;
CREATE TABLE `gsms_iteration`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '迭代ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '迭代名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '迭代描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '迭代状态 1:未开始 2:进行中 3:已完成',
  `plan_start_date` date NULL DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` date NULL DEFAULT NULL COMMENT '计划结束日期',
  `actual_start_date` date NULL DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` date NULL DEFAULT NULL COMMENT '实际结束日期',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_iteration_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_iteration_status`(`status` ASC) USING BTREE,
  INDEX `idx_iteration_plan_start_date`(`plan_start_date` ASC) USING BTREE,
  INDEX `fk_iteration_creator`(`create_user_id` ASC) USING BTREE,
  INDEX `fk_iteration_updater`(`update_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_iteration_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_iteration_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_iteration_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '迭代表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gsms_iteration
-- ----------------------------
INSERT INTO `gsms_iteration` VALUES (1, 1, '迭代1', '', 2, '2026-02-24', '2026-03-19', NULL, NULL, 3, '2026-02-24 16:34:02', '2026-02-24 16:34:02', 1, 0);

-- ----------------------------
-- Table structure for gsms_project
-- ----------------------------
DROP TABLE IF EXISTS `gsms_project`;
CREATE TABLE `gsms_project`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目编码',
  `project_type` tinyint NULL DEFAULT 1 COMMENT '项目类型 1:日程型 2:中大型（含迭代）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项目描述',
  `manager_id` bigint NOT NULL COMMENT '项目负责人ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '项目状态 1:未开始 2:进行中 3:已挂起 4:已归档',
  `plan_start_date` date NULL DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` date NULL DEFAULT NULL COMMENT '计划结束日期',
  `actual_start_date` date NULL DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` date NULL DEFAULT NULL COMMENT '实际结束日期',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_project_manager_id`(`manager_id` ASC) USING BTREE,
  INDEX `idx_project_create_user_id`(`create_user_id` ASC) USING BTREE,
  INDEX `idx_project_status`(`status` ASC) USING BTREE,
  INDEX `idx_project_create_time`(`create_time` ASC) USING BTREE,
  INDEX `fk_project_updater`(`update_user_id` ASC) USING BTREE,
  INDEX `idx_project_type`(`project_type` ASC) USING BTREE,
  CONSTRAINT `fk_project_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_project_manager` FOREIGN KEY (`manager_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_project_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gsms_project
-- ----------------------------
INSERT INTO `gsms_project` VALUES (1, '测试项目', 'CG1', 1, '测试项目', 98, 2, '2026-02-23', '2026-04-30', NULL, NULL, 3, '2026-02-24 11:29:26', '2026-02-24 18:22:12', 3, 0);

-- ----------------------------
-- Table structure for gsms_project_member
-- ----------------------------
DROP TABLE IF EXISTS `gsms_project_member`;
CREATE TABLE `gsms_project_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_type` tinyint NOT NULL DEFAULT 1 COMMENT '角色类型 1:项目管理员 2:项目成员 3:只读访客',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_user`(`project_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_project_member_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_project_member_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_project_member_creator`(`create_user_id` ASC) USING BTREE,
  INDEX `fk_project_member_updater`(`update_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_project_member_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_project_member_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_project_member_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_project_member_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '项目成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gsms_project_member
-- ----------------------------
INSERT INTO `gsms_project_member` VALUES (1, 1, 3, 1, 3, '2026-02-24 11:29:26', '2026-02-24 11:29:26', 3, 0);
INSERT INTO `gsms_project_member` VALUES (2, 1, 98, 2, 1, '2026-02-24 18:26:04', '2026-02-24 18:26:04', 1, 0);
INSERT INTO `gsms_project_member` VALUES (3, 1, 99, 2, 1, '2026-02-24 18:26:04', '2026-02-24 18:26:04', 1, 0);
INSERT INTO `gsms_project_member` VALUES (4, 1, 100, 2, 1, '2026-02-24 18:26:04', '2026-02-24 18:26:04', 1, 0);
INSERT INTO `gsms_project_member` VALUES (5, 1, 101, 2, 1, '2026-02-24 18:26:04', '2026-02-24 18:26:04', 1, 0);
INSERT INTO `gsms_project_member` VALUES (6, 1, 102, 2, 1, '2026-02-24 18:26:04', '2026-02-24 18:26:04', 1, 0);
INSERT INTO `gsms_project_member` VALUES (7, 1, 103, 2, 1, '2026-02-24 18:26:04', '2026-02-24 18:26:04', 1, 0);

-- ----------------------------
-- Table structure for gsms_task
-- ----------------------------
DROP TABLE IF EXISTS `gsms_task`;
CREATE TABLE `gsms_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `iteration_id` bigint NULL DEFAULT NULL COMMENT '迭代ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父任务ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务描述',
  `type` tinyint NULL DEFAULT 1 COMMENT '任务类型 1:任务 2:需求 3:缺陷',
  `priority` tinyint NULL DEFAULT 2 COMMENT '优先级 1:高 2:中 3:低',
  `assignee_id` bigint NULL DEFAULT NULL COMMENT '负责人ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '任务状态 1:待处理 2:进行中 3:已完成',
  `plan_start_date` date NULL DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` date NULL DEFAULT NULL COMMENT '计划结束日期',
  `actual_start_date` date NULL DEFAULT NULL COMMENT '实际开始日期',
  `actual_end_date` date NULL DEFAULT NULL COMMENT '实际结束日期',
  `estimate_hours` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '预估工时',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_task_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_task_iteration_id`(`iteration_id` ASC) USING BTREE,
  INDEX `idx_task_assignee_id`(`assignee_id` ASC) USING BTREE,
  INDEX `idx_task_status`(`status` ASC) USING BTREE,
  INDEX `idx_task_type_priority`(`type` ASC, `priority` ASC) USING BTREE,
  INDEX `idx_task_create_time`(`create_time` ASC) USING BTREE,
  INDEX `fk_task_creator`(`create_user_id` ASC) USING BTREE,
  INDEX `fk_task_updater`(`update_user_id` ASC) USING BTREE,
  INDEX `idx_task_parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `fk_task_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_task_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_task_iteration` FOREIGN KEY (`iteration_id`) REFERENCES `gsms_iteration` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_task_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_task_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gsms_task
-- ----------------------------
INSERT INTO `gsms_task` VALUES (1, 1, 1, NULL, '需求1', '', 2, 2, 3, 1, '2026-02-24', '2026-03-18', NULL, NULL, 80.00, 3, '2026-02-24 16:34:30', '2026-02-24 16:34:30', 3, 0);
INSERT INTO `gsms_task` VALUES (2, 1, 1, NULL, '需求2', '', 2, 2, 3, 1, '2026-03-24', '2026-04-14', NULL, NULL, 40.00, 3, '2026-02-24 17:28:22', '2026-02-24 17:28:22', 3, 0);
INSERT INTO `gsms_task` VALUES (3, 1, 1, 1, '需求1任务1', '', 2, 2, 98, 2, '2026-02-25', '2026-02-27', '2026-02-24', NULL, 10.00, 1, '2026-02-24 18:26:54', '2026-02-24 18:29:42', 98, 0);

-- ----------------------------
-- Table structure for gsms_work_hour
-- ----------------------------
DROP TABLE IF EXISTS `gsms_work_hour`;
CREATE TABLE `gsms_work_hour`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工时记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `task_id` bigint NULL DEFAULT NULL COMMENT '任务ID',
  `work_date` date NOT NULL COMMENT '工作日期',
  `hours` decimal(5, 2) NOT NULL DEFAULT 0.00 COMMENT '工时数',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工作内容描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1:已保存 2:已提交 3:已确认',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL COMMENT '创建人ID',
  `update_user_id` bigint NOT NULL COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_work_date`(`work_date` ASC) USING BTREE,
  INDEX `idx_work_hour_project_id`(`project_id` ASC) USING BTREE,
  INDEX `idx_work_hour_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_workhour_status_date`(`status` ASC, `work_date` ASC) USING BTREE,
  INDEX `idx_workhour_user_date`(`user_id` ASC, `work_date` ASC) USING BTREE,
  INDEX `fk_work_hour_creator`(`create_user_id` ASC) USING BTREE,
  INDEX `fk_work_hour_updater`(`update_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_work_hour_creator` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_work_hour_project` FOREIGN KEY (`project_id`) REFERENCES `gsms_project` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_work_hour_task` FOREIGN KEY (`task_id`) REFERENCES `gsms_task` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_work_hour_updater` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_work_hour_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工时记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gsms_work_hour
-- ----------------------------
INSERT INTO `gsms_work_hour` VALUES (1, 98, 1, 3, '2026-02-24', 1.00, '111', NULL, '2026-02-24 18:29:56', '2026-02-24 18:29:56', 98, 98, 0);

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门ID',
  `level` int NULL DEFAULT 1 COMMENT '层级',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_department_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_department_level_sort`(`level` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (1, '总公司', 0, 1, 0, '系统根部门', '2026-02-24 10:17:17', '2026-02-24 10:17:17', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径（菜单项才有，目录为空）',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径（前端路由组件）',
  `icon` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单ID，0表示根菜单',
  `sort` int NULL DEFAULT 0 COMMENT '排序号（同级菜单排序）',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '菜单类型 1:目录 2:菜单',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1:启用 2:禁用',
  `visible` tinyint NULL DEFAULT 1 COMMENT '是否可见 1:可见 2:隐藏',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_menu_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_menu_type`(`type` ASC) USING BTREE,
  INDEX `idx_menu_status`(`status` ASC) USING BTREE,
  INDEX `idx_menu_sort`(`sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '首页', '/dashboard', 'Dashboard', 'Odometer', 0, 1, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (2, '项目管理', '/projects', 'ProjectList', 'FolderOpened', 0, 2, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (3, '任务中心', '/tasks', 'TaskList', 'List', 0, 3, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (4, '工时管理', NULL, NULL, 'Clock', 0, 4, 1, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (5, '统计分析', NULL, NULL, 'DataAnalysis', 0, 5, 1, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (6, '系统管理', NULL, NULL, 'Operation', 0, 6, 1, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (7, '工时日历', '/workhours/calendar', 'WorkHourCalendar', NULL, 4, 1, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (8, '工时列表', '/workhours/list', 'WorkHourList', NULL, 4, 2, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (9, '项目工时统计', '/statistics/project', 'ProjectStatistics', NULL, 5, 1, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (10, '用户工时统计', '/statistics/user', 'UserStatistics', NULL, 5, 2, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (11, '工时趋势分析', '/statistics/trend', 'TrendStatistics', NULL, 5, 3, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (12, '用户管理', '/system/users', 'UserList', NULL, 6, 1, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (13, '部门管理', '/system/departments', 'DepartmentList', NULL, 6, 2, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (14, '角色管理', '/system/roles', 'RoleList', NULL, 6, 3, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (15, '权限管理', '/system/permissions', 'PermissionList', NULL, 6, 4, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (16, '菜单管理', '/system/menus', 'MenuList', NULL, 6, 5, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);
INSERT INTO `sys_menu` VALUES (17, '操作日志', '/system/operation-logs', 'OperationLogList', NULL, 6, 6, 2, 1, 1, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_menu_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_permission`;
CREATE TABLE `sys_menu_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_menu_permission`(`menu_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_menu_permission_menu`(`menu_id` ASC) USING BTREE,
  INDEX `idx_menu_permission_permission`(`permission_id` ASC) USING BTREE,
  CONSTRAINT `fk_menu_permission_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_menu_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu_permission
-- ----------------------------
INSERT INTO `sys_menu_permission` VALUES (1, 1, 4, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (2, 2, 5, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (3, 3, 6, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (4, 4, 7, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (5, 7, 7, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (6, 8, 7, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (7, 5, 8, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (8, 9, 8, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (9, 10, 8, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (10, 11, 8, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (11, 6, 9, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (12, 12, 9, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (13, 13, 9, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (14, 14, 9, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (15, 15, 9, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (16, 16, 9, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);
INSERT INTO `sys_menu_permission` VALUES (17, 17, 9, '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 0);

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '操作人ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作人用户名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型（CREATE/UPDATE/DELETE/ASSIGN等）',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称（USER/ROLE/PROJECT等）',
  `business_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型（ROLE_PERMISSION/PROJECT_MEMBER等）',
  `business_id` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务ID（JSON格式或关键ID）',
  `old_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '旧值（JSON格式）',
  `new_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '新值（JSON格式）',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法（GET/POST/PUT/DELETE）',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `uri` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URI',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `execute_time` int NULL DEFAULT NULL COMMENT '执行时长',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1:成功 0:失败',
  `error_msg` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_operation`(`operation` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_module_business`(`module` ASC, `business_type` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_business`(`business_type` ASC, `business_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------
INSERT INTO `sys_operation_log` VALUES (1, 1, 'admin', '1', '1', 'USER', '98', NULL, '{\"id\":98,\"username\":\"user1\",\"password\":\"$2a$12$ZRavm03V97yzZ6LwXa7Sne8iaRlVLSuo5xXqbN2MdTOPBsDs/pztm\",\"nickname\":\"用户1\",\"email\":\"user1@example.com\",\"phone\":\"13800000001\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:52\",\"updateTime\":\"2026-02-24 18:08:52\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:52');
INSERT INTO `sys_operation_log` VALUES (2, 1, 'admin', '1', '1', 'USER', '99', NULL, '{\"id\":99,\"username\":\"user2\",\"password\":\"$2a$12$09oUhnuqA.hwqiHmJDS/zuydahP6fTvlIj.7uw8ncyzQFM7IumH42\",\"nickname\":\"用户2\",\"email\":\"user2@example.com\",\"phone\":\"13800000002\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:52\",\"updateTime\":\"2026-02-24 18:08:52\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:52');
INSERT INTO `sys_operation_log` VALUES (3, 1, 'admin', '1', '1', 'USER', '100', NULL, '{\"id\":100,\"username\":\"user3\",\"password\":\"$2a$12$pgG.iRF2MShCdnqVKsLHTu100LNb1Zz/sqE1z3ecWekozR8dm4AiO\",\"nickname\":\"用户3\",\"email\":\"user3@example.com\",\"phone\":\"13800000003\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:53\",\"updateTime\":\"2026-02-24 18:08:53\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:53');
INSERT INTO `sys_operation_log` VALUES (4, 1, 'admin', '1', '1', 'USER', '101', NULL, '{\"id\":101,\"username\":\"user4\",\"password\":\"$2a$12$BE8U2rDA9a3p04khWe8Q6.fh55bZrkthMklKTXUjWmsFVuC25No/W\",\"nickname\":\"用户4\",\"email\":\"user4@example.com\",\"phone\":\"13800000004\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:53\",\"updateTime\":\"2026-02-24 18:08:53\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:53');
INSERT INTO `sys_operation_log` VALUES (5, 1, 'admin', '1', '1', 'USER', '102', NULL, '{\"id\":102,\"username\":\"user5\",\"password\":\"$2a$12$RAcLqsHO8mlG4O.BNO.PJ.KDPspRFhkD8n/qwHmSGjC95fTtkBBlu\",\"nickname\":\"用户5\",\"email\":\"user5@example.com\",\"phone\":\"13800000005\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:53\",\"updateTime\":\"2026-02-24 18:08:53\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:53');
INSERT INTO `sys_operation_log` VALUES (6, 1, 'admin', '1', '1', 'USER', '103', NULL, '{\"id\":103,\"username\":\"user6\",\"password\":\"$2a$12$LpnVETkWBQNROnhIQIE8SO75PKkO58OR5emgx48kXQqM2egEdT0C2\",\"nickname\":\"用户6\",\"email\":\"user6@example.com\",\"phone\":\"13800000006\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:54\",\"updateTime\":\"2026-02-24 18:08:54\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:54');
INSERT INTO `sys_operation_log` VALUES (7, 1, 'admin', '1', '1', 'USER', '104', NULL, '{\"id\":104,\"username\":\"user7\",\"password\":\"$2a$12$jcuRC2cGRDwDwIrCgu4FZO6dpki1NkirvvoQZ1/I2aOxOUG1BWksW\",\"nickname\":\"用户7\",\"email\":\"user7@example.com\",\"phone\":\"13800000007\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:54\",\"updateTime\":\"2026-02-24 18:08:54\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:54');
INSERT INTO `sys_operation_log` VALUES (8, 1, 'admin', '1', '1', 'USER', '105', NULL, '{\"id\":105,\"username\":\"user8\",\"password\":\"$2a$12$syfxoGQcYqauOKorW1E2ouFeuC2eDFOBSx9bf6x3aImqaXXtftckS\",\"nickname\":\"用户8\",\"email\":\"user8@example.com\",\"phone\":\"13800000008\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:55\",\"updateTime\":\"2026-02-24 18:08:55\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:55');
INSERT INTO `sys_operation_log` VALUES (9, 1, 'admin', '1', '1', 'USER', '106', NULL, '{\"id\":106,\"username\":\"user9\",\"password\":\"$2a$12$c9JGMN95ohNC5ts97SNwf./Fidp7HlTWeUY6Fuvx0aW9SacjNPwbu\",\"nickname\":\"用户9\",\"email\":\"user9@example.com\",\"phone\":\"13800000009\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:55\",\"updateTime\":\"2026-02-24 18:08:55\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:55');
INSERT INTO `sys_operation_log` VALUES (10, 1, 'admin', '1', '1', 'USER', '107', NULL, '{\"id\":107,\"username\":\"user10\",\"password\":\"$2a$12$RuUL5.2JHoVc/NJEUOI3BebtMtliZ7lqeTejiss7sW2kWr0WwUIBa\",\"nickname\":\"用户10\",\"email\":\"user10@example.com\",\"phone\":\"13800000010\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:55\",\"updateTime\":\"2026-02-24 18:08:55\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:55');
INSERT INTO `sys_operation_log` VALUES (11, 1, 'admin', '1', '1', 'USER', '108', NULL, '{\"id\":108,\"username\":\"user11\",\"password\":\"$2a$12$2dbw2ImFR4omspVv5oqDpOhv8kcq9dBVNoPJGJBy85E939WNZh3Hm\",\"nickname\":\"用户11\",\"email\":\"user11@example.com\",\"phone\":\"13800000011\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:56\",\"updateTime\":\"2026-02-24 18:08:56\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:56');
INSERT INTO `sys_operation_log` VALUES (12, 1, 'admin', '1', '1', 'USER', '109', NULL, '{\"id\":109,\"username\":\"user12\",\"password\":\"$2a$12$fFSC0CknU9orupkLBGbmW.x1/pZyNu/9Lqpv8eS6ly252Di9XCKLq\",\"nickname\":\"用户12\",\"email\":\"user12@example.com\",\"phone\":\"13800000012\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:56\",\"updateTime\":\"2026-02-24 18:08:56\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:56');
INSERT INTO `sys_operation_log` VALUES (13, 1, 'admin', '1', '1', 'USER', '110', NULL, '{\"id\":110,\"username\":\"user13\",\"password\":\"$2a$12$K4JeNU0IVcFC2uWhmNoxZOoubvjqSLL9jUx/8CHhBMwkIAawtjK6K\",\"nickname\":\"用户13\",\"email\":\"user13@example.com\",\"phone\":\"13800000013\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:57\",\"updateTime\":\"2026-02-24 18:08:57\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:57');
INSERT INTO `sys_operation_log` VALUES (14, 1, 'admin', '1', '1', 'USER', '111', NULL, '{\"id\":111,\"username\":\"user14\",\"password\":\"$2a$12$fQQsKB554LQQkM/tfPYnuOqJbSQ.5TgvhT3hePE/VVsQUHulbB65m\",\"nickname\":\"用户14\",\"email\":\"user14@example.com\",\"phone\":\"13800000014\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:57\",\"updateTime\":\"2026-02-24 18:08:57\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:57');
INSERT INTO `sys_operation_log` VALUES (15, 1, 'admin', '1', '1', 'USER', '112', NULL, '{\"id\":112,\"username\":\"user15\",\"password\":\"$2a$12$SqIQsn3GnabcJPCaPN3GQuWnBu40QN9H1WUsvLXuTQio8ya/D5ho6\",\"nickname\":\"用户15\",\"email\":\"user15@example.com\",\"phone\":\"13800000015\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:57\",\"updateTime\":\"2026-02-24 18:08:57\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:57');
INSERT INTO `sys_operation_log` VALUES (16, 1, 'admin', '1', '1', 'USER', '113', NULL, '{\"id\":113,\"username\":\"user16\",\"password\":\"$2a$12$BhBFe3kYs9J/MLaHjIUjv.HDjbGCuEIObG351XZsgbrarKs/gcgGW\",\"nickname\":\"用户16\",\"email\":\"user16@example.com\",\"phone\":\"13800000016\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:58\",\"updateTime\":\"2026-02-24 18:08:58\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:58');
INSERT INTO `sys_operation_log` VALUES (17, 1, 'admin', '1', '1', 'USER', '114', NULL, '{\"id\":114,\"username\":\"user17\",\"password\":\"$2a$12$M0KdC8CU0a6aoCGPUkp4Lu69sWg7bwv2m3NCDYJsuwnSz32CvvP2u\",\"nickname\":\"用户17\",\"email\":\"user17@example.com\",\"phone\":\"13800000017\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:58\",\"updateTime\":\"2026-02-24 18:08:58\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:58');
INSERT INTO `sys_operation_log` VALUES (18, 1, 'admin', '1', '1', 'USER', '115', NULL, '{\"id\":115,\"username\":\"user18\",\"password\":\"$2a$12$u8B5MglnFBj.IT5T3ApwyeLVz.4Fglv2nJjMo5EJessSEXi9./vD.\",\"nickname\":\"用户18\",\"email\":\"user18@example.com\",\"phone\":\"13800000018\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:59\",\"updateTime\":\"2026-02-24 18:08:59\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:59');
INSERT INTO `sys_operation_log` VALUES (19, 1, 'admin', '1', '1', 'USER', '116', NULL, '{\"id\":116,\"username\":\"user19\",\"password\":\"$2a$12$cbQO25Mm6r8G0Jy8BGKiFeYU8b7xoI/ZBEsdJ8Wis8tOPU7my7tQO\",\"nickname\":\"用户19\",\"email\":\"user19@example.com\",\"phone\":\"13800000019\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:59\",\"updateTime\":\"2026-02-24 18:08:59\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:59');
INSERT INTO `sys_operation_log` VALUES (20, 1, 'admin', '1', '1', 'USER', '117', NULL, '{\"id\":117,\"username\":\"user20\",\"password\":\"$2a$12$qTXrTl3IXOrur0aatedQXuq.on6vdHQ5bNbZ4UZOTidCioSdufgrG\",\"nickname\":\"用户20\",\"email\":\"user20@example.com\",\"phone\":\"13800000020\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:08:59\",\"updateTime\":\"2026-02-24 18:08:59\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:08:59');
INSERT INTO `sys_operation_log` VALUES (21, 1, 'admin', '1', '1', 'USER', '118', NULL, '{\"id\":118,\"username\":\"user21\",\"password\":\"$2a$12$GoVikBpPQlsLDe5JrdAQluq4w1wHjV8j37dMDZT1A8Jql3ra9Ohq6\",\"nickname\":\"用户21\",\"email\":\"user21@example.com\",\"phone\":\"13800000021\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:00\",\"updateTime\":\"2026-02-24 18:09:00\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:00');
INSERT INTO `sys_operation_log` VALUES (22, 1, 'admin', '1', '1', 'USER', '119', NULL, '{\"id\":119,\"username\":\"user22\",\"password\":\"$2a$12$8IE.HO75M8lsxWC155vDOeJ4Sm4MWZpt9vZUwEQEaIbOXtr2wHdAS\",\"nickname\":\"用户22\",\"email\":\"user22@example.com\",\"phone\":\"13800000022\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:00\",\"updateTime\":\"2026-02-24 18:09:00\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:00');
INSERT INTO `sys_operation_log` VALUES (23, 1, 'admin', '1', '1', 'USER', '120', NULL, '{\"id\":120,\"username\":\"user23\",\"password\":\"$2a$12$LOu0.cPJ8TdDMaFwrx4PIu.aHEP7y0l6CO3HEmhqniHByaMHHdANa\",\"nickname\":\"用户23\",\"email\":\"user23@example.com\",\"phone\":\"13800000023\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:00\",\"updateTime\":\"2026-02-24 18:09:00\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:00');
INSERT INTO `sys_operation_log` VALUES (24, 1, 'admin', '1', '1', 'USER', '121', NULL, '{\"id\":121,\"username\":\"user24\",\"password\":\"$2a$12$illyl9jNOOxOLDIeLIF85eJLLHqsyjLmSyupnbre7dodgSovL1WpK\",\"nickname\":\"用户24\",\"email\":\"user24@example.com\",\"phone\":\"13800000024\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:01\",\"updateTime\":\"2026-02-24 18:09:01\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:01');
INSERT INTO `sys_operation_log` VALUES (25, 1, 'admin', '1', '1', 'USER', '122', NULL, '{\"id\":122,\"username\":\"user25\",\"password\":\"$2a$12$mZz5ts9yPILMIKVhR3dN6uCJlkopNYbUR15/m/2xfllMBHt1sj9wG\",\"nickname\":\"用户25\",\"email\":\"user25@example.com\",\"phone\":\"13800000025\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:01\",\"updateTime\":\"2026-02-24 18:09:01\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:01');
INSERT INTO `sys_operation_log` VALUES (26, 1, 'admin', '1', '1', 'USER', '123', NULL, '{\"id\":123,\"username\":\"user26\",\"password\":\"$2a$12$2i1fmI.1XiLwy/.HoQbpq.1J7KyI5xP1TCPR/t0OQ/c.BpTJo9Ugq\",\"nickname\":\"用户26\",\"email\":\"user26@example.com\",\"phone\":\"13800000026\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:01\",\"updateTime\":\"2026-02-24 18:09:01\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:01');
INSERT INTO `sys_operation_log` VALUES (27, 1, 'admin', '1', '1', 'USER', '124', NULL, '{\"id\":124,\"username\":\"user27\",\"password\":\"$2a$12$qLUgGyGT6UjT.gw18zlvfOezwXxaHh45NHE6aijb/SKFp.SMexLJe\",\"nickname\":\"用户27\",\"email\":\"user27@example.com\",\"phone\":\"13800000027\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:01\",\"updateTime\":\"2026-02-24 18:09:01\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:01');
INSERT INTO `sys_operation_log` VALUES (28, 1, 'admin', '1', '1', 'USER', '125', NULL, '{\"id\":125,\"username\":\"user28\",\"password\":\"$2a$12$FEPhXxfGqN3svDpt/ST/H.zQbYyZXNWOlQJLuQmFa21lTwC.1PPfS\",\"nickname\":\"用户28\",\"email\":\"user28@example.com\",\"phone\":\"13800000028\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:02\",\"updateTime\":\"2026-02-24 18:09:02\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:02');
INSERT INTO `sys_operation_log` VALUES (29, 1, 'admin', '1', '1', 'USER', '126', NULL, '{\"id\":126,\"username\":\"user29\",\"password\":\"$2a$12$FaG7QZXetpsUwNWKHjCPee2NAhWWsxXLQysKGxPOiStTsnsBXW58m\",\"nickname\":\"用户29\",\"email\":\"user29@example.com\",\"phone\":\"13800000029\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:02\",\"updateTime\":\"2026-02-24 18:09:02\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:02');
INSERT INTO `sys_operation_log` VALUES (30, 1, 'admin', '1', '1', 'USER', '127', NULL, '{\"id\":127,\"username\":\"user30\",\"password\":\"$2a$12$tH1Syf42WMP90TAp/cPdZOCf0BJRG7lV9ILAotPb.zFNlqOgaB1VC\",\"nickname\":\"用户30\",\"email\":\"user30@example.com\",\"phone\":\"13800000030\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:02\",\"updateTime\":\"2026-02-24 18:09:02\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:02');
INSERT INTO `sys_operation_log` VALUES (31, 1, 'admin', '1', '1', 'USER', '128', NULL, '{\"id\":128,\"username\":\"user31\",\"password\":\"$2a$12$qOm.fzDsYNedvYd0RqhUe..kwmdmysVEURDC1qZCq8WGYZ61mfo0u\",\"nickname\":\"用户31\",\"email\":\"user31@example.com\",\"phone\":\"13800000031\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:02\",\"updateTime\":\"2026-02-24 18:09:02\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:02');
INSERT INTO `sys_operation_log` VALUES (32, 1, 'admin', '1', '1', 'USER', '129', NULL, '{\"id\":129,\"username\":\"user32\",\"password\":\"$2a$12$Fy.aWdLG/LUFBW.OLnmjLu6laEsZ4si.p3QonVlEiiRLk2G1BHSs2\",\"nickname\":\"用户32\",\"email\":\"user32@example.com\",\"phone\":\"13800000032\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:03\",\"updateTime\":\"2026-02-24 18:09:03\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:03');
INSERT INTO `sys_operation_log` VALUES (33, 1, 'admin', '1', '1', 'USER', '130', NULL, '{\"id\":130,\"username\":\"user33\",\"password\":\"$2a$12$KKU3AcjgIqry7zdepiYVqeldjSojnPve6dzqoYg8by6J7Ek73zaWm\",\"nickname\":\"用户33\",\"email\":\"user33@example.com\",\"phone\":\"13800000033\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:03\",\"updateTime\":\"2026-02-24 18:09:03\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:03');
INSERT INTO `sys_operation_log` VALUES (34, 1, 'admin', '1', '1', 'USER', '131', NULL, '{\"id\":131,\"username\":\"user34\",\"password\":\"$2a$12$plv4wcak2bgKeCEedmtSyu4Qg65QWf0gP5PijZv4/.62ApRQVWZRa\",\"nickname\":\"用户34\",\"email\":\"user34@example.com\",\"phone\":\"13800000034\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:03\",\"updateTime\":\"2026-02-24 18:09:03\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:03');
INSERT INTO `sys_operation_log` VALUES (35, 1, 'admin', '1', '1', 'USER', '132', NULL, '{\"id\":132,\"username\":\"user35\",\"password\":\"$2a$12$nVDqLOu39bx6XQ14ubnx4.g/6jc.S3LzlI7tV29nSgauXhP9jTHn6\",\"nickname\":\"用户35\",\"email\":\"user35@example.com\",\"phone\":\"13800000035\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:04\",\"updateTime\":\"2026-02-24 18:09:04\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:04');
INSERT INTO `sys_operation_log` VALUES (36, 1, 'admin', '1', '1', 'USER', '133', NULL, '{\"id\":133,\"username\":\"user36\",\"password\":\"$2a$12$VBoTbjkTCy3.TokMB2.vXefFsDpIsk9bdiexJUjT0cSpbTWaAOvCK\",\"nickname\":\"用户36\",\"email\":\"user36@example.com\",\"phone\":\"13800000036\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:04\",\"updateTime\":\"2026-02-24 18:09:04\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:04');
INSERT INTO `sys_operation_log` VALUES (37, 1, 'admin', '1', '1', 'USER', '134', NULL, '{\"id\":134,\"username\":\"user37\",\"password\":\"$2a$12$0bwgIeU86vtd24nCXmlcpO5yUZDZ2lbdIf5/kWT07Lt8z2QwWm8oG\",\"nickname\":\"用户37\",\"email\":\"user37@example.com\",\"phone\":\"13800000037\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:04\",\"updateTime\":\"2026-02-24 18:09:04\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:04');
INSERT INTO `sys_operation_log` VALUES (38, 1, 'admin', '1', '1', 'USER', '135', NULL, '{\"id\":135,\"username\":\"user38\",\"password\":\"$2a$12$ucy/YvkMckTqndtFZiolFumyLQKW2P9Uj1Ap36KGQcPBQT1vN7EQW\",\"nickname\":\"用户38\",\"email\":\"user38@example.com\",\"phone\":\"13800000038\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:04\",\"updateTime\":\"2026-02-24 18:09:04\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:04');
INSERT INTO `sys_operation_log` VALUES (39, 1, 'admin', '1', '1', 'USER', '136', NULL, '{\"id\":136,\"username\":\"user39\",\"password\":\"$2a$12$N7O2qaoai4BI23oGruc.IO6z8ZnplJXvzPdmQgA5ULmaQG1SvZp42\",\"nickname\":\"用户39\",\"email\":\"user39@example.com\",\"phone\":\"13800000039\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:05\",\"updateTime\":\"2026-02-24 18:09:05\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:05');
INSERT INTO `sys_operation_log` VALUES (40, 1, 'admin', '1', '1', 'USER', '137', NULL, '{\"id\":137,\"username\":\"user40\",\"password\":\"$2a$12$7P3OAi8ApiRKHWBYpS.QUOlhhGEOor1vKyE527jiQpGJ6JzQVQZJC\",\"nickname\":\"用户40\",\"email\":\"user40@example.com\",\"phone\":\"13800000040\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:05\",\"updateTime\":\"2026-02-24 18:09:05\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:05');
INSERT INTO `sys_operation_log` VALUES (41, 1, 'admin', '1', '1', 'USER', '138', NULL, '{\"id\":138,\"username\":\"user41\",\"password\":\"$2a$12$i7TD6KUIBwXY4ml.i3kodehYhnB7uHfZT3WizbuD4XEk1xR5PaA6i\",\"nickname\":\"用户41\",\"email\":\"user41@example.com\",\"phone\":\"13800000041\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:05\",\"updateTime\":\"2026-02-24 18:09:05\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:05');
INSERT INTO `sys_operation_log` VALUES (42, 1, 'admin', '1', '1', 'USER', '139', NULL, '{\"id\":139,\"username\":\"user42\",\"password\":\"$2a$12$YWUhsTE3h8ZZqTp4hZqk5.UVkdXyFX0uP8G5ma10MyYV7cobzpov.\",\"nickname\":\"用户42\",\"email\":\"user42@example.com\",\"phone\":\"13800000042\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:05\",\"updateTime\":\"2026-02-24 18:09:05\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:05');
INSERT INTO `sys_operation_log` VALUES (43, 1, 'admin', '1', '1', 'USER', '140', NULL, '{\"id\":140,\"username\":\"user43\",\"password\":\"$2a$12$SuSGmKbcnU5l2c7Zyq/lqOAVB3FZArE5XYnvTllcX3cw9UfXnOYgK\",\"nickname\":\"用户43\",\"email\":\"user43@example.com\",\"phone\":\"13800000043\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:06\",\"updateTime\":\"2026-02-24 18:09:06\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:06');
INSERT INTO `sys_operation_log` VALUES (44, 1, 'admin', '1', '1', 'USER', '141', NULL, '{\"id\":141,\"username\":\"user44\",\"password\":\"$2a$12$8a4xfOlQ2RQmNEKm9gXhH.x/QPXMAEE0vWK31HLHUIhXFwlM6bgM6\",\"nickname\":\"用户44\",\"email\":\"user44@example.com\",\"phone\":\"13800000044\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:06\",\"updateTime\":\"2026-02-24 18:09:06\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:06');
INSERT INTO `sys_operation_log` VALUES (45, 1, 'admin', '1', '1', 'USER', '142', NULL, '{\"id\":142,\"username\":\"user45\",\"password\":\"$2a$12$6zQjyiCF2BCeNO2LQR7DquZa8a/SYJlBMWGh5ZYspB0CAm9WZ0DwO\",\"nickname\":\"用户45\",\"email\":\"user45@example.com\",\"phone\":\"13800000045\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:06\",\"updateTime\":\"2026-02-24 18:09:06\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:06');
INSERT INTO `sys_operation_log` VALUES (46, 1, 'admin', '1', '1', 'USER', '143', NULL, '{\"id\":143,\"username\":\"user46\",\"password\":\"$2a$12$qXbSXxs0KgReto3Cr9dA2ezYOVppl5oh/3dYZgvfhXybH2uHM1twC\",\"nickname\":\"用户46\",\"email\":\"user46@example.com\",\"phone\":\"13800000046\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:06\",\"updateTime\":\"2026-02-24 18:09:06\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:06');
INSERT INTO `sys_operation_log` VALUES (47, 1, 'admin', '1', '1', 'USER', '144', NULL, '{\"id\":144,\"username\":\"user47\",\"password\":\"$2a$12$9wer/KNtGdVXKp2R1LG4b.8jReezqr5oQuyAkt5zW9yh7D/w6e/76\",\"nickname\":\"用户47\",\"email\":\"user47@example.com\",\"phone\":\"13800000047\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:07\",\"updateTime\":\"2026-02-24 18:09:07\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:07');
INSERT INTO `sys_operation_log` VALUES (48, 1, 'admin', '1', '1', 'USER', '145', NULL, '{\"id\":145,\"username\":\"user48\",\"password\":\"$2a$12$0FVWvueHTDHDlX60yu920uDVYFt9edCvAbYwMC9kof9BW6PsXQuu6\",\"nickname\":\"用户48\",\"email\":\"user48@example.com\",\"phone\":\"13800000048\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:07\",\"updateTime\":\"2026-02-24 18:09:07\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:07');
INSERT INTO `sys_operation_log` VALUES (49, 1, 'admin', '1', '1', 'USER', '146', NULL, '{\"id\":146,\"username\":\"user49\",\"password\":\"$2a$12$koYzDl/w7sQDpOP.SnvY7um0ZIxMfyDx011TRJAGiHVXXnJ202ESa\",\"nickname\":\"用户49\",\"email\":\"user49@example.com\",\"phone\":\"13800000049\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:07\",\"updateTime\":\"2026-02-24 18:09:07\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:07');
INSERT INTO `sys_operation_log` VALUES (50, 1, 'admin', '1', '1', 'USER', '147', NULL, '{\"id\":147,\"username\":\"user50\",\"password\":\"$2a$12$cxE7.fk2pwf3mrDyTzMT/.Owy6rP7bENSq3sYackt7pHBG5cjGWCG\",\"nickname\":\"用户50\",\"email\":\"user50@example.com\",\"phone\":\"13800000050\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:07\",\"updateTime\":\"2026-02-24 18:09:07\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:08');
INSERT INTO `sys_operation_log` VALUES (51, 1, 'admin', '1', '1', 'USER', '148', NULL, '{\"id\":148,\"username\":\"user51\",\"password\":\"$2a$12$mw/jd1YCp1nseAXJvJIA3ubb3yTH7HCPS/OwCCjwSYA3kehvY4Tj2\",\"nickname\":\"用户51\",\"email\":\"user51@example.com\",\"phone\":\"13800000051\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:08\",\"updateTime\":\"2026-02-24 18:09:08\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:08');
INSERT INTO `sys_operation_log` VALUES (52, 1, 'admin', '1', '1', 'USER', '149', NULL, '{\"id\":149,\"username\":\"user52\",\"password\":\"$2a$12$U2FSBOJsBk4Jv4UI4e9cJ.4Uy6RX2p/UNVP0rqvFyHjQTU5RpK4vW\",\"nickname\":\"用户52\",\"email\":\"user52@example.com\",\"phone\":\"13800000052\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:08\",\"updateTime\":\"2026-02-24 18:09:08\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:08');
INSERT INTO `sys_operation_log` VALUES (53, 1, 'admin', '1', '1', 'USER', '150', NULL, '{\"id\":150,\"username\":\"user53\",\"password\":\"$2a$12$0MZvgR7uCBbqslqlYMkqNOLNNSqgYPT1O.G/bf5ZwShAes9rPc.am\",\"nickname\":\"用户53\",\"email\":\"user53@example.com\",\"phone\":\"13800000053\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:08\",\"updateTime\":\"2026-02-24 18:09:08\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:08');
INSERT INTO `sys_operation_log` VALUES (54, 1, 'admin', '1', '1', 'USER', '151', NULL, '{\"id\":151,\"username\":\"user54\",\"password\":\"$2a$12$3YB5rTDfFSPGSXpwy1qjKeYK4zHcDQn6FYV6d/pKP68ZqcUqN93Tm\",\"nickname\":\"用户54\",\"email\":\"user54@example.com\",\"phone\":\"13800000054\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:09\",\"updateTime\":\"2026-02-24 18:09:09\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:09');
INSERT INTO `sys_operation_log` VALUES (55, 1, 'admin', '1', '1', 'USER', '152', NULL, '{\"id\":152,\"username\":\"user55\",\"password\":\"$2a$12$kc6Rr67tdPwkLG.4/4.dh.fs04cB8UCwama7udqI91srQMs6TgyXy\",\"nickname\":\"用户55\",\"email\":\"user55@example.com\",\"phone\":\"13800000055\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:09\",\"updateTime\":\"2026-02-24 18:09:09\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:09');
INSERT INTO `sys_operation_log` VALUES (56, 1, 'admin', '1', '1', 'USER', '153', NULL, '{\"id\":153,\"username\":\"user56\",\"password\":\"$2a$12$dm2/Dg2kWH9oRGfSrPCBYePCVFK3mobk31Z3sm0F03e886.RHz9fO\",\"nickname\":\"用户56\",\"email\":\"user56@example.com\",\"phone\":\"13800000056\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:09\",\"updateTime\":\"2026-02-24 18:09:09\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:09');
INSERT INTO `sys_operation_log` VALUES (57, 1, 'admin', '1', '1', 'USER', '154', NULL, '{\"id\":154,\"username\":\"user57\",\"password\":\"$2a$12$zRVkNYTXt2RSA7l1NwwIJOuIkkmWmmphL1o50SWowrEIxB7e/hohS\",\"nickname\":\"用户57\",\"email\":\"user57@example.com\",\"phone\":\"13800000057\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:09\",\"updateTime\":\"2026-02-24 18:09:09\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:09');
INSERT INTO `sys_operation_log` VALUES (58, 1, 'admin', '1', '1', 'USER', '155', NULL, '{\"id\":155,\"username\":\"user58\",\"password\":\"$2a$12$3Rwi8Vn37xjzfhGPEU/BPeOw8jqHNkv6IKKVDtayLdrBPuCnTwJOO\",\"nickname\":\"用户58\",\"email\":\"user58@example.com\",\"phone\":\"13800000058\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:10\",\"updateTime\":\"2026-02-24 18:09:10\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:10');
INSERT INTO `sys_operation_log` VALUES (59, 1, 'admin', '1', '1', 'USER', '156', NULL, '{\"id\":156,\"username\":\"user59\",\"password\":\"$2a$12$8EIeBuahb8WEAs/OgLoen.PHD2pVSjjVBTETSQW8n/IimCGRUzDyu\",\"nickname\":\"用户59\",\"email\":\"user59@example.com\",\"phone\":\"13800000059\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:10\",\"updateTime\":\"2026-02-24 18:09:10\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:10');
INSERT INTO `sys_operation_log` VALUES (60, 1, 'admin', '1', '1', 'USER', '157', NULL, '{\"id\":157,\"username\":\"user60\",\"password\":\"$2a$12$b6SpD00NP8/rTWbcvN7bs.587blJN.JDv4Vv4Rv64nTEaHkcXK98G\",\"nickname\":\"用户60\",\"email\":\"user60@example.com\",\"phone\":\"13800000060\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:10\",\"updateTime\":\"2026-02-24 18:09:10\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:10');
INSERT INTO `sys_operation_log` VALUES (61, 1, 'admin', '1', '1', 'USER', '158', NULL, '{\"id\":158,\"username\":\"user61\",\"password\":\"$2a$12$GkbiE15F6dMouP1zxRrWVOFrGhukSue97ULxGuL8oqkwo16S/KVqm\",\"nickname\":\"用户61\",\"email\":\"user61@example.com\",\"phone\":\"13800000061\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:10\",\"updateTime\":\"2026-02-24 18:09:10\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:10');
INSERT INTO `sys_operation_log` VALUES (62, 1, 'admin', '1', '1', 'USER', '159', NULL, '{\"id\":159,\"username\":\"user62\",\"password\":\"$2a$12$czEyeY0qK8YoJAnyz1Ea9OXAmKAEAQW2imu2Cjg0uHHpPGm17.QLC\",\"nickname\":\"用户62\",\"email\":\"user62@example.com\",\"phone\":\"13800000062\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:11\",\"updateTime\":\"2026-02-24 18:09:11\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:11');
INSERT INTO `sys_operation_log` VALUES (63, 1, 'admin', '1', '1', 'USER', '160', NULL, '{\"id\":160,\"username\":\"user63\",\"password\":\"$2a$12$mwKaPsT2Y/S2hF0VAUYTC.m/WfEnBFxdVFL7BAWAk2WmCHiWvpIAa\",\"nickname\":\"用户63\",\"email\":\"user63@example.com\",\"phone\":\"13800000063\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:11\",\"updateTime\":\"2026-02-24 18:09:11\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:11');
INSERT INTO `sys_operation_log` VALUES (64, 1, 'admin', '1', '1', 'USER', '161', NULL, '{\"id\":161,\"username\":\"user64\",\"password\":\"$2a$12$mb6TR5gs.89h6nLFLIbvEuNkiYT5jWnJGz6tojcFHMczXXgQDwLX.\",\"nickname\":\"用户64\",\"email\":\"user64@example.com\",\"phone\":\"13800000064\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:11\",\"updateTime\":\"2026-02-24 18:09:11\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:11');
INSERT INTO `sys_operation_log` VALUES (65, 1, 'admin', '1', '1', 'USER', '162', NULL, '{\"id\":162,\"username\":\"user65\",\"password\":\"$2a$12$W0bEfxPx/bndMxLN.FCkE.//342lL3XVakD/9NWiuNHQFWXKk/7Fy\",\"nickname\":\"用户65\",\"email\":\"user65@example.com\",\"phone\":\"13800000065\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:11\",\"updateTime\":\"2026-02-24 18:09:11\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:11');
INSERT INTO `sys_operation_log` VALUES (66, 1, 'admin', '1', '1', 'USER', '163', NULL, '{\"id\":163,\"username\":\"user66\",\"password\":\"$2a$12$TFsbqdY.5OeRAsIJaLlpF.BJYg6j7S6RFGA2GLRFiKqf3ywSYXggO\",\"nickname\":\"用户66\",\"email\":\"user66@example.com\",\"phone\":\"13800000066\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:12\",\"updateTime\":\"2026-02-24 18:09:12\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:12');
INSERT INTO `sys_operation_log` VALUES (67, 1, 'admin', '1', '1', 'USER', '164', NULL, '{\"id\":164,\"username\":\"user67\",\"password\":\"$2a$12$qCzAHHcQWKt.Vkuybpbp/eswAfAhBLf9Gev.R8eZabxgLMxCBTYmG\",\"nickname\":\"用户67\",\"email\":\"user67@example.com\",\"phone\":\"13800000067\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:12\",\"updateTime\":\"2026-02-24 18:09:12\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:12');
INSERT INTO `sys_operation_log` VALUES (68, 1, 'admin', '1', '1', 'USER', '165', NULL, '{\"id\":165,\"username\":\"user68\",\"password\":\"$2a$12$vWL66766eRtVqXBnbP5fcec44UlhUmBIj0smv3tnSfhlHPg3GJeiu\",\"nickname\":\"用户68\",\"email\":\"user68@example.com\",\"phone\":\"13800000068\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:12\",\"updateTime\":\"2026-02-24 18:09:12\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:12');
INSERT INTO `sys_operation_log` VALUES (69, 1, 'admin', '1', '1', 'USER', '166', NULL, '{\"id\":166,\"username\":\"user69\",\"password\":\"$2a$12$4MZYNB21OR/2e4SPaJ1aH.HbpPCCgmYaPnZnui85/Qqz3wymnpQr6\",\"nickname\":\"用户69\",\"email\":\"user69@example.com\",\"phone\":\"13800000069\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:13\",\"updateTime\":\"2026-02-24 18:09:13\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:13');
INSERT INTO `sys_operation_log` VALUES (70, 1, 'admin', '1', '1', 'USER', '167', NULL, '{\"id\":167,\"username\":\"user70\",\"password\":\"$2a$12$xVModM2Ndcwpt43aQuXeVuJ3Xcq.LN9lrx0fTcB31wc8l5kO9T9Je\",\"nickname\":\"用户70\",\"email\":\"user70@example.com\",\"phone\":\"13800000070\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:13\",\"updateTime\":\"2026-02-24 18:09:13\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:13');
INSERT INTO `sys_operation_log` VALUES (71, 1, 'admin', '1', '1', 'USER', '168', NULL, '{\"id\":168,\"username\":\"user71\",\"password\":\"$2a$12$CDwj6/htlFt5tXf3dwMAXeQ2hqFJMRmg7yLCfnoNd07da0TvbpUK6\",\"nickname\":\"用户71\",\"email\":\"user71@example.com\",\"phone\":\"13800000071\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:13\",\"updateTime\":\"2026-02-24 18:09:13\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:13');
INSERT INTO `sys_operation_log` VALUES (72, 1, 'admin', '1', '1', 'USER', '169', NULL, '{\"id\":169,\"username\":\"user72\",\"password\":\"$2a$12$pHv66AvbvQNgZ/HQ2oHjXufyr0li.HP.J8wI2LJ2PLGAFINnhgaiu\",\"nickname\":\"用户72\",\"email\":\"user72@example.com\",\"phone\":\"13800000072\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:13\",\"updateTime\":\"2026-02-24 18:09:13\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:13');
INSERT INTO `sys_operation_log` VALUES (73, 1, 'admin', '1', '1', 'USER', '170', NULL, '{\"id\":170,\"username\":\"user73\",\"password\":\"$2a$12$uHdDwPiiRTusQrwmPCkIVuL7j1WVh8SywIilitRYd0StX8L7N3D9y\",\"nickname\":\"用户73\",\"email\":\"user73@example.com\",\"phone\":\"13800000073\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:14\",\"updateTime\":\"2026-02-24 18:09:14\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:14');
INSERT INTO `sys_operation_log` VALUES (74, 1, 'admin', '1', '1', 'USER', '171', NULL, '{\"id\":171,\"username\":\"user74\",\"password\":\"$2a$12$C2pR6GvU6KVaVCYnhJ/cqu4PZjQwsnzJ5pdAp0cW16qDeKTpXaFSS\",\"nickname\":\"用户74\",\"email\":\"user74@example.com\",\"phone\":\"13800000074\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:14\",\"updateTime\":\"2026-02-24 18:09:14\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:14');
INSERT INTO `sys_operation_log` VALUES (75, 1, 'admin', '1', '1', 'USER', '172', NULL, '{\"id\":172,\"username\":\"user75\",\"password\":\"$2a$12$RYGwpuihp9HP/WZ3xAmZluJcI/mdsRZGTC4f7Jiov/9p8kQHw1Aey\",\"nickname\":\"用户75\",\"email\":\"user75@example.com\",\"phone\":\"13800000075\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:14\",\"updateTime\":\"2026-02-24 18:09:14\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:14');
INSERT INTO `sys_operation_log` VALUES (76, 1, 'admin', '1', '1', 'USER', '173', NULL, '{\"id\":173,\"username\":\"user76\",\"password\":\"$2a$12$teih3FOFDD9b365VseslD.mQ.PU2nReL8N4zbaocjrHcDay3lac3m\",\"nickname\":\"用户76\",\"email\":\"user76@example.com\",\"phone\":\"13800000076\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:14\",\"updateTime\":\"2026-02-24 18:09:14\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:14');
INSERT INTO `sys_operation_log` VALUES (77, 1, 'admin', '1', '1', 'USER', '174', NULL, '{\"id\":174,\"username\":\"user77\",\"password\":\"$2a$12$UElOJZ7psiaRvwJJkLtld.8xLmU99ozCDhDAVFdleXEFXxzRR03vC\",\"nickname\":\"用户77\",\"email\":\"user77@example.com\",\"phone\":\"13800000077\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:15\",\"updateTime\":\"2026-02-24 18:09:15\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:15');
INSERT INTO `sys_operation_log` VALUES (78, 1, 'admin', '1', '1', 'USER', '175', NULL, '{\"id\":175,\"username\":\"user78\",\"password\":\"$2a$12$35KoK16iNm2AgYMMVlzHVeqbe/qBZMU0QjW4xmcX1Gyi6jUN3hCaW\",\"nickname\":\"用户78\",\"email\":\"user78@example.com\",\"phone\":\"13800000078\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:15\",\"updateTime\":\"2026-02-24 18:09:15\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:15');
INSERT INTO `sys_operation_log` VALUES (79, 1, 'admin', '1', '1', 'USER', '176', NULL, '{\"id\":176,\"username\":\"user79\",\"password\":\"$2a$12$KdWMiUAWZqvd91ZcVa9VLedOiMmqDl9Uyzlz9KcZNi.w2ZQvqubXm\",\"nickname\":\"用户79\",\"email\":\"user79@example.com\",\"phone\":\"13800000079\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:15\",\"updateTime\":\"2026-02-24 18:09:15\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:15');
INSERT INTO `sys_operation_log` VALUES (80, 1, 'admin', '1', '1', 'USER', '177', NULL, '{\"id\":177,\"username\":\"user80\",\"password\":\"$2a$12$aWSJHxbMUkNmvg98bpxJweFgOxXeW0g.Du/3PGWZWGUDHujtw5Kxq\",\"nickname\":\"用户80\",\"email\":\"user80@example.com\",\"phone\":\"13800000080\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:16\",\"updateTime\":\"2026-02-24 18:09:16\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:16');
INSERT INTO `sys_operation_log` VALUES (81, 1, 'admin', '1', '1', 'USER', '178', NULL, '{\"id\":178,\"username\":\"user81\",\"password\":\"$2a$12$t/fskcrZRttk0GQNZj.gNe6h8yAu45zCY.JJEqXQudNXlD/OEePSe\",\"nickname\":\"用户81\",\"email\":\"user81@example.com\",\"phone\":\"13800000081\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:16\",\"updateTime\":\"2026-02-24 18:09:16\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:16');
INSERT INTO `sys_operation_log` VALUES (82, 1, 'admin', '1', '1', 'USER', '179', NULL, '{\"id\":179,\"username\":\"user82\",\"password\":\"$2a$12$hl/o0skjtQRxT.OFTsv32uKiz871/UhNAbDB.VvDjMrp/Ed06rnkK\",\"nickname\":\"用户82\",\"email\":\"user82@example.com\",\"phone\":\"13800000082\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:16\",\"updateTime\":\"2026-02-24 18:09:16\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:16');
INSERT INTO `sys_operation_log` VALUES (83, 1, 'admin', '1', '1', 'USER', '180', NULL, '{\"id\":180,\"username\":\"user83\",\"password\":\"$2a$12$WTz9bayQ78dxLnzbCYV5MedCHNSS1OLeMe7fl/4.5WFr.27LIdhaK\",\"nickname\":\"用户83\",\"email\":\"user83@example.com\",\"phone\":\"13800000083\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:16\",\"updateTime\":\"2026-02-24 18:09:16\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:16');
INSERT INTO `sys_operation_log` VALUES (84, 1, 'admin', '1', '1', 'USER', '181', NULL, '{\"id\":181,\"username\":\"user84\",\"password\":\"$2a$12$gJYjZE2/lAfwBxssyjBsmuypnXq.6pmfzXb0dH5lvtpzsWCa5i/wy\",\"nickname\":\"用户84\",\"email\":\"user84@example.com\",\"phone\":\"13800000084\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:17\",\"updateTime\":\"2026-02-24 18:09:17\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:17');
INSERT INTO `sys_operation_log` VALUES (85, 1, 'admin', '1', '1', 'USER', '182', NULL, '{\"id\":182,\"username\":\"user85\",\"password\":\"$2a$12$EHyR2CtBbp7z3HTuaT3dZOoLrqJkVViU5A.EV6scj32o.9y/7xkx.\",\"nickname\":\"用户85\",\"email\":\"user85@example.com\",\"phone\":\"13800000085\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:17\",\"updateTime\":\"2026-02-24 18:09:17\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:17');
INSERT INTO `sys_operation_log` VALUES (86, 1, 'admin', '1', '1', 'USER', '183', NULL, '{\"id\":183,\"username\":\"user86\",\"password\":\"$2a$12$BgGkNc0AuJqPtuZIq33YS.IMY1Mh8RcB2zbrzABlR6U7Dk20iO.j.\",\"nickname\":\"用户86\",\"email\":\"user86@example.com\",\"phone\":\"13800000086\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:17\",\"updateTime\":\"2026-02-24 18:09:17\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:17');
INSERT INTO `sys_operation_log` VALUES (87, 1, 'admin', '1', '1', 'USER', '184', NULL, '{\"id\":184,\"username\":\"user87\",\"password\":\"$2a$12$rHIfGdrjzdLlIj7G.eT1.e/ImeRvP2rLmwwMvYCQXB9CoZ0Q3WwM2\",\"nickname\":\"用户87\",\"email\":\"user87@example.com\",\"phone\":\"13800000087\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:17\",\"updateTime\":\"2026-02-24 18:09:17\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:17');
INSERT INTO `sys_operation_log` VALUES (88, 1, 'admin', '1', '1', 'USER', '185', NULL, '{\"id\":185,\"username\":\"user88\",\"password\":\"$2a$12$W09X4PBfEa9weosPNNbg0uBvo4hBTFiMap64ASt33muQr7Etq.zGS\",\"nickname\":\"用户88\",\"email\":\"user88@example.com\",\"phone\":\"13800000088\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:18\",\"updateTime\":\"2026-02-24 18:09:18\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:18');
INSERT INTO `sys_operation_log` VALUES (89, 1, 'admin', '1', '1', 'USER', '186', NULL, '{\"id\":186,\"username\":\"user89\",\"password\":\"$2a$12$lgWckcGw0KDv8gOZ6ph4o.NGWUphh83LcPJFLt6lyqbKiuIu.kum2\",\"nickname\":\"用户89\",\"email\":\"user89@example.com\",\"phone\":\"13800000089\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:18\",\"updateTime\":\"2026-02-24 18:09:18\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:18');
INSERT INTO `sys_operation_log` VALUES (90, 1, 'admin', '1', '1', 'USER', '187', NULL, '{\"id\":187,\"username\":\"user90\",\"password\":\"$2a$12$2QdicAynLDypMa8CbXhnquAnVFMvLDgYxFAH2O.xnuG0EGZv4RRme\",\"nickname\":\"用户90\",\"email\":\"user90@example.com\",\"phone\":\"13800000090\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:18\",\"updateTime\":\"2026-02-24 18:09:18\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:18');
INSERT INTO `sys_operation_log` VALUES (91, 1, 'admin', '1', '1', 'USER', '188', NULL, '{\"id\":188,\"username\":\"user91\",\"password\":\"$2a$12$y29y5WfYhZL8FRStvi70bOKL6aZuQJ1hGLYR5XhMV6lW1xR7hyKki\",\"nickname\":\"用户91\",\"email\":\"user91@example.com\",\"phone\":\"13800000091\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:19\",\"updateTime\":\"2026-02-24 18:09:19\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:19');
INSERT INTO `sys_operation_log` VALUES (92, 1, 'admin', '1', '1', 'USER', '189', NULL, '{\"id\":189,\"username\":\"user92\",\"password\":\"$2a$12$Dw3mUqIUM05rT1SrrungDei.L3BDHTGbpSvztWGv//18RqyzSLJf6\",\"nickname\":\"用户92\",\"email\":\"user92@example.com\",\"phone\":\"13800000092\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:19\",\"updateTime\":\"2026-02-24 18:09:19\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:19');
INSERT INTO `sys_operation_log` VALUES (93, 1, 'admin', '1', '1', 'USER', '190', NULL, '{\"id\":190,\"username\":\"user93\",\"password\":\"$2a$12$ZZ4oVXpxkIkiE54vp0AZ2u.mM1vpAquGX69x8bTNqTBjSTbOa6eZa\",\"nickname\":\"用户93\",\"email\":\"user93@example.com\",\"phone\":\"13800000093\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:19\",\"updateTime\":\"2026-02-24 18:09:19\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:19');
INSERT INTO `sys_operation_log` VALUES (94, 1, 'admin', '1', '1', 'USER', '191', NULL, '{\"id\":191,\"username\":\"user94\",\"password\":\"$2a$12$2IIEqwIETkUmiUcDmnt3qO0IyVnBlOJqMS6garMzCZ0XwQa5jclCS\",\"nickname\":\"用户94\",\"email\":\"user94@example.com\",\"phone\":\"13800000094\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:19\",\"updateTime\":\"2026-02-24 18:09:19\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:19');
INSERT INTO `sys_operation_log` VALUES (95, 1, 'admin', '1', '1', 'USER', '192', NULL, '{\"id\":192,\"username\":\"user95\",\"password\":\"$2a$12$/CBCANZHT86DEBtjYmonq.vCj7nk.gawWyiYZypMx.JWcKZBif03m\",\"nickname\":\"用户95\",\"email\":\"user95@example.com\",\"phone\":\"13800000095\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:20\",\"updateTime\":\"2026-02-24 18:09:20\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:20');
INSERT INTO `sys_operation_log` VALUES (96, 1, 'admin', '1', '1', 'USER', '193', NULL, '{\"id\":193,\"username\":\"user96\",\"password\":\"$2a$12$7U9ivXUxFFa9Zm8Bivn1/.9qH.T.cXFAa9Eugd8w.sS6FGbbhHLX6\",\"nickname\":\"用户96\",\"email\":\"user96@example.com\",\"phone\":\"13800000096\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:20\",\"updateTime\":\"2026-02-24 18:09:20\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:20');
INSERT INTO `sys_operation_log` VALUES (97, 1, 'admin', '1', '1', 'USER', '194', NULL, '{\"id\":194,\"username\":\"user97\",\"password\":\"$2a$12$LdGvv6meDQzclI9rfAw29OrDRanqbR/TyL7LfoZSjx2ea5deKbHnq\",\"nickname\":\"用户97\",\"email\":\"user97@example.com\",\"phone\":\"13800000097\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:20\",\"updateTime\":\"2026-02-24 18:09:20\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:20');
INSERT INTO `sys_operation_log` VALUES (98, 1, 'admin', '1', '1', 'USER', '195', NULL, '{\"id\":195,\"username\":\"user98\",\"password\":\"$2a$12$llyF.DsGNOqK70NKnXowBusjGxf6eAkXG32h8um4oIb5nJQa7dGhC\",\"nickname\":\"用户98\",\"email\":\"user98@example.com\",\"phone\":\"13800000098\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:20\",\"updateTime\":\"2026-02-24 18:09:20\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:20');
INSERT INTO `sys_operation_log` VALUES (99, 1, 'admin', '1', '1', 'USER', '196', NULL, '{\"id\":196,\"username\":\"user99\",\"password\":\"$2a$12$7q/REejxH7JVAyCstUb1s./MsLwULKX8UR7v0JiXAjLC2Vw5DqZMW\",\"nickname\":\"用户99\",\"email\":\"user99@example.com\",\"phone\":\"13800000099\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:21\",\"updateTime\":\"2026-02-24 18:09:21\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:21');
INSERT INTO `sys_operation_log` VALUES (100, 1, 'admin', '1', '1', 'USER', '197', NULL, '{\"id\":197,\"username\":\"user100\",\"password\":\"$2a$12$Q6gCPlwODV5wQgKe2prCmepcecbcv3nMHIVwZQZb7SQQ6lT3DwKDS\",\"nickname\":\"用户100\",\"email\":\"user100@example.com\",\"phone\":\"13800000100\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:21\",\"updateTime\":\"2026-02-24 18:09:21\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:09:21');
INSERT INTO `sys_operation_log` VALUES (101, 3, 'yanyan', '2', '1', 'USER', '196', '{\"id\":196,\"username\":\"user99\",\"password\":\"$2a$12$7q/REejxH7JVAyCstUb1s./MsLwULKX8UR7v0JiXAjLC2Vw5DqZMW\",\"nickname\":\"用户99\",\"email\":\"user99@example.com\",\"phone\":\"13800000099\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:21\",\"updateTime\":\"2026-02-24 18:09:21\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', '{\"id\":196,\"username\":\"user99\",\"password\":\"$2a$12$7q/REejxH7JVAyCstUb1s./MsLwULKX8UR7v0JiXAjLC2Vw5DqZMW\",\"nickname\":\"用户99\",\"email\":\"user99@example.com\",\"phone\":\"13800000099\",\"departmentId\":1,\"status\":\"NORMAL\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:21\",\"updateTime\":\"2026-02-24 18:10:20\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:10:20');
INSERT INTO `sys_operation_log` VALUES (102, 3, 'yanyan', '2', '1', 'USER', '197', '{\"id\":197,\"username\":\"user100\",\"password\":\"$2a$12$Q6gCPlwODV5wQgKe2prCmepcecbcv3nMHIVwZQZb7SQQ6lT3DwKDS\",\"nickname\":\"用户100\",\"email\":\"user100@example.com\",\"phone\":\"13800000100\",\"departmentId\":1,\"status\":\"DISABLED\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:21\",\"updateTime\":\"2026-02-24 18:09:21\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', '{\"id\":197,\"username\":\"user100\",\"password\":\"$2a$12$Q6gCPlwODV5wQgKe2prCmepcecbcv3nMHIVwZQZb7SQQ6lT3DwKDS\",\"nickname\":\"用户100\",\"email\":\"user100@example.com\",\"phone\":\"13800000100\",\"departmentId\":1,\"status\":\"NORMAL\",\"passwordResetRequired\":0,\"createTime\":\"2026-02-24 18:09:21\",\"updateTime\":\"2026-02-24 18:10:23\",\"isDeleted\":0,\"createUserId\":null,\"updateUserId\":null}', NULL, NULL, '0:0:0:0:0:0:0:1', NULL, NULL, NULL, 1, NULL, '2026-02-24 18:10:23');

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  `permission_type` tinyint NOT NULL DEFAULT 1 COMMENT '权限类型 1:功能权限 2:菜单权限 3:数据权限',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, '查看所有项目', 'PROJECT_VIEW_ALL', '可查看系统中所有项目', '2026-02-24 10:17:17', '2026-02-24 10:17:19', 1, 1, 0, 3);
INSERT INTO `sys_permission` VALUES (2, '查看所有任务', 'TASK_VIEW_ALL', '可查看系统中所有任务', '2026-02-24 10:17:17', '2026-02-24 10:17:19', 1, 1, 0, 3);
INSERT INTO `sys_permission` VALUES (3, '查看所有工时', 'WORKHOUR_VIEW_ALL', '可查看系统中所有工时记录', '2026-02-24 10:17:17', '2026-02-24 10:17:19', 1, 1, 0, 3);
INSERT INTO `sys_permission` VALUES (4, '访问首页', 'MENU_DASHBOARD', '可访问首页菜单', '2026-02-24 10:17:18', '2026-02-24 10:17:19', 1, 1, 0, 2);
INSERT INTO `sys_permission` VALUES (5, '访问项目管理', 'MENU_PROJECT', '可访问项目管理菜单', '2026-02-24 10:17:18', '2026-02-24 10:17:19', 1, 1, 0, 2);
INSERT INTO `sys_permission` VALUES (6, '访问任务中心', 'MENU_TASK', '可访问任务中心菜单', '2026-02-24 10:17:18', '2026-02-24 10:17:19', 1, 1, 0, 2);
INSERT INTO `sys_permission` VALUES (7, '访问工时管理', 'MENU_WORKHOUR', '可访问工时管理相关菜单', '2026-02-24 10:17:18', '2026-02-24 10:17:19', 1, 1, 0, 2);
INSERT INTO `sys_permission` VALUES (8, '访问统计分析', 'MENU_STATISTICS', '可访问统计分析相关菜单', '2026-02-24 10:17:18', '2026-02-24 10:17:19', 1, 1, 0, 2);
INSERT INTO `sys_permission` VALUES (9, '访问系统管理', 'MENU_SYSTEM', '可访问系统管理相关菜单', '2026-02-24 10:17:18', '2026-02-24 10:17:19', 1, 1, 0, 2);
INSERT INTO `sys_permission` VALUES (10, '创建任务', 'TASK_CREATE', '可创建任务', '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0, 1);
INSERT INTO `sys_permission` VALUES (11, '编辑任务', 'TASK_EDIT', '可编辑任务', '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0, 1);
INSERT INTO `sys_permission` VALUES (12, '删除任务', 'TASK_DELETE', '可删除任务', '2026-02-24 10:17:18', '2026-02-24 10:17:18', 1, 1, 0, 1);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  `role_type` enum('SYSTEM','CUSTOM') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CUSTOM' COMMENT '角色类型 SYSTEM:系统预置(不可删除) CUSTOM:用户自定义(可删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '系统管理员', 'SYS_ADMIN', '系统级管理员，拥有所有权限', '2026-02-24 10:17:17', '2026-02-24 10:17:18', 1, 1, 0, 'SYSTEM');
INSERT INTO `sys_role` VALUES (2, '业务经理', 'BUSINESS_MANAGER', '业务分析/统计角色，全局只读', '2026-02-24 10:17:17', '2026-02-24 10:17:17', 1, 1, 0, 'CUSTOM');
INSERT INTO `sys_role` VALUES (3, '项目经理', 'PROJECT_MANAGER', '项目级管理者，管理参与项目范围内数据', '2026-02-24 10:17:17', '2026-02-24 10:17:17', 1, 1, 0, 'CUSTOM');
INSERT INTO `sys_role` VALUES (4, '员工', 'EMPLOYEE', '普通员工，管理个人任务与工时', '2026-02-24 10:17:17', '2026-02-24 10:17:18', 1, 1, 0, 'SYSTEM');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_permission_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_role_permission_permission_id`(`permission_id` ASC) USING BTREE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 86 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1, '2026-02-24 10:17:17', 0);
INSERT INTO `sys_role_permission` VALUES (2, 1, 2, '2026-02-24 10:17:17', 0);
INSERT INTO `sys_role_permission` VALUES (3, 1, 3, '2026-02-24 10:17:17', 0);
INSERT INTO `sys_role_permission` VALUES (4, 2, 1, '2026-02-24 10:17:17', 0);
INSERT INTO `sys_role_permission` VALUES (5, 2, 2, '2026-02-24 10:17:17', 0);
INSERT INTO `sys_role_permission` VALUES (6, 2, 3, '2026-02-24 10:17:17', 0);
INSERT INTO `sys_role_permission` VALUES (7, 1, 4, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (8, 1, 5, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (9, 1, 8, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (10, 1, 9, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (11, 1, 6, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (12, 1, 7, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (14, 2, 4, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (15, 2, 5, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (16, 2, 8, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (17, 2, 6, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (18, 2, 7, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (21, 3, 4, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (22, 3, 5, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (23, 3, 6, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (24, 3, 7, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (28, 4, 4, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (29, 4, 6, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (30, 4, 7, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (31, 4, 5, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (32, 1, 10, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (33, 1, 12, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (34, 1, 11, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (35, 3, 10, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (36, 3, 12, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (37, 3, 11, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (38, 2, 10, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (39, 2, 12, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (40, 2, 11, '2026-02-24 10:17:18', 0);
INSERT INTO `sys_role_permission` VALUES (59, 3, 8, '2026-02-24 16:30:08', 0);
INSERT INTO `sys_role_permission` VALUES (60, 3, 9, '2026-02-24 16:30:08', 0);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '电话',
  `department_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1:正常 2:禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` bigint NOT NULL DEFAULT 1 COMMENT '创建人ID',
  `update_user_id` bigint NOT NULL DEFAULT 1 COMMENT '更新人ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  `password_reset_required` tinyint(1) NULL DEFAULT 0 COMMENT '是否需要重置密码（0-否，1-是）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_department_id`(`department_id` ASC) USING BTREE,
  INDEX `idx_user_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_user_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 198 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$12$CQpisUhdgrlmuz.76H79Y.jJGFR3dGribbfDyrCASJzkUsJkWVXfG', '系统管理员', 'admin@gsms.com', '13800138000', 1, 1, '2026-02-24 10:17:17', '2026-02-24 11:24:22', 1, 1, 0, 1);
INSERT INTO `sys_user` VALUES (2, 'zhangsan', '$2a$12$qvJzHaeiF0QXOYqvAs8OD.fHUO/i/ebk3q0aVt95rbS/PZRFu1VfK', '张三', 'zhangsan@example.com', '13800138000', 1, 1, '2026-02-24 11:08:01', '2026-02-24 16:36:29', 1, 3, 0, 0);
INSERT INTO `sys_user` VALUES (3, 'yanyan', '$2a$12$sxAG2lCRNWufEsH.I3LWfe2UxB2Y96o.oa0ebQbTIECztkrh/Vj2y', 'yanyan', 'andymon413@163.com', '17788889999', 1, 1, '2026-02-24 11:16:18', '2026-02-24 11:33:42', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (98, 'user1', '$2a$12$ZRavm03V97yzZ6LwXa7Sne8iaRlVLSuo5xXqbN2MdTOPBsDs/pztm', '用户1', 'user1@example.com', '13800000001', 1, 1, '2026-02-24 18:08:52', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (99, 'user2', '$2a$12$09oUhnuqA.hwqiHmJDS/zuydahP6fTvlIj.7uw8ncyzQFM7IumH42', '用户2', 'user2@example.com', '13800000002', 1, 1, '2026-02-24 18:08:52', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (100, 'user3', '$2a$12$pgG.iRF2MShCdnqVKsLHTu100LNb1Zz/sqE1z3ecWekozR8dm4AiO', '用户3', 'user3@example.com', '13800000003', 1, 1, '2026-02-24 18:08:53', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (101, 'user4', '$2a$12$BE8U2rDA9a3p04khWe8Q6.fh55bZrkthMklKTXUjWmsFVuC25No/W', '用户4', 'user4@example.com', '13800000004', 1, 1, '2026-02-24 18:08:53', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (102, 'user5', '$2a$12$RAcLqsHO8mlG4O.BNO.PJ.KDPspRFhkD8n/qwHmSGjC95fTtkBBlu', '用户5', 'user5@example.com', '13800000005', 1, 1, '2026-02-24 18:08:53', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (103, 'user6', '$2a$12$LpnVETkWBQNROnhIQIE8SO75PKkO58OR5emgx48kXQqM2egEdT0C2', '用户6', 'user6@example.com', '13800000006', 1, 1, '2026-02-24 18:08:54', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (104, 'user7', '$2a$12$jcuRC2cGRDwDwIrCgu4FZO6dpki1NkirvvoQZ1/I2aOxOUG1BWksW', '用户7', 'user7@example.com', '13800000007', 1, 1, '2026-02-24 18:08:54', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (105, 'user8', '$2a$12$syfxoGQcYqauOKorW1E2ouFeuC2eDFOBSx9bf6x3aImqaXXtftckS', '用户8', 'user8@example.com', '13800000008', 1, 1, '2026-02-24 18:08:55', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (106, 'user9', '$2a$12$c9JGMN95ohNC5ts97SNwf./Fidp7HlTWeUY6Fuvx0aW9SacjNPwbu', '用户9', 'user9@example.com', '13800000009', 1, 1, '2026-02-24 18:08:55', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (107, 'user10', '$2a$12$RuUL5.2JHoVc/NJEUOI3BebtMtliZ7lqeTejiss7sW2kWr0WwUIBa', '用户10', 'user10@example.com', '13800000010', 1, 1, '2026-02-24 18:08:55', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (108, 'user11', '$2a$12$2dbw2ImFR4omspVv5oqDpOhv8kcq9dBVNoPJGJBy85E939WNZh3Hm', '用户11', 'user11@example.com', '13800000011', 1, 1, '2026-02-24 18:08:56', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (109, 'user12', '$2a$12$fFSC0CknU9orupkLBGbmW.x1/pZyNu/9Lqpv8eS6ly252Di9XCKLq', '用户12', 'user12@example.com', '13800000012', 1, 1, '2026-02-24 18:08:56', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (110, 'user13', '$2a$12$K4JeNU0IVcFC2uWhmNoxZOoubvjqSLL9jUx/8CHhBMwkIAawtjK6K', '用户13', 'user13@example.com', '13800000013', 1, 1, '2026-02-24 18:08:57', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (111, 'user14', '$2a$12$fQQsKB554LQQkM/tfPYnuOqJbSQ.5TgvhT3hePE/VVsQUHulbB65m', '用户14', 'user14@example.com', '13800000014', 1, 1, '2026-02-24 18:08:57', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (112, 'user15', '$2a$12$SqIQsn3GnabcJPCaPN3GQuWnBu40QN9H1WUsvLXuTQio8ya/D5ho6', '用户15', 'user15@example.com', '13800000015', 1, 1, '2026-02-24 18:08:57', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (113, 'user16', '$2a$12$BhBFe3kYs9J/MLaHjIUjv.HDjbGCuEIObG351XZsgbrarKs/gcgGW', '用户16', 'user16@example.com', '13800000016', 1, 1, '2026-02-24 18:08:58', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (114, 'user17', '$2a$12$M0KdC8CU0a6aoCGPUkp4Lu69sWg7bwv2m3NCDYJsuwnSz32CvvP2u', '用户17', 'user17@example.com', '13800000017', 1, 1, '2026-02-24 18:08:58', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (115, 'user18', '$2a$12$u8B5MglnFBj.IT5T3ApwyeLVz.4Fglv2nJjMo5EJessSEXi9./vD.', '用户18', 'user18@example.com', '13800000018', 1, 1, '2026-02-24 18:08:59', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (116, 'user19', '$2a$12$cbQO25Mm6r8G0Jy8BGKiFeYU8b7xoI/ZBEsdJ8Wis8tOPU7my7tQO', '用户19', 'user19@example.com', '13800000019', 1, 1, '2026-02-24 18:08:59', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (117, 'user20', '$2a$12$qTXrTl3IXOrur0aatedQXuq.on6vdHQ5bNbZ4UZOTidCioSdufgrG', '用户20', 'user20@example.com', '13800000020', 1, 1, '2026-02-24 18:08:59', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (118, 'user21', '$2a$12$GoVikBpPQlsLDe5JrdAQluq4w1wHjV8j37dMDZT1A8Jql3ra9Ohq6', '用户21', 'user21@example.com', '13800000021', 1, 1, '2026-02-24 18:09:00', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (119, 'user22', '$2a$12$8IE.HO75M8lsxWC155vDOeJ4Sm4MWZpt9vZUwEQEaIbOXtr2wHdAS', '用户22', 'user22@example.com', '13800000022', 1, 1, '2026-02-24 18:09:00', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (120, 'user23', '$2a$12$LOu0.cPJ8TdDMaFwrx4PIu.aHEP7y0l6CO3HEmhqniHByaMHHdANa', '用户23', 'user23@example.com', '13800000023', 1, 1, '2026-02-24 18:09:00', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (121, 'user24', '$2a$12$illyl9jNOOxOLDIeLIF85eJLLHqsyjLmSyupnbre7dodgSovL1WpK', '用户24', 'user24@example.com', '13800000024', 1, 1, '2026-02-24 18:09:01', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (122, 'user25', '$2a$12$mZz5ts9yPILMIKVhR3dN6uCJlkopNYbUR15/m/2xfllMBHt1sj9wG', '用户25', 'user25@example.com', '13800000025', 1, 1, '2026-02-24 18:09:01', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (123, 'user26', '$2a$12$2i1fmI.1XiLwy/.HoQbpq.1J7KyI5xP1TCPR/t0OQ/c.BpTJo9Ugq', '用户26', 'user26@example.com', '13800000026', 1, 1, '2026-02-24 18:09:01', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (124, 'user27', '$2a$12$qLUgGyGT6UjT.gw18zlvfOezwXxaHh45NHE6aijb/SKFp.SMexLJe', '用户27', 'user27@example.com', '13800000027', 1, 1, '2026-02-24 18:09:01', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (125, 'user28', '$2a$12$FEPhXxfGqN3svDpt/ST/H.zQbYyZXNWOlQJLuQmFa21lTwC.1PPfS', '用户28', 'user28@example.com', '13800000028', 1, 1, '2026-02-24 18:09:02', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (126, 'user29', '$2a$12$FaG7QZXetpsUwNWKHjCPee2NAhWWsxXLQysKGxPOiStTsnsBXW58m', '用户29', 'user29@example.com', '13800000029', 1, 1, '2026-02-24 18:09:02', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (127, 'user30', '$2a$12$tH1Syf42WMP90TAp/cPdZOCf0BJRG7lV9ILAotPb.zFNlqOgaB1VC', '用户30', 'user30@example.com', '13800000030', 1, 1, '2026-02-24 18:09:02', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (128, 'user31', '$2a$12$qOm.fzDsYNedvYd0RqhUe..kwmdmysVEURDC1qZCq8WGYZ61mfo0u', '用户31', 'user31@example.com', '13800000031', 1, 1, '2026-02-24 18:09:02', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (129, 'user32', '$2a$12$Fy.aWdLG/LUFBW.OLnmjLu6laEsZ4si.p3QonVlEiiRLk2G1BHSs2', '用户32', 'user32@example.com', '13800000032', 1, 1, '2026-02-24 18:09:03', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (130, 'user33', '$2a$12$KKU3AcjgIqry7zdepiYVqeldjSojnPve6dzqoYg8by6J7Ek73zaWm', '用户33', 'user33@example.com', '13800000033', 1, 1, '2026-02-24 18:09:03', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (131, 'user34', '$2a$12$plv4wcak2bgKeCEedmtSyu4Qg65QWf0gP5PijZv4/.62ApRQVWZRa', '用户34', 'user34@example.com', '13800000034', 1, 1, '2026-02-24 18:09:03', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (132, 'user35', '$2a$12$nVDqLOu39bx6XQ14ubnx4.g/6jc.S3LzlI7tV29nSgauXhP9jTHn6', '用户35', 'user35@example.com', '13800000035', 1, 1, '2026-02-24 18:09:04', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (133, 'user36', '$2a$12$VBoTbjkTCy3.TokMB2.vXefFsDpIsk9bdiexJUjT0cSpbTWaAOvCK', '用户36', 'user36@example.com', '13800000036', 1, 1, '2026-02-24 18:09:04', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (134, 'user37', '$2a$12$0bwgIeU86vtd24nCXmlcpO5yUZDZ2lbdIf5/kWT07Lt8z2QwWm8oG', '用户37', 'user37@example.com', '13800000037', 1, 1, '2026-02-24 18:09:04', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (135, 'user38', '$2a$12$ucy/YvkMckTqndtFZiolFumyLQKW2P9Uj1Ap36KGQcPBQT1vN7EQW', '用户38', 'user38@example.com', '13800000038', 1, 1, '2026-02-24 18:09:04', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (136, 'user39', '$2a$12$N7O2qaoai4BI23oGruc.IO6z8ZnplJXvzPdmQgA5ULmaQG1SvZp42', '用户39', 'user39@example.com', '13800000039', 1, 1, '2026-02-24 18:09:05', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (137, 'user40', '$2a$12$7P3OAi8ApiRKHWBYpS.QUOlhhGEOor1vKyE527jiQpGJ6JzQVQZJC', '用户40', 'user40@example.com', '13800000040', 1, 1, '2026-02-24 18:09:05', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (138, 'user41', '$2a$12$i7TD6KUIBwXY4ml.i3kodehYhnB7uHfZT3WizbuD4XEk1xR5PaA6i', '用户41', 'user41@example.com', '13800000041', 1, 1, '2026-02-24 18:09:05', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (139, 'user42', '$2a$12$YWUhsTE3h8ZZqTp4hZqk5.UVkdXyFX0uP8G5ma10MyYV7cobzpov.', '用户42', 'user42@example.com', '13800000042', 1, 1, '2026-02-24 18:09:05', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (140, 'user43', '$2a$12$SuSGmKbcnU5l2c7Zyq/lqOAVB3FZArE5XYnvTllcX3cw9UfXnOYgK', '用户43', 'user43@example.com', '13800000043', 1, 1, '2026-02-24 18:09:06', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (141, 'user44', '$2a$12$8a4xfOlQ2RQmNEKm9gXhH.x/QPXMAEE0vWK31HLHUIhXFwlM6bgM6', '用户44', 'user44@example.com', '13800000044', 1, 1, '2026-02-24 18:09:06', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (142, 'user45', '$2a$12$6zQjyiCF2BCeNO2LQR7DquZa8a/SYJlBMWGh5ZYspB0CAm9WZ0DwO', '用户45', 'user45@example.com', '13800000045', 1, 1, '2026-02-24 18:09:06', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (143, 'user46', '$2a$12$qXbSXxs0KgReto3Cr9dA2ezYOVppl5oh/3dYZgvfhXybH2uHM1twC', '用户46', 'user46@example.com', '13800000046', 1, 1, '2026-02-24 18:09:06', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (144, 'user47', '$2a$12$9wer/KNtGdVXKp2R1LG4b.8jReezqr5oQuyAkt5zW9yh7D/w6e/76', '用户47', 'user47@example.com', '13800000047', 1, 1, '2026-02-24 18:09:07', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (145, 'user48', '$2a$12$0FVWvueHTDHDlX60yu920uDVYFt9edCvAbYwMC9kof9BW6PsXQuu6', '用户48', 'user48@example.com', '13800000048', 1, 1, '2026-02-24 18:09:07', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (146, 'user49', '$2a$12$koYzDl/w7sQDpOP.SnvY7um0ZIxMfyDx011TRJAGiHVXXnJ202ESa', '用户49', 'user49@example.com', '13800000049', 1, 1, '2026-02-24 18:09:07', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (147, 'user50', '$2a$12$cxE7.fk2pwf3mrDyTzMT/.Owy6rP7bENSq3sYackt7pHBG5cjGWCG', '用户50', 'user50@example.com', '13800000050', 1, 1, '2026-02-24 18:09:07', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (148, 'user51', '$2a$12$mw/jd1YCp1nseAXJvJIA3ubb3yTH7HCPS/OwCCjwSYA3kehvY4Tj2', '用户51', 'user51@example.com', '13800000051', 1, 1, '2026-02-24 18:09:08', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (149, 'user52', '$2a$12$U2FSBOJsBk4Jv4UI4e9cJ.4Uy6RX2p/UNVP0rqvFyHjQTU5RpK4vW', '用户52', 'user52@example.com', '13800000052', 1, 1, '2026-02-24 18:09:08', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (150, 'user53', '$2a$12$0MZvgR7uCBbqslqlYMkqNOLNNSqgYPT1O.G/bf5ZwShAes9rPc.am', '用户53', 'user53@example.com', '13800000053', 1, 1, '2026-02-24 18:09:08', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (151, 'user54', '$2a$12$3YB5rTDfFSPGSXpwy1qjKeYK4zHcDQn6FYV6d/pKP68ZqcUqN93Tm', '用户54', 'user54@example.com', '13800000054', 1, 1, '2026-02-24 18:09:09', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (152, 'user55', '$2a$12$kc6Rr67tdPwkLG.4/4.dh.fs04cB8UCwama7udqI91srQMs6TgyXy', '用户55', 'user55@example.com', '13800000055', 1, 1, '2026-02-24 18:09:09', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (153, 'user56', '$2a$12$dm2/Dg2kWH9oRGfSrPCBYePCVFK3mobk31Z3sm0F03e886.RHz9fO', '用户56', 'user56@example.com', '13800000056', 1, 1, '2026-02-24 18:09:09', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (154, 'user57', '$2a$12$zRVkNYTXt2RSA7l1NwwIJOuIkkmWmmphL1o50SWowrEIxB7e/hohS', '用户57', 'user57@example.com', '13800000057', 1, 1, '2026-02-24 18:09:09', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (155, 'user58', '$2a$12$3Rwi8Vn37xjzfhGPEU/BPeOw8jqHNkv6IKKVDtayLdrBPuCnTwJOO', '用户58', 'user58@example.com', '13800000058', 1, 1, '2026-02-24 18:09:10', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (156, 'user59', '$2a$12$8EIeBuahb8WEAs/OgLoen.PHD2pVSjjVBTETSQW8n/IimCGRUzDyu', '用户59', 'user59@example.com', '13800000059', 1, 1, '2026-02-24 18:09:10', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (157, 'user60', '$2a$12$b6SpD00NP8/rTWbcvN7bs.587blJN.JDv4Vv4Rv64nTEaHkcXK98G', '用户60', 'user60@example.com', '13800000060', 1, 1, '2026-02-24 18:09:10', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (158, 'user61', '$2a$12$GkbiE15F6dMouP1zxRrWVOFrGhukSue97ULxGuL8oqkwo16S/KVqm', '用户61', 'user61@example.com', '13800000061', 1, 1, '2026-02-24 18:09:10', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (159, 'user62', '$2a$12$czEyeY0qK8YoJAnyz1Ea9OXAmKAEAQW2imu2Cjg0uHHpPGm17.QLC', '用户62', 'user62@example.com', '13800000062', 1, 1, '2026-02-24 18:09:11', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (160, 'user63', '$2a$12$mwKaPsT2Y/S2hF0VAUYTC.m/WfEnBFxdVFL7BAWAk2WmCHiWvpIAa', '用户63', 'user63@example.com', '13800000063', 1, 1, '2026-02-24 18:09:11', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (161, 'user64', '$2a$12$mb6TR5gs.89h6nLFLIbvEuNkiYT5jWnJGz6tojcFHMczXXgQDwLX.', '用户64', 'user64@example.com', '13800000064', 1, 1, '2026-02-24 18:09:11', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (162, 'user65', '$2a$12$W0bEfxPx/bndMxLN.FCkE.//342lL3XVakD/9NWiuNHQFWXKk/7Fy', '用户65', 'user65@example.com', '13800000065', 1, 1, '2026-02-24 18:09:11', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (163, 'user66', '$2a$12$TFsbqdY.5OeRAsIJaLlpF.BJYg6j7S6RFGA2GLRFiKqf3ywSYXggO', '用户66', 'user66@example.com', '13800000066', 1, 1, '2026-02-24 18:09:12', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (164, 'user67', '$2a$12$qCzAHHcQWKt.Vkuybpbp/eswAfAhBLf9Gev.R8eZabxgLMxCBTYmG', '用户67', 'user67@example.com', '13800000067', 1, 1, '2026-02-24 18:09:12', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (165, 'user68', '$2a$12$vWL66766eRtVqXBnbP5fcec44UlhUmBIj0smv3tnSfhlHPg3GJeiu', '用户68', 'user68@example.com', '13800000068', 1, 1, '2026-02-24 18:09:12', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (166, 'user69', '$2a$12$4MZYNB21OR/2e4SPaJ1aH.HbpPCCgmYaPnZnui85/Qqz3wymnpQr6', '用户69', 'user69@example.com', '13800000069', 1, 1, '2026-02-24 18:09:13', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (167, 'user70', '$2a$12$xVModM2Ndcwpt43aQuXeVuJ3Xcq.LN9lrx0fTcB31wc8l5kO9T9Je', '用户70', 'user70@example.com', '13800000070', 1, 1, '2026-02-24 18:09:13', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (168, 'user71', '$2a$12$CDwj6/htlFt5tXf3dwMAXeQ2hqFJMRmg7yLCfnoNd07da0TvbpUK6', '用户71', 'user71@example.com', '13800000071', 1, 1, '2026-02-24 18:09:13', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (169, 'user72', '$2a$12$pHv66AvbvQNgZ/HQ2oHjXufyr0li.HP.J8wI2LJ2PLGAFINnhgaiu', '用户72', 'user72@example.com', '13800000072', 1, 1, '2026-02-24 18:09:13', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (170, 'user73', '$2a$12$uHdDwPiiRTusQrwmPCkIVuL7j1WVh8SywIilitRYd0StX8L7N3D9y', '用户73', 'user73@example.com', '13800000073', 1, 1, '2026-02-24 18:09:14', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (171, 'user74', '$2a$12$C2pR6GvU6KVaVCYnhJ/cqu4PZjQwsnzJ5pdAp0cW16qDeKTpXaFSS', '用户74', 'user74@example.com', '13800000074', 1, 1, '2026-02-24 18:09:14', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (172, 'user75', '$2a$12$RYGwpuihp9HP/WZ3xAmZluJcI/mdsRZGTC4f7Jiov/9p8kQHw1Aey', '用户75', 'user75@example.com', '13800000075', 1, 1, '2026-02-24 18:09:14', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (173, 'user76', '$2a$12$teih3FOFDD9b365VseslD.mQ.PU2nReL8N4zbaocjrHcDay3lac3m', '用户76', 'user76@example.com', '13800000076', 1, 1, '2026-02-24 18:09:14', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (174, 'user77', '$2a$12$UElOJZ7psiaRvwJJkLtld.8xLmU99ozCDhDAVFdleXEFXxzRR03vC', '用户77', 'user77@example.com', '13800000077', 1, 1, '2026-02-24 18:09:15', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (175, 'user78', '$2a$12$35KoK16iNm2AgYMMVlzHVeqbe/qBZMU0QjW4xmcX1Gyi6jUN3hCaW', '用户78', 'user78@example.com', '13800000078', 1, 1, '2026-02-24 18:09:15', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (176, 'user79', '$2a$12$KdWMiUAWZqvd91ZcVa9VLedOiMmqDl9Uyzlz9KcZNi.w2ZQvqubXm', '用户79', 'user79@example.com', '13800000079', 1, 1, '2026-02-24 18:09:15', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (177, 'user80', '$2a$12$aWSJHxbMUkNmvg98bpxJweFgOxXeW0g.Du/3PGWZWGUDHujtw5Kxq', '用户80', 'user80@example.com', '13800000080', 1, 1, '2026-02-24 18:09:16', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (178, 'user81', '$2a$12$t/fskcrZRttk0GQNZj.gNe6h8yAu45zCY.JJEqXQudNXlD/OEePSe', '用户81', 'user81@example.com', '13800000081', 1, 1, '2026-02-24 18:09:16', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (179, 'user82', '$2a$12$hl/o0skjtQRxT.OFTsv32uKiz871/UhNAbDB.VvDjMrp/Ed06rnkK', '用户82', 'user82@example.com', '13800000082', 1, 1, '2026-02-24 18:09:16', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (180, 'user83', '$2a$12$WTz9bayQ78dxLnzbCYV5MedCHNSS1OLeMe7fl/4.5WFr.27LIdhaK', '用户83', 'user83@example.com', '13800000083', 1, 1, '2026-02-24 18:09:16', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (181, 'user84', '$2a$12$gJYjZE2/lAfwBxssyjBsmuypnXq.6pmfzXb0dH5lvtpzsWCa5i/wy', '用户84', 'user84@example.com', '13800000084', 1, 1, '2026-02-24 18:09:17', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (182, 'user85', '$2a$12$EHyR2CtBbp7z3HTuaT3dZOoLrqJkVViU5A.EV6scj32o.9y/7xkx.', '用户85', 'user85@example.com', '13800000085', 1, 1, '2026-02-24 18:09:17', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (183, 'user86', '$2a$12$BgGkNc0AuJqPtuZIq33YS.IMY1Mh8RcB2zbrzABlR6U7Dk20iO.j.', '用户86', 'user86@example.com', '13800000086', 1, 1, '2026-02-24 18:09:17', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (184, 'user87', '$2a$12$rHIfGdrjzdLlIj7G.eT1.e/ImeRvP2rLmwwMvYCQXB9CoZ0Q3WwM2', '用户87', 'user87@example.com', '13800000087', 1, 1, '2026-02-24 18:09:17', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (185, 'user88', '$2a$12$W09X4PBfEa9weosPNNbg0uBvo4hBTFiMap64ASt33muQr7Etq.zGS', '用户88', 'user88@example.com', '13800000088', 1, 1, '2026-02-24 18:09:18', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (186, 'user89', '$2a$12$lgWckcGw0KDv8gOZ6ph4o.NGWUphh83LcPJFLt6lyqbKiuIu.kum2', '用户89', 'user89@example.com', '13800000089', 1, 1, '2026-02-24 18:09:18', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (187, 'user90', '$2a$12$2QdicAynLDypMa8CbXhnquAnVFMvLDgYxFAH2O.xnuG0EGZv4RRme', '用户90', 'user90@example.com', '13800000090', 1, 1, '2026-02-24 18:09:18', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (188, 'user91', '$2a$12$y29y5WfYhZL8FRStvi70bOKL6aZuQJ1hGLYR5XhMV6lW1xR7hyKki', '用户91', 'user91@example.com', '13800000091', 1, 1, '2026-02-24 18:09:19', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (189, 'user92', '$2a$12$Dw3mUqIUM05rT1SrrungDei.L3BDHTGbpSvztWGv//18RqyzSLJf6', '用户92', 'user92@example.com', '13800000092', 1, 1, '2026-02-24 18:09:19', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (190, 'user93', '$2a$12$ZZ4oVXpxkIkiE54vp0AZ2u.mM1vpAquGX69x8bTNqTBjSTbOa6eZa', '用户93', 'user93@example.com', '13800000093', 1, 1, '2026-02-24 18:09:19', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (191, 'user94', '$2a$12$2IIEqwIETkUmiUcDmnt3qO0IyVnBlOJqMS6garMzCZ0XwQa5jclCS', '用户94', 'user94@example.com', '13800000094', 1, 1, '2026-02-24 18:09:19', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (192, 'user95', '$2a$12$/CBCANZHT86DEBtjYmonq.vCj7nk.gawWyiYZypMx.JWcKZBif03m', '用户95', 'user95@example.com', '13800000095', 1, 1, '2026-02-24 18:09:20', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (193, 'user96', '$2a$12$7U9ivXUxFFa9Zm8Bivn1/.9qH.T.cXFAa9Eugd8w.sS6FGbbhHLX6', '用户96', 'user96@example.com', '13800000096', 1, 1, '2026-02-24 18:09:20', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (194, 'user97', '$2a$12$LdGvv6meDQzclI9rfAw29OrDRanqbR/TyL7LfoZSjx2ea5deKbHnq', '用户97', 'user97@example.com', '13800000097', 1, 1, '2026-02-24 18:09:20', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (195, 'user98', '$2a$12$llyF.DsGNOqK70NKnXowBusjGxf6eAkXG32h8um4oIb5nJQa7dGhC', '用户98', 'user98@example.com', '13800000098', 1, 1, '2026-02-24 18:09:20', '2026-02-24 18:20:23', 1, 1, 0, 0);
INSERT INTO `sys_user` VALUES (196, 'user99', '$2a$12$7q/REejxH7JVAyCstUb1s./MsLwULKX8UR7v0JiXAjLC2Vw5DqZMW', '用户99', 'user99@example.com', '13800000099', 1, 1, '2026-02-24 18:09:21', '2026-02-24 18:10:20', 1, 3, 0, 0);
INSERT INTO `sys_user` VALUES (197, 'user100', '$2a$12$Q6gCPlwODV5wQgKe2prCmepcecbcv3nMHIVwZQZb7SQQ6lT3DwKDS', '用户100', 'user100@example.com', '13800000100', 1, 1, '2026-02-24 18:09:21', '2026-02-24 18:10:23', 1, 3, 0, 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0:否 1:是',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_user_role_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_role_role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 207 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1, '2026-02-24 10:17:17', 0);
INSERT INTO `sys_user_role` VALUES (2, 2, 4, '2026-02-24 11:08:01', 0);
INSERT INTO `sys_user_role` VALUES (3, 3, 4, '2026-02-24 11:16:19', 1);
INSERT INTO `sys_user_role` VALUES (8, 3, 3, '2026-02-24 11:27:42', 0);
INSERT INTO `sys_user_role` VALUES (11, 1, 3, '2026-02-24 16:30:28', 0);
INSERT INTO `sys_user_role` VALUES (107, 98, 4, '2026-02-24 18:08:52', 0);
INSERT INTO `sys_user_role` VALUES (108, 99, 4, '2026-02-24 18:08:52', 0);
INSERT INTO `sys_user_role` VALUES (109, 100, 4, '2026-02-24 18:08:53', 0);
INSERT INTO `sys_user_role` VALUES (110, 101, 4, '2026-02-24 18:08:53', 0);
INSERT INTO `sys_user_role` VALUES (111, 102, 4, '2026-02-24 18:08:53', 0);
INSERT INTO `sys_user_role` VALUES (112, 103, 4, '2026-02-24 18:08:54', 0);
INSERT INTO `sys_user_role` VALUES (113, 104, 4, '2026-02-24 18:08:54', 0);
INSERT INTO `sys_user_role` VALUES (114, 105, 4, '2026-02-24 18:08:55', 0);
INSERT INTO `sys_user_role` VALUES (115, 106, 4, '2026-02-24 18:08:55', 0);
INSERT INTO `sys_user_role` VALUES (116, 107, 4, '2026-02-24 18:08:55', 0);
INSERT INTO `sys_user_role` VALUES (117, 108, 4, '2026-02-24 18:08:56', 0);
INSERT INTO `sys_user_role` VALUES (118, 109, 4, '2026-02-24 18:08:56', 0);
INSERT INTO `sys_user_role` VALUES (119, 110, 4, '2026-02-24 18:08:57', 0);
INSERT INTO `sys_user_role` VALUES (120, 111, 4, '2026-02-24 18:08:57', 0);
INSERT INTO `sys_user_role` VALUES (121, 112, 4, '2026-02-24 18:08:57', 0);
INSERT INTO `sys_user_role` VALUES (122, 113, 4, '2026-02-24 18:08:58', 0);
INSERT INTO `sys_user_role` VALUES (123, 114, 4, '2026-02-24 18:08:58', 0);
INSERT INTO `sys_user_role` VALUES (124, 115, 4, '2026-02-24 18:08:59', 0);
INSERT INTO `sys_user_role` VALUES (125, 116, 4, '2026-02-24 18:08:59', 0);
INSERT INTO `sys_user_role` VALUES (126, 117, 4, '2026-02-24 18:08:59', 0);
INSERT INTO `sys_user_role` VALUES (127, 118, 4, '2026-02-24 18:09:00', 0);
INSERT INTO `sys_user_role` VALUES (128, 119, 4, '2026-02-24 18:09:00', 0);
INSERT INTO `sys_user_role` VALUES (129, 120, 4, '2026-02-24 18:09:00', 0);
INSERT INTO `sys_user_role` VALUES (130, 121, 4, '2026-02-24 18:09:01', 0);
INSERT INTO `sys_user_role` VALUES (131, 122, 4, '2026-02-24 18:09:01', 0);
INSERT INTO `sys_user_role` VALUES (132, 123, 4, '2026-02-24 18:09:01', 0);
INSERT INTO `sys_user_role` VALUES (133, 124, 4, '2026-02-24 18:09:01', 0);
INSERT INTO `sys_user_role` VALUES (134, 125, 4, '2026-02-24 18:09:02', 0);
INSERT INTO `sys_user_role` VALUES (135, 126, 4, '2026-02-24 18:09:02', 0);
INSERT INTO `sys_user_role` VALUES (136, 127, 4, '2026-02-24 18:09:02', 0);
INSERT INTO `sys_user_role` VALUES (137, 128, 4, '2026-02-24 18:09:02', 0);
INSERT INTO `sys_user_role` VALUES (138, 129, 4, '2026-02-24 18:09:03', 0);
INSERT INTO `sys_user_role` VALUES (139, 130, 4, '2026-02-24 18:09:03', 0);
INSERT INTO `sys_user_role` VALUES (140, 131, 4, '2026-02-24 18:09:03', 0);
INSERT INTO `sys_user_role` VALUES (141, 132, 4, '2026-02-24 18:09:04', 0);
INSERT INTO `sys_user_role` VALUES (142, 133, 4, '2026-02-24 18:09:04', 0);
INSERT INTO `sys_user_role` VALUES (143, 134, 4, '2026-02-24 18:09:04', 0);
INSERT INTO `sys_user_role` VALUES (144, 135, 4, '2026-02-24 18:09:04', 0);
INSERT INTO `sys_user_role` VALUES (145, 136, 4, '2026-02-24 18:09:05', 0);
INSERT INTO `sys_user_role` VALUES (146, 137, 4, '2026-02-24 18:09:05', 0);
INSERT INTO `sys_user_role` VALUES (147, 138, 4, '2026-02-24 18:09:05', 0);
INSERT INTO `sys_user_role` VALUES (148, 139, 4, '2026-02-24 18:09:05', 0);
INSERT INTO `sys_user_role` VALUES (149, 140, 4, '2026-02-24 18:09:06', 0);
INSERT INTO `sys_user_role` VALUES (150, 141, 4, '2026-02-24 18:09:06', 0);
INSERT INTO `sys_user_role` VALUES (151, 142, 4, '2026-02-24 18:09:06', 0);
INSERT INTO `sys_user_role` VALUES (152, 143, 4, '2026-02-24 18:09:06', 0);
INSERT INTO `sys_user_role` VALUES (153, 144, 4, '2026-02-24 18:09:07', 0);
INSERT INTO `sys_user_role` VALUES (154, 145, 4, '2026-02-24 18:09:07', 0);
INSERT INTO `sys_user_role` VALUES (155, 146, 4, '2026-02-24 18:09:07', 0);
INSERT INTO `sys_user_role` VALUES (156, 147, 4, '2026-02-24 18:09:08', 0);
INSERT INTO `sys_user_role` VALUES (157, 148, 4, '2026-02-24 18:09:08', 0);
INSERT INTO `sys_user_role` VALUES (158, 149, 4, '2026-02-24 18:09:08', 0);
INSERT INTO `sys_user_role` VALUES (159, 150, 4, '2026-02-24 18:09:08', 0);
INSERT INTO `sys_user_role` VALUES (160, 151, 4, '2026-02-24 18:09:09', 0);
INSERT INTO `sys_user_role` VALUES (161, 152, 4, '2026-02-24 18:09:09', 0);
INSERT INTO `sys_user_role` VALUES (162, 153, 4, '2026-02-24 18:09:09', 0);
INSERT INTO `sys_user_role` VALUES (163, 154, 4, '2026-02-24 18:09:09', 0);
INSERT INTO `sys_user_role` VALUES (164, 155, 4, '2026-02-24 18:09:10', 0);
INSERT INTO `sys_user_role` VALUES (165, 156, 4, '2026-02-24 18:09:10', 0);
INSERT INTO `sys_user_role` VALUES (166, 157, 4, '2026-02-24 18:09:10', 0);
INSERT INTO `sys_user_role` VALUES (167, 158, 4, '2026-02-24 18:09:10', 0);
INSERT INTO `sys_user_role` VALUES (168, 159, 4, '2026-02-24 18:09:11', 0);
INSERT INTO `sys_user_role` VALUES (169, 160, 4, '2026-02-24 18:09:11', 0);
INSERT INTO `sys_user_role` VALUES (170, 161, 4, '2026-02-24 18:09:11', 0);
INSERT INTO `sys_user_role` VALUES (171, 162, 4, '2026-02-24 18:09:11', 0);
INSERT INTO `sys_user_role` VALUES (172, 163, 4, '2026-02-24 18:09:12', 0);
INSERT INTO `sys_user_role` VALUES (173, 164, 4, '2026-02-24 18:09:12', 0);
INSERT INTO `sys_user_role` VALUES (174, 165, 4, '2026-02-24 18:09:12', 0);
INSERT INTO `sys_user_role` VALUES (175, 166, 4, '2026-02-24 18:09:13', 0);
INSERT INTO `sys_user_role` VALUES (176, 167, 4, '2026-02-24 18:09:13', 0);
INSERT INTO `sys_user_role` VALUES (177, 168, 4, '2026-02-24 18:09:13', 0);
INSERT INTO `sys_user_role` VALUES (178, 169, 4, '2026-02-24 18:09:13', 0);
INSERT INTO `sys_user_role` VALUES (179, 170, 4, '2026-02-24 18:09:14', 0);
INSERT INTO `sys_user_role` VALUES (180, 171, 4, '2026-02-24 18:09:14', 0);
INSERT INTO `sys_user_role` VALUES (181, 172, 4, '2026-02-24 18:09:14', 0);
INSERT INTO `sys_user_role` VALUES (182, 173, 4, '2026-02-24 18:09:14', 0);
INSERT INTO `sys_user_role` VALUES (183, 174, 4, '2026-02-24 18:09:15', 0);
INSERT INTO `sys_user_role` VALUES (184, 175, 4, '2026-02-24 18:09:15', 0);
INSERT INTO `sys_user_role` VALUES (185, 176, 4, '2026-02-24 18:09:15', 0);
INSERT INTO `sys_user_role` VALUES (186, 177, 4, '2026-02-24 18:09:16', 0);
INSERT INTO `sys_user_role` VALUES (187, 178, 4, '2026-02-24 18:09:16', 0);
INSERT INTO `sys_user_role` VALUES (188, 179, 4, '2026-02-24 18:09:16', 0);
INSERT INTO `sys_user_role` VALUES (189, 180, 4, '2026-02-24 18:09:16', 0);
INSERT INTO `sys_user_role` VALUES (190, 181, 4, '2026-02-24 18:09:17', 0);
INSERT INTO `sys_user_role` VALUES (191, 182, 4, '2026-02-24 18:09:17', 0);
INSERT INTO `sys_user_role` VALUES (192, 183, 4, '2026-02-24 18:09:17', 0);
INSERT INTO `sys_user_role` VALUES (193, 184, 4, '2026-02-24 18:09:17', 0);
INSERT INTO `sys_user_role` VALUES (194, 185, 4, '2026-02-24 18:09:18', 0);
INSERT INTO `sys_user_role` VALUES (195, 186, 4, '2026-02-24 18:09:18', 0);
INSERT INTO `sys_user_role` VALUES (196, 187, 4, '2026-02-24 18:09:18', 0);
INSERT INTO `sys_user_role` VALUES (197, 188, 4, '2026-02-24 18:09:19', 0);
INSERT INTO `sys_user_role` VALUES (198, 189, 4, '2026-02-24 18:09:19', 0);
INSERT INTO `sys_user_role` VALUES (199, 190, 4, '2026-02-24 18:09:19', 0);
INSERT INTO `sys_user_role` VALUES (200, 191, 4, '2026-02-24 18:09:19', 0);
INSERT INTO `sys_user_role` VALUES (201, 192, 4, '2026-02-24 18:09:20', 0);
INSERT INTO `sys_user_role` VALUES (202, 193, 4, '2026-02-24 18:09:20', 0);
INSERT INTO `sys_user_role` VALUES (203, 194, 4, '2026-02-24 18:09:20', 0);
INSERT INTO `sys_user_role` VALUES (204, 195, 4, '2026-02-24 18:09:20', 0);
INSERT INTO `sys_user_role` VALUES (205, 196, 4, '2026-02-24 18:09:21', 0);
INSERT INTO `sys_user_role` VALUES (206, 197, 4, '2026-02-24 18:09:21', 0);

SET FOREIGN_KEY_CHECKS = 1;
