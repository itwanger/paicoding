package com.github.paicoding.forum.service.chatai.bot;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 基于大模型的杠精机器人
 *
 * @author YiHui
 * @date 2025/2/24
 */
@Component
public class AiBotService {

    @Autowired
    private ChatFacade chatFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private RegisterService registerService;

    private Map<AiBotEnum, BaseUserInfoDTO> botUsers = new HashMap<>();

    /**
     * 在应用完全启动后初始化 AI 机器人用户
     * 使用 ApplicationReadyEvent 确保 Liquibase 已完成数据库表创建
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initBotUser() {
        for (AiBotEnum bot : AiBotEnum.values()) {
            BaseUserInfoDTO user = userService.queryUserByLoginName(bot.getUserName());
            if (user == null) {
                // 避免某些同学本地使用的版本，无法借助Liquid实现自动初始化AI机器人；我们这里加一个兜底的创建逻辑
                Long userId = registerService.registerSystemUser(bot.getUserName(), bot.getNickName(), bot.getAvatar());
                user = userService.queryBasicUserInfo(userId);
            }
            botUsers.put(bot, user);
        }
    }

    /**
     * 触发AI机器人
     *
     * @param question
     * @return
     */
    public void trigger(AiBotEnum bot, String question, String sourceBizId, Consumer<String> consumer) {
        BaseUserInfoDTO user = botUsers.get(bot);
        AsyncUtil.execute(() -> {
            // 设置AI机器人问答上下文
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUser(user);
            reqInfo.setUserId(user.getUserId());
            reqInfo.setChatId(sourceBizId);
            ReqInfoContext.addReqInfo(reqInfo);

            // 机器人，默认使用智谱模型
            chatFacade.autoChat(AISourceEnum.ZHI_PU_AI, question, vo -> {
                ChatItemVo item = vo.getRecords().get(0);
                if (item.getAnswerType() == ChatAnswerTypeEnum.JSON
                        || item.getAnswerType() == ChatAnswerTypeEnum.TEXT
                        || item.getAnswerType() == ChatAnswerTypeEnum.STREAM_END) {
                    try {
                        consumer.accept(item.getAnswer());
                    } finally {
                        // 清空上下文信息
                        ReqInfoContext.clear();
                    }
                }
            });
        });
    }

    /**
     * 获取杠精机器人相关信息
     *
     * @return
     */
    public AiBotEnum getBotEnumByUserId(Long userId) {
        for (Map.Entry<AiBotEnum, BaseUserInfoDTO> entry : botUsers.entrySet()) {
            if (Objects.equals(entry.getValue().getUserId(), userId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public BaseUserInfoDTO getBotEnumByUserId(AiBotEnum bot) {
        return botUsers.get(bot);
    }
}
