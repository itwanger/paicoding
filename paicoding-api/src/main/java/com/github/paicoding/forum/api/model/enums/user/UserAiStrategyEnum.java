package com.github.paicoding.forum.api.model.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai可用次数的条件策略
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Getter
@AllArgsConstructor
public enum UserAiStrategyEnum {
    WECHAT(1),
    INVITE_USER(2),
    STAR_JAVA_GUIDE(4),
    STAR_TECH_PAI(8),
    ;

    /**
     * 二进制使用姿势
     * 第0位： = 1 表示已绑定微信公众号
     * 第1位： = 1 表示绑定了邀请用户
     * 第2位： = 1 表示绑定了java星球
     * 第3位： = 1 表示绑定了技术派星球
     */
    private Integer condition;

    public Integer updateCondition(Integer input) {
        if (input == null) {
            input = 0;
        }
        return input | condition;
    }

    public boolean match(Integer strategy) {
        return strategy != null && (strategy & condition) == condition.intValue();
    }
}
