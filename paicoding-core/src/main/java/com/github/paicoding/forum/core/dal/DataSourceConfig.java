package com.github.paicoding.forum.core.dal;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@ConditionalOnProperty(prefix = "spring.dynamic", name = "primary")
@EnableConfigurationProperties(DsProperties.class)
public class DataSourceConfig {

    public DataSourceConfig() {
        log.info("动态数据源初始化!");
    }

    @Bean
    public DsAspect dsAspect() {
        return new DsAspect();
    }

    @Bean
    public SqlStateInterceptor sqlStateInterceptor() {
        return new SqlStateInterceptor();
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
        Object key = dsProperties.getPrimary().toUpperCase();
        if (!targetDataSources.containsKey(key)) {
            if (targetDataSources.containsKey(MasterSlaveDsEnum.MASTER.name())) {
                // 当们没有配置primary对应的数据源时，存在MASTER数据源，则将主库作为默认的数据源
                key = MasterSlaveDsEnum.MASTER.name();
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
