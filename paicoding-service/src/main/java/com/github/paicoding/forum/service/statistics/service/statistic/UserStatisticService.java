package com.github.paicoding.forum.service.statistics.service.statistic;

/**
 * 用户统计服务
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface UserStatisticService {
    /**
     * 添加在线人数
     *
     * @param add 正数，表示添加在线人数；负数，表示减少在线人数
     * @return
     */
    default void incrOnlineUserCnt(int add){};


    /**
     * 减少在线人数
     *
     * @param decr
     * @return
     */
    default void decrOnlineUserCnt(int decr){}

    /**
     * 使session失效
     * @param sessionStr
     */
    default void invalidateSession(String sessionStr){
        decrOnlineUserCnt(1);
    }

    /**
     * 查询在线用户人数
     *
     * @return
     */
    int getOnlineUserCnt();

    /**
     * 查询某用户是否在线
     */
    boolean isOnline(String sessionStr);

    /**
     * 更新session的过期时间
     */
    void updateSessionExpireTime(String sessionStr);
}
