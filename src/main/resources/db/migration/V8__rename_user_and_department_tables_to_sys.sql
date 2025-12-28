-- 将用户表和部门表从gsms_前缀重命名为sys_前缀（系统表）
-- 因为用户表和部门表都属于系统基础表，而不是业务表

-- 将用户表从gsms_user重命名为sys_user
RENAME TABLE `gsms_user` TO `sys_user`;

-- 将部门表从gsms_department重命名为sys_department
RENAME TABLE `gsms_department` TO `sys_department`;