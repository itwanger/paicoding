package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import java.lang.reflect.Type;

public interface IArgParser extends Comparable<IArgParser> {


    ImmutablePair<Type, Object> parse(String type, String value);

    /**
     * 排序
     *
     * @return
     */
    default int order() {
        return 10;
    }
}

