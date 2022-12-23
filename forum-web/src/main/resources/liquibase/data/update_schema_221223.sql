alter table column_info add `nums` int unsigned NOT NULL DEFAULT '0' COMMENT '专栏预计的更新的文章数';
alter table column_info add `type` int unsigned NOT NULL DEFAULT '0' COMMENT '专栏类型 0-免费 1-登录阅读 2-限时免费';
alter table column_info add `free_start_time` timestamp NOT NULL  DEFAULT '1970-01-02 00:00:00' COMMENT '限时免费开始时间';
alter table column_info add `free_end_time` timestamp NOT NULL  DEFAULT '1970-01-02 00:00:00' COMMENT '限时免费结束时间';

-- 更新默认专栏的限时免费时间
update column_info set `nums` = 100, `type` = 2, `free_start_time` = '2022-12-22 10:00:00', `free_end_time` = '2023-03-20 10:00:00' where id = 1;