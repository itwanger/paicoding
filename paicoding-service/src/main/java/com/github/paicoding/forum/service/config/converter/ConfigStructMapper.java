package com.github.paicoding.forum.service.config.converter;

import com.github.paicoding.forum.api.model.vo.banner.SearchConfigReq;
import com.github.paicoding.forum.service.config.repository.params.SearchConfigParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConfigStructMapper {
    // instance
    ConfigStructMapper INSTANCE = Mappers.getMapper( ConfigStructMapper.class );

    // req to params
    @Mapping(source = "pageNumber", target = "pageNum")
    SearchConfigParams toSearchParams(SearchConfigReq req);

}
