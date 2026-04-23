CREATE TABLE IF NOT EXISTS `user_login_audit`
(
    `id`           bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      bigint(20)          DEFAULT NULL COMMENT '用户ID',
    `login_name`   varchar(128)        DEFAULT NULL COMMENT '登录用户名',
    `login_type`   int(11)    NOT NULL DEFAULT 1 COMMENT '登录方式',
    `event_type`   varchar(32) NOT NULL COMMENT '审计事件',
    `device_id`    varchar(64)         DEFAULT NULL COMMENT '设备ID',
    `device_name`  varchar(128)        DEFAULT NULL COMMENT '设备名称',
    `ua_hash`      varchar(64)         DEFAULT NULL COMMENT 'UA哈希',
    `user_agent`   varchar(512)        DEFAULT NULL COMMENT '用户代理',
    `ip`           varchar(64)         DEFAULT NULL COMMENT 'IP地址',
    `region`       varchar(128)        DEFAULT NULL COMMENT '归属地',
    `session_hash` varchar(64)         DEFAULT NULL COMMENT '会话哈希',
    `risk_tag`     varchar(128)        DEFAULT NULL COMMENT '风险标签',
    `reason`       varchar(255)        DEFAULT NULL COMMENT '事件原因',
    `create_time`  datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_create` (`user_id`, `create_time`),
    KEY `idx_login_name_create` (`login_name`, `create_time`),
    KEY `idx_device_create` (`device_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录审计日志';

CREATE TABLE IF NOT EXISTS `user_active_session`
(
    `id`              bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`         bigint(20) NOT NULL COMMENT '用户ID',
    `login_name`      varchar(128)        DEFAULT NULL COMMENT '登录用户名',
    `login_type`      int(11)    NOT NULL DEFAULT 1 COMMENT '登录方式',
    `session_hash`    varchar(64) NOT NULL COMMENT '会话哈希',
    `device_id`       varchar(64)         DEFAULT NULL COMMENT '设备ID',
    `device_name`     varchar(128)        DEFAULT NULL COMMENT '设备名称',
    `ua_hash`         varchar(64)         DEFAULT NULL COMMENT 'UA哈希',
    `user_agent`      varchar(512)        DEFAULT NULL COMMENT '用户代理',
    `ip`              varchar(64)         DEFAULT NULL COMMENT 'IP地址',
    `region`          varchar(128)        DEFAULT NULL COMMENT '归属地',
    `latest_seen_time` datetime   NOT NULL COMMENT '最近活跃时间',
    `expire_time`     datetime   NOT NULL COMMENT '会话过期时间',
    `offline_time`    datetime            DEFAULT NULL COMMENT '下线时间',
    `offline_reason`  varchar(255)        DEFAULT NULL COMMENT '下线原因',
    `create_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_hash` (`session_hash`),
    KEY `idx_user_latest_seen` (`user_id`, `latest_seen_time`),
    KEY `idx_user_device` (`user_id`, `device_id`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户活跃登录会话';
