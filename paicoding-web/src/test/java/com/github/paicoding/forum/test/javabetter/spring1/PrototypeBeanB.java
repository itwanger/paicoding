package com.github.paicoding.forum.test.javabetter.spring1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 3/10/24
 */
@Component
@Scope("prototype")
public class PrototypeBeanB {
    private final PrototypeBeanA prototypeBeanA;

    @Autowired
    public PrototypeBeanB(PrototypeBeanA prototypeBeanA) {
        this.prototypeBeanA = prototypeBeanA;
    }
}