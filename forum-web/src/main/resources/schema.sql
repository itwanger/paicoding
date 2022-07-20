-- forum.article definition

CREATE TABLE `article`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_type` tinyint      NOT NULL DEFAULT '1' COMMENT '文章类型：1-博文，2-问答',
    `title`        varchar(120) NOT NULL COMMENT '文章标题',
    `short_title`  varchar(120) NOT NULL COMMENT '短标题',
    `picture`      varchar(128) NOT NULL DEFAULT '' COMMENT '文章头图',
    `summary`      varchar(300) NOT NULL DEFAULT '' COMMENT '文章摘要',
    `category_id`  int unsigned NOT NULL DEFAULT '0' COMMENT '类目ID',
    `source`       tinyint      NOT NULL DEFAULT '1' COMMENT '来源：1-转载，2-原创，3-翻译',
    `status`       tinyint      NOT NULL DEFAULT '0' COMMENT '状态：0-未发布，1-已发布',
    `deleted`      tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章表';


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章详情表';


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章标签映射';


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='类目管理表';


-- forum.comment definition

CREATE TABLE `comment`
(
    `id`                int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`        int unsigned NOT NULL COMMENT '文章ID',
    `user_id`           int unsigned NOT NULL COMMENT '用户ID',
    `content`           varchar(300) NOT NULL DEFAULT '' COMMENT '评论内容',
    `parent_comment_id` int unsigned NOT NULL COMMENT '父评论ID',
    `deleted`           tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY                 `idx_article_id` (`article_id`),
    KEY                 `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签管理表';


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录表';


-- forum.user_foot definition

CREATE TABLE `user_foot`
(
    `id`              int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         int unsigned NOT NULL COMMENT '用户ID',
    `doucument_id`    int unsigned NOT NULL COMMENT '文档ID（文章/评论）',
    `doucument_type`  tinyint   NOT NULL DEFAULT '1' COMMENT '文档类型：1-文章，2-评论',
    `collection_stat` tinyint unsigned NOT NULL COMMENT '收藏状态: 0-未收藏，1-已收藏，2-取消收藏',
    `read_stat`       tinyint unsigned NOT NULL COMMENT '阅读状态: 0-未读，1-已读',
    `comment_stat`    tinyint unsigned NOT NULL COMMENT '评论状态: 0-未评论，1-已评论，2-删除评论',
    `praise_stat`     tinyint unsigned NOT NULL COMMENT '点赞状态: 0-未点赞，1-已点赞，2-取消点赞',
    `create_time`     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_doucument` (`user_id`,`doucument_id`),
    KEY               `idx_doucument_id` (`doucument_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户足迹表';


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户个人信息表';


-- forum.user_relation definition

CREATE TABLE `user_relation`
(
    `id`             int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        int unsigned NOT NULL COMMENT '用户ID',
    `follow_user_id` int unsigned NOT NULL COMMENT '关注用户ID',
    `create_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`,`follow_user_id`),
    KEY              `key_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关系表';