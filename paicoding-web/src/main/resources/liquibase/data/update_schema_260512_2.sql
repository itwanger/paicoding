CREATE TABLE IF NOT EXISTS `user_share_risk_account`
(
    `id`                  bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`             bigint(20)          DEFAULT NULL COMMENT '用户ID',
    `login_name`          varchar(128)        DEFAULT NULL COMMENT '登录用户名',
    `star_number`         varchar(64)         DEFAULT NULL COMMENT '星球编号',
    `kickout_count`       bigint(20)          DEFAULT 0 COMMENT '被踢下线次数',
    `login_success_count` bigint(20)          DEFAULT 0 COMMENT '登录成功次数',
    `device_count`        bigint(20)          DEFAULT 0 COMMENT '设备数',
    `ip_count`            bigint(20)          DEFAULT 0 COMMENT 'IP数',
    `last_kickout_time`   datetime            DEFAULT NULL COMMENT '最后被踢下线时间',
    `last_active_time`    datetime            DEFAULT NULL COMMENT '最后活跃时间',
    `risk_level`          varchar(32)         DEFAULT NULL COMMENT '风险等级',
    `risk_reason`         varchar(128)        DEFAULT NULL COMMENT '风险依据',
    `forbidden`           tinyint(1)          DEFAULT 0 COMMENT '当前是否禁用',
    `forbid_until`        datetime            DEFAULT NULL COMMENT '禁用截止时间',
    `forbid_reason`       varchar(255)        DEFAULT NULL COMMENT '禁用原因',
    `forbid_operator_id`  bigint(20)          DEFAULT NULL COMMENT '禁用操作人ID',
    `recent_days`         int(11)             DEFAULT NULL COMMENT '统计周期，空表示全部时间',
    `create_time`         datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_star_number` (`star_number`),
    KEY `idx_forbidden` (`forbidden`),
    KEY `idx_risk_level` (`risk_level`),
    KEY `idx_last_kickout_time` (`last_kickout_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疑似共享账号状态';

SET @has_share_risk_snapshot = (
    SELECT COUNT(1)
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name = 'user_share_risk_snapshot'
);

SET @migrate_share_risk_snapshot_sql = IF(
    @has_share_risk_snapshot > 0,
    'INSERT INTO user_share_risk_account(user_id, login_name, star_number, kickout_count, login_success_count, device_count, ip_count, last_kickout_time, last_active_time, risk_level, risk_reason, forbidden, forbid_until, forbid_reason, recent_days, create_time, update_time) SELECT user_id, login_name, star_number, kickout_count, login_success_count, device_count, ip_count, last_kickout_time, last_active_time, risk_level, risk_reason, 0, NULL, NULL, recent_days, create_time, update_time FROM user_share_risk_snapshot ON DUPLICATE KEY UPDATE login_name = VALUES(login_name), star_number = VALUES(star_number), kickout_count = VALUES(kickout_count), login_success_count = VALUES(login_success_count), device_count = VALUES(device_count), ip_count = VALUES(ip_count), last_kickout_time = VALUES(last_kickout_time), last_active_time = VALUES(last_active_time), risk_level = VALUES(risk_level), risk_reason = VALUES(risk_reason), recent_days = VALUES(recent_days), update_time = VALUES(update_time)',
    'SELECT 1'
);

PREPARE migrate_share_risk_snapshot_stmt FROM @migrate_share_risk_snapshot_sql;
EXECUTE migrate_share_risk_snapshot_stmt;
DEALLOCATE PREPARE migrate_share_risk_snapshot_stmt;

DROP TABLE IF EXISTS `user_share_risk_snapshot`;
