package com.github.paicoding.forum.web.javabetter.io1;

import com.github.paicoding.forum.web.javabetter.top.copydown.Constants;

import java.nio.file.Paths;

public class PathsDemo {
    public static void main(String[] args) {
        System.out.println(Paths.get(Constants.DESTINATION,
                "images","nice-article").toString());
    }
}
