package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class AioFileDemo {

    public static void main(String[] args) {
        AioFileDemo demo = new AioFileDemo();
        demo.writeFile();
        demo.readFile();
    }

    // 使用 AsynchronousFileChannel 写入文件
    public void writeFile() {
        // 使用 Paths.get() 获取文件路径
        Path path = Paths.get("logs/itwanger/paicoding.txt");
        try {
            // 用 AsynchronousFileChannel.open() 打开文件通道，指定写入和创建文件的选项。
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            // 将要写入的字符串（"学编程就上技术派"）转换为 ByteBuffer。
            ByteBuffer buffer = StandardCharsets.UTF_8.encode("学编程就上技术派");
            // 调用 fileChannel.write() 方法将 ByteBuffer 中的内容写入文件。这是一个异步操作，因此需要使用 Future 对象等待写入操作完成。
            Future<Integer> result = fileChannel.write(buffer, 0);
            // 等待写操作完成
            result.get();

            System.out.println("写入完成");
            fileChannel.close();
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
        }
    }

    // 使用 AsynchronousFileChannel 读取文件
    public void readFile() {
        Path path = Paths.get("logs/itwanger/paicoding.txt");
        try {
            // 指定读取文件的选项。
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ);
            // 创建一个 ByteBuffer，用于存储从文件中读取的数据。
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 调用 fileChannel.read() 方法从文件中异步读取数据。该方法接受一个 CompletionHandler 对象，用于处理异步操作完成后的回调。
            fileChannel.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    // 在 CompletionHandler 的 completed() 方法中，翻转 ByteBuffer（attachment.flip()），然后使用 Charset.forName("UTF-8").decode() 将其解码为字符串并打印。最后，清空缓冲区并关闭文件通道。
                    attachment.flip();
                    System.out.println("读取的内容: " + StandardCharsets.UTF_8.decode(attachment));
                    attachment.clear();
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    // 如果异步读取操作失败，CompletionHandler 的 failed() 方法将被调用，打印错误信息。
                    System.out.println("读取失败");
                    exc.printStackTrace();
                }
            });

            // 等待异步操作完成
            Thread.sleep(1000);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
