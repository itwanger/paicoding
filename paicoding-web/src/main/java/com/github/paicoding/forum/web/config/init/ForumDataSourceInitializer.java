package com.github.paicoding.forum.web.config.init;

import com.github.paicoding.forum.core.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.*;
import java.util.List;

/**
 * 表初始化，只有首次启动时，才会执行
 *
 * @author YiHui
 * @date 2022/10/15
 */
@Slf4j
@Configuration
public class ForumDataSourceInitializer {
    //    @Value("classpath:liquibase/data/init_schema_221209.sql")
//    private Resource schemaSql;
//    @Value("classpath:liquibase/data/init_data_221209.sql")
//    private Resource initData;
    @Value("${database.name}")
    private String database;

    @Value("${spring.liquibase.enabled:true}")
    private Boolean liquibaseEnable;

    @Value("${spring.liquibase.change-log}")
    private String liquibaseChangeLog;

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        // 设置数据源
        initializer.setDataSource(dataSource);
        boolean enable = needInit(dataSource);
        initializer.setEnabled(enable);
        initializer.setDatabasePopulator(databasePopulator(enable));
        return initializer;
    }

    private DatabasePopulator databasePopulator(boolean initEnable) {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        // 下面这种是根据sql文件来进行初始化；改成 liquibase 之后不再使用这种方案，由liquibase来统一管理表结构数据变更
        if (initEnable && !liquibaseEnable) {
            // fixme: 首次启动时, 对于不支持liquibase的数据库，如mariadb，采用主动初始化
            // fixme 这种方式不支持后续动态的数据表结构更新、数据变更
            populator.addScripts(DbChangeSetLoader.loadDbChangeSetResources(liquibaseChangeLog).toArray(new ClassPathResource[]{}));
            populator.setSeparator(";");
            log.info("非Liquibase管理数据库，手动执行数据库表初始化!");
        }
        return populator;
    }

    /**
     * 检测一下数据库中表是否存在，若存在则不初始化；否则基于 schema-all.sql 进行初始化表
     *
     * @param dataSource
     * @return
     */
    private boolean needInit(DataSource dataSource) {
        if (autoInitDatabase()) {
            return true;
        }
        // 根据是否存在表来判断是否需要执行sql操作
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        List list = jdbcTemplate.queryForList("SELECT table_name FROM information_schema.TABLES where table_name = 'user_info' and table_schema = '" + database + "';");
        return CollectionUtils.isEmpty(list);
    }


    /**
     * 数据库不存在时，尝试创建数据库
     */
    private boolean autoInitDatabase() {
        // 查询失败，可能是数据库不存在，尝试创建数据库之后再次测试
        URI url = URI.create(SpringUtil.getConfigOrElse("spring.datasource.url", "spring.dynamic.datasource.master.url").substring(5));
        String uname = SpringUtil.getConfigOrElse("spring.datasource.username", "spring.dynamic.datasource.master.username");
        String pwd = SpringUtil.getConfigOrElse("spring.datasource.password", "spring.dynamic.datasource.master.password");
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + url.getHost() + ":" + url.getPort() +
                "?useUnicode=true&characterEncoding=UTF-8&useSSL=false", uname, pwd);
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery("select schema_name from information_schema.schemata where schema_name = '" + database + "'");
            if (!set.next()) {
                // 不存在时，创建数据库
                String createDb = "CREATE DATABASE IF NOT EXISTS " + database;
                connection.setAutoCommit(false);
                statement.execute(createDb);
                connection.commit();
                log.info("创建数据库（{}）成功", database);
                if (set.isClosed()) {
                    set.close();
                }
                return true;
            }
            set.close();
            log.info("数据库已存在，无需初始化");
            return false;
        } catch (SQLException e2) {
            throw new RuntimeException(e2);
        }
    }
}
