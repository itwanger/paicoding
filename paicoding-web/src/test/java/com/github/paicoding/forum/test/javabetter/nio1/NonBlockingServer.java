package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class NonBlockingServer {
    public static void main(String[] args) throws IOException {
        // 创建服务器套接字
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);

        // 创建选择器
        Selector selector = Selector.open();
        // 注册服务器套接字到选择器
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    // 接收客户端连接
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    // 读取数据
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = socketChannel.read(buffer);

                    if (bytesRead != -1) {
                        buffer.flip();
                        System.out.print(StandardCharsets.UTF_8.decode(buffer));
                        buffer.clear();
                    } else {
                        // 客户端已断开连接，取消选择键并关闭通道
                        key.cancel();
                        socketChannel.close();
                    }
                }
            }
        }
    }
}
