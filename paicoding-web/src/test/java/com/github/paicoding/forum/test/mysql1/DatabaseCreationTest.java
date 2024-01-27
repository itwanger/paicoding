package com.github.paicoding.forum.test.mysql1;

import com.github.paicoding.forum.web.QuickForumApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 1/26/24
 */
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.*;

@Slf4j
@SpringBootTest(classes = QuickForumApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DatabaseCreationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void createDatabaseTest() throws SQLException {
        String dbName = "itwanger";
        if (!databaseExists(dbName)) {
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS " + dbName);
            System.out.println("创建成功");
        } else {
            System.out.println("已存在");
        }
    }

    private boolean databaseExists(String dbName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet set = statement.executeQuery("select schema_name from information_schema.schemata where schema_name = '" + dbName + "'");
            return set.next();
        }
    }
}

