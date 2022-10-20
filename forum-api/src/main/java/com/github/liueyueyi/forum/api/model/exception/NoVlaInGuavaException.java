package com.github.liueyueyi.forum.api.model.exception;

/**
 * 未命中异常
 *
 * @author yihui
 * @date 2022/8/15
 */
public class NoVlaInGuavaException extends RuntimeException {
    public NoVlaInGuavaException(String msg) {
        super(msg);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}