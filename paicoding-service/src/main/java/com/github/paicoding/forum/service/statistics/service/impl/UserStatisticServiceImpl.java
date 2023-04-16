package com.github.paicoding.forum.service.statistics.service.impl;

import com.github.paicoding.forum.service.statistics.service.UserStatisticService;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户统计服务
 *
 * @author YiHui
 * @date 2023/3/26
 */
@Service
public class UserStatisticServiceImpl implements UserStatisticService {

    /**
     * 对于单机的场景，可以直接使用本地局部变量来实现计数
     * 对于集群的场景，可考虑借助 redis的zset 来实现集群的在线用户人数统计
     */
    private AtomicInteger onlineUserCnt = new AtomicInteger(0);

    /**
     * 添加在线人数
     *
     * @param add 正数，表示添加在线人数；负数，表示减少在线人数
     * @return
     */
    public int incrOnlineUserCnt(int add) {
        return onlineUserCnt.addAndGet(add);
    }

    /**
     * 查询在线用户人数
     *
     * @return
     */
    public int getOnlineUserCnt() {
        return onlineUserCnt.get();
    }

}
