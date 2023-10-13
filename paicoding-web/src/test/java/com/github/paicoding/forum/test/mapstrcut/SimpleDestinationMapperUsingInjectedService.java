package com.github.paicoding.forum.test.mapstrcut;

import com.github.paicoding.forum.service.article.service.ArticleReadService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 9/21/23
 */
@Mapper(componentModel = "spring")
public abstract class SimpleDestinationMapperUsingInjectedService {
    @Autowired
    protected ArticleReadService articleReadService;

//    @Mapping(target = "name", expression = "java(articleReadService.generateSummary(source.getName()))")
    public abstract SimpleDestination sourceToDestination(SimpleSource source);
}
