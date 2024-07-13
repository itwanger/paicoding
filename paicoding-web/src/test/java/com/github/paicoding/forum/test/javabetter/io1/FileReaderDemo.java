package com.github.paicoding.forum.test.javabetter.io1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class FileReaderDemo {
    public static void main(String[] args) throws IOException {

        File textFile = new File("docs/约定.md");
        // 给一个 FileReader 的示例
        // try-with-resources FileReader
        try(FileReader reader = new FileReader(textFile);) {
            // read(char[] cbuf)
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer, 0, buffer.length)) != -1) {
                System.out.print(new String(buffer, 0, len));
            }
        }
    }
}
