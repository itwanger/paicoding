package com.github.paicoding.forum.test.markdown;

import com.github.paicoding.forum.core.util.MarkdownConverter;
import org.junit.Test;

/**
 * 图片说明扩展测试
 *
 * @author 沉默王二
 * @date 2025-10-20
 */
public class ImageCaptionTest {

    @Test
    public void testImageWithAlt() {
        String markdown = "![这是一张美丽的风景图](https://example.com/image.jpg)";
        String html = MarkdownConverter.markdownToHtml(markdown);
        System.out.println("========== 带 alt 属性的图片 ==========");
        System.out.println("Markdown: " + markdown);
        System.out.println("HTML: " + html);
        System.out.println();
    }

    @Test
    public void testImageWithoutAlt() {
        String markdown = "![](https://example.com/image.jpg)";
        String html = MarkdownConverter.markdownToHtml(markdown);
        System.out.println("========== 不带 alt 属性的图片 ==========");
        System.out.println("Markdown: " + markdown);
        System.out.println("HTML: " + html);
        System.out.println();
    }

    @Test
    public void testImageWithTitle() {
        String markdown = "![图片说明](https://example.com/image.jpg \"这是标题\")";
        String html = MarkdownConverter.markdownToHtml(markdown);
        System.out.println("========== 带 alt 和 title 的图片 ==========");
        System.out.println("Markdown: " + markdown);
        System.out.println("HTML: " + html);
        System.out.println();
    }

    @Test
    public void testMultipleImages() {
        String markdown = "# 图片测试\n\n" +
                "这是第一张图片：\n\n" +
                "![美丽的风景](https://example.com/landscape.jpg)\n\n" +
                "这是第二张图片（没有说明）：\n\n" +
                "![](https://example.com/no-caption.jpg)\n\n" +
                "这是第三张图片：\n\n" +
                "![可爱的小猫](https://example.com/cat.jpg \"小猫的标题\")";

        String html = MarkdownConverter.markdownToHtml(markdown);
        System.out.println("========== 多张图片测试 ==========");
        System.out.println("Markdown:\n" + markdown);
        System.out.println("\nHTML:\n" + html);
    }
}
