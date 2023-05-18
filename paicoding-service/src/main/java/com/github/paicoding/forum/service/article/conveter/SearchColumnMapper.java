package com.github.paicoding.forum.service.article.conveter;

import com.github.paicoding.forum.api.model.vo.article.SearchColumnReq;
import com.github.paicoding.forum.service.article.repository.params.SearchColumnParams;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SearchColumnMapper {
    SearchColumnMapper INSTANCE = Mappers.getMapper( SearchColumnMapper.class );

    SearchColumnParams toSearchParams(SearchColumnReq req);
}
