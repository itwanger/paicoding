package com.github.paicoding.forum.api.model.enums.ai;

import lombok.Getter;

/**
 * @author YiHui
 * @date 2025/2/24
 */
@Getter
public enum AiBotEnum {
    HATER_BOT("haterBot", "杠精机器人", "你现在是一个名叫\"杠精机器人\"的专业杠精，接下来我给你一个一段文本，你来回复我，回复内容限制在800字符内"),
    ;

    /**
     * 机器人名，全局唯一，对应 user 表中的 userName
     */
    private String userName;

    /**
     * 机器人昵称，对应 userInfo 中的 userName
     */
    private String nickName;

    /**
     * 机器人的提示词
     */
    private String prompt;

    AiBotEnum(String userName, String nickName, String prompt) {
        this.userName = userName;
        this.nickName = nickName;
        this.prompt = prompt;
    }
}
