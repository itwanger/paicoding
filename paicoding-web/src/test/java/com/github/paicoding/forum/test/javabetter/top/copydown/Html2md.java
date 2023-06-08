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
        String url = "https://www.nowcoder.com/discuss/490203144152981504";

        // itwanger/Documents/GitHub/toBeBetterJavaer/docs/nice-article/
        HtmlSourceOption option = HtmlSourceOption.builder()
                .keywordsKey("meta[name='keywords']")
                .titleKey("head title")
                .descriptionKey("meta[name='description']")
                .url(url)
                .build();

        Document document = Jsoup.connect(option.getUrl()).get();
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
