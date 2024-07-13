package com.github.paicoding.forum.test.javabetter.top.copydown;

import com.github.paicoding.forum.test.javabetter.top.copydown.strategy.*;
import com.github.paicoding.forum.test.javabetter.top.furstenheim.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class Html2md {
    public static void main(String[] args) throws IOException {
        String url = "https://mp.weixin.qq.com/s/qF9HTmJa9d5Zv60N2DBs4w";

        // itwanger/Documents/GitHub/toBeBetterJavaer/docs/nice-article/
        HtmlSourceOption option = HtmlSourceOption.builder()
                .keywordsKey("meta[name='keywords']")
                .titleKey("head title")
                .descriptionKey("meta[name='description']")
                .url(url)
                .build();

//        // 首先登录
//        Connection.Response loginResponse = Jsoup.connect("https://blog.csdn.net/login")
//                .data("username", "www.qing_gee@163.com", "password", "") // 你的登录表单参数
//                .method(Connection.Method.POST)
//                .execute();

        Document document = Jsoup.connect(option.getUrl())
//                .cookies(loginResponse.cookies())
//                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537")
                .get();
        // 生成 markdown
        OptionsBuilder optionsBuilder = OptionsBuilder.anOptions();
        // 设置 markdown 选项
        Options options = optionsBuilder
                .withEmDelimiter("*")
                .withBr("\r")
                .withCodeBlockStyle(CodeBlockStyle.FENCED)
                .withHeadingStyle(HeadingStyle.ATX)
                .build();

        // 创建转换器
        CopyDown copyDown = new CopyDown(options);

        List<UrlHandlerStrategy> strategies = Arrays.asList(
                new JuejinUrlHandlerStrategy(copyDown, document),
                new WeixinUrlHandlerStrategy(copyDown, document),
                new ZhihuUrlHandlerStrategy(copyDown, document),
                new DefaultUrlHandlerStrategy(copyDown, document)
                // 其他策略...
        );

        for (UrlHandlerStrategy strategy : strategies) {
            if (strategy.match(url)) {
                strategy.handleOptions(option);

                HtmlSourceResult result = strategy.convertToMD(option);
                result.setFileDir(Paths.get(Constants.DESTINATION,
                        "docs",
                        "nice-article").toString());
                result.setImgDest(Paths.get(Constants.DESTINATION,
                        "images","nice-article").toString());
                // 转载链接
                result.setSourceLink(option.getUrl());
                // 转一下
                result.setHtmlSourceType(option.getHtmlSourceType());

                strategy.md2file(result);
                break;
            }
        }
    }
}
