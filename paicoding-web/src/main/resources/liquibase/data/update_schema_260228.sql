-- 扩大 global_conf 表的 value 字段长度,从 varchar(512) 改为 TEXT 类型
-- 用于支持敏感词白名单等需要存储长文本的配置项
ALTER TABLE `global_conf` MODIFY COLUMN `value` TEXT NOT NULL COMMENT '配置value';
