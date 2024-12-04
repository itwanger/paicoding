-- 新增微信支付相关信息
alter table article_pay_record
    add column pre_pay_id varchar(256) null comment '微信支付创建订单回传的关键信息',
    add column pre_pay_expire_time timestamp null comment 'prePayId失效时间',
    add column pay_callback_time timestamp null comment '支付成功时间',
    add column third_trans_code varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '第三方交易流水号',
    add column pay_way varchar(12) not null default 'email' comment '支付方式: email-个人收款码, wx_h5-微信h5支付, wx_jsapi-微信jsapi, wx_native-微信native',
    add column pay_amount int NULL COMMENT '支付金额，单位为分';


-- 支付记录，将verify_code标记为支付唯一标识，传递给外部系统使用
alter table `article_pay_record` modify `verify_code` varchar (32) not null DEFAULT '' COMMENT '唯一code，会传递给外部支付系统';


-- 文章添加支付相关信息
alter table `article`
    add column `pay_amount` int not null default '0' comment '付费阅读金额，单位为分',
    add column `pay_way` varchar(12) null comment '支付方式: email-个人收款码, wx_h5-微信h5支付, wx_jsapi-微信jsapi, wx_native-微信native';