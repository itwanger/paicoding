package com.github.paicoding.forum.test.seo;

import com.github.paicoding.forum.service.sitemap.service.impl.SitemapServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SitemapServiceImplTest {

    @Test
    public void robotsTxtBlocksGeneratedApiAndPrivateEntrypoints() {
        SitemapServiceImpl service = new SitemapServiceImpl();
        ReflectionTestUtils.setField(service, "host", "https://paicoding.com");
        ReflectionTestUtils.setField(service, "environment", new MockEnvironment());

        String robots = service.getRobotsTxt();

        assertTrue(robots.contains("Disallow: /*/api/"));
        assertTrue(robots.contains("Disallow: /search/api/"));
        assertTrue(robots.contains("Disallow: /user/home"));
        assertTrue(robots.contains("Disallow: /wx/"));
        assertTrue(robots.contains("Sitemap: https://paicoding.com/sitemap.xml"));
    }
}
