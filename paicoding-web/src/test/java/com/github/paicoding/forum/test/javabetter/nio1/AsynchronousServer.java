package com.github.paicoding.forum.test.javabetter.nio1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/7/23
 */
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;

public class AsynchronousServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress("localhost", 5000));

        System.out.println("服务器端启动");

        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void attachment) {
                // 接收下一个连接请求
                server.accept(null, this);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Future<Integer> readResult = client.read(buffer);

                try {
                    readResult.get();
                    buffer.flip();
                    String message = new String(buffer.array(), 0, buffer.remaining());
                    System.out.println("接收到的消息: " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });

        // 为了让服务器继续运行，我们需要阻止 main 线程退出
        Thread.currentThread().join();
    }
}

