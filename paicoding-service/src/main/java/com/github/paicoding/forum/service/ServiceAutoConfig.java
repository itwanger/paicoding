package com.github.paicoding.forum.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Configuration
@ComponentScan("com.github.paicoding.forum.service")
@MapperScan(basePackages = {
        "com.github.paicoding.forum.service.article.repository.mapper",
        "com.github.paicoding.forum.service.user.repository.mapper",
        "com.github.paicoding.forum.service.comment.repository.mapper",
        "com.github.paicoding.forum.service.config.repository.mapper",
        "com.github.paicoding.forum.service.statistics.repository.mapper",
        "com.github.paicoding.forum.service.notify.repository.mapper",
        "com.github.paicoding.forum.service.shortlink.repository.mapper",
})
public class ServiceAutoConfig {


}
