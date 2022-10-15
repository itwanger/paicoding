package com.github.liuyueyi.forum.web;

import com.github.liuyueyi.forum.core.util.SpringUtil;
import com.github.liuyueyi.forum.web.config.GlobalViewConfig;
import com.github.liuyueyi.forum.web.global.ForumExceptionHandler;
import com.github.liuyueyi.forum.web.hook.interceptor.GlobalViewInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.List;

/**
 * 入口，直接运行即可
 *
 * @author yihui
 * @date 2022/7/6
 */
@Slf4j
@ServletComponentScan
@SpringBootApplication
public class QuickForumApplication implements WebMvcConfigurer, ApplicationRunner {

    @Resource
    private GlobalViewInterceptor globalViewInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalViewInterceptor).addPathPatterns("/**");
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(0, new ForumExceptionHandler());
    }

    public static void main(String[] args) {
        SpringApplication.run(QuickForumApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 应用启动之后执行
        log.info("启动成功，点击进入首页: {}", SpringUtil.getBean(GlobalViewConfig.class).getHost());
    }
}
