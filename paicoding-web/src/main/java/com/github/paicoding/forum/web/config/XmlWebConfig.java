package com.github.paicoding.forum.web.config;

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
                .defaultContentType(MediaType.APPLICATION_JSON, MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN, MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_OCTET_STREAM, MediaType.MULTIPART_FORM_DATA, MediaType.MULTIPART_MIXED, MediaType.MULTIPART_RELATED)
                // 当下面的配置为false（默认值）时，通过浏览器访问后端接口，会根据acceptHeader协商进行返回，返回结果都是xml格式；与我们日常习惯不太匹配
                // 因此禁用请求头的AcceptHeader，在需要进行xml交互的接口上，手动加上 consumer, produces 属性； 因为本项目中，只有微信的交互是采用的xml进行传参、返回，其他的是通过json进行交互，所以只在微信的 WxRestController 中需要特殊处理；其他的默认即可
                .ignoreAcceptHeader(true)
                .parameterName("mediaType")
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML)
                .mediaType("html", MediaType.TEXT_HTML)
                .mediaType("text", MediaType.TEXT_PLAIN)
                .mediaType("text/event-stream", MediaType.TEXT_EVENT_STREAM)
                .mediaType("application/octet-stream", MediaType.APPLICATION_OCTET_STREAM)
                .mediaType("multipart/form-data", MediaType.MULTIPART_FORM_DATA)
        ;
    }
}
