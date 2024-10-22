package com.github.paicoding.forum.service.statistics.service.statistic.impl;

import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.statistics.service.statistic.UserStatisticService;

/**
 * @program: pai_coding
 * @description:
 * @author: XuYifei
 * @create: 2024-10-21
 */
public class UserStatisticServiceRedisImpl implements UserStatisticService {

    private static final String ONLINE_USER_CNT_KEY = "online_user_cnt";

    @Override
    public void incrOnlineUserCnt(int cnt) {
        int onlineUserCnt = getOnlineUserCnt();
        RedisClient.setStr(ONLINE_USER_CNT_KEY, String.valueOf(onlineUserCnt + cnt));
    }

    @Override
    public int getOnlineUserCnt() {
        int onlineUserCnt = 0;
        try {
            onlineUserCnt = Integer.parseInt(RedisClient.getStr(ONLINE_USER_CNT_KEY));
        } catch (Exception e) {
            RedisClient.setStr(ONLINE_USER_CNT_KEY, "0");
        }
        return onlineUserCnt;
    }

    @Override
    public boolean isOnline(String sessionStr) {
        return false;
    }

    @Override
    public void updateSessionExpireTime(String sessionStr) {
    }
}
