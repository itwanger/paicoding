package com.github.paicoding.forum.service.statistics.service.statistic.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.paicoding.forum.service.statistics.service.statistic.UserStatisticService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: pai_coding
 * @description: 使用caffeine缓存实现在线人数的统计
 * @author: XuYifei
 * @create: 2024-10-21
 */

public class UserStatisticServiceCaffeineImpl implements UserStatisticService {

    @Autowired
    private Cache<String, Boolean> sessionCache;

    @Override
    public void invalidateSession(String sessionStr) {
        sessionCache.invalidate(sessionStr);
    }

    @Override
    public int getOnlineUserCnt() {
        return (int) sessionCache.estimatedSize();
    }

    @Override
    public boolean isOnline(String sessionStr) {
        return sessionCache.asMap().containsKey(sessionStr);
    }

    @Override
    public void updateSessionExpireTime(String sessionStr) {
        sessionCache.put(sessionStr, true);
    }
}
