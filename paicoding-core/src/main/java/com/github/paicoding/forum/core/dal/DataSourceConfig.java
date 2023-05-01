package com.github.paicoding.forum.core.dal;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 当配置了多数据源时，启用
 *
 * @author YiHui
 * @date 2023/4/30
 */
@Slf4j
@Configuration
//@ConditionalOnProperty(prefix = "spring.dynamic", name = {"master"})
@EnableConfigurationProperties(DsProperties.class)
public class DataSourceConfig {
    private static final String DEFAULT_DB = "DEFAULT";

    @Bean
    public DsAspect dsAspect() {
        return new DsAspect();
    }

    /**
     * 整合主从数据源
     *
     * @param dsProperties
     * @return 1
     */
    @Bean
    @Primary
    public DataSource dataSource(DsProperties dsProperties) {
        Map<Object, Object> targetDataSources = Maps.newHashMapWithExpectedSize(dsProperties.getDatasource().size());
        dsProperties.getDatasource().forEach((k, v) -> targetDataSources.put(k.toUpperCase(), v.initializeDataSourceBuilder().build()));

        if (CollectionUtils.isEmpty(targetDataSources)) {
            throw new IllegalStateException("多数据源配置，请以 spring.dynamic 开头");
        }

        MyRoutingDataSource myRoutingDataSource = new MyRoutingDataSource();
        Object key = DbEnum.MASTER.name();
        if (!targetDataSources.containsKey(key)) {
            if (targetDataSources.containsKey(DEFAULT_DB)) {
                // 当master没有配置，default有配置时，使用default作为默认数据源，并添加master -> defaultDataSource的映射
                key = DEFAULT_DB;
                targetDataSources.put(DbEnum.MASTER.name(), targetDataSources.get(DEFAULT_DB));
            } else {
                key = targetDataSources.keySet().iterator().next();
            }
        }

        log.info("动态数据源，默认启用为： " + key);
        myRoutingDataSource.setDefaultTargetDataSource(targetDataSources.get(key));
        myRoutingDataSource.setTargetDataSources(targetDataSources);
        return myRoutingDataSource;
    }

//    @Bean
//    public JdbcTemplate notifyFullJdbcTemplate(DataSource myRoutingDataSource) {
//        return new JdbcTemplate(myRoutingDataSource);
//    }
//
//    @Bean(name = "SqlSessionFactory")
//    public SqlSessionFactory test1SqlSessionFactory(DataSource dynamicDataSource, GlobalConfig globalConfig)
//            throws Exception {
//        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
//        bean.setDataSource(dynamicDataSource);
//        /**当使用多数据源时，mybatisPlus默认配置将会失效，需要单独将其注入数据源中 */
////        bean.setPlugins(plugins);
//        /** 设置全局配置 */
//        bean.setGlobalConfig(globalConfig);
//        return bean.getObject();
//    }
//
//    /** 全局自定义配置 */
//    @Bean(name = "globalConfig")
//    @ConfigurationProperties(prefix = "mybatis-plus.global-config")
//    public GlobalConfig globalConfig(){
//        return new GlobalConfig();
//    }
}
