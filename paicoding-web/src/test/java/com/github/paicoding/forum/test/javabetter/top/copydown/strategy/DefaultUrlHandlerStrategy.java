package com.github.paicoding.forum.test.javabetter.top.copydown.strategy;

import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceOption;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceResult;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceType;
import com.github.paicoding.forum.test.javabetter.top.furstenheim.CopyDown;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DefaultUrlHandlerStrategy extends Coverter implements UrlHandlerStrategy{
    public DefaultUrlHandlerStrategy(CopyDown copyDown, Document document) {
        super(copyDown, document);
    }

    @Override
    public boolean match(String url) {
        return true;
    }

    @Override
    public void handleOptions(HtmlSourceOption option) {
        // 其他定制
        option.setHtmlSourceType(HtmlSourceType.OTHER);
        // 字符串，用, 隔开
        String selector = StringUtils.joinWith(",",
                ".blogpost-body", // 博客园
                ".article_content", // CSDN
                ".article-wrapper", // 开发者社区阿里云
                "article.article-content", // 小白学堂，麦客搜
                ".article-content", // 思否 infoq
                ".post-topic-des, .post-content-box, .nc-post-content", // 牛客
                "article.markdown-body", // GitHub
                ".e13l6k8o9", // LeetCode
                // .writing-content 墨滴
                "writing-content",
                // div.byte-viewer-container 开发者客栈
                "div.byte-viewer-container",
                // div#x-content 廖雪峰
                "div#x-content",
                // div.cloud-blog-detail-content-wrap 华为云
                "div.cloud-blog-detail-content-wrap",
                // div#arc-body C 语言中文网
                "div#arc-body",
                "article");
        option.setContentSelector(selector);
    }

    @Override
    public HtmlSourceResult convertToMD(HtmlSourceOption option) {
        Document doc = getDocument();

        HtmlSourceResult result = HtmlSourceResult.builder().build();
        // 标题
        Elements title = doc.select(option.getTitleKey());
        result.setTitle(title.text());

        // keywords
        if (StringUtils.isNotBlank(option.getKeywordsKey())) {
            Elements keywords = doc.select(option.getKeywordsKey());
            result.setKeywords(keywords.attr("content"));
        }

        if (StringUtils.isNotBlank(option.getDescriptionKey())) {
            // description
            Elements description = doc.select(option.getDescriptionKey());
            result.setDescription(description.attr("content"));
        }

        // 获取文章内容
        Elements content = doc.select(option.getContentSelector());
        String input = content.html();
        result.setMarkdown(getCopyDown().convert(input));

        return result;
    }
}
