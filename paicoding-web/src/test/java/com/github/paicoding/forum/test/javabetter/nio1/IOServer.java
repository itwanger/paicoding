package com.github.paicoding.forum.test.javabetter.nio1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class IOServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080);

            while (true) {
                Socket client = serverSocket.accept();
                InputStream in = client.getInputStream();
                OutputStream out = client.getOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead = in.read(buffer);
                out.write(buffer, 0, bytesRead);

                in.close();
                out.close();
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
