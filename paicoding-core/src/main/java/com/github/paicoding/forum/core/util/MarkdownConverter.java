package com.github.paicoding.forum.core.util;

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.gitlab.GitLabExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.Arrays;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/15/23
 */
public class MarkdownConverter {
    // 定义一个静态方法，将 Markdown 文本转换为 HTML
    public static String markdownToHtml(String markdown) {
        // 创建一个 MutableDataSet 对象来配置 Markdown 解析器的选项
        MutableDataSet options = new MutableDataSet();

        // 添加各种 Markdown 解析器的扩展
        options.set(Parser.EXTENSIONS, Arrays.asList(
                AutolinkExtension.create(),     // 自动链接扩展，将URL文本转换为链接
                EmojiExtension.create(),        // 表情符号扩展，用于解析表情符号
                GitLabExtension.create(),       // GitLab特有的Markdown扩展
                FootnoteExtension.create(),     // 脚注扩展，用于添加和解析脚注
                TaskListExtension.create(),     // 任务列表扩展，用于创建任务列表
                TablesExtension.create()));     // 表格扩展，用于解析和渲染表格

        // 使用配置的选项构建一个 Markdown 解析器
        Parser parser = Parser.builder(options).build();
        // 使用相同的选项构建一个 HTML 渲染器
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // 解析传入的 Markdown 文本并将其渲染为 HTML
        return renderer.render(parser.parse(markdown));
    }
}
