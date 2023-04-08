package com.github.paicoding.forum.test.javabetter.nio1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/7/23
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsynchronousClient {

    public static void main(String[] args) {
        try {
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
            Future<Void> connectResult = client.connect(new InetSocketAddress("localhost", 5000));
            connectResult.get(); // 等待连接完成

            String message = "沉默王二，在吗？";
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
            Future<Integer> writeResult = client.write(buffer);
            writeResult.get(); // 等待发送完成

            System.out.println("消息发送完毕");

            client.close();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
