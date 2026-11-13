-- 1. article 新增当前默认正式版本指针（NULL 表示未启用多版本管理，兼容全部存量文章）
ALTER TABLE `article`
    ADD COLUMN `current_edition_id` bigint unsigned NULL DEFAULT NULL COMMENT '当前默认公开的正式版本ID(关联article_edition.id，NULL表示未启用多版本)' AFTER `url_slug`,
    ADD INDEX `idx_current_edition_id` (`current_edition_id`);

-- 2. article_detail 新增正式版归属、元数据快照与首次公开时间
ALTER TABLE `article_detail`
    ADD COLUMN `edition_id` bigint unsigned NULL DEFAULT NULL COMMENT '归属正式版本ID(关联article_edition.id)' AFTER `article_id`,
    ADD COLUMN `metadata_snapshot` text NULL COMMENT '该快照保存时的标题/短标题/摘要/封面JSON快照' AFTER `content`,
    ADD COLUMN `first_published_at` datetime NULL DEFAULT NULL COMMENT '首次公开发布时间(NULL表示内部草稿，禁止读者端接口读取)' AFTER `metadata_snapshot`,
    ADD INDEX `idx_article_edition_detail` (`article_id`, `edition_id`, `deleted`);

-- 3. 新建面向读者的文章正式版本表 article_edition
CREATE TABLE IF NOT EXISTS `article_edition` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` bigint unsigned NOT NULL COMMENT '文章ID(关联article.id)',
    `edition_no` int unsigned NOT NULL COMMENT '文章内单调递增正式版序号(1, 2, ...)',
    `edition_code` varchar(32) NOT NULL COMMENT '正式版本短编码(如 v1、v2，限小写字母、数字、连字符)',
    `edition_name` varchar(80) NOT NULL COMMENT '版本展示名称(如 v1 初版·零基础直觉版、v2 重构实战版)',
    `audience_tag` varchar(120) NOT NULL DEFAULT '' COMMENT '受众定位标签(如 适合零基础建立直觉)',
    `guide_tip` varchar(500) NOT NULL DEFAULT '' COMMENT '标题下方分流引导提示语',
    `change_note` text NULL COMMENT '版本更新说明',
    `published_detail_id` bigint unsigned NULL DEFAULT NULL COMMENT '当前对外公开的article_detail.id',
    `draft_detail_id` bigint unsigned NULL DEFAULT NULL COMMENT '待发布或待审核的草稿article_detail.id',
    `draft_status` tinyint NOT NULL DEFAULT '0' COMMENT '草稿状态：0-无草稿，1-草稿编辑中，2-待审核，3-审核驳回',
    `state` tinyint NOT NULL DEFAULT '0' COMMENT '正式版状态：0-未发布，1-已公开发布，2-已撤回',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-正常，1-已删除',
    `first_published_at` datetime NULL DEFAULT NULL COMMENT '该正式版首次公开发布时间',
    `last_published_at` datetime NULL DEFAULT NULL COMMENT '该正式版最近一次修订发布时间',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_edition_no` (`article_id`, `edition_no`),
    UNIQUE KEY `uk_article_edition_code` (`article_id`, `edition_code`),
    KEY `idx_article_state` (`article_id`, `state`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章正式版本表';

-- 4. comment 新增正式版本归属与原始公开正文快照关联
ALTER TABLE `comment`
    ADD COLUMN `edition_id` bigint unsigned NULL DEFAULT NULL COMMENT '所属正式版本ID(关联article_edition.id，早期未归属讨论为NULL)' AFTER `article_id`,
    ADD COLUMN `article_detail_id` bigint unsigned NULL DEFAULT NULL COMMENT '发言或划线时所依据的公开article_detail.id' AFTER `edition_id`,
    ADD INDEX `idx_article_edition_top` (`article_id`, `edition_id`, `deleted`, `top_comment_id`, `id`),
    ADD INDEX `idx_top_comment_deleted` (`top_comment_id`, `deleted`, `id`);
