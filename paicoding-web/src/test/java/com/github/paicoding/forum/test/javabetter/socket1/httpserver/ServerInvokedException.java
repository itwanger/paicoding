package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

public class ServerInvokedException extends RuntimeException {
    public ServerInvokedException(String message, Throwable e) {
        super(message, e);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}