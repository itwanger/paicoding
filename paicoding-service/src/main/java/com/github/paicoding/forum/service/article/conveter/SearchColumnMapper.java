package com.github.paicoding.forum.service.article.conveter;

import com.github.paicoding.forum.api.model.vo.article.SearchColumnReq;
import com.github.paicoding.forum.api.model.vo.article.dto.ColumnDTO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnInfoDO;
import com.github.paicoding.forum.service.article.repository.params.SearchColumnParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SearchColumnMapper {
    SearchColumnMapper INSTANCE = Mappers.getMapper( SearchColumnMapper.class );

    SearchColumnParams toSearchParams(SearchColumnReq req);

    // sources 是参数，target 是目标
    @Mapping(source = "id", target = "columnId")
    @Mapping(source = "columnName", target = "column")
    @Mapping(source = "userId", target = "author")
    // Date 转 Long
    @Mapping(target = "publishTime", expression = "java(columnInfoDO.getPublishTime().getTime())")
    @Mapping(target = "freeStartTime", expression = "java(columnInfoDO.getFreeStartTime().getTime())")
    @Mapping(target = "freeEndTime", expression = "java(columnInfoDO.getFreeEndTime().getTime())")
    ColumnDTO toDto(ColumnInfoDO columnInfoDO);

    List<ColumnDTO> toDtos(List<ColumnInfoDO> columnInfoDOs);
}
