package com.github.paicoding.forum.core.dal;

import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author YiHui
 * @date 2023/4/30
 */
@Data
@ConfigurationProperties(prefix = "spring.dynamic")
public class DsProperties {
    private Map<String, DataSourceProperties> datasource;
}