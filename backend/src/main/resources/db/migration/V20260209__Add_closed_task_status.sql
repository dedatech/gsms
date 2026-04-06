-- 添加任务状态 CLOSED (已关闭)
-- 状态值：6

-- 说明：任务完成后的最终状态是 CLOSED（已关闭）
-- 普通任务工作流：TODO(1) -> IN_PROGRESS(2) -> DONE(3) -> CLOSED(6)
-- 缺陷工作流：TODO(1) -> IN_PROGRESS(2) -> TESTING(4) -> DONE(3) -> CLOSED(6)
--                         ^                                  |
--                         |----------- REOPENED(5) ----------|

-- 注释更新（仅供参考，实际枚举值在 Java 代码中维护）
-- 任务状态表为枚举类型，无需修改数据库结构
-- 新增状态在应用层通过 TaskStatus 枚举自动支持
