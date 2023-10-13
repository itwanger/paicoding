package com.github.paicoding.forum.api.model.vo.statistics.dto;

import lombok.Data;

/**
 * 每天的统计计数
 *
 * @author louzai
 * @date 2022-10-1
 */
@Data
public class StatisticsDayDTO {

    /**
     * 日期
     */
    private String date;

    /**
     * 数量
     */
    private Long pvCount;

    /**
     * UV数量
     */
    private Long uvCount;
}
