-- 创建附件表
CREATE TABLE IF NOT EXISTS gsms_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    display_name VARCHAR(255) COMMENT '显示名称(可修改)',
    file_path VARCHAR(500) NOT NULL COMMENT '存储路径/URL',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_type VARCHAR(50) NOT NULL COMMENT '文件类型/扩展名',
    mime_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    storage_type VARCHAR(20) NOT NULL DEFAULT 'local' COMMENT '存储类型: local/oss/cos/minio',

    -- 关联信息
    target_type VARCHAR(20) NOT NULL COMMENT '关联对象类型: task/requirement',
    target_id BIGINT NOT NULL COMMENT '关联对象ID',

    -- 上传者信息
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    uploader_name VARCHAR(50) COMMENT '上传者名称(冗余)',

    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-有效, 1-已删除',

    INDEX idx_target (target_type, target_id),
    INDEX idx_uploader (uploader_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';
