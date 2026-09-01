package com.github.paicoding.forum.core.util;

import com.github.paicoding.forum.core.config.ImageProperties;
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
        ImageProperties properties = new ImageProperties();
        properties.setCdnHost("https://cdn.paicoding.com/");
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
    public void shouldRewriteCdnImageSrc() {
        String html = "<p>hi</p><img src=\"https://cdn.paicoding.com/stutymore/a.png\">";

        String out = StrUtil.stabilizeHtmlImages(html);

        System.out.println("渲染结果: " + out);
        assertTrue(out.contains("x-oss-process=image/resize,w_1400/format,webp"), "src 未被重写：" + out);
        assertTrue(out.contains("data-src="), "未写入放大地址：" + out);
    }
}
