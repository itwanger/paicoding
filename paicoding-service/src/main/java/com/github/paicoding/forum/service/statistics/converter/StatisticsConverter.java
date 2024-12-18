package com.github.paicoding.forum.service.statistics.converter;

import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayExcelDTO;

import java.util.List;
import java.util.stream.Collectors;

public class StatisticsConverter {
    public static StatisticsDayExcelDTO convertToExcelDTO(StatisticsDayDTO dto) {
        StatisticsDayExcelDTO excelDTO = new StatisticsDayExcelDTO();
        excelDTO.setDate(dto.getDate());
        excelDTO.setUvCount(dto.getUvCount());
        excelDTO.setPvCount(dto.getPvCount());
        return excelDTO;
    }

    public static List<StatisticsDayExcelDTO> convertToExcelDTOList(List<StatisticsDayDTO> dtoList) {
        return dtoList.stream()
                .map(StatisticsConverter::convertToExcelDTO)
                .collect(Collectors.toList());
    }
}
