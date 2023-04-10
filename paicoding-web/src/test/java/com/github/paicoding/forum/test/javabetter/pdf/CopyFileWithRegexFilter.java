package com.github.paicoding.forum.test.javabetter.pdf;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/10/23
 */
public class CopyFileWithRegexFilter {
    // 定义一个常量，用于读取文件的路径
    private static final String SOURCE_PATH = "/Users/maweiqing/Documents/GitHub/toBeBetterJavaer/docs/";
    // 定义一个常量，用于存储 PDF 文件的路径
    private static final String PDF_PATH = "/Users/maweiqing/Documents/GitHub/toBeBetterJavaer/二哥的 Java 进阶之路.md";

    public static void main(String[] args) throws IOException {
        // 读取文件
        Path path = Paths.get(SOURCE_PATH + "thread/callable-future-futuretask.md");

        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        String text = String.join("\n", lines);
        text = text.substring(text.indexOf("---", 1) + 3);
        text = "\n#" + StringUtils.trim(text) + "\n";
        System.out.println(text);

        // 添加，不覆盖

        Files.write(Paths.get(PDF_PATH), text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

    }
}
