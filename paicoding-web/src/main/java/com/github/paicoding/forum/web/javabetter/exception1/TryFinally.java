package com.github.paicoding.forum.web.javabetter.exception1;

public class TryFinally {
    public static void main(String[] args) {
        try {
            throw new Exception("Exception in try");
        } catch (Exception e) {
            throw new RuntimeException("Exception in catch");
        } finally {
            throw new IllegalArgumentException("Exception in finally");
        }
    }
}
