package com.github.paicoding.forum.test.javabetter.string1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/9/23
 */
public class StringHashCodeExample {
    public static void main(String[] args) {
        String text1 = "沉默王二";
        String text2 = "沉默王二";

        // 计算字符串 text1 的哈希值，此时会进行计算并缓存哈希值
        int hashCode1 = text1.hashCode();
        System.out.println("第一次计算 text1 的哈希值: " + hashCode1);

        // 再次计算字符串 text1 的哈希值，此时直接返回缓存的哈希值
        int hashCode1Cached = text1.hashCode();
        System.out.println("第二次计算: " + hashCode1Cached);

        // 计算字符串 text2 的哈希值，由于字符串常量池的存在，实际上 text1 和 text2 指向同一个字符串对象
        // 所以这里直接返回缓存的哈希值
        int hashCode2 = text2.hashCode();
        System.out.println("text2 直接使用缓存: " + hashCode2);
    }
}
