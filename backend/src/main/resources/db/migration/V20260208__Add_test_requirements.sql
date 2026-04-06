-- 为项目7（冰沟河智慧景区）创建测试需求数据
-- 用于测试看板表格功能
-- 使用 INSERT IGNORE 避免重复执行时出错

-- 插入需求任务（iteration_id=3 项目一期）
INSERT IGNORE INTO gsms_task (project_id, iteration_id, title, description, type, priority, status, parent_id, assignee_id, estimate_hours, plan_start_date, plan_end_date, create_user_id, update_user_id, create_time, update_time, is_deleted)
VALUES
(7, 3, '用户登录功能', '实现用户登录功能，包括用户名密码登录、记住密码等功能', 2, 2, 3, NULL, 18, 16.0, '2026-01-01', '2026-01-15', 1, 1, NOW(), NOW(), 0),
(7, 3, '数据报表模块', '实现各类数据统计和报表展示功能', 2, 2, 1, NULL, 18, 24.0, '2026-01-05', '2026-01-25', 1, 1, NOW(), NOW(), 0),
(7, 3, '系统设置模块', '实现用户权限管理、系统配置等功能', 2, 1, 1, NULL, NULL, 20.0, '2026-01-10', '2026-01-30', 1, 1, NOW(), NOW(), 0);

-- 插入需求任务（iteration_id=4 项目二期）
INSERT IGNORE INTO gsms_task (project_id, iteration_id, title, description, type, priority, status, parent_id, assignee_id, estimate_hours, plan_start_date, plan_end_date, create_user_id, update_user_id, create_time, update_time, is_deleted)
VALUES
(7, 4, '移动端适配', '实现移动端H5页面适配，支持手机访问', 2, 2, 1, NULL, 18, 40.0, '2026-02-01', '2026-02-20', 1, 1, NOW(), NOW(), 0),
(7, 4, '消息通知功能', '实现站内消息、邮件通知、短信通知功能', 2, 1, 1, NULL, NULL, 32.0, '2026-02-05', '2026-02-25', 1, 1, NOW(), NOW(), 0),
(7, 4, '数据导出功能', '支持将各类数据导出为 Excel、PDF 格式', 2, 2, 1, NULL, 18, 12.0, '2026-02-10', '2026-02-28', 1, 1, NOW(), NOW(), 0);

-- 将现有任务关联到需求（更新 parent_id）
-- 使用条件判断避免重复更新
-- 项目一期的任务关联到"用户登录功能"
UPDATE gsms_task SET parent_id = (SELECT id FROM (SELECT id FROM gsms_task WHERE title = '用户登录功能' AND project_id = 7 LIMIT 1) AS tmp)
WHERE id IN (8, 9, 10) AND iteration_id = 3 AND parent_id IS NULL;

-- 项目一期的任务关联到"数据报表模块"
UPDATE gsms_task SET parent_id = (SELECT id FROM (SELECT id FROM gsms_task WHERE title = '数据报表模块' AND project_id = 7 LIMIT 1) AS tmp)
WHERE id IN (32, 33) AND iteration_id = 3 AND parent_id IS NULL;

-- 项目二期的任务关联到"移动端适配"
UPDATE gsms_task SET parent_id = (SELECT id FROM (SELECT id FROM gsms_task WHERE title = '移动端适配' AND project_id = 7 LIMIT 1) AS tmp)
WHERE id IN (11, 12, 13, 14, 18, 30, 31, 34, 36) AND iteration_id = 4 AND parent_id IS NULL;
