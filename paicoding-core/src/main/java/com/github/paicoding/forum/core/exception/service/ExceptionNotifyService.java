package com.github.paicoding.forum.core.exception.service;

import com.github.paicoding.forum.core.exception.model.ExceptionContext;

/**
 * 异常通知能力抽象，定义在 core 以避免 core 反向依赖 service。
 */
public interface ExceptionNotifyService {

    boolean sendExceptionNotify(ExceptionContext context, String notifyEmails);

    boolean sendExceptionNotify(ExceptionContext context);

    boolean shouldNotify(ExceptionContext context, boolean enableRateLimit);
}

