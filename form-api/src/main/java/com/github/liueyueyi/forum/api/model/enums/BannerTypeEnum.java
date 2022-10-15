package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

/**
 * Banner类型枚举
 *
 * @author louzai
 * @since 2022/7/19
 */
@Getter
public enum BannerTypeEnum {

    EMPTY(0, ""),
    HOME_PAGE(1, "首页Banner"),
    SIDE_PAGE(2, "侧边Banner"),
    Advertisement(3, "广告Banner");

    BannerTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final Integer code;
    private final String desc;

    public static BannerTypeEnum formCode(Integer code) {
        for (BannerTypeEnum value : BannerTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return BannerTypeEnum.EMPTY;
    }
}
