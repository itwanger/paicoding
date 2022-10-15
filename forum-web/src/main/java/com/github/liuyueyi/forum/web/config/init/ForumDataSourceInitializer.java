package com.github.liuyueyi.forum.web.config.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * 表初始化，只有首次启动时，才会执行
 *
 * @author YiHui
 * @date 2022/10/15
 */
@Configuration
public class ForumDataSourceInitializer {
    @Value("classpath:schema-all.sql")
    private Resource schemaSql;
    @Value("${database.name}")
    private String database;

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        // 设置数据源
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator());
        initializer.setEnabled(needInit(dataSource));
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(schemaSql);
        populator.setSeparator(";");
        return populator;
    }

    /**
     * 检测一下数据库中表是否存在，若存在则不初始化；否则基于 schema-all.sql 进行初始化表
     *
     * @param dataSource
     * @return
     */
    private boolean needInit(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List list = jdbcTemplate.queryForList("SELECT table_name FROM information_schema.TABLES where table_name = 'user_info' and table_schema = '" + database + "';");
        return CollectionUtils.isEmpty(list);
    }
}
