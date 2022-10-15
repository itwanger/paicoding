package com.github.liueyueyi.forum.api.model.enums;

import lombok.Getter;

/**
 * @author YiHui
 * @date 2022/9/6
 */
@Getter
public enum SidebarStyleEnum {

    NOTICE(1),
    ARTICLES(2),
    RECOMMEND(3),
    ABOUT(4);

    private int style;

    SidebarStyleEnum(int style) {
        this.style = style;
    }
}
