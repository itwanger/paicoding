-- Knowledge Base schema

CREATE TABLE IF NOT EXISTS `knowledge_category` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id` BIGINT(20) UNSIGNED DEFAULT 0 COMMENT '父分类ID，一级为0',
    `level` TINYINT NOT NULL COMMENT '分类层级:1/2',
    `category_name` VARCHAR(120) NOT NULL COMMENT '分类名',
    `slug` VARCHAR(160) DEFAULT NULL COMMENT '分类slug',
    `rank` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0下线,1上线',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_parent_name_deleted` (`parent_id`, `category_name`, `deleted`),
    KEY `idx_level_status` (`level`, `status`, `deleted`),
    KEY `idx_parent_rank` (`parent_id`, `rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库分类表';

CREATE TABLE IF NOT EXISTS `knowledge_doc` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '所属二级分类ID',
    `title` VARCHAR(255) NOT NULL COMMENT '标题',
    `description` VARCHAR(1000) DEFAULT NULL COMMENT '文档描述，供LLM理解',
    `content_md` LONGTEXT NOT NULL COMMENT 'Markdown正文',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态:0下线,1上线',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `create_user_id` BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '创建人',
    `update_user_id` BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '更新人',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_status` (`category_id`, `status`, `deleted`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

CREATE TABLE IF NOT EXISTS `knowledge_tag` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tag_name` VARCHAR(80) NOT NULL COMMENT '标签名',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name_deleted` (`tag_name`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库标签表';

CREATE TABLE IF NOT EXISTS `knowledge_doc_tag_rel` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `doc_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '文档ID',
    `tag_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '标签ID',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_doc_tag` (`doc_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档标签关系表';

CREATE TABLE IF NOT EXISTS `knowledge_change_task` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_type` VARCHAR(20) NOT NULL COMMENT '任务类型:CREATE/UPDATE',
    `target_doc_id` BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '目标文档ID',
    `payload_json` LONGTEXT NOT NULL COMMENT '变更内容JSON',
    `llm_prompt` TEXT DEFAULT NULL COMMENT '触发问题/提示词',
    `llm_answer` LONGTEXT DEFAULT NULL COMMENT '模型输出原文',
    `tool_trace_json` LONGTEXT DEFAULT NULL COMMENT '工具调用轨迹',
    `proposer_user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '提案人',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    `reviewer_user_id` BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '审核人',
    `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status_create` (`status`, `create_time`),
    KEY `idx_target_doc` (`target_doc_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库变更审核任务表';

ALTER TABLE `knowledge_doc`
    ADD FULLTEXT INDEX `ft_title_desc_content` (`title`, `description`, `content_md`) WITH PARSER ngram;

INSERT INTO `knowledge_category` (`parent_id`, `level`, `category_name`, `slug`, `rank`, `status`, `deleted`)
SELECT 0, 1, 'MySQL', 'mysql', 100, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `knowledge_category` WHERE `parent_id`=0 AND `level`=1 AND `category_name`='MySQL' AND `deleted`=0
);

INSERT INTO `knowledge_category` (`parent_id`, `level`, `category_name`, `slug`, `rank`, `status`, `deleted`)
SELECT 0, 1, 'Redis', 'redis', 90, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `knowledge_category` WHERE `parent_id`=0 AND `level`=1 AND `category_name`='Redis' AND `deleted`=0
);

INSERT INTO `knowledge_tag` (`tag_name`, `status`, `deleted`)
SELECT '高频面试题', 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `knowledge_tag` WHERE `tag_name`='高频面试题' AND `deleted`=0
);

INSERT INTO `knowledge_tag` (`tag_name`, `status`, `deleted`)
SELECT '生产实战', 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `knowledge_tag` WHERE `tag_name`='生产实战' AND `deleted`=0
);
