-- H2数据库兼容的表结构初始化脚本
-- 基于项目gsms_ddl.sql和update_ddl.sql

-- 用户表
CREATE TABLE `user` (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  nickname VARCHAR(50),
  email VARCHAR(100),
  phone VARCHAR(20),
  department_id BIGINT,
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 部门表
CREATE TABLE department (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  level INT DEFAULT 1,
  sort INT DEFAULT 0,
  remark VARCHAR(200),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 项目表
CREATE TABLE project (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(50) NOT NULL,
  description VARCHAR(500),
  manager_id BIGINT NOT NULL,
  status TINYINT DEFAULT 1,
  plan_start_date DATE,
  plan_end_date DATE,
  actual_start_date DATE,
  actual_end_date DATE,
  create_user_id BIGINT NOT NULL,
  update_user_id BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 项目成员表
CREATE TABLE project_member (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role_type TINYINT NOT NULL DEFAULT 1,
  create_user_id BIGINT NOT NULL,
  update_user_id BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE(project_id, user_id)
);

-- 迭代表
CREATE TABLE iteration (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  status TINYINT DEFAULT 1,
  plan_start_date DATE,
  plan_end_date DATE,
  actual_start_date DATE,
  actual_end_date DATE,
  create_user_id BIGINT NOT NULL,
  update_user_id BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 任务表
CREATE TABLE task (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  project_id BIGINT NOT NULL,
  iteration_id BIGINT,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  type TINYINT DEFAULT 1,
  priority TINYINT DEFAULT 2,
  assignee_id BIGINT,
  status TINYINT DEFAULT 1,
  plan_start_date DATE,
  plan_end_date DATE,
  actual_start_date DATE,
  actual_end_date DATE,
  estimate_hours DECIMAL(5,2) DEFAULT 0.00,
  create_user_id BIGINT NOT NULL,
  update_user_id BIGINT,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 工时记录表
CREATE TABLE work_hour (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  update_user_id BIGINT,
  project_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  hours DECIMAL(5,2) NOT NULL,
  content VARCHAR(500),
  status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  code VARCHAR(50) NOT NULL,
  description VARCHAR(200),
  role_level TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_user_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, role_id)
);

-- 权限表
CREATE TABLE permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  code VARCHAR(100) NOT NULL,
  description VARCHAR(200),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE role_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  create_user_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(role_id, permission_id)
);

-- 操作日志表
CREATE TABLE operation_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL,
  operation VARCHAR(100) NOT NULL,
  method VARCHAR(100) NOT NULL,
  params VARCHAR(500),
  ip VARCHAR(50),
  uri VARCHAR(200) NOT NULL,
  time INT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);