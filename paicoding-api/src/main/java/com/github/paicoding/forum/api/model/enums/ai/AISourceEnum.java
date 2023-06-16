package com.github.paicoding.forum.api.model.enums.ai;

/**
 * @author YiHui
 * @date 2023/6/9
 */
public enum AISourceEnum {
    /**
     * chatgpt 3.5
     */
    CHAT_GPT_3_5("chatGpt3.5"),
    /**
     * chatgpt 4
     */
    CHAT_GPT_4("chatGpt4"),
    /**
     * 技术派的模拟AI
     */
    PAI_AI("技术派"),
    /**
     * 讯飞
     */
    XUN_FEI_AI("讯飞") {
        @Override
        public boolean syncSupport() {
            return false;
        }
    },
    ;

    private String name;

    AISourceEnum(String name) {
        this.name = name;
    }

    /**
     * 是否支持同步
     *
     * @return
     */
    public boolean syncSupport() {
        return true;
    }

    /**
     * 是否支持异步
     *
     * @return
     */
    public boolean asyncSupport() {
        return true;
    }
}
