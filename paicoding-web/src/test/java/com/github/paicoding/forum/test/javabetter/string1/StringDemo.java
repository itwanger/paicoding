package com.github.paicoding.forum.test.javabetter.string1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/9/23
 */
public class StringDemo {
    public static void main(String[] args) {
        String text = "沉默王二";
//        System.out.println(text.reverse());
//        System.out.println(Arrays.toString(text.getBytes(StandardCharsets.UTF_8)));
//
//        System.out.println("  沉默王二   ".trim());
//
//
//        String str = "Hello, world!";
//        String prefix = str.substring(0, 5);  // 提取前5个字符，即 "Hello,"
//        String suffix = str.substring(7);     // 提取从第7个字符开始的所有字符，即 "world!"
//
//        System.out.println(prefix);
//        System.out.println(suffix);


//        String str = "Hello, world!";
//        String subStr = str.substring(7, 12);  // 从第7个字符（包括）提取到第12个字符（不包括）
//        System.out.println(subStr);  // 输出 "world"
//
//        String prefix = str.substring(0, 5);  // 提取前5个字符，即 "Hello"
//        String suffix = str.substring(7);     // 提取从第7个字符开始的所有字符，即 "world!"
//        System.out.println(prefix);
//        System.out.println(suffix);


//        String str = "   Hello,   world!  ";
//        String trimmed = str.trim();                  // 去除字符串开头和结尾的空格
//        String[] words = trimmed.split("\\s+");       // 将字符串按照空格分隔成单词数组
//        String firstWord = words[0].substring(0, 1);  // 提取第一个单词的首字母
//        System.out.println(firstWord);                // 输出 "H"

        String str = "1234-5678-9012-3456";
        String[] parts = str.split("-");             // 将字符串按照连字符分隔成四个部分
        String last4Digits = parts[3].substring(1);  // 提取最后一个部分的后三位数字
        System.out.println(last4Digits);             // 输出 "456"
    }
}
