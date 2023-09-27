package com.github.paicoding.forum.test.mapstrcut;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 9/21/23
 */
@Mapper
public interface SimpleSourceDestinationMapper {
    SimpleSourceDestinationMapper INSTANCE = Mappers.getMapper(SimpleSourceDestinationMapper.class);
    SimpleDestination sourceToDestination(SimpleSource source);

//    @Mapping(source = "name", target = "fullName")
//    @Mapping(target = "status", constant = "ACTIVE")
//    @Mapping(source = "count", target = "total", defaultValue = "0")
//    @Mapping(target = "timestamp", expression = "java(source.getDate().getTime())")
//    @Mapping(source = "date", target = "formattedDate", dateFormat = "yyyy-MM-dd")
//    @Mapping(source = "value", target = "data", qualifiedByName = "specialConverter")
//    @Mapping(source = "address.street", target = "streetName")
//    @Mapping(target = "internalId", ignore = true)
//    @Mapping(target = "data", source = "value", qualifiedByName = "customMethod")
    SimpleSource destinationToSource(SimpleDestination destination);
}
