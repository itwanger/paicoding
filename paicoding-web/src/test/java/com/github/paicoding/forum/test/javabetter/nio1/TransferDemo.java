package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TransferDemo {
    public static void main(String[] args) {
        try (FileChannel sourceChannel = FileChannel.open(Paths.get("logs/javabetter/itwanger.txt"), StandardOpenOption.READ);
             FileChannel destinationChannel = FileChannel.open(Paths.get("logs/javabetter/itwanger3.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.READ)) {
            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
