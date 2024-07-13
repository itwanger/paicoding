package com.github.paicoding.forum.core.util;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务辅助工具类
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class TransactionUtil {
    /**
     * 注册事务回调-事务提交前执行，如果没在事务中就立即执行
     *
     * @param runnable
     */
    public static void registryBeforeCommitOrImmediatelyRun(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        // 处于事务中
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 等事务提交前执行，发生错误会回滚事务
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCommit(boolean readOnly) {
                    runnable.run();
                }
            });
        } else {
            // 马上执行
            runnable.run();
        }
    }

    /**
     * 事务执行完/回滚完之后执行
     *
     * @param runnable
     */
    public static void registryAfterCompletionOrImmediatelyRun(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        // 处于事务中
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 等事务提交或者回滚之后执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    runnable.run();
                }
            });
        } else {
            // 马上执行
            runnable.run();
        }
    }


    /**
     * 事务正常提交之后执行
     *
     * @param runnable
     */
    public static void registryAfterCommitOrImmediatelyRun(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        // 处于事务中
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // 等事务提交之后执行
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            // 马上执行
            runnable.run();
        }
    }
}
