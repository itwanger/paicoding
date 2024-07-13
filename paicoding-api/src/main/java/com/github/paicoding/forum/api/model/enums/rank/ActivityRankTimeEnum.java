package com.github.paicoding.forum.api.model.enums.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活跃排行榜时间周期
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@AllArgsConstructor
@Getter
public enum ActivityRankTimeEnum {
    DAY(1, "day"),
    MONTH(2, "month"),
    ;

    private int type;
    private String desc;

    public static ActivityRankTimeEnum nameOf(String name) {
        if (DAY.desc.equalsIgnoreCase(name)) {
            return DAY;
        } else if (MONTH.desc.equalsIgnoreCase(name)) {
            return MONTH;
        }
        return null;
    }
}
