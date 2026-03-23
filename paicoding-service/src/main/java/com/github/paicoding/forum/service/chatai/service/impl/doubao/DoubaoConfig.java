package com.github.paicoding.forum.service.chatai.service.impl.doubao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "doubao")
public class DoubaoConfig{
    private String apiKey;
    private String apiHost;
    private String endPoint;
}
