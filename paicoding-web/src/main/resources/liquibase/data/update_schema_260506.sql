ALTER TABLE `user`
    ADD COLUMN `last_login_time` datetime DEFAULT NULL COMMENT '最近一次成功登录时间' AFTER `password`,
    ADD KEY `idx_last_login_time` (`last_login_time`);

ALTER TABLE `user_login_audit`
    ADD COLUMN `star_number` varchar(32) DEFAULT NULL COMMENT '登录时星球编号快照' AFTER `login_name`,
    ADD KEY `idx_star_number_create` (`star_number`, `create_time`);

UPDATE `user` u
    LEFT JOIN (
        SELECT user_id, MAX(create_time) AS last_login_time
        FROM user_login_audit
        WHERE event_type = 'LOGIN_SUCCESS'
        GROUP BY user_id
    ) la ON la.user_id = u.id
SET u.last_login_time = la.last_login_time
WHERE la.last_login_time IS NOT NULL;

UPDATE user_login_audit a
    LEFT JOIN user_ai ai ON ai.user_id = a.user_id AND ai.deleted = 0
SET a.star_number = COALESCE(ai.star_number, CASE WHEN a.login_name LIKE 'zsxq_%' THEN SUBSTRING(a.login_name, 6) ELSE NULL END)
WHERE a.star_number IS NULL
  AND (ai.star_number IS NOT NULL OR a.login_name LIKE 'zsxq_%');
