alter table `article` modify `id` bigint unsigned AUTO_INCREMENT COMMENT '主键ID', modify `user_id` bigint unsigned not null default '0' comment '用户id';
alter table `article_detail` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `article_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文章ID';
alter table `article_tag` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `article_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文章ID';

alter table `column_article` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `article_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文章ID';
alter table `column_info` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id';

alter table `comment` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `article_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文章ID', modify `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id';

alter table `read_count` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `document_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文档ID（文章/评论）';
alter table `request_count` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID';


alter table `notify_msg` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `related_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '关联的主键', modify `notify_user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '通知的用户id', modify `operate_user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '触发这个通知的用户id';

alter table `user` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID';
alter table `user_ai` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id', modify `inviter_user_id` bigint NOT NULL DEFAULT '0' COMMENT '当前用户绑定的邀请者';
alter table `user_ai_history` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id';
alter table `user_foot` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id', modify `document_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '文档ID（文章/评论）', modify `document_user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '发布该文档的用户ID';
alter table `user_info` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id';
alter table `user_relation` modify `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID', modify `user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id', modify `follow_user_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '用户id';
