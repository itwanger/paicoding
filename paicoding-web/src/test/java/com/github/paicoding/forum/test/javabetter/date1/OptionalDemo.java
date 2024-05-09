package com.github.paicoding.forum.test.javabetter.date1;

import java.util.Optional;

public class OptionalDemo {
    public static void main(String[] args) {
        Optional<String> optional = Optional.of("沉默王二");
        System.out.println(optional.isPresent());          // true
        System.out.println(optional.get());                 // "沉默王二"
        System.out.println(optional.orElse("沉默王三"));    // "沉默王二"
        optional.ifPresent((s) -> System.out.println(s.charAt(0)));     // "沉"
    }
}
