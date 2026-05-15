package com.github.paicoding.forum.core.util;

import com.github.paicoding.forum.core.markdown.CustomAdmonitionBlockParser;
import com.github.paicoding.forum.core.markdown.CustomAdmonitionExtension;
import com.github.paicoding.forum.core.markdown.ImageCaptionExtension;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
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
        if (markdown == null) {
            return "";
        }
        markdown = renderVideoEmbeds(markdown);

        // 创建一个 MutableDataSet 对象来配置 Markdown 解析器的选项
        MutableDataSet options = new MutableDataSet();

        // 添加各种 Markdown 解析器的扩展
        options.set(Parser.EXTENSIONS, Arrays.asList(
                AutolinkExtension.create(),     // 自动链接扩展，将URL文本转换为链接
                EmojiExtension.create(),        // 表情符号扩展，用于解析表情符号
                GitLabExtension.create(),       // GitLab特有的Markdown扩展
                FootnoteExtension.create(),     // 脚注扩展，用于添加和解析脚注
                TaskListExtension.create(),     // 任务列表扩展，用于创建任务列表
                CustomAdmonitionExtension.create(),   // 提示框扩展，用于创建提示框
                ImageCaptionExtension.create(), // 图片说明扩展，将alt文本显示为图片底部说明
                TablesExtension.create()));     // 表格扩展，用于解析和渲染表格


        // 使用配置的选项构建一个 Markdown 解析器
        Parser parser = Parser.builder(options).build();
        // 使用相同的选项构建一个 HTML 渲染器
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // 解析传入的 Markdown 文本并将其渲染为 HTML
        return renderer.render(parser.parse(markdown));
    }

    /**
     * 仅把独占一行的视频短语法转换为播放器；普通链接仍交给 Markdown 的 link/autolink 规则处理。
     */
    private static String renderVideoEmbeds(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return markdown;
        }

        String[] lines = markdown.split("\\r?\\n", -1);
        StringBuilder builder = new StringBuilder(markdown.length());
        boolean inCodeFence = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                inCodeFence = !inCodeFence;
                appendLine(builder, line, i);
                continue;
            }

            String videoHtml = inCodeFence ? null : buildVideoHtml(trimmed);
            appendLine(builder, videoHtml == null ? line : videoHtml, i);
        }

        return builder.toString();
    }

    private static void appendLine(StringBuilder builder, String line, int index) {
        if (index > 0) {
            builder.append('\n');
        }
        builder.append(line);
    }

    private static String buildVideoHtml(String line) {
        if (line.startsWith("@[bilibili](") && line.endsWith(")")) {
            String bvid = line.substring("@[bilibili](".length(), line.length() - 1);
            if (bvid.matches("BV[a-zA-Z0-9]+")) {
                return "<div class=\"video-container\">\n"
                        + "<iframe src=\"https://player.bilibili.com/player.html?bvid=" + bvid + "&page=1&high_quality=1&danmaku=0\" scrolling=\"no\" border=\"0\" frameborder=\"0\" framespacing=\"0\" loading=\"lazy\" allowfullscreen=\"true\"></iframe>\n"
                        + "</div>";
            }
        }

        if (line.startsWith("@[youtube](") && line.endsWith(")")) {
            String videoId = line.substring("@[youtube](".length(), line.length() - 1);
            if (videoId.matches("[a-zA-Z0-9_-]+")) {
                return "<div class=\"video-container\">\n"
                        + "<iframe src=\"https://www.youtube.com/embed/" + videoId + "\" frameborder=\"0\" loading=\"lazy\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen=\"true\"></iframe>\n"
                        + "</div>";
            }
        }

        if (line.startsWith("@[tencent](") && line.endsWith(")")) {
            String videoId = line.substring("@[tencent](".length(), line.length() - 1);
            if (videoId.matches("[a-z0-9]+")) {
                return "<div class=\"video-container\">\n"
                        + "<iframe src=\"https://v.qq.com/txp/iframe/player.html?vid=" + videoId + "\" frameborder=\"0\" loading=\"lazy\" allowfullscreen=\"true\"></iframe>\n"
                        + "</div>";
            }
        }

        if (line.startsWith("@[aliyun-vod](") && line.endsWith(")")) {
            String videoId = line.substring("@[aliyun-vod](".length(), line.length() - 1);
            if (videoId.matches("[a-zA-Z0-9_-]+")) {
                return "<div class=\"video-container video-container--aliyun-vod\">\n"
                        + "<video src=\"/video/play/proxy?videoId=" + videoId + "\" controls preload=\"metadata\"></video>\n"
                        + "</div>";
            }
        }

        return null;
    }
}
