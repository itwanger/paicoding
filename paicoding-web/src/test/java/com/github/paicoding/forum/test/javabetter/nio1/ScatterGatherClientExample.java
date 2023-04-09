package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ScatterGatherClientExample {
    public static void main(String[] args) throws IOException {
        // 创建一个SocketChannel
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 9000));

        // 发送数据到服务器
        String header = "Header Content";
        String body = "Body Content";

        ByteBuffer headerBuffer = ByteBuffer.wrap(header.getBytes());
        ByteBuffer bodyBuffer = ByteBuffer.wrap(body.getBytes());

        ByteBuffer[] buffers = {headerBuffer, bodyBuffer};
        socketChannel.write(buffers);

        // 从服务器接收数据
        ByteBuffer headerResponseBuffer = ByteBuffer.allocate(128);
        ByteBuffer bodyResponseBuffer = ByteBuffer.allocate(1024);

        ByteBuffer[] responseBuffers = {headerResponseBuffer, bodyResponseBuffer};

        long bytesRead = socketChannel.read(responseBuffers);

        // 输出接收到的数据
        headerResponseBuffer.flip();
        while (headerResponseBuffer.hasRemaining()) {
            System.out.print((char) headerResponseBuffer.get());
        }

        bodyResponseBuffer.flip();
        while (bodyResponseBuffer.hasRemaining()) {
            System.out.print((char) bodyResponseBuffer.get());
        }

        // 关闭连接
        socketChannel.close();
    }
}
