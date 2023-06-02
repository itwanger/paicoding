package com.github.paicoding.forum.service.chatgpt.service;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.core.ai.ChatGptHelper;
import com.github.paicoding.forum.core.ai.ChatRecord;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.chatgpt.constants.ChatGptConstants;
import com.github.paicoding.forum.service.user.repository.entity.UserDO;
import com.github.paicoding.forum.service.user.service.UserService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

/**
 * chatgpt 服务：
 * <p>
 * 1. 同一个用户，只有50次的提问机会，采用redis计数
 * 2. 因为微信有5s的自动回复超时，因此需要做一个容错兼容，当执行超过3.5s就提前返回，将结果保存到内存中，等待下次交互再进行返回
 *
 * @author YiHui
 * @date 2023/6/2
 */
@Slf4j
@Service
public class ChatgptServiceImpl implements ChatgptService {
    @Autowired
    private ChatGptHelper chatGptHelper;
    private ExecutorService tp = Executors.newFixedThreadPool(2);

    private LoadingCache<Long, ChatRecord> chatCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(50).build(new CacheLoader<Long, ChatRecord>() {
                @Override
                public ChatRecord load(Long userId) throws Exception {
                    return new ChatRecord();
                }
            });

    private boolean rateLimit(Long userId) {
        Integer cnt = RedisClient.hGet(ChatGptConstants.USER_RATE_LIMIT_KEY, String.valueOf(userId), Integer.class);
        if (cnt == null) {
            cnt = 0;
        }
        return cnt < ChatGptConstants.MAX_CHATGPT_QAS_CNT;
    }

    private void incrCnt(Long userId) {
        RedisClient.hIncr(ChatGptConstants.USER_RATE_LIMIT_KEY, String.valueOf(userId), 1);
    }

    @Autowired
    private UserService userService;


    @Override
    public boolean inChat(String wxUuid, String content) {
        if (content.toLowerCase().trim().startsWith("chat")) {
            return true;
        }

        UserDO user = userService.getWxUser(wxUuid);
        if (user == null) {
            return false;
        }

        // 存在会话记录，表示在会话中
        chatCache.cleanUp();
        return chatCache.getIfPresent(user.getId()) != null;
    }

    @Override
    public String chat(String wxUuid, String content) {
        if (content.toLowerCase().trim().startsWith("chat")) {
            // 开始会话
            UserDO user = userService.getWxUser(wxUuid);
            if (user == null) {
                return ChatGptConstants.CHAT_REPLY_RECOMMEND;
            }

            chatCache.put(user.getId(), new ChatRecord());
            return ChatGptConstants.CHAT_REPLY_BEGIN;
        }

        if (content.toLowerCase().trim().equalsIgnoreCase("end") || content.trim().startsWith("结束")) {
            // 结束会话
            chatCache.cleanUp();
            return ChatGptConstants.CHAT_REPLY_OVER;
        }


        // 正常对话
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        if (!rateLimit(userId)) {
            // 次数已经用完了，直接返回
            chatCache.cleanUp();
            return ChatGptConstants.CHAT_REPLY_CNT_OVER;
        }

        // 判断用户的上一次访问结果有没有正确返回，如果没有，那么这一次的交互不响应，直接返回上一次的返回结果；
        ChatRecord chatRecord = chatCache.getUnchecked(userId);
        if (System.currentTimeMillis() - chatRecord.getQasTime() < ChatGptConstants.QAS_TIME_INTERVAL) {
            // 限制交互频率
            if (chatRecord.canReply()) {
                // 上次没有回复时；如果现在有结论了，那就回复一下
                return chatRecord.reply();
            } else {
                return ChatGptConstants.CHAT_REPLY_QAS_TOO_FAST;
            }
        }


        // 执行正常的提问、应答; 针对上次结果还没有拿到的场景做一个兼容，只有拿到结果之后，才继续响应后续的问答
        if (StringUtils.isBlank(chatRecord.getQas()) || chatRecord.isLastReturn()) {
            // 首次提问 或者上次的提问正确返回了结果
            ChatRecord newRecord = doQuery(userId, content, chatRecord);
            if (newRecord.canReply()) {
                return chatRecord.reply();
            } else {
                // 只有超时没拿到结果的场景，会走这里
                return ChatGptConstants.CHAT_REPLY_TIME_WAITING;
            }
        } else if (chatRecord.canReply()) {
            // 判断上次的结果是否已经获取到了
            return chatRecord.reply();
        } else {
            // 结果还没有拿到，继续等待
            return ChatGptConstants.CHAT_REPLY_TIME_WAITING;
        }
    }

    /**
     * 执行具体的chatgpt请求，并做一个超时的限制
     *
     * @param userId
     * @param content
     * @param currentChat
     * @return
     */
    private ChatRecord doQuery(Long userId, String content, ChatRecord currentChat) {
        // 访问计数+1
        incrCnt(userId);

        // 重新构建当前的聊天记录
        ChatRecord newRecord = new ChatRecord().setPre(currentChat);
        newRecord.setQas(content).setLastReturn(false).setQasTime(System.currentTimeMillis());
        currentChat.setNext(newRecord);
        chatCache.put(userId, newRecord);
        Future<Boolean> ans = tp.submit(() -> chatGptHelper.simpleGptReturn(userId, newRecord));
        try {
            ans.get(3500, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // 超时中断的场景
            newRecord.setLastReturn(false);
        } catch (Exception e) {
            log.warn("chatgpt出现了非预期异常! content:{}", content, e);
            newRecord.setLastReturn(true);
            if (newRecord.getSysErr() == null) {
                newRecord.setSysErr(e.getMessage());
            }
        }
        return newRecord;
    }

}
