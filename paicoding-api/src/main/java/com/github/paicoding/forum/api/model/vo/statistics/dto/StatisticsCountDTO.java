package com.github.paicoding.forum.api.model.vo.statistics.dto;

import lombok.Data;

/**
 * 统计计数
 *
 * @author louzai
 * @date 2022-10-1
 */
@Data
public class StatisticsCountDTO {

    /**
     * PV 数量
     */
    private Integer pvCount;

    /**
     * 总用户数
     */
    private Integer userCount;

    /**
     * 文章数量
     */
    private Integer articleCount;
}
