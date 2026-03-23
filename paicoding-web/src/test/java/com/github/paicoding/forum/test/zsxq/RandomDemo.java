package com.github.paicoding.forum.test.zsxq;

import cn.hutool.core.util.NumberUtil;

import java.util.Arrays;

public class RandomDemo {
    public static void main(String[] args) {
        System.out.println("恭喜这6位球友：");
        // 一共 432 位球友留言，抽 6 位锦鲤送 50 元红包
        System.out.println(Arrays.toString(NumberUtil.generateBySet(1,433,6)));
    }
}
