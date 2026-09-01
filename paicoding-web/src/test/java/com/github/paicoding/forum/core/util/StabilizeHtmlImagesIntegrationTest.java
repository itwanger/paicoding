package com.github.paicoding.forum.core.util;

import com.github.paicoding.forum.core.config.ImageProperties;
import com.github.paicoding.forum.core.config.OssProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 在真实 Spring 上下文下跑一遍 stabilizeHtmlImages，复现线上"代码已部署但参数没加上"的现象。
 *
 * @author Claude
 */
public class StabilizeHtmlImagesIntegrationTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    public void setUp() {
        context = new AnnotationConfigApplicationContext();
        // 刻意复现线上的配置形态：cdn-host 指向另一个不支持 OSS 处理的域名，
        // 真正该用来匹配的是 oss.host
        ImageProperties properties = new ImageProperties();
        properties.setCdnHost("https://cdn.tobebetterjavaer.com/");
        OssProperties oss = new OssProperties();
        oss.setHost("https://cdn.paicoding.com/");
        properties.setOss(oss);
        context.registerBean(ImageProperties.class, () -> properties);
        context.registerBean(SpringUtil.class);
        context.refresh();
    }

    @AfterEach
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void shouldRewriteOssImageButNotOtherCdn() {
        String html = "<p>hi</p>"
                + "<img src=\"https://cdn.paicoding.com/stutymore/a.png\">"
                + "<img src=\"https://cdn.tobebetterjavaer.com/paicoding/b.jpg\">";

        String out = StrUtil.stabilizeHtmlImages(html);

        System.out.println("渲染结果: " + out);
        assertTrue(out.contains("cdn.paicoding.com/stutymore/a.png?x-oss-process=image/resize,w_1400/format,webp"),
                "OSS 桶的图片未被重写：" + out);
        assertTrue(out.contains("data-src="), "未写入放大地址：" + out);
        assertTrue(out.contains("cdn.tobebetterjavaer.com/paicoding/b.jpg\""),
                "另一个 CDN 的图片应原样保留：" + out);
    }
}
