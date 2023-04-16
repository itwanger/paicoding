package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class BlockingServer {
    public static void main(String[] args) throws IOException {
        // 创建服务器套接字
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        // 设置为阻塞模式（默认为阻塞模式）
        serverSocketChannel.configureBlocking(true);

        while (true) {
            // 接收客户端连接
            SocketChannel socketChannel = serverSocketChannel.accept();
            // 分配缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 读取数据
            int bytesRead = socketChannel.read(buffer);
            while (bytesRead != -1) {
                buffer.flip();
                System.out.println(StandardCharsets.UTF_8.decode(buffer));
                buffer.clear();
                bytesRead = socketChannel.read(buffer);
            }
            // 关闭套接字
            socketChannel.close();
        }
    }
}
