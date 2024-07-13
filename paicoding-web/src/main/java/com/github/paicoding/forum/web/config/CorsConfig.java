package com.github.paicoding.forum.web.config;

import org.springframework.context.annotation.Configuration;

/**
 * @program: tech-pai
 * @description:
 * @author: XuYifei
 * @create: 2024-07-10
 */

@Configuration
public class CorsConfig {

//    @Bean
//    public CorsFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.addAllowedOriginPattern("*"); // 允许所有来源，但会根据请求的 Origin 动态设置
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        config.setExposedHeaders(List.of("Set-Cookie"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
}