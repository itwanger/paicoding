package com.github.paicoding.forum.web;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.paicoding.forum.core.util.SocketUtil;
import com.github.paicoding.forum.core.util.SpringUtil;
import com.github.paicoding.forum.web.config.GlobalViewConfig;
import com.github.paicoding.forum.web.hook.interceptor.GlobalViewInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 入口，直接运行即可
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@EnableAsync
@EnableScheduling
@EnableCaching
@ServletComponentScan
//@SpringBootApplication(scanBasePackages = {"com.github.paicoding.forum.web", "com.github.paicoding.forum.service"})
@SpringBootApplication
public class QuickForumApplication implements WebMvcConfigurer, ApplicationRunner {
    @Value("${server.port:8080}")
    private Integer webPort;

    @Resource
    private GlobalViewInterceptor globalViewInterceptor;

    @Resource
    private AsyncHandlerInterceptor onlineUserStatisticInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(globalViewInterceptor).addPathPatterns("/**");
        registry.addInterceptor(onlineUserStatisticInterceptor).addPathPatterns("/**").excludePathPatterns("/test/**").excludePathPatterns("/subscribe");
    }
//    @Override
//    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
//        resolvers.add(0, new ForumExceptionHandler());
//    }

    // 跨域设置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //项目中的所有接口都支持跨域
        registry.addMapping("/**")
                .allowedMethods("POST", "GET", "PUT", "OPTIONS", "DELETE");
    }


    public static void main(String[] args) {
        SpringApplication.run(QuickForumApplication.class, args);
    }

    /**
     * 兼容本地启动时8080端口被占用的场景; 只有dev启动方式才做这个逻辑
     *
     * @return
     */
    @Bean
    @ConditionalOnExpression(value = "#{'dev'.equals(environment.getProperty('env.name'))}")
    public TomcatConnectorCustomizer customServerPortTomcatConnectorCustomizer() {
        // 开发环境时，首先判断8080d端口是否可用；若可用则直接使用，否则选择一个可用的端口号启动
        int port = SocketUtil.findAvailableTcpPort(8000, 10000, webPort);
        if (port != webPort) {
            log.info("默认端口号{}被占用，随机启用新端口号: {}", webPort, port);
            webPort = port;
        }
        return connector -> connector.setPort(port);
    }

    @Override
    public void run(ApplicationArguments args) {
        // 设置类型转换, 主要用于mybatis读取varchar/json类型数据据，并写入到json格式的实体Entity中
        JacksonTypeHandler.setObjectMapper(new ObjectMapper());
        // 应用启动之后执行
        GlobalViewConfig config = SpringUtil.getBean(GlobalViewConfig.class);
        if (webPort != null) {
            config.setHost("http://127.0.0.1:" + webPort);
        }
        log.info("启动成功，点击进入首页: {}", config.getHost());
    }
}
