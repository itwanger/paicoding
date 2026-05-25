ALTER TABLE `column_article`
    ADD COLUMN `preview_percent` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '未解锁试看比例，0表示使用全局试看字数配置' AFTER `read_type`;
