package com.github.paicoding.forum.web.javabetter.io1;

import java.io.*;
import java.net.*;

public class TcpServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080); // 创建服务器端Socket
        System.out.println("Server started, waiting for connection...");
        Socket socket = serverSocket.accept(); // 等待客户端连接
        System.out.println("Client connected: " + socket.getInetAddress());

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        String message;
        while ((message = in.readLine()) != null) {
            System.out.println("Received: " + message);
            out.println("Echo: " + message); // 回送消息
        }

        socket.close();
        serverSocket.close();
    }
}
