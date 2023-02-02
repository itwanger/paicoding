alter table `user_info`
    add `user_role` int(4) not null default '0' comment '0 普通用户 1 超管' after `profile`;

update user_info
set `user_role` = 1
where id = 1;


replace INTO `user` (id, third_account_id, user_name, password, login_type)
VALUES (2, 'ojfU95x47r52Jtsl9q6tuljq85Pk', 'guest', '168128f21e4702eb07d39b7478602e64', 0);

replace INTO user_info (id, user_id, user_name, photo, `position`, company, profile, user_role, ip)
VALUES (2, 2, '游客', 'https://cdn.tobebetterjavaer.com/paicoding/avatar/0010.png', '首席32', 'tech派', '我的简介', 0, '{"firstIp": "121.40.134.96", "latestIp": "58.48.23.111", "firstRegion": "浙江·杭州", "latestRegion": "湖北·武汉"}');
