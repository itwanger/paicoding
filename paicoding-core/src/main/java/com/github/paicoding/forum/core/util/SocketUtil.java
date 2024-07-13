package com.github.paicoding.forum.core.util;

import javax.net.ServerSocketFactory;
import java.net.ServerSocket;
import java.util.Random;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class SocketUtil {

    /**
     * 判断端口是否可用
     *
     * @param port
     * @return
     */
    public static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1);
            serverSocket.close();
            return true;
        } catch (Exception var3) {
            return false;
        }
    }

    private static Random random = new Random();

    private static int findRandomPort(int minPort, int maxPort) {
        int portRange = maxPort - minPort;
        return minPort + random.nextInt(portRange + 1);
    }

    /**
     * 找一个可用的端口号
     *
     * @param minPort
     * @param maxPort
     * @param defaultPort
     * @return
     */
    public static int findAvailableTcpPort(int minPort, int maxPort, int defaultPort) {
        if (isPortAvailable(defaultPort)) {
            return defaultPort;
        }

        if (maxPort <= minPort) {
            throw new IllegalArgumentException("maxPort should bigger than miPort!");
        }
        int portRange = maxPort - minPort;
        int searchCounter = 0;

        while (searchCounter <= portRange) {
            int candidatePort = findRandomPort(minPort, maxPort);
            ++searchCounter;
            if (isPortAvailable(candidatePort)) {
                return candidatePort;
            }
        }

        throw new IllegalStateException(String.format("Could not find an available %s port in the range [%d, %d] after %d attempts", SocketUtil.class.getName(), minPort, maxPort, searchCounter));
    }
}
