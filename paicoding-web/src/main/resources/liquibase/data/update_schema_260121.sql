-- 增加 user_info 表的 photo 字段长度，支持更长的图片URL
-- Author: Qoder
-- Date: 2026-01-21

ALTER TABLE `user_info` MODIFY COLUMN `photo` varchar(512) NOT NULL DEFAULT '' COMMENT '用户图像';
