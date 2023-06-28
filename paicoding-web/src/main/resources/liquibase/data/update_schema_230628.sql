CREATE TABLE `global_conf`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `key`         varchar(128) NOT NULL DEFAULT '' COMMENT '配置key',
    `value`       varchar(512) NOT NULL DEFAULT '' COMMENT '配置value',
    `comment`     varchar(128) NOT NULL DEFAULT '' COMMENT '注释',
    `deleted`     tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除 0 未删除 1 已删除',
    `create_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    KEY           `idx_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局配置表';
