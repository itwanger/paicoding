alter table `user_info` add `user_role` int(4) not null default '0' comment '0 普通用户 1 超管' after `profile`;

update user_info set `user_role` = 1 where id = 1;