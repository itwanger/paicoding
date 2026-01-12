package com.github.paicoding.forum.api.model.vo.recommend;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class SideBarItemDTO {

    private String title;

    private String name;

    private String url;

    @JsonSerialize(using = CdnImgSerializer.class)
    private String img;

    private Long time;

    /**
     * tag列表
     */
    private List<Integer> tags;

    /**
     * 评分信息
     */
    private RateVisitDTO visit;

    public SideBarItemDTO setImg(String img) {
        this.img = CdnUtil.autoTransCdn(img);
        return this;
    }
}
