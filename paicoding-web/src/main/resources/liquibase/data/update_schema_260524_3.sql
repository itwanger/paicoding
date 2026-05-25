ALTER TABLE `column_info`
    ADD COLUMN `readme_article_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '教程说明页文章ID' AFTER `url_slug`;

ALTER TABLE `column_info`
    ADD KEY `idx_readme_article_id` (`readme_article_id`);
