package com.github.paicoding.forum.api.model.enums.ai;

import lombok.Getter;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
@Getter
public enum AISourceEnum {
    /**
     * chatgpt 3.5
     */
    CHAT_GPT_3_5(0, "chatGpt3.5"),
    /**
     * chatgpt 4
     */
    CHAT_GPT_4(1, "chatGpt4"),
    /**
     * 技术派的模拟AI
     */
    PAI_AI(2, "技术派"),
    /**
     * 讯飞
     */
    XUN_FEI_AI(3,"讯飞") {
        @Override
        public boolean syncSupport() {
            return false;
        }
    },
    ;

    private String name;
    private Integer code;

    AISourceEnum(Integer code, String name) {
        this.code = code;
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
