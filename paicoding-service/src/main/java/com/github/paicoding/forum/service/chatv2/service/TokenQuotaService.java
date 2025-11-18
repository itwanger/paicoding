package com.github.paicoding.forum.service.chatv2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.paicoding.forum.api.model.vo.chatv2.UserModelQuotaVO;
import com.github.paicoding.forum.service.chatv2.repository.entity.*;
import com.github.paicoding.forum.service.chatv2.repository.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Token 配额管理服务
 *
 * @author XuYifei
 * @date 2025-11-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenQuotaService {

    private final UserModelQuotaMapper userModelQuotaMapper;
    private final QuotaRechargeRecordMapper rechargeRecordMapper;
    private final DefaultQuotaConfigMapper defaultQuotaConfigMapper;
    private final ChatMessageMapper chatMessageMapper;

    /**
     * 检查用户对指定模型的配额是否充足
     *
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param estimatedTokens 预估 token 数（可选，用于预检查）
     * @return 是否有足够配额
     */
    public boolean checkQuota(Long userId, String modelId, Integer estimatedTokens) {
        UserModelQuotaDO quota = getUserModelQuota(userId, modelId);

        if (quota == null) {
            log.warn("User quota not found: userId={}, modelId={}", userId, modelId);
            return false;
        }

        // 如果没有提供预估值，只检查是否还有剩余配额
        if (estimatedTokens == null || estimatedTokens <= 0) {
            return quota.getRemainingQuota() > 0;
        }

        // 检查剩余配额是否足够
        boolean sufficient = quota.getRemainingQuota() >= estimatedTokens;

        if (!sufficient) {
            log.warn("Insufficient quota: userId={}, modelId={}, remaining={}, required={}",
                    userId, modelId, quota.getRemainingQuota(), estimatedTokens);
        }

        return sufficient;
    }

    /**
     * 扣除配额并记录到消息
     *
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param messageId 消息ID
     * @param promptTokens 输入 token 数
     * @param completionTokens 输出 token 数
     * @param totalTokens 总 token 数
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductQuotaAndRecord(Long userId, String modelId, Long messageId,
                                     Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        if (totalTokens == null || totalTokens <= 0) {
            log.warn("Invalid token count, skip deduction: userId={}, modelId={}, totalTokens={}",
                    userId, modelId, totalTokens);
            return;
        }

        // 1. 获取用户配额
        UserModelQuotaDO quota = getUserModelQuota(userId, modelId);
        if (quota == null) {
            log.error("User quota not found when deducting: userId={}, modelId={}", userId, modelId);
            throw new RuntimeException("用户配额不存在");
        }

        // 2. 检查配额是否足够（防御性检查）
        if (quota.getRemainingQuota() < totalTokens) {
            log.error("Insufficient quota when deducting: userId={}, modelId={}, remaining={}, required={}",
                    userId, modelId, quota.getRemainingQuota(), totalTokens);
            throw new RuntimeException("配额不足");
        }

        // 3. 更新配额
        UserModelQuotaDO updateQuota = new UserModelQuotaDO();
        updateQuota.setId(quota.getId());
        updateQuota.setUsedQuota(quota.getUsedQuota() + totalTokens);
        updateQuota.setRemainingQuota(quota.getRemainingQuota() - totalTokens);
        updateQuota.setTotalUsed(quota.getTotalUsed() + totalTokens);
        updateQuota.setLastUsedTime(new Date());
        updateQuota.setUpdateTime(new Date());

        userModelQuotaMapper.updateById(updateQuota);

        // 4. 更新消息的 token 字段
        ChatMessageDO message = new ChatMessageDO();
        message.setId(messageId);
        message.setPromptTokens(promptTokens);
        message.setCompletionTokens(completionTokens);
        message.setTotalTokens(totalTokens);

        chatMessageMapper.updateById(message);

        log.info("Quota deducted successfully: userId={}, modelId={}, messageId={}, tokens={}, remaining={}",
                userId, modelId, messageId, totalTokens, updateQuota.getRemainingQuota());
    }

    /**
     * 获取用户指定模型的配额信息
     *
     * @param userId 用户ID
     * @param modelId 模型ID
     * @return 配额VO
     */
    public UserModelQuotaVO getUserModelQuotaVO(Long userId, String modelId) {
        UserModelQuotaDO quotaDO = getUserModelQuota(userId, modelId);
        if (quotaDO == null) {
            return null;
        }

        return convertToVO(quotaDO);
    }

    /**
     * 获取用户所有模型的配额信息
     *
     * @param userId 用户ID
     * @return 配额列表
     */
    public List<UserModelQuotaVO> getAllModelQuotas(Long userId) {
        LambdaQueryWrapper<UserModelQuotaDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserModelQuotaDO::getUserId, userId)
                .orderByDesc(UserModelQuotaDO::getUpdateTime);

        List<UserModelQuotaDO> quotaList = userModelQuotaMapper.selectList(wrapper);

        return quotaList.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 充值配额
     *
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param amount 充值数量
     * @param operatorId 操作员ID
     * @param operatorName 操作员名称
     * @param reason 充值原因
     * @param remark 备注
     */
    @Transactional(rollbackFor = Exception.class)
    public void rechargeQuota(Long userId, String modelId, Long amount,
                              Long operatorId, String operatorName, String reason, String remark) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("充值数量必须大于0");
        }

        // 1. 获取或创建用户配额
        UserModelQuotaDO quota = getUserModelQuota(userId, modelId);
        if (quota == null) {
            // 如果用户没有该模型的配额，创建一个
            quota = new UserModelQuotaDO();
            quota.setUserId(userId);
            quota.setModelId(modelId);
            quota.setTotalQuota(0L);
            quota.setUsedQuota(0L);
            quota.setRemainingQuota(0L);
            quota.setTotalUsed(0L);
            quota.setCreateTime(new Date());
            quota.setUpdateTime(new Date());
            userModelQuotaMapper.insert(quota);
        }

        Long beforeQuota = quota.getRemainingQuota();
        Long afterQuota = beforeQuota + amount;

        // 2. 更新配额
        UserModelQuotaDO updateQuota = new UserModelQuotaDO();
        updateQuota.setId(quota.getId());
        updateQuota.setTotalQuota(quota.getTotalQuota() + amount);
        updateQuota.setRemainingQuota(afterQuota);
        updateQuota.setUpdateTime(new Date());

        userModelQuotaMapper.updateById(updateQuota);

        // 3. 记录充值记录
        QuotaRechargeRecordDO record = new QuotaRechargeRecordDO();
        record.setUserId(userId);
        record.setModelId(modelId);
        record.setRechargeAmount(amount);
        record.setBeforeQuota(beforeQuota);
        record.setAfterQuota(afterQuota);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setReason(reason);
        record.setRemark(remark);
        record.setCreateTime(new Date());

        rechargeRecordMapper.insert(record);

        log.info("Quota recharged successfully: userId={}, modelId={}, amount={}, before={}, after={}",
                userId, modelId, amount, beforeQuota, afterQuota);
    }

    /**
     * 为新用户初始化配额
     *
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void initUserQuota(Long userId) {
        // 获取所有启用的默认配额配置
        LambdaQueryWrapper<DefaultQuotaConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DefaultQuotaConfigDO::getEnabled, 1)
                .orderByDesc(DefaultQuotaConfigDO::getPriority);

        List<DefaultQuotaConfigDO> configs = defaultQuotaConfigMapper.selectList(wrapper);

        if (configs.isEmpty()) {
            log.warn("No default quota config found, skip initialization: userId={}", userId);
            return;
        }

        // 为每个模型创建配额
        for (DefaultQuotaConfigDO config : configs) {
            // 检查是否已存在
            UserModelQuotaDO existing = getUserModelQuota(userId, config.getModelId());
            if (existing != null) {
                log.info("User quota already exists, skip: userId={}, modelId={}", userId, config.getModelId());
                continue;
            }

            // 创建配额
            UserModelQuotaDO quota = new UserModelQuotaDO();
            quota.setUserId(userId);
            quota.setModelId(config.getModelId());
            quota.setTotalQuota(config.getDefaultQuota());
            quota.setUsedQuota(0L);
            quota.setRemainingQuota(config.getDefaultQuota());
            quota.setTotalUsed(0L);
            quota.setCreateTime(new Date());
            quota.setUpdateTime(new Date());

            userModelQuotaMapper.insert(quota);

            log.info("User quota initialized: userId={}, modelId={}, quota={}",
                    userId, config.getModelId(), config.getDefaultQuota());
        }
    }

    /**
     * 获取用户模型配额 DO
     */
    private UserModelQuotaDO getUserModelQuota(Long userId, String modelId) {
        LambdaQueryWrapper<UserModelQuotaDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserModelQuotaDO::getUserId, userId)
                .eq(UserModelQuotaDO::getModelId, modelId);

        return userModelQuotaMapper.selectOne(wrapper);
    }

    /**
     * 转换 DO 为 VO
     */
    private UserModelQuotaVO convertToVO(UserModelQuotaDO quotaDO) {
        UserModelQuotaVO vo = new UserModelQuotaVO();
        BeanUtils.copyProperties(quotaDO, vo);

        // 计算使用率
        if (quotaDO.getTotalQuota() > 0) {
            double rate = (double) quotaDO.getUsedQuota() / quotaDO.getTotalQuota() * 100;
            vo.setUsageRate(Math.round(rate * 100.0) / 100.0); // 保留两位小数
        } else {
            vo.setUsageRate(0.0);
        }

        // 查询模型名称（从默认配置中获取）
        LambdaQueryWrapper<DefaultQuotaConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DefaultQuotaConfigDO::getModelId, quotaDO.getModelId());
        DefaultQuotaConfigDO config = defaultQuotaConfigMapper.selectOne(wrapper);
        if (config != null) {
            vo.setModelName(config.getModelName());
        } else {
            vo.setModelName(quotaDO.getModelId());
        }

        return vo;
    }
}
