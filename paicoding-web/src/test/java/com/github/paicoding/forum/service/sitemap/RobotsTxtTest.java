package com.github.paicoding.forum.service.sitemap;

import com.github.paicoding.forum.service.sitemap.service.impl.SitemapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * robots.txt 的抓取边界。
 *
 * <p>用户主页数量上万、早已 noindex 并退出索引，但谷歌仍在复爬（实测占 Googlebot 抓取量 36%），
 * 所以整段屏蔽 /user/。危险的地方在于站内有以 user- 开头的文章 slug
 * （/user-management-design、/user-interview-questions 都在 sitemap 里），
 * 它们必须仍然可抓——robots 是前缀匹配，/user/ 的尾斜杠是这条边界的关键。</p>
 *
 * @author Claude
 */
public class RobotsTxtTest {

    private String robots;

    @BeforeEach
    public void setUp() {
        SitemapServiceImpl service = new SitemapServiceImpl();
        ReflectionTestUtils.setField(service, "host", "https://paicoding.com");
        ReflectionTestUtils.setField(service, "environment", Mockito.mock(Environment.class));
        robots = service.getRobotsTxt();
    }

    @Test
    public void shouldBlockUserPages() {
        assertTrue(hasRule("Disallow: /user/"), robots);
    }

    @Test
    public void shouldKeepContentPathsCrawlable() {
        for (String path : new String[]{"/", "/article/", "/column/", "/article/category/", "/user-"}) {
            assertFalse(hasRule("Disallow: " + path + "\n"), "不应屏蔽内容路径 " + path + "：\n" + robots);
        }
    }

    @Test
    public void shouldStillDeclareSitemap() {
        assertTrue(robots.contains("Sitemap: https://paicoding.com/sitemap.xml"), robots);
    }

    private boolean hasRule(String rule) {
        return robots.contains(rule);
    }
}
