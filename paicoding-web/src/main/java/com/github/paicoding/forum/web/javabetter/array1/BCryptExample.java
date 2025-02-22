package com.github.paicoding.forum.web.javabetter.array1;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptExample {

    public static void main(String[] args) {
        // 创建一个 BCryptPasswordEncoder 实例
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 注册时加密密码
        String rawPassword = "沉默王二是条狗";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 打印加密后的密码（每次加密结果都不同）
        System.out.println("加密后的密码: " + encodedPassword);

        System.out.println(args);

        // 登录时验证密码
        boolean isPasswordMatch = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("密码验证结果: " + isPasswordMatch); // 输出为 true
    }
}
