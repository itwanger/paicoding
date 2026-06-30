package com.github.paicoding.forum.test.markdown;

import com.github.paicoding.forum.core.util.MarkdownConverter;
import org.junit.Assert;
import org.junit.Test;

public class VideoEmbedTest {

    @Test
    public void renderBilibiliEmbedWithStyledContainer() {
        String html = MarkdownConverter.markdownToHtml("@[bilibili](BV13dT562Eqr)");

        Assert.assertTrue(html.contains("video-container--bilibili"));
        Assert.assertTrue(html.contains("video-container__stage"));
        Assert.assertTrue(html.contains("BV13dT562Eqr"));
        Assert.assertTrue(html.contains("https://player.bilibili.com/player.html"));
    }

    @Test
    public void normalizeRawBilibiliIframeEmbed() {
        String html = MarkdownConverter.markdownToHtml("<iframe src=\"//player.bilibili.com/player.html?isOutside=true&aid=116831331292575&bvid=BV13dT562Eqr&cid=39496189679&p=1\" scrolling=\"no\" border=\"0\" frameborder=\"no\" framespacing=\"0\" allowfullscreen=\"true\"></iframe>");

        Assert.assertTrue(html.contains("video-container--bilibili"));
        Assert.assertTrue(html.contains("video-container__stage"));
        Assert.assertTrue(html.contains("BV13dT562Eqr"));
        Assert.assertFalse(html.contains("src=\"//player.bilibili.com"));
    }

    @Test
    public void renderAliyunVodEmbed() {
        String html = MarkdownConverter.markdownToHtml("@[aliyun-vod](70d6ad52500c71f180805107e0c90102)");

        Assert.assertTrue(html.contains("video-container--aliyun-vod"));
        Assert.assertTrue(html.contains("/video/play/redirect?videoId=70d6ad52500c71f180805107e0c90102"));
        Assert.assertTrue(html.contains("<video"));
    }

    @Test
    public void normalizeFullWidthAliyunVodId() {
        String html = MarkdownConverter.markdownToHtml("@[aliyun-vod](70d6ad52500c71f１８０８０５１０７e0c90102)");

        Assert.assertTrue(html.contains("/video/play/redirect?videoId=70d6ad52500c71f180805107e0c90102"));
        Assert.assertFalse(html.contains("@<a href"));
    }
}
