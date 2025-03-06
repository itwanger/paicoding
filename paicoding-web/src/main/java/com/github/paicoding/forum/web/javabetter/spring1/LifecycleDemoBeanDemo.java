package com.github.paicoding.forum.web.javabetter.spring1;

import org.springframework.context.annotation.Bean;

public class LifecycleDemoBeanDemo {
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public LifecycleDemoBean lifecycleDemoBean() {
        return new LifecycleDemoBean();
    }

    private class LifecycleDemoBean {
    }
}
