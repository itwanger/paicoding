-- forum.article definition

CREATE TABLE `article`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      int unsigned NOT NULL COMMENT '用户ID',
    `article_type` tinyint      NOT NULL DEFAULT '1' COMMENT '文章类型：1-博文，2-问答',
    `title`        varchar(120) NOT NULL COMMENT '文章标题',
    `short_title`  varchar(120) NOT NULL COMMENT '短标题',
    `picture`      varchar(128) NOT NULL DEFAULT '' COMMENT '文章头图',
    `summary`      varchar(300) NOT NULL DEFAULT '' COMMENT '文章摘要',
    `category_id`  int unsigned NOT NULL DEFAULT '0' COMMENT '类目ID',
    `source`       tinyint      NOT NULL DEFAULT '1' COMMENT '来源：1-转载，2-原创，3-翻译',
    `source_url`   varchar(128) NOT NULL DEFAULT '1' COMMENT '原文链接',
    `status`       tinyint      NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `deleted`      tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='文章表';


-- forum.article_detail definition

CREATE TABLE `article_detail`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  int unsigned NOT NULL COMMENT '文章ID',
    `version`     int unsigned NOT NULL COMMENT '版本号',
    `content`     text COMMENT '文章内容',
    `deleted`     tinyint   NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_article_version` (`article_id`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='文章详情表';


-- forum.article_tag definition

CREATE TABLE `article_tag`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  int unsigned NOT NULL DEFAULT '0' COMMENT '文章ID',
    `tag_id`      int       NOT NULL DEFAULT '0' COMMENT '标签',
    `deleted`     tinyint   NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='文章标签映射';


-- forum.category definition

CREATE TABLE `category`
(
    `id`            int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` varchar(64) NOT NULL COMMENT '类目名称',
    `status`        tinyint     NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `deleted`       tinyint     NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='类目管理表';


-- forum.comment definition

CREATE TABLE `comment`
(
    `id`                int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`        int unsigned NOT NULL COMMENT '文章ID',
    `user_id`           int unsigned NOT NULL COMMENT '用户ID',
    `content`           varchar(300) NOT NULL DEFAULT '' COMMENT '评论内容',
    `top_comment_id`    int unsigned NOT NULL DEFAULT '0' COMMENT '顶级评论ID',
    `parent_comment_id` int unsigned NOT NULL DEFAULT '0' COMMENT '父评论ID',
    `deleted`           tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY                 `idx_article_id_parent_comment_id` (`article_id`, `parent_comment_id`),
    KEY                 `idx_user_id` (`user_id`),
    KEY                 `idx_article_id` (`top_comment_id`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='评论表';


-- forum.tag definition

CREATE TABLE `tag`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `tag_name`    varchar(120) NOT NULL COMMENT '标签名称',
    `tag_type`    tinyint      NOT NULL DEFAULT '1' COMMENT '标签类型：1-系统标签，2-自定义标签',
    `category_id` int unsigned NOT NULL DEFAULT '0' COMMENT '类目ID',
    `status`      tinyint      NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `deleted`     tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='标签管理表';

-- forum.read_count 访问计数

CREATE TABLE `read_count`
(
    `id`            int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id`   int unsigned NOT NULL COMMENT '文档ID（文章/评论）',
    `document_type` tinyint   NOT NULL DEFAULT '1' COMMENT '文档类型：1-文章，2-评论',
    `cnt`           int unsigned NOT NULL COMMENT '访问计数',
    `create_time`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_document_id_type` (`document_id`,`document_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='计数表';

-- forum.`user` definition

CREATE TABLE `user`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `third_account_id` varchar(128) NOT NULL DEFAULT '' COMMENT '第三方用户ID',
    `login_type`       tinyint      NOT NULL DEFAULT '0' COMMENT '登录方式: 0-微信登录，1-账号密码登录',
    `deleted`          tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY                `key_third_account_id` (`third_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='用户登录表';


-- forum.user_foot definition

CREATE TABLE `user_foot`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          int unsigned NOT NULL COMMENT '用户ID',
    `document_id`      int unsigned NOT NULL COMMENT '文档ID（文章/评论）',
    `document_type`    tinyint   NOT NULL DEFAULT '1' COMMENT '文档类型：1-文章，2-评论',
    `document_user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '发布该文档的用户ID',
    `collection_stat`  tinyint unsigned NOT NULL DEFAULT '0' COMMENT '收藏状态: 0-未收藏，1-已收藏，2-取消收藏',
    `read_stat`        tinyint unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未读，1-已读',
    `comment_stat`     tinyint unsigned NOT NULL DEFAULT '0' COMMENT '评论状态: 0-未评论，1-已评论，2-删除评论',
    `praise_stat`      tinyint unsigned NOT NULL DEFAULT '0' COMMENT '点赞状态: 0-未点赞，1-已点赞，2-取消点赞',
    `create_time`      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_document` (`user_id`,`document_id`,`document_type`,`comment_id`),
    KEY                `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='用户足迹表';


-- forum.user_info definition

CREATE TABLE `user_info`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     int unsigned NOT NULL COMMENT '用户ID',
    `user_name`   varchar(50)   NOT NULL DEFAULT '' COMMENT '用户名',
    `photo`       varchar(128)  NOT NULL DEFAULT '' COMMENT '用户图像',
    `position`    varchar(50)   NOT NULL DEFAULT '' COMMENT '职位',
    `company`     varchar(50)   NOT NULL DEFAULT '' COMMENT '公司',
    `profile`     varchar(225)  NOT NULL DEFAULT '' COMMENT '个人简介',
    `extend`      varchar(1024) NOT NULL DEFAULT '' COMMENT '扩展字段',
    `deleted`     tinyint       NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `key_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='用户个人信息表';


-- forum.user_relation definition

CREATE TABLE `user_relation`
(
    `id`             int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        int unsigned NOT NULL COMMENT '用户ID',
    `follow_user_id` int unsigned NOT NULL COMMENT '关注用户ID',
    `follow_state`   tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未关注，1-已关注，2-取消关注',
    `create_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`,`follow_user_id`),
    KEY              `key_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='用户关系表';

-- 消息通知列表

CREATE TABLE `notify_msg`
(
    `id`              int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `related_id`      int unsigned NOT NULL default '0' COMMENT '关联的主键',
    `notify_user_id`  int unsigned NOT NULL default '0' COMMENT '通知的用户id',
    `operate_user_id` int unsigned NOT NULL default '0' COMMENT '触发这个通知的用户id',
    `msg`             varchar(1024) NOT NULL default '' COMMENT '消息内容',
    `type`            tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '类型: 0-默认，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-系统',
    `state`           tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未读，1-已读',
    `create_time`     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY               `key_notify_user_id_type_state` (`notify_user_id`, `type`, `state`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='消息通知列表';


-- 专栏
CREATE TABLE `column_info`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '专栏ID',
    `column_name` varchar(64)  NOT NULL default '' COMMENT '专栏名',
    `user_id`     int unsigned not null default '0' comment '作者id',
    `summary`     varchar(256) NOT NULL default '' COMMENT '专栏简述',
    `cover`       varchar(128) NOT NULL default '' COMMENT '专栏封面',
    `state`       tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '状态: 0-审核中，1-连载，2-完结',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='专栏';

CREATE TABLE `column_article`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `column_id`   int unsigned NOT NULL default '0' COMMENT '专栏ID',
    `article_id`  int unsigned NOT NULL default '0' COMMENT '文章ID',
    `order`       int unsigned NOT NULL default '0' COMMENT '排序，越小越靠前',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `column_id` (`column_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='专栏文章列表';

-- 变更记录
# alter table user_relation
#     add `follow_state` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未关注，1-已关注，2-取消关注';
# alter table comment change parent_comment_id `parent_comment_id` int unsigned NOT NULL DEFAULT '0' COMMENT '父评论ID';
# alter table user_foot add `document_user_id` int unsigned NOT NULL COMMENT '发布该文档的用户ID';
# alter table user_foot
#     add `comment_id` int unsigned NOT NULL DEFAULT '0' COMMENT '当前发起评论的ID';
# alter table user_foot change praise_stat `praise_stat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '点赞状态: 0-未点赞，1-已点赞';
# alter table user_foot change collection_stat `collection_stat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '收藏状态: 0-未收藏，1-已收藏';
# alter table user_foot change comment_stat `comment_stat` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '评论状态: 0-未评论，1-已评论';
# drop index idx_user_document on user_foot;
# alter table user_foot add unique index `idx_user_document` (`user_id`,`document_id`,`document_type`,`comment_id`);
# alter table user_foot rename column doucument_id to document_id;
# alter table user_foot rename column doucument_type to document_type;
# alter table user_foot rename column doucument_user_id to document_user_id;
-- 删除用户足迹中的评论id
# alter table user_foot  drop column comment_id;
# alter table `comment` add column `top_comment_id` int not null default '0' comment '顶级评论ID'  after `content`;
# alter table `comment` add column `deleted` tinyint not null default '0' comment '0有效1删除'  after `parent_comment_id`;