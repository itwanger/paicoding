package com.github.paicoding.forum.test.javabetter.io1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/19/24
 */
import java.nio.ByteBuffer;

public class ByteBufferExample {

    public static void main(String[] args) {
        // 模拟接收到的数据
        byte[] receivedData = {1, 2, 3, 4, 5};
        int bufferSize = 1024;  // 设置一个合理的缓冲区大小

        // 创建ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

        // 写入数据之前检查容量是否足够
        if (buffer.remaining() >= receivedData.length) {
            buffer.put(receivedData);
        } else {
            System.out.println("Not enough space in buffer to write data.");
        }

        // 准备读取数据：将limit设置为当前位置，position设回0
        buffer.flip();

        // 读取数据
        while (buffer.hasRemaining()) {
            byte data = buffer.get();
            System.out.println("Read data: " + data);
        }

        // 清空缓冲区以便再次使用
        buffer.clear();
    }
}
