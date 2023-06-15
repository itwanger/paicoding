package com.github.paicoding.forum.core.net;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

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
    private static final Cache<String, Integer> HOST_PROXY_INDEX = Caffeine.newBuilder().maximumSize(16).build();
    /**
     * proxy
     */
    private static List<ProxyType> PROXIES = new ArrayList<>();


    public static void initProxyPool(List<ProxyType> proxyTypes) {
        PROXIES = proxyTypes;
    }

    public static void initProxyPool(Environment environment, String proxyConfigPrefix) {
        PROXIES = Binder.get(environment).bind(proxyConfigPrefix, Bindable.listOf(ProxyType.class)).get();
    }

    /**
     * get proxy
     *
     * @return
     */
    static ProxyType getProxy(String host) {
        Integer index = HOST_PROXY_INDEX.getIfPresent(host);
        if (index == null) {
            index = -1;
        }

        ++index;
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
