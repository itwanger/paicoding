package com.github.paicoding.forum.test.javabetter.io1;

import java.io.File;
import java.net.URL;

public class ClassLoaderDemo {
    public static void main(String[] args) {
        String resourceName = ClassLoaderDemo.class.getName().replace('.', '/') + ".class";
        URL resourceUrl = ClassLoader.getSystemClassLoader().getResource(resourceName);
        String resourcePath = resourceUrl.getPath();
        File file = new File(resourcePath);
        System.out.println(file.getParent());
    }
}
