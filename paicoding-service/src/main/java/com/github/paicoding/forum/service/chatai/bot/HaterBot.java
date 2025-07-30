package com.github.paicoding.forum.service.chatai.bot;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import com.github.paicoding.forum.service.chatai.constants.ChatConstants;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.UserService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 基于大模型的杠精机器人
 *
 * @author YiHui
 * @date 2025/2/24
 */
@Component
public class HaterBot {

    @Autowired
    private ChatFacade chatFacade;

    @Autowired
    private UserService userService;

    @Autowired
    private RegisterService registerService;

    private Supplier<BaseUserInfoDTO> haterBotUser = Suppliers.memoizeWithExpiration(() -> {
        BaseUserInfoDTO user = userService.queryUserByLoginName(AiBotEnum.HATER_BOT.getUserName());
        if (user == null) {
            // 避免某些同学本地使用的版本，无法借助Liquid实现自动初始化AI机器人；我们这里加一个兜底的创建逻辑
            Long userId = registerService.registerSystemUser(AiBotEnum.HATER_BOT.getUserName(), AiBotEnum.HATER_BOT.getUserName(), "https://cdn.tobebetterjavaer.com/paicoding/e0f01d775d3f67b309b394bc04d4e091.jpg");
            user = userService.queryBasicUserInfo(userId);
        }
        return user;
    }, 1, TimeUnit.HOURS);

    /**
     * 触发AI机器人
     *
     * @param question
     * @return
     */
    public void trigger(String question, String sourceBizId, Consumer<String> consumer) {
        BaseUserInfoDTO user = haterBotUser.get();
        AsyncUtil.execute(() -> {
            // 设置AI机器人问答上下文
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUser(user);
            reqInfo.setUserId(user.getUserId());
            reqInfo.setChatId(sourceBizId);
            ReqInfoContext.addReqInfo(reqInfo);

            chatFacade.autoChat(AISourceEnum.DEEP_SEEK, question, vo -> {
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
    public BaseUserInfoDTO getBotUser() {
        return haterBotUser.get();
    }

    /**
     * 添加机器人提示词
     *
     * @param userId
     * @return
     */
    public ChatItemVo addPrompt(Long userId) {
        if (Objects.equals(userId, getBotUser().getUserId())) {
            return new ChatItemVo()
                    .setQuestion(ChatConstants.PROMPT_TAG + AiBotEnum.HATER_BOT.getPrompt());
        }
        return null;
    }
}
