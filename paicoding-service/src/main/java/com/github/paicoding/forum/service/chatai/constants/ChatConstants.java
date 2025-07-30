package com.github.paicoding.forum.service.chatai.constants;

import com.github.paicoding.forum.api.model.enums.ai.AISourceEnum;
import com.github.paicoding.forum.api.model.vo.chat.ChatItemVo;
import com.github.paicoding.forum.api.model.vo.chat.ChatRecordsVo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author YiHui
 * @date 2023/6/2
 */
public final class ChatConstants {
    /**
     * 记录每个用户的使用次数
     */
    public static String getAiRateKey(AISourceEnum ai) {
        return "chat.rates." + ai.name().toLowerCase();
    }

    public static String getAiRateKeyPerDay(AISourceEnum ai) {
        return "chat.rates." + ai.name().toLowerCase() + "-" + LocalDate.now();
    }

    /**
     * 对话列表缓存
     *
     * @param ai
     * @param user
     * @return
     */
    public static String getAiChatListKey(AISourceEnum ai, Long user) {
        return "chat.list." + ai.name().toLowerCase() + "." + user;
    }

    /**
     * 聊天历史记录
     *
     * @param ai
     * @param user
     * @return
     */
    public static String getAiHistoryRecordsKey(AISourceEnum ai, Long user) {
        return "chat.history." + ai.name().toLowerCase() + "." + user;
    }

    /**
     * 聊天历史记录
     *
     * @param ai
     * @param user
     * @return
     */
    public static String getAiHistoryRecordsKey(AISourceEnum ai, String user) {
        return "chat.history." + ai.name().toLowerCase() + "." + user;
    }

    /**
     * 聊天历史构建问答上下问
     *
     * @param chatList  聊天记录，包含历史聊天内容，最新的提问在前面
     * @param function 实体转换方式
     * @param <T>
     * @return
     */
    public static <T> List<T> toMsgList(List<ChatItemVo> chatList, Function<ChatItemVo, List<T>> function) {
        int qaCnt = chatList.size();
        List<T> ans = new ArrayList<>(qaCnt << 1);
        for (int i = qaCnt - 1; i >= 0; i--) {
            ans.addAll(function.apply(chatList.get(i)));
        }
        return ans;
    }

    /**
     * 每个用户的最大使用次数
     */
    public static final int MAX_CHATGPT_QAS_CNT = 10;

    /**
     * 最多保存的历史聊天记录
     */
    public static final int MAX_HISTORY_RECORD_ITEMS = 500;

    /**
     * 两次提问的间隔时间，要求20s
     */
    public static final long QAS_TIME_INTERVAL = 20_000;


    public static final String CHAT_REPLY_RECOMMEND = "请注册技术派之后再来体验吧，技术派官网： \n https://paicoding.com";
    public static final String CHAT_REPLY_BEGIN = "让我们开始体验ChatGPT的魅力吧~";
    public static final String CHAT_REPLY_OVER = "体验结束，让我们下次再见吧~";
    public static final String CHAT_REPLY_CNT_OVER = "次数使用完了哦，勾搭一下群主，多申请点使用次数吧~\n微信：itwanger";


    public static final String CHAT_REPLY_TIME_WAITING = "chatgpt还在努力回答中，请等待几秒之后再问一次吧....";
    public static final String CHAT_REPLY_QAS_TOO_FAST = "提问太频繁了，喝一杯咖啡，暂缓一下...";


    public static final String TOKEN_OVER = "您的免费次数已经使用完毕了!";

    /**
     * 异步聊天时返回得提示文案
     */
    public static final String ASYNC_CHAT_TIP = "小派正在努力回答中, 耐心等待一下吧...";

    /**
     * 请切换到其他大模型
     */
    public static final String SWITCH_TO_OTHER_MODEL = "当前模型还在开发当中，请右上角下拉框切换到其他模型";


    public static final String SENSITIVE_QUESTION = "提问中包含敏感词:%s，请微信联系二哥「itwanger」加入白名单!";

    /**
     * 提示词标识
     */
    public static final String PROMPT_TAG = "prompt-";
}
