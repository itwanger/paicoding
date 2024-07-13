package com.github.paicoding.forum.test.javabetter.spring1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/11/24
 */
@SpringBootTest
public class LifecycleBeanTest {

    @Autowired
    private ApplicationContext context;

    @Test
    public void testBeanLifecycle() {
        System.out.println("获取LifecycleDemoBean实例...");
        LifecycleDemoBean bean = context.getBean(LifecycleDemoBean.class);
    }
}
