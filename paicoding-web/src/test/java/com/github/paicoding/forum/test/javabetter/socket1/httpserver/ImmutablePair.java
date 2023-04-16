package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

import lombok.Getter;

/**
 * 需要重写equals/hashcode方法，否则这个对象不能作为HashMap的key，否则会出问题
 *
 * @param <L>
 * @param <R>
 * @author yihui
 */
public final class ImmutablePair<L, R> {
    @Getter
    private final L left;
    @Getter
    private final R right;

    private ImmutablePair(final L l, final R r) {
        this.left = l;
        this.right = r;
    }

    public static <L, R> ImmutablePair<L, R> of(L left, R right) {
        return new ImmutablePair<>(left, right);
    }
}
