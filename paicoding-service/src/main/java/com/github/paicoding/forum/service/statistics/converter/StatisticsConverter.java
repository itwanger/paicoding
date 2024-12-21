package com.github.paicoding.forum.service.statistics.converter;

import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountDO;
import com.github.paicoding.forum.service.statistics.repository.entity.RequestCountExcelDO;
import com.github.paicoding.forum.service.statistics.repository.entity.StatisticsDayExcelDO;

import java.util.List;
import java.util.stream.Collectors;

public class StatisticsConverter {
    public static StatisticsDayExcelDO convertToExcelDO(StatisticsDayDTO dto) {
        StatisticsDayExcelDO excelDO = new StatisticsDayExcelDO();
        excelDO.setDate(dto.getDate());
        excelDO.setUvCount(dto.getUvCount());
        excelDO.setPvCount(dto.getPvCount());
        return excelDO;
    }

    public static RequestCountExcelDO ConvertToRequestCountDO(RequestCountDO requestCountDO) {
        RequestCountExcelDO excelDO = new RequestCountExcelDO();
        excelDO.setHost(requestCountDO.getHost());
        excelDO.setCnt(requestCountDO.getCnt());
        excelDO.setDate(requestCountDO.getDate());
        return excelDO;
    }

    public static List<RequestCountExcelDO> convertToRequestCountExcelDOList(List<RequestCountDO> requestCountDOList) {
        return requestCountDOList.stream()
                .map(StatisticsConverter::ConvertToRequestCountDO)
                .collect(Collectors.toList());
    }

    public static List<StatisticsDayExcelDO> convertToExcelDOList(List<StatisticsDayDTO> dtoList) {
        return dtoList.stream()
                .map(StatisticsConverter::convertToExcelDO)
                .collect(Collectors.toList());
    }
}
