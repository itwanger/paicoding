package com.github.paicoding.forum.test.mapstrcut;

import com.github.paicoding.forum.service.article.service.ArticleReadService;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class SimpleDestinationMapperUsingInjectedService {
    @Autowired
    protected ArticleReadService articleReadService;

//    @Mapping(target = "name", expression = "java(articleReadService.generateSummary(source.getName()))")
    public abstract SimpleDestination sourceToDestination(SimpleSource source);
}
