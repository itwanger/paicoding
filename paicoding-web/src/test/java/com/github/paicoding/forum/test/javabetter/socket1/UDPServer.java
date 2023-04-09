package com.github.paicoding.forum.test.javabetter.socket1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {
    public static void main(String[] args) throws IOException {
        int port = 12345;
        DatagramSocket serverSocket = new DatagramSocket(port);
        System.out.println("Server is listening on port " + port);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        serverSocket.receive(packet);
        String message = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Received: " + message);

        serverSocket.close();
    }
}
