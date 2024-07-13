package com.github.paicoding.forum.core.mdc;

import org.slf4j.MDC;

/**
 * @author XuYifei
 * @date 2024-07-12
 */
public class MdcUtil {
    public static final String TRACE_ID_KEY = "traceId";

    public static void add(String key, String val) {
        MDC.put(key, val);
    }

    public static void addTraceId() {
        // traceId的生成规则，技术派提供了两种生成策略，可以使用自定义的也可以使用SkyWalking; 实际项目中选择一种即可
        MDC.put(TRACE_ID_KEY, SelfTraceIdGenerator.generate());
    }

    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static void reset() {
        String traceId = MDC.get(TRACE_ID_KEY);
        MDC.clear();
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static void clear() {
        MDC.clear();
    }
}
