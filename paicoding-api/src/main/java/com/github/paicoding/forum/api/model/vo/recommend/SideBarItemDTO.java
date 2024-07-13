package com.github.paicoding.forum.api.model.vo.recommend;

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
public class SideBarItemDTO {

    private String title;

    private String name;

    private String url;

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
}
