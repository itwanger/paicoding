ALTER TABLE `user`
    ADD COLUMN `forbid_time` datetime DEFAULT NULL COMMENT '禁用开始时间' AFTER `password`,
    ADD COLUMN `forbid_until` datetime DEFAULT NULL COMMENT '禁用截止时间' AFTER `forbid_time`,
    ADD COLUMN `forbid_reason` varchar(255) DEFAULT NULL COMMENT '禁用原因' AFTER `forbid_until`,
    ADD COLUMN `forbid_operator_id` bigint(20) DEFAULT NULL COMMENT '禁用操作人ID' AFTER `forbid_reason`;
