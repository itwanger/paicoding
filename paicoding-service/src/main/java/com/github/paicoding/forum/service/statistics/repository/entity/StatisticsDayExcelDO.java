package com.github.paicoding.forum.service.statistics.repository.entity;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StatisticsDayExcelDO {

    /**
     * 日期
     */
    @ExcelProperty("日期")
    private String date;

    /**
     * 数量
     */
    @ExcelProperty("PV")
    private Long pvCount;

    /**
     * UV数量
     */
    @ExcelProperty("UV")
    private Long uvCount;
}