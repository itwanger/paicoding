package com.github.paicoding.forum.service.statistics.service.impl;

import com.github.paicoding.forum.api.model.enums.DocumentTypeEnum;
import com.github.paicoding.forum.core.cache.RedisClient;
import com.github.paicoding.forum.service.article.repository.mapper.ReadCountMapper;
import com.github.paicoding.forum.service.statistics.constants.CountConstants;
import com.github.paicoding.forum.service.user.repository.dao.UserFootDao;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

class CountServiceImplLocalIntegrationTest {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/pai_coding?useUnicode=true&allowPublicKeyRetrieval=true&autoReconnect=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "123456";
    private static final long TEST_ARTICLE_ID = 987654321012345L;
    private static final String TEST_KEY = CountConstants.ARTICLE_STATISTIC_INFO + TEST_ARTICLE_ID;

    private HikariDataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private LettuceConnectionFactory redisConnectionFactory;
    private StringRedisTemplate stringRedisTemplate;
    private SqlSession sqlSession;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(JDBC_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);

        jdbcTemplate = new JdbcTemplate(dataSource);

        redisConnectionFactory = new LettuceConnectionFactory("localhost", 6379);
        redisConnectionFactory.afterPropertiesSet();

        stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        stringRedisTemplate.afterPropertiesSet();
        RedisClient.register(stringRedisTemplate);

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        sqlSessionFactory.getConfiguration().addMapper(ReadCountMapper.class);
        sqlSession = sqlSessionFactory.openSession(true);

        cleanup();
    }

    @AfterEach
    void tearDown() {
        cleanup();
        if (sqlSession != null) {
            sqlSession.close();
        }
        if (redisConnectionFactory != null) {
            redisConnectionFactory.destroy();
        }
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    void syncArticleReadCountToDbShouldKeepMysqlCountEqualToRedisTotal() {
        CountServiceImpl service = spy(new CountServiceImpl(mock(UserFootDao.class)));
        ReflectionTestUtils.setField(service, "readCountMapper", sqlSession.getMapper(ReadCountMapper.class));
        ReflectionTestUtils.setField(service, "stringRedisTemplate", stringRedisTemplate);
        doReturn(Collections.singleton(TEST_KEY)).when(service).scanKeys(CountConstants.ARTICLE_STATISTIC_INFO + "*");

        RedisClient.hSet(TEST_KEY, CountConstants.READ_COUNT, 12);
        service.syncArticleReadCountToDb();

        assertThat(queryReadCount()).isEqualTo(12);

        RedisClient.hSet(TEST_KEY, CountConstants.READ_COUNT, 19);
        service.syncArticleReadCountToDb();

        assertThat(queryReadCount()).isEqualTo(19);
        assertThat(queryReadCountRowNum()).isEqualTo(1);
    }

    private Integer queryReadCount() {
        return jdbcTemplate.queryForObject(
                "SELECT cnt FROM read_count WHERE document_id = ? AND document_type = ?",
                Integer.class,
                TEST_ARTICLE_ID,
                DocumentTypeEnum.ARTICLE.getCode());
    }

    private Integer queryReadCountRowNum() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM read_count WHERE document_id = ? AND document_type = ?",
                Integer.class,
                TEST_ARTICLE_ID,
                DocumentTypeEnum.ARTICLE.getCode());
    }

    private void cleanup() {
        if (stringRedisTemplate != null) {
            RedisClient.del(TEST_KEY);
        }
        if (jdbcTemplate != null) {
            jdbcTemplate.update(
                    "DELETE FROM read_count WHERE document_id = ? AND document_type = ?",
                    TEST_ARTICLE_ID,
                    DocumentTypeEnum.ARTICLE.getCode());
        }
    }
}
