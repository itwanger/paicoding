package com.github.paicoding.forum.test.javabetter.socket1;

import java.io.IOException;
import java.net.*;

public class UDPClient {
    public static void main(String[] args) throws IOException {
        String hostname = "localhost";
        int port = 12345;

        InetAddress address = InetAddress.getByName(hostname);
        DatagramSocket clientSocket = new DatagramSocket();

        String message = "Hello, server!";
        byte[] buffer = message.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        clientSocket.send(packet);
        System.out.println("Message sent");

        clientSocket.close();
    }
}

