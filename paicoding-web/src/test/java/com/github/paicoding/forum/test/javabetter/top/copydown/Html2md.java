package com.github.paicoding.forum.test.javabetter.top.copydown;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.text.StrSplitter;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.http.HttpUtil;
import com.github.paicoding.forum.test.javabetter.top.furstenheim.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

@Slf4j
public class Html2md {
    public static final String urls [] = {
            "https://juejin.cn/post/7017732278509453348", // 0
            "https://mp.weixin.qq.com/s?__biz=MzUxODAzNDg4NQ==&mid=2247526594&idx=1&sn=ac0d0453b137bf7f8e92c0beacded108&chksm=f98d2c68cefaa57e092455cb7da7aee4902bb43efe0fc16bc125dea3a7b7d79cc03b81ba94a5#rd",
            "https://www.zhihu.com/question/554345647/answer/2682150828", // 2
            "http://www.cnblogs.com/dolphin0520/p/3949310.html",//博客园 3
            "http://www.itmind.net/16553.html", // 4
            "https://www.ajihuo.com/idea/4222.html", // 5 其他
            "https://blog.csdn.net/K346K346/article/details/127150054", // 6
            "https://www.mdnice.com/writing/d70e8a6f8c17430ea6981a6e0c05184b", // 7
            "https://segmentfault.com/a/1190000017040893", // 8
            "https://www.nowcoder.com/discuss/1067139",//9
            "https://github.com/CarpenterLee/JCFInternals/blob/master/markdown/4-Stack%20and%20Queue.md",//10
            "https://leetcode.cn/circle/discuss/MDq50z/view/dgDwBC/",// LeetCode 11
    };

    public static void main(String[] args) {
        String url = urls[1];

        HtmlSourceOption option = HtmlSourceOption.builder()
                // itwanger/Documents/GitHub/toBeBetterJavaer/images/nice-article/
                .imgDest(Constants.destination + "images" + Constants.fileSeparator
                        + Constants.html2mdCategory + Constants.fileSeparator)
                .mdDest(Constants.destination + "docs" + Constants.fileSeparator
                        + Constants.html2mdCategory+ Constants.fileSeparator)
                .url(url)
                .build();

        if (url.indexOf("juejin.cn") != -1) {
            // 掘金的链接
            option.setHtmlSourceType(HtmlSourceType.JUEJIN);
            option.setContentSelector("mark_content");
            option.setTitleKey("meta[itemprop='headline']");
            option.setAuthorKey("div[itemprop='author'] meta[itemprop='name']");
            option.setDescriptionKey("meta[name='description']");
            option.setKeywordsKey("meta[name='keywords']");
        } else if (url.indexOf("mp.weixin.qq.com") != -1) {

            // 微信的链接
            option.setHtmlSourceType(HtmlSourceType.WEIXIN);
            option.setContentSelector("div.rich_media_content");
            option.setCoverImageKey("msg_cdn_url");
            option.setTitleKey("msg_title");
            option.setNicknameKey("nickname");
            option.setAuthorKey("author");
            option.setDescriptionKey("meta[name='description']");
        } else if (url.indexOf("zhihu.com") != -1) {
            // 知乎的链接
            option.setHtmlSourceType(HtmlSourceType.ZHIHU);
            option.setDescriptionKey("meta[name='description']");
            option.setKeywordsKey("meta[name='keywords']");

            // 文章
            if (url.indexOf("zhuanlan") != -1) {
                option.setContentSelector("div.RichText");
                option.setTitleKey("header.Post-Header h1.Post-Title");
                option.setAuthorKey(".AuthorInfo meta[itemprop='name']");

            } else {
                // 回答
                option.setContentSelector("div.QuestionAnswer-content .RichContent-inner");
                option.setTitleKey("h1.QuestionHeader-title");
                option.setAuthorKey(".AuthorInfo-content .AuthorInfo-head .AuthorInfo-name a");
            }
        } else if (url.indexOf("cnblogs.com") != -1) {
            // 博客园的链接
            option.setHtmlSourceType(HtmlSourceType.BOKEYUAN);
            option.setContentSelector(".blogpost-body");
            option.setTitleKey("head title");
            // 文章标题里有作者名
        }  else if (url.indexOf("blog.csdn.net") != -1) {
            // CSDN的链接
            option.setHtmlSourceType(HtmlSourceType.CSDN);
            option.setContentSelector("div#article_content");
            option.setTitleKey("head title");
            option.setAuthorKey(".article-header a.follow-nickName");
            option.setDescriptionKey("meta[name='description']");
            option.setKeywordsKey("meta[name='keywords']");
            // 文章标题里有作者名
        } else if (url.indexOf("itmind.net") != -1) {
            // 小白学堂的链接
            option.setHtmlSourceType(HtmlSourceType.ITMIND);
            option.setContentSelector("article.article-content");
            option.setTitleKey("head title");
            option.setKeywordsKey("meta[name='keywords']");
            option.setDescriptionKey("meta[name='description']");
        } else if (url.indexOf("segmentfault.com") != -1) {
            // 思否的链接
            option.setHtmlSourceType(HtmlSourceType.segmentfault);
            option.setContentSelector(".article-content");
            option.setTitleKey("head title");
            option.setKeywordsKey("meta[name='keywords']");
            option.setDescriptionKey("meta[name='description']");
        } else if (url.indexOf("nowcoder.com") != -1) {
            // 牛客的链接
            option.setHtmlSourceType(HtmlSourceType.newcoder);
            option.setContentSelector(".post-topic-des");
            option.setTitleKey("head title");
            option.setKeywordsKey("meta[name='keywords']");
            option.setDescriptionKey("meta[name='description']");
        } else if (url.indexOf("github.com") != -1) {
            // GitHub的链接
            option.setHtmlSourceType(HtmlSourceType.github);
            option.setContentSelector("article.markdown-body");
            option.setTitleKey("head title");
        } else if (url.indexOf("leetcode.cn") != -1) {
            // LeetCode的链接
            option.setHtmlSourceType(HtmlSourceType.leetcode);
            option.setContentSelector(".e13l6k8o9");
            option.setTitleKey("head title");
            option.setKeywordsKey("meta[name='keywords']");
            option.setDescriptionKey("meta[name='description']");
        } else {
            // 其他定制
            // .writing-content 墨滴
            // div.byte-viewer-container 开发者客栈
            // article.article-content 麦客搜
            // div#x-content 廖雪峰
            // div.cloud-blog-detail-content-wrap 华为云
            // div#arc-body C 语言中文网
            option.setHtmlSourceType(HtmlSourceType.OTHER);
            option.setContentSelector("article");
            option.setTitleKey("head title");
            option.setKeywordsKey("meta[name='keywords']");
            option.setDescriptionKey("meta[name='description']");
        }

        convert(option);
    }

    private static void convert(HtmlSourceOption htmlSourceOption) {
        OptionsBuilder optionsBuilder = OptionsBuilder.anOptions();
        Options options = optionsBuilder
                .withEmDelimiter("*")
                .withBr("\r")
                .withCodeBlockStyle(CodeBlockStyle.FENCED)
                .withHeadingStyle(HeadingStyle.ATX)
                .build();

        CopyDown copyDown = new CopyDown(options);

        // 根据 URL 获取 jsoup 文档对象
        Document doc = null;
        try {
            doc = Jsoup.connect(htmlSourceOption.getUrl()).get();
        } catch (IOException e) {
            log.error("jsoup error{}", e);
        }

        HtmlSourceResult result = null;
        String markdown = null;

        switch (htmlSourceOption.getHtmlSourceType()) {

            case WEIXIN:
                // 微信链接时获取封面图、标题、昵称（订阅号名字）
                result = MdUtil.findWeixin(doc,htmlSourceOption);
                break;
            case JUEJIN:
                result = MdUtil.findJuejin(doc,htmlSourceOption);
                List<String> splits = StrSplitter.split(result.getMdInput(), "\\n", 0, false, false);

                StrBuilder builder = StrBuilder.create();
                for (String str : splits) {
                    builder.append(str);
                    builder.append("\n");
                }

                markdown = UnicodeUtil.toString(builder.toString());
                break;
            case ZHIHU:
                result = MdUtil.findZhihu(doc,htmlSourceOption);
                break;
            case BOKEYUAN:
                result = MdUtil.findBokeyuan(doc,htmlSourceOption);
                break;
            default:
                result = MdUtil.findDefault(doc,htmlSourceOption);
                break;
        }

        // 将标题转换为拼音
        String filename = Pinyin4jUtil.getFirstSpellPinYin(result.getMdTitle(), false);
        log.info("filename{}", filename);

        // category
        String category = htmlSourceOption.getHtmlSourceType().getName();
        log.info("category{}", category);

        // 图片参数

        // 下载封面图
        if (StringUtils.isNotBlank(result.getCoverImageUrl())) {
            long size = HttpUtil.downloadFile(result.getCoverImageUrl(),
                    FileUtil.file(htmlSourceOption.getImgDest() + category + "-"+filename + ".jpg"));
            log.info("cover image size{}", size);
        }

        StrBuilder builder = StrBuilder.create();
        builder.append("---\n");
        // 标题写入到文件中
        builder.append("title: " + result.getMdTitle() + "\n");
        builder.append("shortTitle: " + result.getMdTitle() + "\n");

        boolean hasMeta = false;
        if (StringUtils.isNotBlank(result.getDescription())) {
            hasMeta = true;
            builder.append("description: " + result.getDescription() + "\n");
        }
        if(StringUtils.isNotBlank(result.getKeywords())) {
            builder.append("tags:" + "\n");
            builder.append("  - 优质文章" + "\n");
        }

        if(StringUtils.isNotBlank(result.getAuthor())) {
            builder.append("author: " + result.getAuthor() + "\n");
        }

        builder.append("category:\n");
        builder.append("  - "+htmlSourceOption.getHtmlSourceType().getCategory()+"\n");

        if (hasMeta) {
            builder.append("head:\n");
        }

        if (StringUtils.isNotBlank(result.getKeywords())) {
            builder.append("  - - meta\n");
            builder.append("    - name: keywords\n");
            builder.append("      content: " + result.getKeywords() +"\n");
        }

        builder.append("---\n\n");

        // HTML 转 MD
        if (StringUtils.isBlank(markdown)) {
            markdown = copyDown.convert(result.getMdInput());
            log.info("markdown\n{}", markdown);
        }

        builder.append(markdown);

        if (StringUtils.isNotBlank(result.getSourceLink())) {
            builder.append("\n\n>参考链接：[" + result.getSourceLink() + "](" +result.getSourceLink() +")");

            if (StringUtils.isNotBlank(result.getNickName())) {
                builder.append("，出处：" + result.getNickName());
            }
            builder.append("，整理：沉默王二\n");
        }

        // 准备吸入到 MD 文档
        FileWriter writer = new FileWriter(htmlSourceOption.getMdDest()+ category+ Constants.fileSeparator+filename + ".md");
        writer.write(builder.toString());
        log.info("all done, category+filename: {}-{}", category, filename);
    }
}
