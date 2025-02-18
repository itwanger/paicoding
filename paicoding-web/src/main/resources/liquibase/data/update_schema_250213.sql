-- 短链接表
CREATE TABLE `short_link` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `original_url` VARCHAR(2048) NOT NULL COMMENT '原始URL',
    `short_code` VARCHAR(255) UNIQUE NOT NULL COMMENT '短链接代码',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `deleted` TINYINT DEFAULT 0 NOT NULL comment '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_short_code` (`short_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接表';

-- 短链接记录表
CREATE TABLE `short_link_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `short_code` VARCHAR(255) NOT NULL COMMENT '短链接代码',
    `user_id` VARCHAR(255) COMMENT '用户ID',
    `access_time` BIGINT NOT NULL COMMENT '访问时间',
    `ip_address` VARCHAR(255) COMMENT 'IP地址',
    `login_method` VARCHAR(255) COMMENT '登录方式',
    `access_source` VARCHAR(255) COMMENT '访问来源',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_short_code` (`short_code`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短链接记录表';

