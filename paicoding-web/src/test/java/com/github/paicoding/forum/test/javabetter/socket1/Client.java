package com.github.paicoding.forum.test.javabetter.socket1;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        String hostname = "localhost";
        int port = 12345;

        Socket socket = new Socket(hostname, port);
        System.out.println("Connected to the server");

        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        writer.println("Hello, server!");
        String response = reader.readLine();
        System.out.println("Server response: " + response);

        socket.close();
    }
}
