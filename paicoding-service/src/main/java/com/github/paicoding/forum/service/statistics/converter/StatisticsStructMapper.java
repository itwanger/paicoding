package com.github.paicoding.forum.service.statistics.converter;

import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayDTO;
import com.github.paicoding.forum.api.model.vo.statistics.dto.StatisticsDayExcelDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface StatisticsStructMapper {
    StatisticsStructMapper INSTANCE = Mappers.getMapper(StatisticsStructMapper.class);

    StatisticsDayExcelDTO toExcelDTO(StatisticsDayDTO dto);

    List<StatisticsDayExcelDTO> toExcelDTOList(List<StatisticsDayDTO> dtoList);
}