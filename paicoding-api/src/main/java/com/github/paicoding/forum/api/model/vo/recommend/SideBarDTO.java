package com.github.paicoding.forum.api.model.vo.recommend;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.paicoding.forum.api.model.enums.SidebarStyleEnum;
import com.github.paicoding.forum.api.model.util.cdn.CdnImgSerializer;
import com.github.paicoding.forum.api.model.util.cdn.CdnUtil;
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
public class SideBarDTO {

    private String title;

    private String subTitle;

    @JsonSerialize(using = CdnImgSerializer.class)
    private String icon;

    @JsonSerialize(using = CdnImgSerializer.class)
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

    public SideBarDTO setIcon(String icon) {
        this.icon = CdnUtil.autoTransCdn( icon);
        return this;
    }

    public SideBarDTO setImg(String img) {
        this.img = CdnUtil.autoTransCdn( img);
        return this;
    }
}
