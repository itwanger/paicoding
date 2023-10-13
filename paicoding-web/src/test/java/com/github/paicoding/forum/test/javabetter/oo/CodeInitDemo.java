package com.github.paicoding.forum.test.javabetter.oo;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/10/23
 */
public class CodeInitDemo {
    // 静态变量
    public static int staticVar = 1;
    // 实例变量
    public int instanceVar = 2;

    // 静态初始化块
    static {
        System.out.println("执行静态初始化块");
        staticVar = 3;
    }

    // 实例初始化块
    {
        System.out.println("执行实例初始化块");
        instanceVar = 4;
    }

    // 构造方法
    public CodeInitDemo() {
        System.out.println("执行构造方法");
    }


        public static void main(String[] args) {
            System.out.println("执行main方法");

            CodeInitDemo e1 = new CodeInitDemo();
            CodeInitDemo e2 = new CodeInitDemo();

            System.out.println("e1的静态变量：" + e1.staticVar);
            System.out.println("e1的实例变量：" + e1.instanceVar);
            System.out.println("e2的静态变量：" + e2.staticVar);
            System.out.println("e2的实例变量：" + e2.instanceVar);
    }
}
