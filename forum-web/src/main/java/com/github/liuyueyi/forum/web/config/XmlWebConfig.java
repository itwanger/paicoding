package com.github.liuyueyi.forum.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 注册xml解析器
 *
 * @author yihui
 * @date 2022/6/20
 */
@Configuration
public class XmlWebConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new MappingJackson2XmlHttpMessageConverter());
    }

    /**
     * fixme 返回数据类型的配置, 相关知识点可以查看  <a href="https://spring.hhui.top/spring-blog/2022/08/17/220817-SpringBoot%E7%B3%BB%E5%88%97%E4%B9%8B%E5%AE%9A%E4%B9%89%E6%8E%A5%E5%8F%A3%E8%BF%94%E5%9B%9E%E7%B1%BB%E5%9E%8B%E7%9A%84%E5%87%A0%E7%A7%8D%E6%96%B9%E5%BC%8F/"/>
     *
     * @param configurer
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorParameter(true)
                .defaultContentType(MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML)
                .parameterName("mediaType")
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("html", MediaType.TEXT_HTML)
                .mediaType("text", MediaType.TEXT_PLAIN)
        ;
    }
}
