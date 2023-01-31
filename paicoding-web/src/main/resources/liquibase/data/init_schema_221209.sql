-- forum.article definition

CREATE TABLE `article`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      int unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
    `article_type` tinyint      NOT NULL DEFAULT '1' COMMENT '文章类型：1-博文，2-问答',
    `title`        varchar(120) NOT NULL DEFAULT '' COMMENT '文章标题',
    `short_title`  varchar(120) NOT NULL DEFAULT '' COMMENT '短标题',
    `picture`      varchar(128) NOT NULL DEFAULT '' COMMENT '文章头图',
    `summary`      varchar(300) NOT NULL DEFAULT '' COMMENT '文章摘要',
    `category_id`  int unsigned NOT NULL DEFAULT '0' COMMENT '类目ID',
    `source`       tinyint      NOT NULL DEFAULT '1' COMMENT '来源：1-转载，2-原创，3-翻译',
    `source_url`   varchar(128) NOT NULL DEFAULT '1' COMMENT '原文链接',
    `offical_stat` int unsigned NOT NULL DEFAULT '0' COMMENT '官方状态：0-非官方，1-官方',
    `topping_stat` int unsigned NOT NULL DEFAULT '0' COMMENT '置顶状态：0-不置顶，1-置顶',
    `cream_stat`   int unsigned NOT NULL DEFAULT '0' COMMENT '加精状态：0-不加精，1-加精',
    `status`       tinyint      NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `deleted`      tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_category_id` (`category_id`),
    KEY `idx_title` (`title`),
    KEY `idx_short_title` (`short_title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='文章表';


-- forum.article_detail definition

CREATE TABLE `article_detail`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  int unsigned NOT NULL DEFAULT '0' COMMENT '文章ID',
    `version`     int unsigned NOT NULL DEFAULT '0' COMMENT '版本号',
    `content`     longtext COMMENT '文章内容',
    `deleted`     tinyint   NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_article_version` (`article_id`,`version`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='文章详情表';


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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='文章标签映射';


-- forum.banner definition

CREATE TABLE `banner`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `banner_name` varchar(64)  NOT NULL DEFAULT '' COMMENT '名称',
    `banner_url`  varchar(256) NOT NULL DEFAULT '' COMMENT '图片url',
    `banner_type` tinyint      NOT NULL DEFAULT '0' COMMENT '类型：1-首页，2-侧边栏，3-广告位',
    `rank`        tinyint      NOT NULL DEFAULT '0' COMMENT '排序',
    `status`      tinyint      NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `deleted`     tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='banner表';


-- forum.category definition

CREATE TABLE `category`
(
    `id`            int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` varchar(64) NOT NULL DEFAULT '' COMMENT '类目名称',
    `status`        tinyint     NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `rank`          tinyint     NOT NULL default '0' COMMENT '排序',
    `deleted`       tinyint     NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='类目管理表';


-- forum.column_article definition

CREATE TABLE `column_article`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `column_id`   int unsigned NOT NULL DEFAULT '0' COMMENT '专栏ID',
    `article_id`  int unsigned NOT NULL DEFAULT '0' COMMENT '文章ID',
    `section`     int unsigned NOT NULL DEFAULT '0' COMMENT '章节顺序，越小越靠前',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `column_id` (`column_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='专栏文章列表';


-- forum.column_info definition

CREATE TABLE `column_info`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT COMMENT '专栏ID',
    `column_name`  varchar(64)  NOT NULL DEFAULT '' COMMENT '专栏名',
    `user_id`      int unsigned NOT NULL DEFAULT '0' COMMENT '作者id',
    `introduction` varchar(256) NOT NULL DEFAULT '' COMMENT '专栏简述',
    `cover`        varchar(128) NOT NULL DEFAULT '' COMMENT '专栏封面',
    `state`        tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态: 0-审核中，1-连载，2-完结',
    `publish_time` timestamp    NOT NULL DEFAULT '1970-01-02 00:00:00' COMMENT '上线时间',
    `create_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY            `user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='专栏';


-- forum.comment definition

CREATE TABLE `comment`
(
    `id`                int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`        int unsigned NOT NULL DEFAULT '0' COMMENT '文章ID',
    `user_id`           int unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
    `content`           varchar(300) NOT NULL DEFAULT '' COMMENT '评论内容',
    `top_comment_id`    int          NOT NULL DEFAULT '0' COMMENT '顶级评论ID',
    `parent_comment_id` int unsigned NOT NULL DEFAULT '0' COMMENT '父评论ID',
    `deleted`           tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY                 `idx_article_id` (`article_id`),
    KEY                 `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='评论表';


-- forum.notify_msg definition

CREATE TABLE `notify_msg`
(
    `id`              int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `related_id`      int unsigned NOT NULL DEFAULT '0' COMMENT '关联的主键',
    `notify_user_id`  int unsigned NOT NULL DEFAULT '0' COMMENT '通知的用户id',
    `operate_user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '触发这个通知的用户id',
    `msg`             varchar(1024) NOT NULL DEFAULT '' COMMENT '消息内容',
    `type`            tinyint unsigned NOT NULL DEFAULT '0' COMMENT '类型: 0-默认，1-评论，2-回复 3-点赞 4-收藏 5-关注 6-系统',
    `state`           tinyint unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未读，1-已读',
    `create_time`     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY               `key_notify_user_id_type_state` (`notify_user_id`,`type`,`state`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='消息通知列表';


-- forum.read_count definition

CREATE TABLE `read_count`
(
    `id`            int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `document_id`   int unsigned NOT NULL DEFAULT '0' COMMENT '文档ID（文章/评论）',
    `document_type` tinyint   NOT NULL DEFAULT '1' COMMENT '文档类型：1-文章，2-评论',
    `cnt`           int unsigned NOT NULL DEFAULT '0' COMMENT '访问计数',
    `create_time`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_document_id_type` (`document_id`,`document_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='计数表';


-- forum.request_count definition

CREATE TABLE `request_count`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `host`        varchar(32) NOT NULL DEFAULT '' COMMENT '机器IP',
    `cnt`         int unsigned NOT NULL DEFAULT '0' COMMENT '访问计数',
    `date`        date        NOT NULL COMMENT '当前日期',
    `create_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_unique_id_date` (`date`,`host`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='请求计数表';


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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='标签管理表';


-- forum.`user` definition

CREATE TABLE `user`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `third_account_id` varchar(128) NOT NULL DEFAULT '' COMMENT '第三方用户ID',
    `user_name` varchar(64) NOT NULL DEFAULT '' COMMENT '用户名',
    `password` varchar(128) NOT NULL DEFAULT '' COMMENT '密码',
    `login_type`       tinyint      NOT NULL DEFAULT '0' COMMENT '登录方式: 0-微信登录，1-账号密码登录',
    `deleted`          tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY                `key_third_account_id` (`third_account_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='用户登录表';


-- forum.user_foot definition

CREATE TABLE `user_foot`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          int unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
    `document_id`      int unsigned NOT NULL DEFAULT '0' COMMENT '文档ID（文章/评论）',
    `document_type`    tinyint   NOT NULL DEFAULT '1' COMMENT '文档类型：1-文章，2-评论',
    `document_user_id` int unsigned NOT NULL DEFAULT '0' COMMENT '发布该文档的用户ID',
    `collection_stat`  tinyint unsigned NOT NULL DEFAULT '0' COMMENT '收藏状态: 0-未收藏，1-已收藏，2-取消收藏',
    `read_stat`        tinyint unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未读，1-已读',
    `comment_stat`     tinyint unsigned NOT NULL DEFAULT '0' COMMENT '评论状态: 0-未评论，1-已评论，2-删除评论',
    `praise_stat`      tinyint unsigned NOT NULL DEFAULT '0' COMMENT '点赞状态: 0-未点赞，1-已点赞，2-取消点赞',
    `create_time`      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_doucument` (`user_id`,`document_id`,`document_type`),
    KEY                `idx_doucument_id` (`document_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='用户足迹表';


-- forum.user_info definition

CREATE TABLE `user_info`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     int unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='用户个人信息表';


-- forum.user_relation definition

CREATE TABLE `user_relation`
(
    `id`             int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        int unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
    `follow_user_id` int unsigned NOT NULL COMMENT '关注userId的用户id，即粉丝userId',
    `follow_state`   tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '阅读状态: 0-未关注，1-已关注，2-取消关注',
    `create_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`,`follow_user_id`),
    KEY              `key_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='用户关系表';


-- 配置表

CREATE TABLE `config`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type`        tinyint      NOT NULL default '0' COMMENT '配置类型：1-首页，2-侧边栏，3-广告位，4-公告',
    `name`        varchar(64)  NOT NULL default '' COMMENT '名称',
    `banner_url`  varchar(256) NOT NULL default '' COMMENT '图片链接',
    `jump_url`    varchar(256) NOT NULL default '' COMMENT '跳转链接',
    `content`     varchar(256) NOT NULL default '' COMMENT '内容',
    `rank`        tinyint      NOT NULL default '0' COMMENT '排序',
    `status`      tinyint      NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `tags`        varchar(64)  not null default '' comment '配置关联标签，英文逗号分隔 1 火 2 官方 3 推荐',
    `deleted`     tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4  COMMENT='配置表';


CREATE TABLE `dict_common` (
                               `id`             int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `type_code`      varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型，sex, status 等',
                               `dict_code`      varchar(100) NOT NULL DEFAULT '' COMMENT '字典类型的值编码',
                               `dict_desc`      varchar(200) NOT NULL DEFAULT '' COMMENT '字典类型的值描述',
                               `sort_no`        int(8) unsigned NOT NULL DEFAULT '0' COMMENT '排序编号',
                               `remark`         varchar(500) DEFAULT '' COMMENT '备注',
                               `create_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_type_code_dict_code` (`type_code`,`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='通用数据字典';