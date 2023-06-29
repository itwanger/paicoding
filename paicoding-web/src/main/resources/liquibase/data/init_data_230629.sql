insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AiChatStat','-2','忽略',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AiChatStat','-1','会话异常',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AiChatStat','0','首次回答',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AiChatStat','1', '中间回答',4);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AiChatStat','2','最后一次回答',5);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AISource','0','chatGpt3.5',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AISource','1','chatGpt4',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AISource','2','技术派',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('AISource','3','讯飞',4);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('LoginType','0','公众号登录',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('LoginType','1','用户名密码登录',2);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('StarSource','0','Java进阶之路知识星球',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('StarSource','1','技术派知识星球',2);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStat','-1','默认',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStat','0','审核中',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStat','1','试用中',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStat','2','正式用户',4);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAIStat','3','未通过',5);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAiStrategy','1','绑定微信公众号',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAiStrategy','2','绑定邀请人',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAiStrategy','4','绑定Java进阶之路知识星球',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('UserAiStrategy','8','绑定技术派知识星球',4);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ChatAnswerType','0','文本返回',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ChatAnswerType','1','JSON返回',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ChatAnswerType','2','Stream返回',3);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ChatAnswerType','3','Stream最后一次返回',4);


insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ChatSocketState','0','Established',1);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ChatSocketState','1','Payload',2);
insert into dict_common(`type_code`,`dict_code`,`dict_desc`,`sort_no`) values('ChatSocketState','2','Closed',3);