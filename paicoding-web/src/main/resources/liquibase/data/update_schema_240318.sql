-- ----------------------------
--  Table structure for `feed`
-- ----------------------------
DROP TABLE IF EXISTS `feed`;
CREATE TABLE `feed`
(
    `id`            bigint(16) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
    `topic_id`      bigint unsigned NOT NULL DEFAULT '0' COMMENT '主题id',
    `content`       varchar(512) not null default '' comment '发布内容',
    `extend`        varchar(512) not null default '' comment '扩展信息,存储发布内容的解析信息',
    `img`           varchar(512) not null default '' comment '图片,英文逗号分割',
    `type`          tinyint(4) not null default '0' comment '信息流类型: 0 普通动态 1 转发/发布文章 2 转发动态 3 转发评论 4 转发专栏 5 外部链接',
    `ref_id`        bigint unsigned not null default '0' comment '被转发的文章/评论/动态主键',
    `ref_url`       varchar(512) not null default '' comment '引用的信息，站内引用时不存；外部引用则存储外部url',
    `view`          tinyint      not null default '0' comment '0: 全部可见 1 登录可见 2 粉丝可见 3 自己可见',
    `praise_count`  int          not null default '0' comment '点赞数',
    `comment_count` int          not null default '0' comment '评论数',
    `status`        tinyint      not null default '0' comment '0: 未发布 1: 已发布  2:审核中',
    `deleted`       tinyint      NOT NULL DEFAULT '0' COMMENT '0: 未删除 1: 已删除',
    `create_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY             `idx_user` (`user_id`),
    KEY             `idx_type_ref_id` (`type`, `ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广场动态信息流';


DROP TABLE IF EXISTS `feed_topic`;
CREATE TABLE `feed_topic`
(
    `id`          bigint(16) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `topic`       varchar(128) not null default '#' comment '发布内容',
    `cnt`         int          not null default '0' comment '总计数',
    `deleted`     tinyint      NOT NULL DEFAULT '0' COMMENT '0: 未删除 1: 已删除',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `u_topic` (`topic`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广场话题圈子';


-- 添加feed相关的字典信息
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedTypeEnum', '0', '普通动态', 1);
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedTypeEnum', '1', '转发文章的动态', 2);
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedTypeEnum', '2', '转发的动态', 3);
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedTypeEnum', '3', '转发的评论', 4);
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedTypeEnum', '4', '外部链接的动态', 5);


insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedViewEnum', '0', '所有人可见', 1);
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedViewEnum', '1', '登录可见', 2);
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedViewEnum', '2', '粉丝可见', 3);
insert into dict_common(`type_code`, `dict_code`, `dict_desc`, `sort_no`)
values ('FeedViewEnum', '3', '自己可见', 4);
