-- liquibase formatted sql
-- changeset yifei:20251118_remove_zsxq_feature

-- 删除知识星球功能相关数据库变更
-- 1. 删除user_ai表（包括所有历史数据）
DROP TABLE IF EXISTS `user_ai`;

-- 2. 删除字典表中的星球相关数据
DELETE FROM `dict_common` WHERE `type_code` = 'StarSource';
DELETE FROM `dict_common` WHERE `type_code` = 'UserAiStrategy' AND `dict_code` IN ('4', '8');

-- 3. 更新专栏文章的阅读类型（将星球阅读类型改为登录阅读）
UPDATE `column_article` SET `read_type` = 1 WHERE `read_type` = 3;

-- 4. 更新专栏类型（将星球专栏类型改为登录阅读）
UPDATE `column_info` SET `type` = 1 WHERE `type` = 3;

-- rollback DROP TABLE IF EXISTS `user_ai`;
-- rollback 注意：此变更删除了user_ai表及所有数据，回滚需要手动恢复表结构和数据
