-- 表重命名：业务表加 gsms_ 前缀，系统表加 sys_ 前缀

-- 业务表
RENAME TABLE `user` TO `gsms_user`;
RENAME TABLE `department` TO `gsms_department`;
RENAME TABLE `project` TO `gsms_project`;
RENAME TABLE `project_member` TO `gsms_project_member`;
RENAME TABLE `iteration` TO `gsms_iteration`;
RENAME TABLE `task` TO `gsms_task`;
RENAME TABLE `work_hour` TO `gsms_work_hour`;

-- 系统表
RENAME TABLE `role` TO `sys_role`;
RENAME TABLE `user_role` TO `sys_user_role`;
RENAME TABLE `permission` TO `sys_permission`;
RENAME TABLE `role_permission` TO `sys_role_permission`;
RENAME TABLE `operation_log` TO `sys_operation_log`;
