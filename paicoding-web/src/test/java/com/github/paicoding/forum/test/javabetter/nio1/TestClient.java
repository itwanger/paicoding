package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestClient {
    public static void main(String[] args) throws InterruptedException {
        int clientCount = 10000;
        ExecutorService executorServiceIO = Executors.newFixedThreadPool(10);
        ExecutorService executorServiceNIO = Executors.newFixedThreadPool(10);

        // 使用传统 IO 的客户端
        Runnable ioClient = () -> {
            try {
                Socket socket = new Socket("localhost", 8080);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                out.write("Hello, 沉默王二 IO!".getBytes());
                byte[] buffer = new byte[1024];
                in.read(buffer);
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // 使用 NIO 的客户端
        Runnable nioClient = () -> {
            try {
                SocketChannel socketChannel = SocketChannel.open();
                socketChannel.connect(new InetSocketAddress("localhost", 8081));
                ByteBuffer buffer = ByteBuffer.wrap("Hello, 沉默王二 NIO!".getBytes());
                socketChannel.write(buffer);
                buffer.clear();
                socketChannel.read(buffer);
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // 分别测试 NIO 和传统 IO 的服务器性能
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        for (int i = 0; i < clientCount; i++) {
            executorServiceIO.execute(ioClient);
        }
        executorServiceIO.shutdown();
        executorServiceIO.awaitTermination(1, TimeUnit.MINUTES);
        endTime = System.currentTimeMillis();
        System.out.println("传统 IO 服务器处理 " + clientCount + " 个客户端耗时: " + (endTime - startTime) + "ms");

        startTime = System.currentTimeMillis();
        for (int i = 0; i < clientCount; i++) {
            executorServiceNIO.execute(nioClient);
        }
        executorServiceNIO.shutdown();
        executorServiceNIO.awaitTermination(1, TimeUnit.MINUTES);
        endTime = System.currentTimeMillis();
        System.out.println("NIO 服务器处理 " + clientCount + " 个客户端耗时: " + (endTime - startTime) + "ms");
    }
}
