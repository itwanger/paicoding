package com.github.paicoding.forum.api.model.enums.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活跃排行榜时间周期
 *
 * @author YiHui
 * @date 2023/8/19
 */
@AllArgsConstructor
@Getter
public enum ActivityRankTimeEnum {
    DAY(1, "天"),
    MONTH(2, "月"),
    ;

    private int type;
    private String desc;
}
