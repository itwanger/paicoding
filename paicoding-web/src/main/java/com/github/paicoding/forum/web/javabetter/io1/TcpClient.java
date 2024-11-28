package com.github.paicoding.forum.web.javabetter.io1;

import java.io.*;
import java.net.*;

public class TcpClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 8080); // 连接服务器
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println("Hello, Server!"); // 发送消息
        System.out.println("Server response: " + in.readLine()); // 接收服务器响应

        socket.close();
    }
}