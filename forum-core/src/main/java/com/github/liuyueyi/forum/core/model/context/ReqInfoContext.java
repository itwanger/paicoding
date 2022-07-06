package com.github.liuyueyi.forum.core.model.context;

import lombok.Data;

/**
 * 请求上下文，携带用户身份相关信息
 *
 * @author YiHui
 * @date 2022/7/6
 */
public class ReqInfoContext {

    /**
     * fixme 注意，下面这种方式导致在子线程中拿不到用户信息
     */
    private static ThreadLocal<ReqInfo> contexts = new ThreadLocal<>();

    public static void addReqInfo(ReqInfo reqInfo) {
        contexts.set(reqInfo);
    }

    public static void clear() {
        contexts.remove();
    }

    public static ReqInfo getReqInfo() {
        return contexts.get();
    }

    @Data
    public static class ReqInfo {
        /**
         * appKey
         */
        private String appKey;
        /**
         * 访问的域名
         */
        private String host;
        /**
         * 访问路径
         */
        private String path;
        /**
         * 客户端ip
         */
        private String clientIp;
        /**
         * referer
         */
        private String referer;
        /**
         * post 表单参数
         */
        private String payload;

        /**
         * 终点看书 app的请求，会携带这个参数
         */
        private String uuid;

        /**
         * 设备信息
         */
        private String userAgent;

        /**
         * 用户id
         */
        private Long userId;
    }
}
