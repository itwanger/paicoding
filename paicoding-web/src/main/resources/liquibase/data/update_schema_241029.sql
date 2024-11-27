
-- 用户添加收款、邮箱信息
alter table `user_info` add column `email` varchar(64) not null default '' comment '用户邮箱', add column `pay_code` varchar(1024) not null default '' comment '聚合收款码';

-- 文章添加付费解锁功能
alter table `article` add column `read_type` tinyint not null default '0' comment '文章阅读类型 0-直接阅读 1-登录阅读 2-付费阅读 3-星球';

-- article_pay_record，文章支付记录
CREATE TABLE `article_pay_record` (
      `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
      `article_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文章ID',
      `pay_user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '支付用户id',
      `receive_user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '收款用户id',
      `pay_status` tinyint DEFAULT '0' COMMENT '支付状态 0-未支付 1-支付中 2-支付成功 3-支付失败',
      `notes` varchar(128) DEFAULT '' COMMENT '备注信息',
      `verify_code` char(16) not null DEFAULT '' COMMENT '用于校验回调合法的随机code',
      `notify_cnt` int not null default 0 COMMENT '邮件通知次数',
      `notify_time` timestamp NULL COMMENT '邮件通知收款用户时间',
      `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
      `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
      PRIMARY KEY (`id`),
      KEY `i_article_id` (`article_id`),
      KEY `i_pay_user_id` (`pay_user_id`),
      KEY `i_receive_user_id` (`receive_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章支付记录表';

