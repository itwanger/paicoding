package com.github.paicoding.forum.api.model.vo.statistics.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 统计计数
 *
 * @author louzai
 * @date 2022-10-1
 */
@Data
@Builder
public class StatisticsCountDTO {

    /**
     * PV 数量
     */
    private Long pvCount;

    /**
     * 总用户数
     */
    private Long userCount;

    /**
     * 文章数量
     */
    private Long articleCount;
}
