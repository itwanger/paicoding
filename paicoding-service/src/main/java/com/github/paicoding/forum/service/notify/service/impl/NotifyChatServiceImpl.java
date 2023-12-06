package com.github.paicoding.forum.service.notify.service.impl;

import com.beust.jcommander.internal.Sets;
import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.util.MapUtils;
import com.github.paicoding.forum.core.ws.WebSocketResponseUtil;
import com.github.paicoding.forum.service.notify.service.NotifyChatService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.paicoding.forum.service.notify.service.NotifyChatService.NotifyChatMsgType.SYSTEM_ADD_CHAT;
import static com.github.paicoding.forum.service.notify.service.NotifyChatService.NotifyChatMsgType.SYSTEM_CHAT_ONLINE;

/**
 * 私信聊天服务类
 *
 * @author YiHui
 * @date 2023/12/6
 */
@Slf4j
@Service
public class NotifyChatServiceImpl implements NotifyChatService {

    /**
     * 记录用户与对应的jwt token之间的缓存关系；用于websocket的广播通知
     */
    private LoadingCache<Long, Set<String>> wsUserSessionCache;

    /**
     * 聊天群组对应的用户列表
     */
    private LoadingCache<String, Map<Long, Integer>> channelUsers;

    /**
     * 临时会话缓存
     */
    private LoadingCache<String, String> tmpChannelCache;

    @PostConstruct
    public void init() {
        wsUserSessionCache = CacheBuilder.newBuilder()
                .maximumSize(200)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build(new CacheLoader<Long, Set<String>>() {
                    @Override
                    public Set<String> load(Long aLong) throws Exception {
                        return new HashSet<>();
                    }
                });

        channelUsers = CacheBuilder.newBuilder().maximumSize(500).expireAfterAccess(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, Map<Long, Integer>>() {
                    @Override
                    public Map<Long, Integer> load(String s) throws Exception {
                        return new HashMap<>();
                    }
                });

        tmpChannelCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterAccess(10, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
            @Override
            public String load(String s) throws Exception {
                return "";
            }
        });
    }

    @Override
    public void notifyToUser(Long userId, String msg) {
        wsUserSessionCache.getUnchecked(userId).forEach(s -> {
            WebSocketResponseUtil.sendMsgToUser(s, NOTICE_TOPIC, msg);
        });
    }

    /**
     * 用户建立连接时，添加用户信息
     *
     * @param userId  用户id
     * @param session jwt token
     */
    private void addUserToken(Long userId, String session) {
        wsUserSessionCache.getUnchecked(userId).add(session);
    }

    /**
     * 断开连接时，移除用户信息
     *
     * @param userId  用户id
     * @param session jwt token
     */
    private void releaseUserToken(Long userId, String session) {
        wsUserSessionCache.getUnchecked(userId).remove(session);
    }

    /**
     * 更新聊天群组的在线人数
     *
     * @param channel 聊天频道
     * @param userId  用户
     * @return
     */
    private void addToChannel(String channel, Long userId, String userName) {
        Map<Long, Integer> users = channelUsers.getUnchecked(channel);
        int sessionCnt = users.getOrDefault(userId, 0) + 1;
        users.put(userId, sessionCnt);

        // 更新再线人数
        WebSocketResponseUtil.broadcastMsg(channel, MapUtils.create("msgType", SYSTEM_CHAT_ONLINE, "content", users.size()));

        // 群聊，广播一个入群通知
        if (sessionCnt == 1) {
            WebSocketResponseUtil.broadcastMsg(channel, MapUtils.create("userId", userId, "msgType", SYSTEM_ADD_CHAT, "content", String.format("欢迎\"%s\"加入群聊", userName)));
        }
    }


    private void addChannelDesc(StompHeaderAccessor accessor) {
        // 保存会话信息
        List<String> list = accessor.getNativeHeader("title");
        if (CollectionUtils.isEmpty(list) || accessor.getDestination() == null) {
            return;
        }
        tmpChannelCache.put(accessor.getDestination(), list.get(0));
    }

    public String getTmpChatChannelInfo(String channelId) {
        return tmpChannelCache.getUnchecked("/msg/group/" + channelId);
    }

    /**
     * 用户连接释放，所在的群组数全部-1
     *
     * @param userId 用户
     */
    private void releaseConnect(Long userId) {
        Set<String> keys = Sets.newHashSet();
        for (Map.Entry<String, Map<Long, Integer>> entry : channelUsers.asMap().entrySet()) {
            Map<Long, Integer> users = entry.getValue();
            if (!users.containsKey(userId)) {
                continue;
            }

            int onlineCnt = users.get(userId) - 1;
            if (onlineCnt <= 0) {
                users.remove(userId);
                // 在线人数 -1
                WebSocketResponseUtil.broadcastMsg(entry.getKey(), MapUtils.create("msgType", SYSTEM_CHAT_ONLINE, "content", users.size()));
            } else {
                // 更新在线次数
                users.put(userId, onlineCnt);
            }

            if (users.isEmpty()) {
                keys.add(entry.getKey());
            }
        }
        channelUsers.invalidateAll(keys);
        channelUsers.cleanUp();
    }

    /**
     * WebSocket聊天的增强
     *
     * @param accessor
     */
    @Override
    public void chatWrapper(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (StringUtils.isBlank(destination) || accessor.getCommand() == null) {
            return;
        }


        // 全局私信、通知长连接入口
        ReqInfoContext.ReqInfo user = (ReqInfoContext.ReqInfo) accessor.getUser();
        if (user == null) {
            log.info("websocket用户未登录! {}", accessor);
            return;
        }
        switch (accessor.getCommand()) {
            case SUBSCRIBE:
                // 保存聊天信息
                addChannelDesc(accessor);
                // 加入群聊
                addUserToken(user.getUserId(), user.getSession());
                // 在线人数和变化
                addToChannel(destination, user.getUserId(), user.getUser().getUserName());
                break;
            case DISCONNECT:
                // 中断连接，改用户所在的群组人数-1
                releaseUserToken(user.getUserId(), user.getSession());
                releaseConnect(user.getUserId());
                break;
        }
    }
}
