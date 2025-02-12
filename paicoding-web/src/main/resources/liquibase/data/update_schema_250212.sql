-- 短链接表
CREATE TABLE `short_link`
(
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `original_url` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原始链接',
    `short_url` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '短链接',
    `username` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '用户名',
    `third_party_user_id` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '第三方用户ID',
    `user_agent` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '用户代理',
    `login_method` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '登录方式',
    `deleted`         tinyint     NOT NULL DEFAULT '0' COMMENT '是否删除',
    `create_time`     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_key` (`short_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接记录表';
DROP TABLE IF EXISTS `short_link`;