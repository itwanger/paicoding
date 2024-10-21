package com.github.paicoding.forum.web.config.init;

import com.github.paicoding.forum.core.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
import java.util.Map;
import java.util.Objects;

/**
 * 表初始化，只有首次启动时，才会执行
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Configuration
@Profile("dev")
public class ForumDataSourceInitializer {
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
            log.info("非Liquibase管理数据库，请手动执行数据库表初始化!");
        }
        return populator;
    }

    /**
     * 检测一下数据库中表是否存在，若存在则不初始化；否则基于 schema-all.sql 进行初始化表
     *
     * @param dataSource
     * @return true 表示需要初始化； false 表示无需初始化
     */
    private boolean needInit(DataSource dataSource) {
        if (autoInitDatabase()) {
            return true;
        }
        // 根据是否存在表来判断是否需要执行sql操作
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        if (!liquibaseEnable) {
            // 非liquibase做数据库版本管理的，根据用户来判断是否有初始化
            List list = jdbcTemplate.queryForList("SELECT table_name FROM information_schema.TABLES where table_name = 'user_info' and table_schema = '" + database + "';");
            return CollectionUtils.isEmpty(list);
        }

        // 对于liquibase做数据版本管控的场景，若使用的不是默认的pai_coding，则需要进行修订
        List<Map<String, Object>> record = jdbcTemplate.queryForList("select * from DATABASECHANGELOG where ID='00000000000020' limit 1;");
        if (CollectionUtils.isEmpty(record)) {
            // 首次启动，需要初始化库表，直接返回
            return true;
        }

        // 非首次启动时，判断记录对应的md5是否准确
        if (Objects.equals(record.get(0).get("MD5SUM"), "8:a1a2d9943b746acf58476ae612c292fc")) {
            // 这里主要是为了解决 <a href="https://github.com/itwanger/paicoding/issues/71">#71</a> 这个问题
            jdbcTemplate.update("update DATABASECHANGELOG set MD5SUM='8:bb81b67a5219be64eff22e2929fed540' where ID='00000000000020'");
        }
        return false;
    }


    /**
     * 数据库不存在时，尝试创建数据库
     */
    private boolean autoInitDatabase() {
        // 查询失败，可能是数据库不存在，尝试创建数据库之后再次测试

        // 数据库链接
        URI url = URI.create(SpringUtil.getConfigOrElse("spring.datasource.url", "spring.dynamic.datasource.master.url").substring(5));
        // 用户名
        String uname = SpringUtil.getConfigOrElse("spring.datasource.username", "spring.dynamic.datasource.master.username");
        // 密码
        String pwd = SpringUtil.getConfigOrElse("spring.datasource.password", "spring.dynamic.datasource.master.password");
        // 创建连接
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://" + url.getHost() + ":" + url.getPort() +
                "?useUnicode=true&characterEncoding=UTF-8&useSSL=false", uname, pwd);
             Statement statement = connection.createStatement()) {
            // 查询数据库是否存在
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
            // 查询数据库的表是否存在
            ResultSet tables = statement.executeQuery("SELECT table_name FROM information_schema.TABLES where table_name = 'user_info' and table_schema = '" + database + "';");
            if (!tables.next()) {
                log.info("数据库存在，但表不存在，需要初始化");
                return true;
            }
            log.info("数据库已存在，无需初始化");
            return false;
        } catch (SQLException e2) {
            throw new RuntimeException(e2);
        }
    }
}
