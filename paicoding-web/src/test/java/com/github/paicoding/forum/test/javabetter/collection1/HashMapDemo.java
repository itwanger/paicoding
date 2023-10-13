package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/12/23
 */
public class HashMapDemo {
    public static void main(String[] args) {
        // 创建 HashMap 对象，键类型为 String，值类型为 String
        Map<String, String> map = new HashMap<>();

        // 使用 put() 方法向 HashMap 中添加数据
        map.put("chenmo", "沉默");
        map.put("wanger", "王二");
        map.put("chenqingyang", "陈清扬");

        // 遍历 HashMap，输出所有键值对
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
        }
    }
}
