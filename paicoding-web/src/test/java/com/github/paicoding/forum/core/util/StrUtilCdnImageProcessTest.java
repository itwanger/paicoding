package com.github.paicoding.forum.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 正文图片的 OSS 处理地址拼接规则。
 *
 * <p>线上实测：一篇文章的 CDN 图片总量 62MB，单张 4000px 宽的 retina 截图 6~8MB，
 * 而正文容器只有 736px。加上缩放和 WebP 之后单张 8.16MB → 121KB。</p>
 *
 * @author Claude
 */
public class StrUtilCdnImageProcessTest {

    private static final String CDN = "https://cdn.paicoding.com/";

    @Test
    public void shouldAppendResizeAndWebpForOwnCdn() {
        assertEquals("https://cdn.paicoding.com/a/b.png?x-oss-process=image/resize,w_1400/format,webp",
                StrUtil.buildProcessedImageUrl(CDN + "a/b.png", CDN, 1400));
        assertEquals("https://cdn.paicoding.com/a/b.png?x-oss-process=image/resize,w_2400/format,webp",
                StrUtil.buildProcessedImageUrl(CDN + "a/b.png", CDN, 2400));
    }

    @Test
    public void shouldLeaveExternalImagesUntouched() {
        assertNull(StrUtil.buildProcessedImageUrl("https://other-cdn.com/a.png", CDN, 1400));
        assertNull(StrUtil.buildProcessedImageUrl("https://cdn.tobebetterjavaer.com/a.png", CDN, 1400));
    }

    @Test
    public void shouldNotAppendTwiceOrTouchSignedUrls() {
        // 已带参数：可能是签名地址或已处理过，重复追加会破坏它
        assertNull(StrUtil.buildProcessedImageUrl(CDN + "a.png?x-oss-process=image/resize,w_1400", CDN, 1400));
        assertNull(StrUtil.buildProcessedImageUrl(CDN + "a.png?Expires=123&Signature=abc", CDN, 1400));
    }

    @Test
    public void shouldSkipAnimatedAndVectorImages() {
        // 转 WebP 会丢动画
        assertNull(StrUtil.buildProcessedImageUrl(CDN + "a.gif", CDN, 1400));
        assertNull(StrUtil.buildProcessedImageUrl(CDN + "a.GIF", CDN, 1400));
        // 矢量图本身就很小，转位图反而变大
        assertNull(StrUtil.buildProcessedImageUrl(CDN + "logo.svg", CDN, 1400));
    }

    @Test
    public void shouldWriteInlineSrcAndZoomDataSrc() {
        org.jsoup.nodes.Element img = org.jsoup.Jsoup.parseBodyFragment(
                "<img src=\"" + CDN + "shot.png\" width=\"3872\" height=\"2522\">").selectFirst("img");

        StrUtil.applyCdnImageProcess(img, CDN + "shot.png", CDN);

        assertEquals(CDN + "shot.png?x-oss-process=image/resize,w_1400/format,webp", img.attr("src"));
        // Fancybox 放大用更清晰的一份
        assertEquals(CDN + "shot.png?x-oss-process=image/resize,w_2400/format,webp", img.attr("data-src"));
        // 宽高必须保持原图数值：等比缩放不改变宽高比，占位盒子仍然正确
        assertEquals("3872", img.attr("width"));
        assertEquals("2522", img.attr("height"));
    }

    @Test
    public void shouldNotTouchElementWhenNotApplicable() {
        org.jsoup.nodes.Element img = org.jsoup.Jsoup.parseBodyFragment(
                "<img src=\"https://other-cdn.com/a.png\">").selectFirst("img");

        StrUtil.applyCdnImageProcess(img, "https://other-cdn.com/a.png", CDN);

        assertEquals("https://other-cdn.com/a.png", img.attr("src"));
        assertEquals("", img.attr("data-src"));
    }

    @Test
    public void compressOnlyRewritesSrcForRenderersWithOwnLayout() {
        // 小程序用 mp-html 自己排版，写入原图宽高会撑破容器，所以只压体积
        String html = "<p>x</p><img src=\"" + CDN + "shot.png\"><img src=\"https://other-cdn.com/a.png\">";

        String out = StrUtil.compressHtmlImages(html, CDN);

        assertTrue(out.contains(CDN + "shot.png?x-oss-process=image/resize,w_1400/format,webp"), out);
        assertTrue(out.contains("https://other-cdn.com/a.png\""), "外链图片应原样保留：" + out);
        assertFalse(out.contains("data-src"), "不应写入放大用地址：" + out);
        assertFalse(out.contains("width="), "不应写入原图宽高：" + out);
    }

    @Test
    public void compressShouldReturnInputWhenCdnHostUnavailable() {
        String html = "<img src=\"" + CDN + "shot.png\">";
        assertEquals(html, StrUtil.compressHtmlImages(html, null));
    }

    @Test
    public void shouldDegradeSafelyWhenInputMissing() {
        // 拿不到 CDN 配置时（如非 Spring 环境）一律不处理，保持原图可用
        assertNull(StrUtil.buildProcessedImageUrl(CDN + "a.png", null, 1400));
        assertNull(StrUtil.buildProcessedImageUrl(null, CDN, 1400));
        assertNull(StrUtil.buildProcessedImageUrl("", CDN, 1400));
    }
}
