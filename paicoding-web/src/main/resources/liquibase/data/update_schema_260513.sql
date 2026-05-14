-- 疑似共享账号表新增 last_release_at / last_handle_reason；user_id 改为 NOT NULL
SET @has_release_col := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user_share_risk_account'
      AND column_name = 'last_release_at'
);
SET @release_sql := IF(@has_release_col = 0,
    'ALTER TABLE `user_share_risk_account` ADD COLUMN `last_release_at` datetime DEFAULT NULL COMMENT "最近一次解禁时间，仅在解禁动作发生时写入" AFTER `recent_days`',
    'SELECT 1');
PREPARE stmt FROM @release_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_handle_col := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user_share_risk_account'
      AND column_name = 'last_handle_reason'
);
SET @handle_sql := IF(@has_handle_col = 0,
    'ALTER TABLE `user_share_risk_account` ADD COLUMN `last_handle_reason` varchar(128) DEFAULT NULL COMMENT "最近一次处理动作描述，与 risk_reason 解耦" AFTER `last_release_at`',
    'SELECT 1');
PREPARE stmt FROM @handle_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 清理可能存在的 user_id IS NULL 孤立行，再把列改成 NOT NULL
DELETE FROM `user_share_risk_account` WHERE `user_id` IS NULL;

SET @user_id_nullable := (
    SELECT IS_NULLABLE
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'user_share_risk_account'
      AND column_name = 'user_id'
);
SET @user_id_sql := IF(@user_id_nullable = 'YES',
    'ALTER TABLE `user_share_risk_account` MODIFY COLUMN `user_id` bigint(20) NOT NULL COMMENT "用户ID"',
    'SELECT 1');
PREPARE stmt FROM @user_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
