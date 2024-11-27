package com.github.paicoding.forum.web.javabetter.exception1;

public class TryFinallyAll {
    public static void main(String[] args) {
        Exception catchException = null;
        try {
            throw new Exception("Exception in try");
        } catch (Exception e) {
            catchException = e;
            throw new RuntimeException("Exception in catch");
        } finally {
            try {
                throw new IllegalArgumentException("Exception in finally");
            } catch (IllegalArgumentException e) {
                if (catchException != null) {
                    System.out.println("Catch exception: " + catchException.getMessage());
                }
                System.out.println("Finally exception: " + e.getMessage());
            }
        }
    }
}
