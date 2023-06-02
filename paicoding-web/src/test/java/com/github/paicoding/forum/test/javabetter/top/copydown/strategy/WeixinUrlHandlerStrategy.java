package com.github.paicoding.forum.test.javabetter.top.copydown.strategy;

import com.github.paicoding.forum.test.javabetter.top.copydown.Constants;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceOption;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceResult;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceType;
import com.github.paicoding.forum.test.javabetter.top.furstenheim.CopyDown;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WeixinUrlHandlerStrategy extends Coverter implements UrlHandlerStrategy{

    public WeixinUrlHandlerStrategy(CopyDown copyDown, Document document) {
        super(copyDown, document);
    }

    @Override
    public boolean match(String url) {
        return url.contains("mp.weixin.qq.com");
    }

    @Override
    public void handleOptions(HtmlSourceOption option) {
        // 微信的链接
        option.setHtmlSourceType(HtmlSourceType.WEIXIN);
        option.setContentSelector("div.rich_media_content");
        option.setCoverImageKey("msg_cdn_url");
        option.setTitleKey("msg_title");
        option.setNicknameKey("nickname");
        option.setAuthorKey("author");
        option.setKeywordsKey("");
    }

    @Override
    public HtmlSourceResult convertToMD(HtmlSourceOption option) {
        Document doc = getDocument();
        // 先找作者名，如果找到，不用找订阅号名了
        HtmlSourceResult result = findWeixinImgAndTitleAndNickname(option);

        String author = findWeixinAuthor(option.getAuthorKey());
        assert result != null;
        result.setAuthor(author);

        Elements description = doc.select(option.getDescriptionKey());
        result.setDescription(description.attr("content"));

        // 获取文章内容
        Elements content = doc.select(option.getContentSelector());
        String input = content.html();
        result.setMarkdown(getCopyDown().convert(input));

        return result;
    }

    /**
     * 查找微信文档作者
     *
     * @param authorKey
     * @return
     */
    private String findWeixinAuthor(String authorKey) {
        for (Element metaTag : getDocument().getElementsByTag("meta")) {
            String content = metaTag.attr("content");
            String name = metaTag.attr("name");
            if (authorKey.equals(name)) {
                return content;
            }
        }

        return Constants.DEFAULT_AUTHOR;
    }

    /**
     * 查找微信网页的封面图、标题、订阅号名
     *
     * @param option
     * @return
     */
    private HtmlSourceResult findWeixinImgAndTitleAndNickname(HtmlSourceOption option) {
        // get <script>
        for (Element scripts : getDocument().getElementsByTag("script")) {
            // get data from <script>
            for (DataNode dataNode : scripts.dataNodes()) {
                // find data which contains
                if (dataNode.getWholeData().contains(option.getCoverImageKey())) {
                    log.info("contains");
                    HtmlSourceResult result = HtmlSourceResult.builder().build();

                    // 昵称
                    Pattern nikeNamePattern = Pattern.compile("var\\s+"+option.getNicknameKey()+"\\s+=\\s+\"(.*)\";");
                    Matcher nikeNameMatcher = nikeNamePattern.matcher(dataNode.getWholeData());
                    if (nikeNameMatcher.find()) {
                        String nickName = nikeNameMatcher.group(1);
                        log.info("find nickName success{}", nickName);
                        result.setAuthor(nickName);
                    }

                    // 文件名
                    Pattern titlePattern = Pattern.compile("var\\s+"+option.getTitleKey()+"\\s+=\\s+'(.*)'\\.html\\(false\\);");
                    Matcher titleMatcher = titlePattern.matcher(dataNode.getWholeData());
                    if (titleMatcher.find()) {
                        String title = titleMatcher.group(1);
                        log.info("find title success{}", title);
                        result.setTitle(title);
                    }

                    // 封面图
                    Pattern pattern = Pattern.compile("var\\s+"+option.getCoverImageKey()+"\\s+=\\s+\"(.*)\";");
                    Matcher matcher = pattern.matcher(dataNode.getWholeData());
                    if (matcher.find()) {
                        String msg_cdn_url = matcher.group(1);
                        log.info("find msg_cdn_url success {}", msg_cdn_url);
                        result.setCover(msg_cdn_url);
                    }
                    return result;
                }
            }
        }
        return null;
    }
}
