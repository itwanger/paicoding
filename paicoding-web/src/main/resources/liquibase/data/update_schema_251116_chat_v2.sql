-- Chat V2: 会话表和消息表
-- 对标 DeepExtract 的 PostgreSQL 结构，适配 MySQL

-- ============================================================================
-- 会话表：存储 LLM 对话会话
-- ============================================================================
CREATE TABLE IF NOT EXISTS `chat_history` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `conversation_id` VARCHAR(200) NOT NULL COMMENT '会话ID (UUID)',
    `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    `title` VARCHAR(500) DEFAULT NULL COMMENT '对话标题',
    `title_generated_by` VARCHAR(20) DEFAULT 'auto' COMMENT '标题生成方式: auto/user/llm',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '删除标记: 0-正常 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_user_model` (`conversation_id`, `user_id`, `model_name`),
    KEY `idx_user_update` (`user_id`, `update_time` DESC),
    KEY `idx_user_deleted` (`user_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLM 对话会话表';

-- ============================================================================
-- 消息表：存储会话中的消息
-- ============================================================================
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `history_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '会话ID (chat_history.id)',
    `role` VARCHAR(20) NOT NULL COMMENT '角色: user/assistant/system/tool',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `metadata_json` JSON DEFAULT NULL COMMENT '元数据 (JSON格式)',
    `sequence_num` INT(11) NOT NULL COMMENT '消息序号',
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间戳',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) DEFAULT 0 COMMENT '删除标记: 0-正常 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_history_seq` (`history_id`, `sequence_num`),
    KEY `idx_history_timestamp` (`history_id`, `timestamp` DESC),
    CONSTRAINT `fk_message_history` FOREIGN KEY (`history_id`)
        REFERENCES `chat_history` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LLM 对话消息表';

-- ============================================================================
-- 全文搜索索引 (可选，MySQL 5.7+ 支持中文 ngram)
-- ============================================================================
-- 为 chat_history 的 title 创建全文索引
ALTER TABLE `chat_history`
ADD FULLTEXT INDEX `ft_title` (`title`) WITH PARSER ngram;

-- 为 chat_message 的 content 创建全文索引
ALTER TABLE `chat_message`
ADD FULLTEXT INDEX `ft_content` (`content`) WITH PARSER ngram;
