package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer {
    public static void main(String[] args) {
        try {
            // 创建 ServerSocketChannel
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 绑定端口
            serverSocketChannel.bind(new InetSocketAddress(8081));
            // 设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);

            // 创建 Selector
            Selector selector = Selector.open();
            // 将 ServerSocketChannel 注册到 Selector，关注 OP_ACCEPT 事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 无限循环，处理事件
            while (true) {
                // 阻塞直到有事件发生
                selector.select();
                // 获取发生事件的 SelectionKey
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // 处理完后，从 selectedKeys 集合中移除
                    iterator.remove();

                    // 判断事件类型
                    if (key.isAcceptable()) {
                        // 有新的连接请求
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        // 接受连接
                        SocketChannel client = server.accept();
                        // 设置为非阻塞模式
                        client.configureBlocking(false);
                        // 将新的 SocketChannel 注册到 Selector，关注 OP_READ 事件
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        // 有数据可读
                        SocketChannel client = (SocketChannel) key.channel();
                        // 创建 ByteBuffer 缓冲区
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // 从 SocketChannel 中读取数据并写入 ByteBuffer
                        client.read(buffer);
                        // 翻转 ByteBuffer，准备读取
                        buffer.flip();
                        // 将数据从 ByteBuffer 写回到 SocketChannel
                        client.write(buffer);
                        // 关闭连接
                        client.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

