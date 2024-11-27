package com.github.paicoding.forum.web.javabetter.shousi;

public class MaxRotatedString {
    public static void main(String[] args) {
        String numStr = "23132";  // 例子中的字符串
        String maxStr = findMaxRotation(numStr);
        System.out.println("最大的轮询结果: " + maxStr);
    }

    public static String findMaxRotation(String numStr) {
        String maxStr = numStr;  // 初始化为原始字符串
        int length = numStr.length();

        // 轮询整个字符串，比较每次轮询结果
        for (int i = 1; i < length; i++) {
            // 生成轮询字符串
            String rotatedStr = numStr.substring(i) + numStr.substring(0, i);

            // 更新最大值
            if (rotatedStr.compareTo(maxStr) > 0) {
                maxStr = rotatedStr;
            }
        }
        return maxStr;
    }
}
