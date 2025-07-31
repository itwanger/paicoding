-- 专栏文章分组；主要用于前台列表显示时，对文章进行聚合
CREATE TABLE `column_article_group`
(
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `column_id`   bigint unsigned NOT NULL DEFAULT '0' COMMENT '专栏ID',
    `parent_group_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '父分组id',
    `title`       varchar(128)   NOT NULL DEFAULT '' COMMENT '分组名',
    `section`     bigint unsigned NOT NULL DEFAULT '0' COMMENT '分组顺序，越小越靠前；第一层小于1000，第二层为上一层 * 1000 + 计数',
    `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `column_id` (`column_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4  COMMENT='专栏文章分组';

alter table column_article
    add column group_id bigint unsigned not null default '0' comment '文章对应的分组id，为1时归属在未分组的集合中';


INSERT INTO column_article_group
(id, column_id, parent_group_id, title, `section`, deleted, create_time, update_time)
VALUES(1, 0, 0, '未分组', 1, 0, '2025-07-30 15:35:14', '2025-07-30 15:36:33');

update column_article_group set `id` = '0' where `id` = 1;