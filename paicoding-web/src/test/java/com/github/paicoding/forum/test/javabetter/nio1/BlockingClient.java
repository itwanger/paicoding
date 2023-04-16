package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class BlockingClient {
    public static void main(String[] args) throws IOException {
        // 创建客户端套接字
        SocketChannel socketChannel = SocketChannel.open();
        // 连接服务器
        socketChannel.connect(new InetSocketAddress("localhost", 8080));
        // 分配缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 向服务器发送数据
        buffer.put("沉默王二，这是来自客户端的消息。".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        socketChannel.write(buffer);
        // 清空缓冲区
        buffer.clear();

        // 关闭套接字
        socketChannel.close();
    }
}

