package com.github.paicoding.forum.web.hook.listener;

import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.statistics.service.UserStatisticService;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * 通过监听session来实现实时人数统计
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@WebListener
public class OnlineUserCountListener implements HttpSessionListener {


    /**
     * 新增session，在线人数统计数+1
     *
     * @param se
     */
    public void sessionCreated(HttpSessionEvent se) {
        HttpSessionListener.super.sessionCreated(se);
        SpringUtil.getBean(UserStatisticService.class).incrOnlineUserCnt(1);
    }

    /**
     * session失效，在线人数统计数-1
     *
     * @param se
     */
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSessionListener.super.sessionDestroyed(se);
        SpringUtil.getBean(UserStatisticService.class).incrOnlineUserCnt(-1);
    }
}
