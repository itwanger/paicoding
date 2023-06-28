package com.github.paicoding.forum.test.zsxq;

import cn.hutool.core.util.RandomUtil;

public class RandomDemo {
    public static void main(String[] args) {
        System.out.println("恭喜这位球友：");
        // 一共 75 位球友留言，抽一位锦鲤送 60 元红包
        System.out.println(RandomUtil.randomInt(1, 75));
    }
}
