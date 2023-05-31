package com.github.paicoding.forum.service.config.converter;

import com.github.paicoding.forum.api.model.vo.banner.ConfigReq;
import com.github.paicoding.forum.api.model.vo.banner.SearchConfigReq;
import com.github.paicoding.forum.api.model.vo.banner.dto.ConfigDTO;
import com.github.paicoding.forum.service.config.repository.entity.ConfigDO;
import com.github.paicoding.forum.service.config.repository.params.SearchConfigParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConfigStructMapper {
    // instance
    ConfigStructMapper INSTANCE = Mappers.getMapper( ConfigStructMapper.class );

    // req to params
    @Mapping(source = "pageNumber", target = "pageNum")
    SearchConfigParams toSearchParams(SearchConfigReq req);

    // do to dto
    ConfigDTO toDTO(ConfigDO configDO);

    List<ConfigDTO> toDTOS(List<ConfigDO> configDOS);

    ConfigDO toDO(ConfigReq configReq);
}
