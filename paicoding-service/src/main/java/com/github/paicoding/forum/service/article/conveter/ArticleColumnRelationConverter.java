package com.github.paicoding.forum.service.article.conveter;


import com.github.paicoding.forum.api.model.vo.article.response.ArticleColumnRelationResponseDTO;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 文章关联专栏转换器
 *
 * @author XuYifei
 * @date 2024/10/24
 */
@Mapper(componentModel = "spring")
public interface ArticleColumnRelationConverter {
    ArticleColumnRelationConverter INSTANCE = Mappers.getMapper(ArticleColumnRelationConverter.class);

    // DO to DTO
    ArticleColumnRelationResponseDTO toResponseDto(ColumnArticleDO columnArticleDO);

    // DTO to DO
    @Mapping(target = "updateTime", expression = "java(new java.util.Date())")
    @Mapping(target = "id", constant = "0L")
    @Mapping(target = "createTime", expression = "java(new java.util.Date())")
    ColumnArticleDO toDo(ArticleColumnRelationResponseDTO articleColumnRelationResponseDTO);
}
