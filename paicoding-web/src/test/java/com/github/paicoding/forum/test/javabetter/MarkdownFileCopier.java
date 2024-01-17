package com.github.paicoding.forum.test.javabetter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/25/23
 */
public class MarkdownFileCopier {
    /**
     * 复制Markdown文件
     * @param sourceDir 源目录
     * @param markdownFileName Markdown文件名称
     * @param targetFilePath 目标文件路径
     * @throws IOException 当读/写操作发生错误时抛出
     */
    public static void copyMarkdownFile(String sourceDir, String markdownFileName, String targetFilePath) throws IOException {
        // 检查源文件是否存在
        Path sourcePath = Paths.get(sourceDir, markdownFileName);
        if (!Files.exists(sourcePath)) {
            throw new IOException("Source markdown file does not exist.");
        }

        // 复制文件到目标路径
        Path targetPath = Paths.get(targetFilePath);

        // 读取文件内容到字符串中
        String content = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);

        // 使用正则表达式定位内容的起始位置
        Pattern pattern = Pattern.compile("---.*?---*(.*)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        StringBuilder desiredContent = new StringBuilder();
        while (matcher.find()) {
            desiredContent.append(matcher.group(1)).append("\n");
        }

        if (desiredContent.length() > 0) {
            // 写入到目标文件中
            // Append to the file instead of overwriting it
            try (FileWriter writer = new FileWriter(targetPath.toFile(), true)) {
                writer.write(desiredContent.toString());
                writer.write("\n"); // Optionally add a newline for separation
            }
        } else {
            System.out.println("Pattern not found in the file.");
        }
    }

    public static void main(String[] args) {
        // 从指定目录读取指定的 markdown 文件
        // 将 markdown 文件中的内容读取出来，然后写入到指定的文件中
        String sourceDir = "/Users/itwanger/Documents/Github/toBeBetterJavaer/docs/thread";
        String markdownFileName = "shengchanzhe-xiaofeizhe.md";
        String targetFilePath = "/Users/itwanger/Documents/Github/private/zsxq/二哥的并发编程进阶之路.md";

        try {
            copyMarkdownFile(sourceDir, markdownFileName, targetFilePath);
            System.out.println("File copied successfully!");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

    }
}
