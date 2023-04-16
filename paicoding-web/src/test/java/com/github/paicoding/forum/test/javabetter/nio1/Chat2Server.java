package com.github.paicoding.forum.test.javabetter.nio1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/7/23
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class Chat2Server {

    public static void main(String[] args) throws IOException {
        // 创建一个 ServerSocketChannel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));

        // 创建一个 Selector
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("聊天室服务端启动了");

        // 客户端连接
        AtomicReference<SocketChannel> clientRef = new AtomicReference<>();

        // 从控制台读取输入并发送给客户端
        Thread sendMessageThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    System.out.println("输入服务器端消息: ");
                    String message = reader.readLine();
                    SocketChannel client = clientRef.get();
                    if (client != null && client.isConnected()) {
                        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
                        client.write(buffer);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sendMessageThread.start();

        while (true) {
            int readyChannels = selector.select();

            if (readyChannels == 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    // 接受客户端连接
                    SocketChannel client = serverSocketChannel.accept();
                    System.out.println("客户端已连接");
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    clientRef.set(client);
                } else if (key.isReadable()) {
                    // 读取客户端消息
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = channel.read(buffer);

                    if (bytesRead > 0) {
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        String message = new String(bytes).trim();
                        System.out.println("客户端消息: " + message);
                    }
                }
                keyIterator.remove();
            }
        }
    }
}

