package com.github.paicoding.forum.test.javabetter.io1;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintWriterDemo {
    public static void main(String[] args) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("output.txt"));
            writer.println("沉默王二");
            writer.printf("他的年纪为 %d.\n", 18);
            writer.close();

            PrintWriter pw = new PrintWriter("output.txt");
            pw.println("沉默王二");
            pw.printf("他的年纪为 %d.\n", 18);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
