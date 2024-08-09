-- 新增简历

CREATE TABLE `resume` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
  `mark` varchar(1024) NOT NULL DEFAULT '' COMMENT '备注内容',
  `resume_name` varchar(256) NOT NULL DEFAULT '' COMMENT '上传的简历文件名',
  `resume_url` varchar(256) NOT NULL DEFAULT '' COMMENT '上传的简历附件',
  `replay_email` varchar(256) NOT NULL DEFAULT '' COMMENT '接收回传信息的邮箱地址',
  `replay` text comment '回复内容',
  `replay_url` varchar(256) comment '修改之后上传的简历附件',
  `type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未处理 1-处理中 2-已处理',
  `email_state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未回复 1-处理中已回复 2-已处理已回复',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='简历表';


-- 添加简历相关的常量

insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ResumeType','0','未处理',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ResumeType','1','处理中',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ResumeType','2','已处理',3);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ResumeEmailState','-1','未回复',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ResumeEmailState','0','上传-已回复',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ResumeEmailState','1','处理中-已回复',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ResumeEmailState','2','已处理-已回复',4);
