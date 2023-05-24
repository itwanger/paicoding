package com.github.paicoding.forum.test.javabetter.top.copydown;

import cn.hutool.core.text.StrSplitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/28/22
 */
@Slf4j
public class MdUtil {


    public static HtmlSourceResult findBokeyuan(Document doc, HtmlSourceOption option) {
        HtmlSourceResult result = new HtmlSourceResult();
        // 标题
        Elements title = doc.select(option.getTitleKey());
        String [] texts = StrSplitter.splitToArray(title.text(),"-", 0, true,true);
        result.setMdTitle(texts[0]);

        // 作者名
        result.setAuthor(texts[1]);

        // 转载链接
        result.setSourceLink(option.getUrl());

        // 获取文章内容
        Elements content = doc.select(option.getContentSelector());
        String input = content.html();
        result.setMdInput(input);

        return result;
    }

    public static HtmlSourceResult findDefault(Document doc, HtmlSourceOption option) {
        HtmlSourceResult result = new HtmlSourceResult();
        // 标题
        Elements title = doc.select(option.getTitleKey());
        result.setMdTitle(title.text());

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
        result.setMdInput(input);

        // 转载链接
        result.setSourceLink(option.getUrl());

        return result;
    }

    public static HtmlSourceResult findZhihu(Document doc, HtmlSourceOption option) {
        HtmlSourceResult result = new HtmlSourceResult();
        // 标题
        Elements title = doc.select(option.getTitleKey());
        result.setMdTitle(title.text());

        // keywords
        Elements keywords = doc.select(option.getKeywordsKey());
        result.setKeywords(keywords.attr("content"));

        // description
        Elements description = doc.select(option.getDescriptionKey());
        result.setDescription(description.attr("content"));

        // 作者名
        Elements authorName = doc.select(option.getAuthorKey());
        result.setAuthor(authorName.text());

        // 转载链接
        result.setSourceLink(option.getUrl());

        // 获取文章内容
        Elements content = doc.select(option.getContentSelector());
        String input = content.html();
        result.setMdInput(input);

        return result;
    }

    /**
     * 找到微信文章的作者、封面图、标题、订阅号名字
     * @param doc
     * @param option
     * @return
     */
    public static HtmlSourceResult findWeixin(Document doc, HtmlSourceOption option) {
        // 先找作者名，如果找到，不用找订阅号名了
        HtmlSourceResult result = findWeixinImgAndTitleAndNickname(doc,option);

        String author = findWeixinAuthor(doc, option.getAuthorKey());
        result.setAuthor(author);

        Elements description = doc.select(option.getDescriptionKey());
        result.setDescription(description.attr("content"));

        // 获取文章内容
        Elements content = doc.select(option.getContentSelector());
        String input = content.html();
        result.setMdInput(input);

        // 转载链接
        result.setSourceLink(option.getUrl());

        return result;
    }

    /**
     * 查找掘金文章标题、作者、封面图
     *
     * @param doc
     * @param option
     * @return
     */
    public static HtmlSourceResult findJuejin(Document doc, HtmlSourceOption option) {
        HtmlSourceResult result = new HtmlSourceResult();
        // 标题
        Elements title = doc.select(option.getTitleKey());
        result.setMdTitle(title.attr("content"));

        // 作者名
        Elements authorName = doc.select(option.getAuthorKey());
        result.setAuthor(authorName.attr("content"));

        // keywords
        Elements keywords = doc.select(option.getKeywordsKey());
        result.setKeywords(keywords.attr("content"));

        // description
        Elements description = doc.select(option.getDescriptionKey());
        result.setDescription(description.attr("content"));

        // 转载链接
        result.setSourceLink(option.getUrl());

        // 文章内容
        // 掘金的不是以 HTML 格式显示的，所以需要额外的处理
        // mark_content:"
        // ,is_english:d,is_original:g,user_index:13.31714372615673,original_type:d,original_author:e,content:e,ctime:"1650429118",mtime:"1650858329",rtime:"1650435284",draft_id:"7088517368665604127",view_count:36440,collect_count:346,digg_count:340,comment_count:239,hot_index:2401,is_hot:d,rank_index:.3438144,status:g,verify_status:g,audit_status:k,mark_content:"---\ntheme: awesome-green\n---\n
        // ",display_count:d}
        // div.global-component-box
        // audit_status:i,mark_content:"---\ntheme: devui-blue\n---\n\n\n# 自我介绍\n\n首页和大家介绍一下我，我叫阿杆（笔名及游戏名🤣），19级本科在读，双非院校，专业是数字媒体技术，但我主修软件工程，学习方向是后端开发，主要语👨‍💻。\n",display_count:b,is_markdown:g
        Pattern mdPattern = Pattern.compile(option.getContentSelector()+":\"(.*)\",display_count");
        for (Element scripts : doc.getElementsByTag("script")) {
            for (DataNode dataNode : scripts.dataNodes()) {
                String wholeData = dataNode.getWholeData();
                log.info("juejin dataNode:{}", wholeData);
                if (wholeData.contains(option.getContentSelector())) {
                    log.info("juejin contains");
                    // 内容
                    Matcher matcher = mdPattern.matcher(wholeData);
                    if (matcher.find()) {
                        String md = matcher.group(1);
                        log.info("find md text success{}", md);
                        result.setMdInput(md);
                        return result;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 查找微信文档作者
     *
     * @param doc
     * @param authorKey
     * @return
     */
    public static String findWeixinAuthor(Document doc, String authorKey) {
        for (Element metaTag : doc.getElementsByTag("meta")) {
            String content = metaTag.attr("content");
            String name = metaTag.attr("name");
            if (authorKey.equals(name)) {
                return content;
            }
        }

        return null;
    }

    /**
     * 查找微信网页的封面图、标题、订阅号名
     *
     * @param doc
     * @param option
     * @return
     */
    public static HtmlSourceResult findWeixinImgAndTitleAndNickname(Document doc, HtmlSourceOption option) {
        // get <script>
        for (Element scripts : doc.getElementsByTag("script")) {
            // get data from <script>
            for (DataNode dataNode : scripts.dataNodes()) {
                // find data which contains
                if (dataNode.getWholeData().contains(option.getCoverImageKey())) {
                    log.info("contains");
                    HtmlSourceResult result = new HtmlSourceResult();

                    // 昵称
                    Pattern nikeNamePattern = Pattern.compile("var\\s+"+option.getNicknameKey()+"\\s+=\\s+\"(.*)\";");
                    Matcher nikeNameMatcher = nikeNamePattern.matcher(dataNode.getWholeData());
                    if (nikeNameMatcher.find()) {
                        String nickName = nikeNameMatcher.group(1);
                        log.info("find nickName success{}", nickName);
                        result.setNickName(nickName);
                    }

                    // 文件名
                    Pattern titlePattern = Pattern.compile("var\\s+"+option.getTitleKey()+"\\s+=\\s+'(.*)'\\.html\\(false\\);");
                    Matcher titleMatcher = titlePattern.matcher(dataNode.getWholeData());
                    if (titleMatcher.find()) {
                        String title = titleMatcher.group(1);
                        log.info("find title success{}", title);
                        result.setMdTitle(title);
                    }

                    // 封面图
                    Pattern pattern = Pattern.compile("var\\s+"+option.getCoverImageKey()+"\\s+=\\s+\"(.*)\";");
                    Matcher matcher = pattern.matcher(dataNode.getWholeData());
                    if (matcher.find()) {
                        String msg_cdn_url = matcher.group(1);
                        log.info("find msg_cdn_url success {}", msg_cdn_url);
                        result.setCoverImageUrl(msg_cdn_url);
                    }
                    return result;
                }
            }
        }
        return null;
    }


}
