package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class MemoryMappedFileCopyDemo {
    public static void main(String[] args) throws IOException {
        try (FileChannel sourceChannel = FileChannel.open(Paths.get("logs/javabetter/itwanger.txt"), StandardOpenOption.READ);
             FileChannel destinationChannel = FileChannel.open(Paths.get("logs/javabetter/itwanger2.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.READ)) {

            long fileSize = sourceChannel.size();
            MappedByteBuffer sourceMappedBuffer = sourceChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            MappedByteBuffer destinationMappedBuffer = destinationChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

            for (int i = 0; i < fileSize; i++) {
                byte b = sourceMappedBuffer.get(i);
                destinationMappedBuffer.put(i, b);
            }
        }
    }
}
