-- 专栏文章分组；主要用于前台列表显示时，对文章进行聚合
CREATE TABLE `column_article_group`
(
    `id`          bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `column_id`   int unsigned NOT NULL DEFAULT '0' COMMENT '专栏ID',
    `title`       varchar   NOT NULL DEFAULT '' COMMENT '分组名',
    `section`     int unsigned NOT NULL DEFAULT '0' COMMENT '分组顺序，越小越靠前',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `column_id` (`column_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4  COMMENT='专栏文章分组';

alter table column_article
    add column group_id bigint unsigned not null default 0 comment '文章对应的分组id，为0时归属在未分组的集合中';