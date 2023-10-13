package com.github.paicoding.forum.test.javabetter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void main(String[] args) {
        // 从指定目录读取指定的 markdown 文件
        // 将 markdown 文件中的内容读取出来，然后写入到指定的文件中
        String sourceDir = "/Users/maweiqing/Documents/GitHub/javabetter/docs/thread";
        String markdownFileName = "wangzhe-thread.md";
        String targetFilePath = "path_to_target_directory/target.md";

        try {
            copyMarkdownFile(sourceDir, markdownFileName, targetFilePath);
            System.out.println("File copied successfully!");
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }

    }
}
