package com.github.paicoding.forum.service.chatgpt.constants;

/**
 * @author YiHui
 * @date 2023/6/2
 */
public class ChatGptConstants {
    /**
     * 记录每个用户的使用次数
     */
    public static final String USER_RATE_LIMIT_KEY = "chaptGpt.rates";

    /**
     * 每个用户的最大使用次数
     */
    public static final int MAX_CHATGPT_QAS_CNT = 50;

    /**
     * 两次提问的间隔时间，要求20s
     */
    public static final long QAS_TIME_INTERVAL = 20_000;


    public static final String CHAT_REPLY_RECOMMEND = "请注册技术派之后再来体验吧，技术派官网： \n https://paicoding.com";
    public static final String CHAT_REPLY_BEGIN = "让我们开始体验ChatGPT的魅力吧~";
    public static final String CHAT_REPLY_OVER = "体验结束，让我们下次再见吧~";
    public static final String CHAT_REPLY_CNT_OVER = "次数使用完了哦，勾搭一下群主，多申请点使用次数吧~\n微信：lml200701158";


    public static final String CHAT_REPLY_TIME_WAITING = "chatgpt还在努力回答中，请继续等待....";
    public static final String CHAT_REPLY_QAS_TOO_FAST = "提问太频繁了，请稍后...";
}
