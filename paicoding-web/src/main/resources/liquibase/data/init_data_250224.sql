-- 自动注册 杠精机器人，用于评论的在线回复

INSERT INTO `user` (third_account_id, user_name, password, login_type) VALUES ('systemUser001', 'haterBot', '', 0);

SET @user_id = LAST_INSERT_ID();

INSERT INTO user_info
(user_id, user_name, photo, `position`, company, profile, user_role, extend, ip)
VALUES (@user_id, '杠精机器人', 'https://cdn.tobebetterjavaer.com/paicoding/e0f01d775d3f67b309b394bc04d4e091.jpg',
        '职业杠精AI', '技术派', 'AIBot', 0, '', '{}');


-- 评论长度调整，最大支持1024
ALTER TABLE comment MODIFY COLUMN content varchar(1024) DEFAULT '' NOT NULL COMMENT '评论内容';
