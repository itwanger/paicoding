package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.Data;

import java.io.Serializable;

/**
 * 使用ognl表达式的执行方式传参
 * Created by @author yihui in 14:33 19/11/29.
 */
@Data
public class OgnlReqDTO implements Serializable {
    private static final long serialVersionUID = -3338589754140559813L;

    /**
     * ognl 表达式
     */
    private String expression;
}
