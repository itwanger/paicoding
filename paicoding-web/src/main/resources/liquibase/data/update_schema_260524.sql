ALTER TABLE `column_info`
    ADD COLUMN `url_slug` varchar(100) NOT NULL DEFAULT '' COMMENT 'URL友好的教程标识，用于SEO优化' AFTER `column_name`;
