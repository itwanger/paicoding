package com.github.paicoding.forum.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 文章站内路径的唯一规则。
 *
 * <p>侧边栏「热门文章」「相关文章」曾直接拼 {@code /article/detail/{id}}，有 slug 的文章
 * 每次点击都要吃一次 301，全站每个页面都在输出这种链接，持续给 GSC 的
 * 「网页会自动重定向」桶供货。sitemap / canonical / 站内链接必须用同一份规则。</p>
 *
 * @author Claude
 */
public class UrlSlugUtilArticlePathTest {

    @Test
    public void shouldPreferSlugWhenCanonical() {
        assertEquals("/codex-agentsmd-guide", UrlSlugUtil.articlePath("codex-agentsmd-guide", 123L));
    }

    @Test
    public void shouldFallbackToIdWhenSlugUnusable() {
        // 无 slug 的老文章：ID 地址本身就是规范地址
        assertEquals("/article/detail/123", UrlSlugUtil.articlePath(null, 123L));
        assertEquals("/article/detail/123", UrlSlugUtil.articlePath("", 123L));
        // 纯数字 slug 会和 ID 地址混淆
        assertEquals("/article/detail/123", UrlSlugUtil.articlePath("456", 123L));
        // 大写、下划线、中文都不是合法 slug
        assertEquals("/article/detail/123", UrlSlugUtil.articlePath("Codex-Guide", 123L));
        assertEquals("/article/detail/123", UrlSlugUtil.articlePath("codex_guide", 123L));
        assertEquals("/article/detail/123", UrlSlugUtil.articlePath("技术派", 123L));
    }

    @Test
    public void canonicalSlugRuleShouldMatchArticlePath() {
        assertEquals(true, UrlSlugUtil.isCanonicalSlug("codex-agentsmd-guide"));
        assertEquals(false, UrlSlugUtil.isCanonicalSlug("456"));
        assertEquals(false, UrlSlugUtil.isCanonicalSlug(null));
    }
}
