package com.github.paicoding.forum.test.javabetter.pdf;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/10/23
 */
public class GeneratePDF {
    // 定义一个常量，用于读取文件的路径
    private static final String SIDEBAR_PATH = "/Users/maweiqing/Documents/GitHub/toBeBetterJavaer/docs/.vuepress/sidebar.ts";
    // 定义一个常量，用于存储 PDF 文件的路径
    private static final String PDF_PATH = "/Users/maweiqing/Documents/GitHub/toBeBetterJavaer/二哥的 Java 进阶之路1.md";
    public static void main(String[] args) {

        try {
            List<String> lines = Files.readAllLines(Paths.get(SIDEBAR_PATH), StandardCharsets.UTF_8);
            String fileContent = String.join("\n", lines);

            Pattern pattern = Pattern.compile("// Java核心开始(.*?)//Java核心结束", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(fileContent);

            if (matcher.find()) {
                String content = matcher.group(1);
                System.out.println("[\n" +content+
                        "\n]");

                JsonArray jsonArray = JsonParser.parseString(content).getAsJsonArray();
                System.out.println(jsonArray);

                Gson gson = new Gson();
                Map<String, Object> jsonObject = gson.fromJson(content, Map.class);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printChildren(MenuItem menuItem) {
        if (menuItem.getChildren() != null) {
            for (Object child : menuItem.getChildren()) {
                if (child instanceof String) {
                    System.out.println(child);
                } else {
                    Gson gson = new Gson();
                    MenuItem childMenuItem = gson.fromJson((JsonElement) child, MenuItem.class);
                    printChildren(childMenuItem);
                }
            }
        }
    }
}

