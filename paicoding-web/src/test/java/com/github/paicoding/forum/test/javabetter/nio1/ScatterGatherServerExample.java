package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ScatterGatherServerExample {
    public static void main(String[] args) throws IOException {
        // 创建一个ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(9000));

        // 接受连接
        SocketChannel socketChannel = serverSocketChannel.accept();

        // Scatter：分散读取数据到多个缓冲区
        ByteBuffer headerBuffer = ByteBuffer.allocate(128);
        ByteBuffer bodyBuffer = ByteBuffer.allocate(1024);

        ByteBuffer[] buffers = {headerBuffer, bodyBuffer};

        long bytesRead = socketChannel.read(buffers);

        // 输出缓冲区数据
        headerBuffer.flip();
        while (headerBuffer.hasRemaining()) {
            System.out.print((char) headerBuffer.get());
        }

        System.out.println();

        bodyBuffer.flip();
        while (bodyBuffer.hasRemaining()) {
            System.out.print((char) bodyBuffer.get());
        }

        // Gather：聚集数据从多个缓冲区写入到Channel
        ByteBuffer headerResponse = ByteBuffer.wrap("Header Response".getBytes());
        ByteBuffer bodyResponse = ByteBuffer.wrap("Body Response".getBytes());

        ByteBuffer[] responseBuffers = {headerResponse, bodyResponse};

        long bytesWritten = socketChannel.write(responseBuffers);

        // 关闭连接
        socketChannel.close();
        serverSocketChannel.close();
    }
}
