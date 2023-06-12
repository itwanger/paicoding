package com.github.paicoding.forum.core.net;

import lombok.Data;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YiHui
 * @date 2023/6/2
 */
public class ProxyCenter {

    @Data
    @Accessors(chain = true)
    public static class ProxyType {
        /**
         * 代理类型
         */
        private Proxy.Type type;
        /**
         * 代理ip
         */
        private String ip;
        /**
         * 代理端口
         */
        private Integer port;
    }

    /**
     * 记录每个source使用的proxy索引
     */
    private static final Map<String, Integer> HOST_PROXY_INDEX = new HashMap<>(16);
    /**
     * proxy
     */
    private static final List<ProxyType> PROXIES =
            new ArrayList<ProxyType>() {{
                add(new ProxyType().setType(Proxy.Type.SOCKS).setIp("127.0.0.1").setPort(1080));
            }};


    /**
     * get proxy
     *
     * @return
     */
    static ProxyType getProxy(String host) {
        int index = -1;
        if (HOST_PROXY_INDEX.containsKey(host)) {
            index = HOST_PROXY_INDEX.get(host);
        }

        index++;
        if (index >= PROXIES.size()) {
            index = 0;
        }
        HOST_PROXY_INDEX.put(host, index);
        return PROXIES.get(index);
    }

    public static Proxy loadProxy(String host) {
        ProxyType proxyType = getProxy(host);
        if (proxyType == null) {
            return null;
        }
        return new Proxy(proxyType.type, new InetSocketAddress(proxyType.ip, proxyType.port));
    }
}
