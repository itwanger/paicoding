package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileChannelTransferToLoopExampleWithPaths {
    public static void main(String[] args) {
        Path sourcePath = Paths.get("logs/itwanger/paicoding.txt");
        Path destinationPath = Paths.get("logs/itwanger/paicoding_copy.txt");

        // 使用 try-with-resources 语句确保通道资源被正确关闭
        try (FileChannel sourceChannel = FileChannel.open(sourcePath, StandardOpenOption.READ);
             FileChannel destinationChannel = FileChannel.open(destinationPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

            long position = 0;
            long count = sourceChannel.size();

            // 循环传输，直到所有字节都被传输
            while (position < count) {
                long transferred = sourceChannel.transferTo(position, count - position, destinationChannel);
                position += transferred;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

