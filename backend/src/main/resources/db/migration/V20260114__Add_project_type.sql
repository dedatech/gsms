-- 添加项目类型字段
ALTER TABLE `gsms_project`
ADD COLUMN `project_type` TINYINT DEFAULT 1 COMMENT '项目类型 1:日程型 2:中大型（含迭代）'
AFTER `code`;

-- 智能迁移：根据是否有关联迭代判断项目类型
UPDATE gsms_project p
SET project_type = CASE
  WHEN EXISTS (
    SELECT 1 FROM gsms_iteration i
    WHERE i.project_id = p.id
    AND i.is_deleted = 0
  ) THEN 2  -- 有迭代则设为中大型项目
  ELSE 1  -- 无迭代则设为日程型项目
END
WHERE project_type IS NULL;

-- 添加索引优化查询
CREATE INDEX idx_project_type ON gsms_project(project_type);
