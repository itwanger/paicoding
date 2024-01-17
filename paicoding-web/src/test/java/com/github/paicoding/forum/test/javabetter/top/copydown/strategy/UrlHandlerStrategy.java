package com.github.paicoding.forum.test.javabetter.top.copydown.strategy;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.http.HttpUtil;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceOption;
import com.github.paicoding.forum.test.javabetter.top.copydown.HtmlSourceResult;
import com.github.paicoding.forum.test.javabetter.top.furstenheim.Pinyin4jUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public interface UrlHandlerStrategy {
    // 构造日志对象
    Logger log = LoggerFactory.getLogger(UrlHandlerStrategy.class);

    boolean match(String url);
    void handleOptions(HtmlSourceOption option);

    HtmlSourceResult convertToMD(HtmlSourceOption option);

    default void md2file(HtmlSourceResult result) {
        // 将标题转换为拼音
        String filename = Pinyin4jUtil.getFirstSpellPinYin(result.getTitle(), false);
        log.info("filename{}", filename);

        StrBuilder builder = StrBuilder.create();
        builder.append("---\n");
        // 标题写入到文件中
        builder.append("title: ").append(result.getTitle()).append("\n");
        builder.append("shortTitle: ").append(result.getTitle()).append("\n");

        boolean hasMeta = false;
        if (StringUtils.isNotBlank(result.getDescription())) {
            builder.append("description: ").append(result.getDescription()).append("\n");
        }
        if(StringUtils.isNotBlank(result.getKeywords())) {
            hasMeta = true;
            builder.append("tag:" + "\n");
            builder.append("  - 优质文章" + "\n");
        }

        if(StringUtils.isNotBlank(result.getAuthor())) {
            builder.append("author: ").append(result.getAuthor()).append("\n");
        }

        builder.append("category:\n");
        builder.append("  - ").append(result.getHtmlSourceType().getCategory()).append("\n");

        if (hasMeta) {
            builder.append("head:\n");
        }

        if (StringUtils.isNotBlank(result.getKeywords())) {
            builder.append("  - - meta\n");
            builder.append("    - name: keywords\n");
            builder.append("      content: ").append(result.getKeywords()).append("\n");
        }

        builder.append("---\n\n");
        log.info("markdown\n{}", result.getMarkdown());
        builder.append(result.getMarkdown());

        if (StringUtils.isNotBlank(result.getSourceLink())) {
            builder.append("\n\n>参考链接：[").append(result.getSourceLink()).append("](").append(result.getSourceLink()).append(")");
            builder.append("，整理：沉默王二\n");
        }

        // 准备写入到 MD 文档
        // category
        String category = result.getHtmlSourceType().getName();
        log.info("category{}", category);
        // 文件路径带文件名
        String mdPath = Paths.get(result.getFileDir(), category, filename + ".md").toString();

        FileWriter writer = new FileWriter(mdPath);
        writer.write(builder.toString());
        log.info("all done, category+filename: {}-{}", category, filename);

        // 调用默认文本编辑器打开文件
        try {
            String pathToSublime = "/Applications/Sublime Text.app/Contents/MacOS/sublime_text";
            String[] command = {pathToSublime, mdPath};
            Runtime.getRuntime().exec(command);

//            CommandLine cmdLine = new CommandLine(pathToSublime);
//            cmdLine.addArgument(mdPath); // false 表示不需要引号包围
//
//            DefaultExecutor executor = new DefaultExecutor();
//            executor.setExitValue(1);
//            ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
//            executor.setWatchdog(watchdog);
//            executor.execute(cmdLine);
        } catch (IOException e) {
            log.error("open file error", e);
        }

        // 下载封面图
        if (StringUtils.isNotBlank(result.getCover())) {
            String coverPath = Paths.get(result.getImgDest(), category, filename + ".jpg").toString();
            long size = HttpUtil.downloadFile(result.getCover(),
                    FileUtil.file(coverPath));
            log.info("cover image size{}", size);
        }
    }
}
