-- Token 配额管理系统 - 数据库表创建
-- 创建时间: 2025-11-17
-- 说明: 实现用户 Token 配额管理功能，包括配额分配、消耗记录和充值管理

-- ======================================
-- 1. 修改现有 chat_message 表，添加 token 统计字段
-- ======================================
-- 使用动态 SQL 方式确保幂等性，避免重复执行时报错

-- 添加 prompt_tokens 列（如果不存在）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'chat_message'
    AND COLUMN_NAME = 'prompt_tokens'
);

SET @sql_add_prompt_tokens = IF(
    @col_exists = 0,
    'ALTER TABLE `chat_message` ADD COLUMN `prompt_tokens` INT DEFAULT 0 COMMENT ''输入token数（仅assistant消息有效）'' AFTER `content`',
    'SELECT ''Column prompt_tokens already exists'' AS message'
);

PREPARE stmt FROM @sql_add_prompt_tokens;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 completion_tokens 列（如果不存在）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'chat_message'
    AND COLUMN_NAME = 'completion_tokens'
);

SET @sql_add_completion_tokens = IF(
    @col_exists = 0,
    'ALTER TABLE `chat_message` ADD COLUMN `completion_tokens` INT DEFAULT 0 COMMENT ''输出token数（仅assistant消息有效）'' AFTER `prompt_tokens`',
    'SELECT ''Column completion_tokens already exists'' AS message'
);

PREPARE stmt FROM @sql_add_completion_tokens;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 total_tokens 列（如果不存在）
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'chat_message'
    AND COLUMN_NAME = 'total_tokens'
);

SET @sql_add_total_tokens = IF(
    @col_exists = 0,
    'ALTER TABLE `chat_message` ADD COLUMN `total_tokens` INT DEFAULT 0 COMMENT ''总token数（仅assistant消息有效）'' AFTER `completion_tokens`',
    'SELECT ''Column total_tokens already exists'' AS message'
);

PREPARE stmt FROM @sql_add_total_tokens;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ======================================
-- 2. 创建用户模型配额表
-- ======================================
CREATE TABLE if not exists `user_model_quota` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `model_id` VARCHAR(50) NOT NULL COMMENT '模型ID（如：gpt-4o, deepseek-chat）',
    `total_quota` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '总配额（tokens）',
    `used_quota` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '已使用配额（tokens）',
    `remaining_quota` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '剩余配额（tokens），冗余字段便于查询',
    `total_used` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '累计使用量（tokens），用于统计，不会因充值而重置',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最后使用时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_model` (`user_id`, `model_id`) COMMENT '用户-模型唯一约束',
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_model_id` (`model_id`) COMMENT '模型ID索引',
    KEY `idx_remaining_quota` (`remaining_quota`) COMMENT '剩余配额索引，用于查询配额不足的用户'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户模型配额表';

-- ======================================
-- 3. 创建配额充值记录表
-- ======================================
CREATE TABLE if not exists `quota_recharge_record` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT(20) UNSIGNED NOT NULL COMMENT '用户ID',
    `model_id` VARCHAR(50) NOT NULL COMMENT '模型ID',
    `recharge_amount` BIGINT(20) NOT NULL COMMENT '充值数量（tokens）',
    `before_quota` BIGINT(20) NOT NULL COMMENT '充值前配额',
    `after_quota` BIGINT(20) NOT NULL COMMENT '充值后配额',
    `operator_id` BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '操作员ID（管理员）',
    `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '操作员名称',
    `reason` VARCHAR(200) DEFAULT NULL COMMENT '充值原因',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注信息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
    KEY `idx_model_id` (`model_id`) COMMENT '模型ID索引',
    KEY `idx_operator_id` (`operator_id`) COMMENT '操作员ID索引',
    KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配额充值记录表';

-- ======================================
-- 4. 创建默认配额配置表
-- ======================================
CREATE TABLE if not exists `default_quota_config` (
    `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `model_id` VARCHAR(50) NOT NULL COMMENT '模型ID',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型显示名称',
    `default_quota` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '新用户默认配额（tokens）',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级，数字越大优先级越高',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_id` (`model_id`) COMMENT '模型ID唯一约束',
    KEY `idx_enabled` (`enabled`) COMMENT '启用状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='默认配额配置表';

-- ======================================
-- 5. 初始化默认配额配置数据
-- ======================================
-- 只初始化 qwen-plus 和 deepseek-v3 两个模型
INSERT INTO `default_quota_config` (`model_id`, `model_name`, `default_quota`, `enabled`, `priority`, `description`) VALUES
('qwen-plus', 'Qwen Plus', 200000, 1, 100, '新用户默认获得 20万 tokens，通义千问高性能模型'),
('deepseek-v3', 'DeepSeek V3', 200000, 1, 90, '新用户默认获得 20万 tokens，DeepSeek 最新版本模型')
ON DUPLICATE KEY UPDATE
    model_name = VALUES(model_name),
    default_quota = VALUES(default_quota),
    enabled = VALUES(enabled),
    priority = VALUES(priority),
    description = VALUES(description);

-- ======================================
-- 6. 为现有用户初始化配额
-- ======================================
-- 6.1 为管理员用户（user_id=1）初始化超大配额
INSERT INTO `user_model_quota` (`user_id`, `model_id`, `total_quota`, `used_quota`, `remaining_quota`, `total_used`)
SELECT
    1 AS user_id,
    c.model_id,
    999999999 AS total_quota,  -- 超大额度：约10亿 tokens
    0 AS used_quota,
    999999999 AS remaining_quota,
    0 AS total_used
FROM
    `default_quota_config` c
WHERE
    c.enabled = 1
    AND NOT EXISTS (
        -- 避免重复插入已有配额的记录
        SELECT 1 FROM `user_model_quota` umq
        WHERE umq.user_id = 1 AND umq.model_id = c.model_id
    );

-- 6.2 为其他现有用户初始化默认配额
INSERT INTO `user_model_quota` (`user_id`, `model_id`, `total_quota`, `used_quota`, `remaining_quota`, `total_used`)
SELECT
    u.id AS user_id,
    c.model_id,
    c.default_quota AS total_quota,
    0 AS used_quota,
    c.default_quota AS remaining_quota,
    0 AS total_used
FROM
    `user` u
CROSS JOIN
    `default_quota_config` c
WHERE
    u.id != 1  -- 排除管理员，管理员已经在上面初始化了
    AND c.enabled = 1
    AND NOT EXISTS (
        -- 避免重复插入已有配额的记录
        SELECT 1 FROM `user_model_quota` umq
        WHERE umq.user_id = u.id AND umq.model_id = c.model_id
    );

-- ======================================
-- 说明文档
-- ======================================
--
-- 表设计说明：
-- 1. user_model_quota: 核心表，存储每个用户对每个模型的配额信息
--    - remaining_quota = total_quota - used_quota（冗余字段，便于快速查询）
--    - total_used 是累计使用量，不会因充值而减少，用于统计分析
--
-- 2. quota_recharge_record: 充值记录表，记录所有配额变更操作
--    - 包含充值前后的配额值，便于审计
--    - operator_id 记录操作员，便于追踪管理操作
--
-- 3. default_quota_config: 默认配额配置表
--    - 新用户注册时，自动为其初始化所有启用模型的配额
--    - 可动态调整默认配额，不影响已有用户
--
-- 4. chat_message 表添加 token 字段
--    - 只有 assistant 角色的消息才记录 token 消耗
--    - user 消息的 token 字段保持为 0
--
-- 使用场景：
-- 1. 用户注册：自动初始化所有启用模型的默认配额
-- 2. 发送消息前：检查用户对指定模型的剩余配额是否充足
-- 3. 消息完成后：扣除配额，更新 used_quota、remaining_quota、total_used
-- 4. 充值操作：管理员为用户充值指定模型的配额，记录充值记录
-- 5. 配额查询：用户查看自己各模型的配额使用情况
-- 6. 统计分析：管理员查看用户的总使用量（total_used）
--
