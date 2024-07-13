package com.github.paicoding.forum.core.dal;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 当配置了多数据源时，启用
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.dynamic", name = "primary")
@EnableConfigurationProperties(DsProperties.class)
public class DataSourceConfig {

    private Environment environment;

    public DataSourceConfig(Environment environment) {
        this.environment = environment;
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
        dsProperties.getDatasource().forEach((k, v) -> targetDataSources.put(k.toUpperCase(), initDataSource(k, v)));

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


    public DataSource initDataSource(String prefix, DataSourceProperties properties) {
        if (!DruidCheckUtil.hasDuridPkg()) {
            log.info("实例化HikarDataSource: {}", prefix);
            return properties.initializeDataSourceBuilder().build();
        }

        if (properties.getType() == null || !properties.getType().isAssignableFrom(DruidDataSource.class)) {
            log.info("实例化HikarDataSource: {}", prefix);
            return properties.initializeDataSourceBuilder().build();
        }

        log.info("实例化DruidDataSource: {}", prefix);
        // fixme 知识点：手动将配置赋值到实例中的方式
        return Binder.get(environment).bindOrCreate(DsProperties.DS_PREFIX + ".datasource." + prefix, DruidDataSource.class);
    }

    /**
     * 在数据源实例化之后进行创建
     *
     * @return
     */
    @Bean
    @ConditionalOnExpression(value = "T(com.github.paicoding.forum.core.dal.DruidCheckUtil).hasDuridPkg()")
    public ServletRegistrationBean<?> druidStatViewServlet() {
        //先配置管理后台的servLet，访问的入口为/druid/
        ServletRegistrationBean<?> servletRegistrationBean = new ServletRegistrationBean<>(
                new StatViewServlet(), "/druid/*");
        // IP白名单 (没有配置或者为空，则允许所有访问)
        servletRegistrationBean.addInitParameter("allow", "127.0.0.1");
        // IP黑名单 (存在共同时，deny优先于allow)
        servletRegistrationBean.addInitParameter("deny", "");
        servletRegistrationBean.addInitParameter("loginUsername", "admin");
        servletRegistrationBean.addInitParameter("loginPassword", "admin");
        servletRegistrationBean.addInitParameter("resetEnable", "false");
        log.info("开启druid数据源监控面板");
        return servletRegistrationBean;
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
