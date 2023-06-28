package com.github.paicoding.forum.service.chatai;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.service.chatai.service.ChatService;
import com.github.paicoding.forum.service.chatai.service.impl.chatgpt.ChatGptIntegration;
import com.github.paicoding.forum.service.chatai.service.impl.xunfei.XunFeiIntegration;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 聊天的门面类
 *
 * @author YiHui
 * @date 2023/6/9
 */
@Slf4j
@Service
public class ChatFacade {
    private final Map<AISourceEnum, ChatService> chatServiceMap;

    public ChatFacade(List<ChatService> chatServiceList) {
        chatServiceMap = Maps.newHashMapWithExpectedSize(chatServiceList.size());
        for (ChatService chatService : chatServiceList) {
            chatServiceMap.put(chatService.source(), chatService);
        }
    }

    /**
     * 自动根据AI的支持方式，选择同步/异步的交互方式
     *
     * @param source
     * @param question
     * @param callback
     * @return
     */
    public ChatRecordsVo autoChat(AISourceEnum source, String question, Consumer<ChatRecordsVo> callback) {
        if (source.asyncSupport() && chatServiceMap.get(source).asyncFirst()) {
            // 支持异步且异步优先的场景下，自动选择异步方式进行聊天
            return asyncChat(source, question, callback);
        }
        return chat(source, question, callback);
    }

    /**
     * 开始聊天
     *
     * @param question
     * @param source
     * @return
     */
    public ChatRecordsVo chat(AISourceEnum source, String question) {
        return chatServiceMap.get(source).chat(ReqInfoContext.getReqInfo().getUserId(), question);
    }

    /**
     * 开始聊天
     *
     * @param question
     * @param source
     * @return
     */
    public ChatRecordsVo chat(AISourceEnum source, String question, Consumer<ChatRecordsVo> callback) {
        return chatServiceMap.get(source).chat(ReqInfoContext.getReqInfo().getUserId(), question, callback);
    }

    /**
     * 异步聊天的方式
     *
     * @param source
     * @param question
     */
    public ChatRecordsVo asyncChat(AISourceEnum source, String question, Consumer<ChatRecordsVo> callback) {
        return chatServiceMap.get(source).asyncChat(ReqInfoContext.getReqInfo().getUserId(), question, callback);
    }


    /**
     * 返回历史聊天记录
     *
     * @param source
     * @return
     */
    public ChatRecordsVo history(AISourceEnum source) {
        return chatServiceMap.get(source).getChatHistory(ReqInfoContext.getReqInfo().getUserId());
    }


    /**
     * 返回推荐的AI模型
     *
     * @return
     */
    public AISourceEnum getRecommendAiSource() {
        AISourceEnum source;
        try {
            ChatGptIntegration.ChatGptConfig config = SpringUtil.getBean(ChatGptIntegration.ChatGptConfig.class);
            if (!CollectionUtils.isEmpty(config.getConf().get(config.getMain()).getKeys())) {
                source = AISourceEnum.CHAT_GPT_3_5;
            } else if (StringUtils.isNotBlank(SpringUtil.getBean(XunFeiIntegration.XunFeiConfig.class).getApiKey())) {
                source = AISourceEnum.XUN_FEI_AI;
            } else {
                source = AISourceEnum.PAI_AI;
            }
        } catch (Exception e) {
            source = AISourceEnum.PAI_AI;
        }
        log.info("当前选中的AI模型：{}", source);
        return source;
    }
}
