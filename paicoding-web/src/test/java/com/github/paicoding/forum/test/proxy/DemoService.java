package com.github.paicoding.forum.test.proxy;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class DemoService {

    public String showHello(String arg) {
        System.out.println("in function!");
        System.out.println("before return:" + arg);
        return "prefix_" + arg;
    }
}
