package com.github.paicoding.forum.service.article.conveter;

import com.github.paicoding.forum.api.model.vo.article.ColumnArticleReq;
import com.github.paicoding.forum.api.model.vo.article.SearchColumnArticleReq;
import com.github.paicoding.forum.service.article.repository.entity.ColumnArticleDO;
import com.github.paicoding.forum.service.article.repository.params.SearchColumnArticleParams;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ColumnArticleStructMapper {
    ColumnArticleStructMapper INSTANCE = Mappers.getMapper( ColumnArticleStructMapper.class );

    SearchColumnArticleParams toSearchParams(SearchColumnArticleReq req);

    ColumnArticleDO reqToDO(ColumnArticleReq req);
}
