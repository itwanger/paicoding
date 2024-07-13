package com.github.paicoding.forum.test.javabetter.socket1.httpserver;

public class ServerNotFoundException extends RuntimeException {
    public ServerNotFoundException(String message) {
        super(message);
    }

    public ServerNotFoundException(String message, Throwable e) {
        super(message, e);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
