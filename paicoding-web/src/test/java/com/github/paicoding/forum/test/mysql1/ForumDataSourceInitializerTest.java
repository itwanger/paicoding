package com.github.paicoding.forum.test.mysql1;

import com.github.paicoding.forum.test.BasicTest;
import com.github.paicoding.forum.web.QuickForumApplication;
import com.github.paicoding.forum.web.config.init.DbChangeSetLoader;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.SQLException;


@Slf4j
public class ForumDataSourceInitializerTest extends BasicTest {
    @Value("classpath:liquibase/data/init_schema_221209.sql")
    private Resource schemaSql;
    @Value("classpath:liquibase/data/init_data_221209.sql")
    private Resource initData;

    @Test
    public void dataSourceInitializer() throws SQLException {
        DataSource dataSource = createCustomDataSource();
        log.info(dataSource.getConnection().getMetaData().getURL());

        final DataSourceInitializer initializer = new DataSourceInitializer();
        // 设置数据源
        initializer.setDataSource(dataSource);
        initializer.setEnabled(true);

        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(schemaSql);
        populator.addScript(initData);
        initializer.setDatabasePopulator(populator);
        initializer.afterPropertiesSet();
    }

    private DataSource createCustomDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/itwanger");
        dataSource.setUsername("root");
        dataSource.setPassword("Codingmore123");
        return dataSource;
    }
}
