package com.github.liuyueyi.forum.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author YiHui
 * @date 2022/7/6
 */
@Configuration
@ComponentScan("com.github.liuyueyi.forum.service")
@MapperScan(basePackages = {
        "com.github.liuyueyi.forum.service.article.repository.mapper",
        "com.github.liuyueyi.forum.service.user.repository.mapper",
        "com.github.liuyueyi.forum.service.comment.repository.mapper",
        "com.github.liuyueyi.forum.service.banner.repository.mapper",
        "com.github.liuyueyi.forum.service.notify.repository.mapper",})
public class ServiceAutoConfig {
}
