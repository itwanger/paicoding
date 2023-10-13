package com.github.paicoding.forum.test.javabetter.socket1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiThreadedServer {
    public static void main(String[] args) throws IOException {
        int port = 12345;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Client connected");
            new ClientHandler(socket).start();
        }
    }
}
class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Received: " + line);
                writer.println("Server: " + line);
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("Client disconnected");
        }
    }
}