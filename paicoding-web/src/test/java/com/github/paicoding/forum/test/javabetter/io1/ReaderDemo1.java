package com.github.paicoding.forum.test.javabetter.io1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

public class ReaderDemo1 {
    Reader  reader;

    public static void main(String[] args) throws IOException {
        // try-with-resources FileInputStream
        try (FileInputStream inputStream = new FileInputStream("a.txt")) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                System.out.print(new String(bytes, 0, len));
            }
        }
    }
}
