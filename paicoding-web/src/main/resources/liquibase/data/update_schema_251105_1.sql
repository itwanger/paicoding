-- 添加 comment_id 字段，用于存储评论ID，方便跳转到具体评论位置
ALTER TABLE `notify_msg` ADD COLUMN `comment_id` BIGINT(20) DEFAULT NULL COMMENT '关联的评论ID' AFTER `related_id`;
-- 修改杠精机器人名称为杠精派
UPDATE user_info
SET user_name = '杠精派'
WHERE user_name = '杠精机器人';