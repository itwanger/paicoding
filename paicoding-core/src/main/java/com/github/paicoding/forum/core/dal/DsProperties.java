package com.github.paicoding.forum.core.dal;

import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * 多数据源的配置加载
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Data
@ConfigurationProperties(prefix = DsProperties.DS_PREFIX)
public class DsProperties {
    public static final String DS_PREFIX = "spring.dynamic";
    /**
     * 默认数据源
     */
    private String primary;

    /**
     * 多数据源配置
     */
    private Map<String, DataSourceProperties> datasource;
}