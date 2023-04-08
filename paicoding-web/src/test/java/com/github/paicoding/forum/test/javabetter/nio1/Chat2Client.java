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
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Chat2Client {

    public static void main(String[] args) throws IOException {
        // 创建一个 SocketChannel
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        // 创建一个 Selector
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        // 从控制台读取输入并发送给服务器端
        Thread sendMessageThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    System.out.println("输入客户端消息: ");
                    String message = reader.readLine();
                    if (socketChannel.isConnected()) {
                        ByteBuffer buffer = ByteBuffer.wrap((message + "\n").getBytes());
                        socketChannel.write(buffer);
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

                if (key.isConnectable()) {
                    // 连接到服务器
                    socketChannel.finishConnect();
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("已连接到服务器");
                } else if (key.isReadable()) {
                    // 读取服务器端消息
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = socketChannel.read(buffer);

                    if (bytesRead > 0) {
                        buffer.flip();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        String message = new String(bytes).trim();
                        System.out.println("服务器端消息: " + message);
                    }
                }
                keyIterator.remove();
            }
        }
    }
}

