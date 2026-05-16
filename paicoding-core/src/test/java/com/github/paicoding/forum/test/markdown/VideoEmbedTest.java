package com.github.paicoding.forum.test.markdown;

import com.github.paicoding.forum.core.util.MarkdownConverter;
import org.junit.Assert;
import org.junit.Test;

public class VideoEmbedTest {

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
