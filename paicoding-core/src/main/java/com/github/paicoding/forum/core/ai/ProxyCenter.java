package com.github.paicoding.forum.core.ai;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YiHui
 * @date 2023/4/20
 */
@Slf4j
class ProxyCenter {
    /**
     * 记录每个source使用的proxy索引
     */
    private static final Map<String, Integer> HOST_PROXY_INDEX = new HashMap<>(16);
    /**
     * proxy
     */
    private static final List<ImmutablePair<String, Integer>> PROXIES =
            new ArrayList<ImmutablePair<String, Integer>>() {{
                add(ImmutablePair.of("212.50.245.101", 9898));
            }};

    /**
     * get proxy
     *
     * @return
     */
    static ImmutablePair<String, Integer> getProxy(String host) {
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
}
