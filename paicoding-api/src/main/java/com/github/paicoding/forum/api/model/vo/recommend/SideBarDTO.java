package com.github.paicoding.forum.api.model.vo.recommend;

import com.github.paicoding.forum.api.model.enums.SidebarStyleEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 侧边推广信息
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@Accessors(chain = true)
public class SideBarDTO {

    private String title;

    private String subTitle;

    private String icon;

    private String img;

    private String url;

    private String content;

    private List<SideBarItemDTO> items;

    /**
     * 侧边栏样式
     *
     * @see SidebarStyleEnum#getStyle()
     */
    private Integer style;
}
