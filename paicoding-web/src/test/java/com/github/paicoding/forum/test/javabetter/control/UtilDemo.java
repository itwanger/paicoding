package com.github.paicoding.forum.test.javabetter.control;

import java.net.*;
import java.util.Enumeration;

public class UtilDemo {
    public static void main(String[] args) {
//        System.out.println(UtilDemo.getLocalIP());
        System.out.println(UtilDemo.getLocalIpByNetcard());
    }
    public static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalIpByNetcard() {
        try {
            // 枚举所有的网络接口
            for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements(); ) {
                // 获取当前网络接口
                NetworkInterface item = e.nextElement();

                // 遍历当前网络接口的所有地址
                for (InterfaceAddress address : item.getInterfaceAddresses()) {
                    // 忽略回环地址和未启用的网络接口
                    if (item.isLoopback() || !item.isUp()) {
                        continue;
                    }

                    // 如果当前地址是 IPv4 地址，则返回其字符串表示
                    if (address.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) address.getAddress();
                        return inet4Address.getHostAddress();
                    }
                }
            }

            // 如果没有找到任何 IPv4 地址，则返回本地主机地址
            return InetAddress.getLocalHost().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            // 抛出运行时异常
            throw new RuntimeException(e);
        }
    }
}
