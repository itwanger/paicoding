package com.github.paicoding.forum.service.article.conveter;

import com.github.paicoding.forum.api.model.vo.article.SearchArticleReq;
import com.github.paicoding.forum.service.article.repository.params.SearchArticleParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArticleStructMapper {
    ArticleStructMapper INSTANCE = Mappers.getMapper( ArticleStructMapper.class );

    @Mapping(source = "pageNumber", target = "pageNum")
    SearchArticleParams toSearchParams(SearchArticleReq req);
}
