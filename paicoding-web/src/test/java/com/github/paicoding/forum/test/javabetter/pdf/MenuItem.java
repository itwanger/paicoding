package com.github.paicoding.forum.test.javabetter.pdf;

import lombok.Data;

import java.util.List;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/10/23
 */
@Data
public class MenuItem {
    private String text;
    private String link;
    private boolean collapsible;
    private List<Object> children;
    private String prefix;
}
