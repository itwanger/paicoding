package com.github.paicoding.forum.test.javabetter.nio1;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class AsyncFileChannelFutureDemo {
    public static void main(String[] args) {
        try {
            readAllBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readAllBytes() throws Exception {
        Path path = Paths.get("docs/配套教程.md");

        try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            long position = 0;

            while (true) {
                Future<Integer> result = fileChannel.read(buffer, position);

                while (!result.isDone()) {
                    // 在这里可以执行其他任务，例如处理其他 I/O 操作
                }

                int bytesRead = result.get();
                if (bytesRead <= 0) {
                    break;
                }

                position += bytesRead;
                buffer.flip();

                byte[] data = new byte[buffer.limit()];
                buffer.get(data);
                System.out.println(new String(data));

                buffer.clear();
            }
        }
    }
}

