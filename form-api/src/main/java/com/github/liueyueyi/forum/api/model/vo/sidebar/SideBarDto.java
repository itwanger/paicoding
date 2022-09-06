package com.github.liueyueyi.forum.api.model.vo.sidebar;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 侧边推广信息
 *
 * @author YiHui
 * @date 2022/9/6
 */
@Data
@Accessors(chain = true)
public class SideBarDto {

    private String title;

    private String subTitle;

    private String icon;

    private String img;

    private String content;

    private List<SideBarItemDto> items;

    /**
     * 侧边栏样式
     */
    private Integer style;
}
