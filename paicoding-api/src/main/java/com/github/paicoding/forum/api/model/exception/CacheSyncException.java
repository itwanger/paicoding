package com.github.paicoding.forum.api.model.exception;


/**
 * @author xuyifei
 * 进行数据同步时如果没有对应的注解则抛出此异常
 */
public class CacheSyncException extends RuntimeException {

    public static enum CacheSyncExceptionEnum{
        NO_CACHE_TYPE_ANNOTATION("没有对应的缓存类型注解");

        private final String message;

        CacheSyncExceptionEnum(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public CacheSyncException(String message) {
        super(message);
    }

    public CacheSyncException(CacheSyncExceptionEnum cacheSyncExceptionEnum) {
        super(cacheSyncExceptionEnum.getMessage());
    }
}
