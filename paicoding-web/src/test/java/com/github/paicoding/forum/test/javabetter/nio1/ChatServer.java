package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/7/23
 */
public class ChatServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private static final int PORT = 8080;

    public ChatServer() {
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("聊天室服务端启动了 " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            while (true) {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        handleKey(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            System.out.println("客户端连接上了: " + socketChannel.getRemoteAddress());
        } else if (key.isReadable()) {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = socketChannel.read(buffer);
            if (read > 0) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, read);
                System.out.println("客户端说: " + msg);
                socketChannel.write(ByteBuffer.wrap(("服务端回复: " + msg).getBytes()));
            }
        }
    }

    public static void main(String[] args) {
        new ChatServer().start();
    }
}
