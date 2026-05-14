package com.github.paicoding.forum.service.user.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.paicoding.forum.service.user.repository.entity.UserShareRiskAccountDO;
import com.github.paicoding.forum.service.user.repository.mapper.UserShareRiskAccountMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 疑似共享账号状态
 *
 * @author Codex
 * @date 2026/5/12
 */
@Repository
public class UserShareRiskAccountDao extends ServiceImpl<UserShareRiskAccountMapper, UserShareRiskAccountDO> {
    public void upsert(UserShareRiskAccountDO account) {
        if (account == null || account.getUserId() == null) {
            return;
        }
        baseMapper.upsert(account);
    }

    public UserShareRiskAccountDO getByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return lambdaQuery()
                .eq(UserShareRiskAccountDO::getUserId, userId)
                .last("limit 1")
                .one();
    }

    public Map<Long, UserShareRiskAccountDO> mapByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserShareRiskAccountDO> list = lambdaQuery()
                .in(UserShareRiskAccountDO::getUserId, userIds)
                .list();
        return list.stream().collect(Collectors.toMap(
                UserShareRiskAccountDO::getUserId,
                Function.identity(),
                (left, right) -> left));
    }
}
