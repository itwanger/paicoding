-- 添加 url_slug 字段到 article 表，用于SEO友好的URL
-- Author: Claude
-- Date: 2025-11-10

ALTER TABLE `article` ADD COLUMN `url_slug` varchar(200) NOT NULL DEFAULT '' COMMENT 'URL友好的文章标识，用于SEO优化' AFTER `short_title`;

-- 为 url_slug 添加索引，提高查询性能
ALTER TABLE `article` ADD KEY `idx_url_slug` (`url_slug`);