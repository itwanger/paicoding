package com.github.paicoding.forum.test.javabetter.thread1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/7/24
 */
public class ThreadLocalDemo {
    public static void main(String[] args) {
        ThreadLocal<User> threadLocal = new ThreadLocal<>();
        threadLocal.set(new User("Updated Value"));
    }
}

class User {
    private String name;

    public User(String name) {
        this.name = name;
    }
}
