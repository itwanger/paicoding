package com.github.paicoding.forum.core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 邮件限流器
 * <p>
 * 基于异常类型和关键信息进行限流，防止相同异常在短时间内重复发送大量邮件
 * </p>
 *
 * <p>限流策略：</p>
 * <ul>
 *   <li>同一个异常在限流窗口期内只发送一次邮件</li>
 *   <li>限流窗口期默认为10分钟</li>
 *   <li>记录限流窗口期内跳过的次数，在窗口期结束后会在邮件中体现</li>
 * </ul>
 *
 * @author XuYifei
 * @date 2025-01-19
 */
@Slf4j
public class EmailRateLimiter {

    /**
     * 限流窗口期（毫秒），默认10分钟
     */
    private final long windowMillis;

    /**
     * 限流记录缓存
     * Key: 异常唯一标识（异常类型 + 方法签名 + 错误消息摘要）
     * Value: 限流信息
     */
    private final Map<String, RateLimitInfo> rateLimitCache = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param windowMinutes 限流窗口期（分钟）
     */
    public EmailRateLimiter(int windowMinutes) {
        this.windowMillis = windowMinutes * 60 * 1000L;
    }

    /**
     * 检查是否允许发送邮件
     *
     * @param exceptionType  异常类型
     * @param methodSig      方法签名
     * @param errorMsg       错误消息
     * @return true-允许发送，false-限流中
     */
    public boolean allowNotify(String exceptionType, String methodSig, String errorMsg) {
        String key = buildKey(exceptionType, methodSig, errorMsg);

        long now = System.currentTimeMillis();
        RateLimitInfo info = rateLimitCache.get(key);

        if (info == null) {
            // 首次出现，允许发送
            rateLimitCache.put(key, new RateLimitInfo(now, 0));
            cleanExpiredCache(now);
            return true;
        }

        // 检查是否超过窗口期
        if (now - info.getFirstOccurTime() >= windowMillis) {
            // 窗口期已过，重置
            int skipCount = info.getSkipCount().get();
            if (skipCount > 0) {
                log.info("异常 [{}] 在过去的限流窗口期内共跳过了 {} 次邮件发送", key, skipCount);
            }
            rateLimitCache.put(key, new RateLimitInfo(now, 0));
            return true;
        }

        // 在窗口期内，限流
        info.getSkipCount().incrementAndGet();
        return false;
    }

    /**
     * 获取限流窗口期内跳过的次数
     *
     * @param exceptionType 异常类型
     * @param methodSig     方法签名
     * @param errorMsg      错误消息
     * @return 跳过次数
     */
    public int getSkipCount(String exceptionType, String methodSig, String errorMsg) {
        String key = buildKey(exceptionType, methodSig, errorMsg);
        RateLimitInfo info = rateLimitCache.get(key);
        return info == null ? 0 : info.getSkipCount().get();
    }

    /**
     * 清理过期的缓存记录
     * <p>
     * 为了防止缓存无限增长，定期清理过期的记录
     * </p>
     *
     * @param currentTime 当前时间戳
     */
    private void cleanExpiredCache(long currentTime) {
        // 每100次新异常清理一次过期缓存
        if (rateLimitCache.size() > 100 && Math.random() < 0.01) {
            rateLimitCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue().getFirstOccurTime() > windowMillis * 2
            );
            log.debug("清理过期限流缓存，当前缓存大小: {}", rateLimitCache.size());
        }
    }

    /**
     * 构建异常唯一标识Key
     *
     * @param exceptionType 异常类型
     * @param methodSig     方法签名
     * @param errorMsg      错误消息
     * @return 唯一标识Key
     */
    private String buildKey(String exceptionType, String methodSig, String errorMsg) {
        // 异常消息可能很长，只取前100个字符作为摘要
        String msgDigest = errorMsg == null ? "" :
            (errorMsg.length() > 100 ? errorMsg.substring(0, 100) : errorMsg);
        return exceptionType + "#" + methodSig + "#" + msgDigest.hashCode();
    }

    /**
     * 清空所有限流记录（用于测试或手动重置）
     */
    public void clear() {
        rateLimitCache.clear();
        log.info("已清空所有邮件限流记录");
    }

    /**
     * 获取当前限流记录数量
     *
     * @return 记录数量
     */
    public int size() {
        return rateLimitCache.size();
    }

    /**
     * 限流信息
     */
    @Data
    @AllArgsConstructor
    private static class RateLimitInfo {
        /**
         * 首次出现时间（毫秒时间戳）
         */
        private long firstOccurTime;

        /**
         * 限流窗口期内跳过的次数
         */
        private AtomicInteger skipCount;

        public RateLimitInfo(long firstOccurTime, int skipCount) {
            this.firstOccurTime = firstOccurTime;
            this.skipCount = new AtomicInteger(skipCount);
        }
    }
}
