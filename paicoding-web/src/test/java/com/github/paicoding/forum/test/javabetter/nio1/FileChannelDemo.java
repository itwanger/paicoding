package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileChannelDemo {
    public static void main(String[] args) throws IOException {
        try (FileChannel sourceChannel = FileChannel.open(Paths.get("logs/javabetter/itwanger.txt"), StandardOpenOption.READ);
             FileChannel destinationChannel = FileChannel.open(Paths.get("logs/javabetter/itwanger1.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (sourceChannel.read(buffer) != -1) {
                buffer.flip();
                destinationChannel.write(buffer);
                buffer.clear();
            }
        }
    }
}
