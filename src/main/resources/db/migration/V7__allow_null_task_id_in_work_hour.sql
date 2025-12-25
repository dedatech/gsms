-- 将工时记录表的 task_id 改为可为空，以支持只按项目记工时

ALTER TABLE `gsms_work_hour`
    MODIFY COLUMN `task_id` bigint(20) NULL COMMENT '任务ID';
