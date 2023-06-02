package com.github.paicoding.forum.test.javabetter.top.copydown.strategy;

import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceOption;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceResult;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceType;
import com.github.paicoding.forum.test.javabetter.top.furstenheim.CopyDown;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ZhihuUrlHandlerStrategy extends Coverter implements UrlHandlerStrategy {
    public ZhihuUrlHandlerStrategy(CopyDown copyDown, Document document) {
        super(copyDown, document);
    }

    @Override
    public boolean match(String url) {
        return url.contains("zhuanlan");
    }

    @Override
    public void handleOptions(HtmlSourceOption option) {
// 知乎的链接
        option.setHtmlSourceType(HtmlSourceType.ZHIHU);

        // 文章
        if (option.getUrl().contains("zhuanlan")) {
            option.setContentSelector("div.RichText");
            option.setTitleKey("header.Post-Header h1.Post-Title");
            option.setAuthorKey(".AuthorInfo meta[itemprop='name']");

        } else {
            // 回答
            option.setContentSelector("div.QuestionAnswer-content .RichContent-inner");
            option.setTitleKey("h1.QuestionHeader-title");
            option.setAuthorKey(".AuthorInfo-content .AuthorInfo-head .AuthorInfo-name a");
        }
    }

    @Override
    public HtmlSourceResult convertToMD(HtmlSourceOption option) {
        Document doc = getDocument();
        HtmlSourceResult result = HtmlSourceResult.builder().build();
        // 标题
        Elements title = doc.select(option.getTitleKey());
        result.setTitle(title.text());

        // keywords
        Elements keywords = doc.select(option.getKeywordsKey());
        result.setKeywords(keywords.attr("content"));

        // description
        Elements description = doc.select(option.getDescriptionKey());
        result.setDescription(description.attr("content"));

        // 作者名
        Elements authorName = doc.select(option.getAuthorKey());
        result.setAuthor(authorName.text());

        // 获取文章内容
        Elements content = doc.select(option.getContentSelector());
        String input = content.html();
        result.setMarkdown(getCopyDown().convert(input));
        return result;
    }
}
