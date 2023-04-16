package com.github.paicoding.forum.test.javabetter.nio1;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

public class AsyncFileChannelDemo {
    public static void main(String[] args) {
        Path path = Paths.get("docs/配套教程.md");
        try {
            readAllBytes(path);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void readAllBytes(Path path) throws IOException, InterruptedException {
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        AtomicLong position = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(1);

        fileChannel.read(buffer, position.get(), null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer bytesRead, Object attachment) {
                if (bytesRead > 0) {
                    position.addAndGet(bytesRead);
                    buffer.flip();
                    byte[] data = new byte[buffer.limit()];
                    buffer.get(data);
                    System.out.print(new String(data));
                    buffer.clear();

                    fileChannel.read(buffer, position.get(), attachment, this);
                } else {
                    latch.countDown();
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.out.println("Error: " + exc.getMessage());
                latch.countDown();
            }
        });

        latch.await();
    }
}



