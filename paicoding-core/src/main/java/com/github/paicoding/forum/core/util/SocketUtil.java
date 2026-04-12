package com.github.paicoding.forum.core.util;

import javax.net.ServerSocketFactory;
import java.io.File;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * @author YiHui
 * @date 2022/11/26
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

    private static final String PORT_FILE = System.getProperty("user.dir") + File.separator + ".dev-port";

    /**
     * 从临时文件中读取上次使用的端口号
     */
    private static int loadPersistedPort() {
        try {
            Path path = Paths.get(PORT_FILE);
            if (Files.exists(path)) {
                String content = new String(Files.readAllBytes(path)).trim();
                return Integer.parseInt(content);
            }
        } catch (Exception e) {
            // 读取失败，忽略
        }
        return -1;
    }

    /**
     * 将端口号持久化到临时文件，热部署重启时可以复用
     */
    private static void savePort(int port) {
        try {
            Files.write(Paths.get(PORT_FILE), String.valueOf(port).getBytes());
        } catch (Exception e) {
            // 写入失败，忽略
        }
    }

    /**
     * 找一个可用的端口号
     * <p>
     * 优先级：上次记住的端口 > 默认端口 > 随机端口
     * 热部署重启时会优先复用上次的端口，避免端口漂移
     *
     * @param minPort
     * @param maxPort
     * @param defaultPort
     * @return
     */
    public static int findAvailableTcpPort(int minPort, int maxPort, int defaultPort) {
        // 1. 优先尝试上次记住的端口
        int persistedPort = loadPersistedPort();
        if (persistedPort > 0 && persistedPort != defaultPort && isPortAvailable(persistedPort)) {
            savePort(persistedPort);
            return persistedPort;
        }

        // 2. 尝试默认端口
        if (isPortAvailable(defaultPort)) {
            savePort(defaultPort);
            return defaultPort;
        }

        // 3. 随机找一个可用端口
        if (maxPort <= minPort) {
            throw new IllegalArgumentException("maxPort should bigger than miPort!");
        }
        int portRange = maxPort - minPort;
        int searchCounter = 0;

        while (searchCounter <= portRange) {
            int candidatePort = findRandomPort(minPort, maxPort);
            ++searchCounter;
            if (isPortAvailable(candidatePort)) {
                savePort(candidatePort);
                return candidatePort;
            }
        }

        throw new IllegalStateException(String.format("Could not find an available %s port in the range [%d, %d] after %d attempts", SocketUtil.class.getName(), minPort, maxPort, searchCounter));
    }
}
