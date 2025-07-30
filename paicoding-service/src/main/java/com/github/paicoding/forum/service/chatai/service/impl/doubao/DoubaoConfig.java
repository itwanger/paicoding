package com.github.paicoding.forum.service.chatai.service.impl.doubao;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Data
@Configuration
@ConfigurationProperties(prefix = "doubao")
public class DoubaoConfig{

    @Value("${doubao.api-key}")
    private String apiKey;
    @Value("${doubao.api-host}")
    private String apiHost;
    @Value("${doubao.end-point}")
    private String endPoint;
}