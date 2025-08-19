-- 知识星球登录集成，保存用户的账号有效期
alter table user_ai add column `star_expire_time` DATETIME  NULL COMMENT '星球账号过期时间';

-- 星球账号字典
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStatEnum','-1','忽略',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStatEnum','0','审核中',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStatEnum','1','试用中',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStatEnum','2','VIP',4);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStatEnum','3','未通过',5);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStatEnum','4','已过期',6);
