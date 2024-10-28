package com.github.paicoding.forum.web.javabetter.shousi;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArrayIntersection {
    public static void main(String[] args) {
        // 示例数组
        String[] a = {"沉默王二", "沉默王三", "沉默王四", "沉默王五"};
        String[] b = {"沉默王三", "沉默王六", "沉默王二", "沉默王八"};

        // 使用 Stream API 获取两个数组的交集
        Set<String> commonElements = findCommonElements(a, b);

        // 输出结果
        System.out.println("相同的元素: " + commonElements);
    }

    public static Set<String> findCommonElements(String[] a, String[] b) {
        // 将数组 a 转换为 List，然后使用 filter 来过滤出数组 b 中也存在的元素
        return Arrays.stream(a)
                .filter(element -> Arrays.asList(b).contains(element))  // 过滤条件
                .collect(Collectors.toSet());  // 收集结果为 Set 集合，去除重复元素
    }
}
