package com.github.paicoding.forum.service.statistics.repository.entity;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class RequestCountExcelDO {

    @ExcelProperty("机器IP")
    private String host;

    @ExcelProperty("访问计数")
    private Integer cnt;

    @ExcelProperty("当前日期")
    private Date date;
}
