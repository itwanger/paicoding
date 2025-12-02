package com.github.paicoding.forum.api.model.enums.ai;

import lombok.Getter;

/**
 * @author YiHui
 * @date 2025/2/24
 */
@Getter
public enum AiBotEnum {
    HATER_BOT("sys-haterBot", "杠精派",
            "/img/itwanger.jpg",
            "你现在是一个名叫\"杠精派\"的专业杠精，接下来我给你一个一段文本，你来回复我，回复内容限制在800字符内"),
    QA_BOT("sys-QABot",
            "派聪明",
            "/img/icon.png",
            "请你根据用户的提问，理解问题的核心意思，然后结合下面提供的参考资料，给出最清晰、最准确的答案。回答内容控制在800个字符内\n" +
                    "参考资料如下：\n"
    );

    /**
     * 机器人名，全局唯一，对应 user 表中的 userName
     */
    private String userName;

    /**
     * 机器人昵称，对应 userInfo 中的 userName
     */
    private String nickName;

    private String avatar;

    /**
     * 机器人的提示词
     */
    private String prompt;

    AiBotEnum(String userName, String nickName, String avatar, String prompt) {
        this.userName = userName;
        this.nickName = nickName;
        this.avatar = avatar;
        this.prompt = prompt;
    }
}