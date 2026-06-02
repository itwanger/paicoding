package com.github.paicoding.forum.service.chatai.bot;

import com.github.paicoding.forum.api.model.context.ReqInfoContext;
import com.github.paicoding.forum.api.model.enums.ChatAnswerTypeEnum;
import com.github.paicoding.forum.api.model.enums.ai.AiBotEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.user.dto.BaseUserInfoDTO;
import com.github.paicoding.forum.core.async.AsyncUtil;
import com.github.paicoding.forum.service.chatai.ChatFacade;
import com.github.paicoding.forum.service.user.repository.dao.UserDao;
import com.github.paicoding.forum.service.user.repository.entity.UserInfoDO;
import com.github.paicoding.forum.service.user.service.RegisterService;
import com.github.paicoding.forum.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private UserDao userDao;

    private Map<AiBotEnum, BaseUserInfoDTO> botUsers = new HashMap<>();

    /**
     * 在应用完全启动后初始化 AI 机器人用户
     * 使用 ApplicationReadyEvent 确保 Liquibase 已完成数据库表创建
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initBotUser() {
        for (AiBotEnum bot : AiBotEnum.values()) {
            BaseUserInfoDTO user = userService.queryUserByLoginName(bot.getUserName());
            String avatarUrl = buildAvatarUrl(bot.getAvatar());
            
            if (user == null) {
                Long userId = registerService.registerSystemUser(bot.getUserName(), bot.getNickName(), avatarUrl);
                user = userService.queryBasicUserInfo(userId);
            } else {
                UserInfoDO userInfoDO = userDao.getByUserId(user.getUserId());
                if (!avatarUrl.equals(userInfoDO.getPhoto())) {
                    userInfoDO.setPhoto(avatarUrl);
                    userDao.updateUserInfo(userInfoDO);
                    user = userService.queryBasicUserInfo(user.getUserId());
                }
            }
            botUsers.put(bot, user);
        }
    }

    private String buildAvatarUrl(String avatarPath) {
        if (avatarPath == null || avatarPath.isEmpty() || avatarPath.startsWith("http://") || avatarPath.startsWith("https://")) {
            return avatarPath;
        }
        return avatarPath;
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

            // 机器人回复统一使用后台 AI 模型配置选择的模型。
            chatFacade.autoChat(question, vo -> {
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
     * 使用后台 AI 模型配置选择的模型同步生成机器人回复，适合需要稳定完成回调的 SSE 场景。
     */
    public void triggerSync(AiBotEnum bot, String question, String sourceBizId, Consumer<String> consumer) {
        BaseUserInfoDTO user = botUsers.get(bot);
        AsyncUtil.execute(() -> {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUser(user);
            reqInfo.setUserId(user.getUserId());
            reqInfo.setChatId(sourceBizId);
            ReqInfoContext.addReqInfo(reqInfo);

            try {
                ChatItemVo item = chatFacade.chat(chatFacade.getRecommendAiSource(), question).getRecords().get(0);
                consumer.accept(item.getAnswer());
            } catch (Exception e) {
                consumer.accept(StringUtils.defaultIfBlank(e.getMessage(), "AI 回复生成失败，请稍后再试"));
            } finally {
                ReqInfoContext.clear();
            }
        });
    }

    /**
     * 触发 AI 机器人，并将流式中间结果持续回调给调用方。
     */
    public void triggerStream(AiBotEnum bot, String question, String sourceBizId, Consumer<ChatItemVo> consumer) {
        BaseUserInfoDTO user = botUsers.get(bot);
        AsyncUtil.execute(() -> {
            ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo();
            reqInfo.setUser(user);
            reqInfo.setUserId(user.getUserId());
            reqInfo.setChatId(sourceBizId);
            ReqInfoContext.addReqInfo(reqInfo);

            try {
                chatFacade.autoChat(question, vo -> {
                    if (vo == null || vo.getRecords() == null || vo.getRecords().isEmpty()) {
                        return;
                    }

                    ChatItemVo item = vo.getRecords().get(0);
                    if (item.getAnswerType() == null && StringUtils.isBlank(item.getAnswer())) {
                        item.initAnswer("AI 回复生成失败，请稍后再试", ChatAnswerTypeEnum.STREAM_END);
                    }
                    try {
                        consumer.accept(item);
                    } finally {
                        if (item.getAnswerType() == ChatAnswerTypeEnum.JSON
                                || item.getAnswerType() == ChatAnswerTypeEnum.TEXT
                                || item.getAnswerType() == ChatAnswerTypeEnum.STREAM_END) {
                            ReqInfoContext.clear();
                        }
                    }
                });
            } catch (Exception e) {
                try {
                    consumer.accept(new ChatItemVo()
                            .initAnswer("AI 回复生成失败，请稍后再试", ChatAnswerTypeEnum.STREAM_END));
                } finally {
                    ReqInfoContext.clear();
                }
            }
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
