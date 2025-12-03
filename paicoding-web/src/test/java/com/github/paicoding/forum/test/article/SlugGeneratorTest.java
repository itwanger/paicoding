package com.github.paicoding.forum.test.article;

import com.github.paicoding.forum.service.article.service.SlugGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * URL Slug生成器测试
 *
 * @author YiHui
 * @date 2025/12/03
 */
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SlugGeneratorTest {

    @Autowired
    private SlugGeneratorService slugGeneratorService;

    @Test
    public void testGenerateSlug() {
        String[] titles = {
            "深入理解Java虚拟机原理",
            "Spring Boot实战教程",
            "MySQL性能优化技巧",
            "Redis分布式锁实现方案",
            "Docker容器化部署指南"
        };

        for (String title : titles) {
            String slug = slugGeneratorService.generateSlugWithAI(title);
            log.info("标题: {} -> Slug: {}", title, slug);
        }
    }
}
