package com.github.paicoding.forum.test.javabetter.collection1;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/12/23
 */
public class TreeMapDemo {
    public static void main(String[] args) {
        TreeMap<String, Integer> treeMap = new TreeMap<>();

        // 增加操作
        treeMap.put("沉默", 100);
        treeMap.put("王二", 200);
        treeMap.put("陈清扬", 300);

        // 查找操作
        int value = treeMap.get("沉默");
        System.out.println("沉默的值为：" + value);

        // 修改操作
        treeMap.put("沉默", 150);
        value = treeMap.get("沉默");
        System.out.println("修改后沉默的值为：" + value);

        // 删除操作
        treeMap.remove("陈清扬");
        System.out.println("删除陈清扬后，TreeMap中的元素为：" + treeMap);


        TreeMap<Integer,String> mapIntReverse = new TreeMap<>(Comparator.reverseOrder());
        mapIntReverse.put(3, "沉默王二");
        mapIntReverse.put(2, "沉默王二");
        mapIntReverse.put(1, "沉默王二");
        mapIntReverse.put(5, "沉默王二");
        mapIntReverse.put(4, "沉默王二");

        System.out.println(mapIntReverse);
    }
}
