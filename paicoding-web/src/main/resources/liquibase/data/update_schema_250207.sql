-- 聊天历史记录，按照对话进行分组
alter table `user_ai_history`
    add column `chat_id` varchar(128) null comment '聊天id';