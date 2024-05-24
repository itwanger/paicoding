package com.github.paicoding.forum.test.javabetter.spring1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 5/12/24
 */
import org.springframework.stereotype.Component;

@Component
public class MessageService {
    public void printMessage() {
        System.out.println("Hello, Spring ApplicationContext with Annotations!");
    }
}
