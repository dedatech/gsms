-- 数据库表结构更新 DDL
-- 用于添加 update_user_id 字段，实现更新人追踪

USE gsms;

-- 项目表：添加更新人字段
ALTER TABLE `project` 
ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `create_user_id`;

-- 任务表：添加更新人字段
ALTER TABLE `task` 
ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `create_user_id`;

-- 工时记录表：添加更新人字段
ALTER TABLE `work_hour` 
ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `user_id`;

-- 迭代表：添加更新人字段
ALTER TABLE `iteration` 
ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `create_user_id`;

-- 项目成员表：添加更新人字段
ALTER TABLE `project_member` 
ADD COLUMN `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新人ID' AFTER `create_user_id`;
