alter table `user_info`
    modify `user_role` int(4) not null default '0' comment '0 普通用户 1 超管 2 运营';

