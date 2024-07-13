package com.github.paicoding.forum.test.javabetter.jvm;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 12/22/23
 */
import java.util.function.Function;

public class LambdaExample {
    public static void main(String[] args) {
        // 使用 Lambda 表达式定义一个函数
        Function<Integer, Integer> square = x -> x * x;

        // 调用这个函数
        int result = square.apply(5);

        System.out.println(result); // 输出 25
    }
}

