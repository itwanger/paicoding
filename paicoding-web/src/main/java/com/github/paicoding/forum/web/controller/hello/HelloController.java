package com.github.paicoding.forum.web.controller.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @program: pai_coding
 * @description: 测试/无业务的controller
 * @author: XuYifei
 * @create: 2024-10-21
 */

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
