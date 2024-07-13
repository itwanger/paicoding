package com.github.paicoding.forum.test.javabetter.thread1;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 8/12/23
 */
public class ConditionDemo {

    public static void main(String[] args) {
        Condition condition = new Condition() {
            @Override
            public void await() throws InterruptedException {

            }

            @Override
            public void awaitUninterruptibly() {

            }

            @Override
            public long awaitNanos(long nanosTimeout) throws InterruptedException {
                return 0;
            }

            @Override
            public boolean await(long time, TimeUnit unit) throws InterruptedException {
                return false;
            }

            @Override
            public boolean awaitUntil(@NotNull Date deadline) throws InterruptedException {
                return false;
            }

            @Override
            public void signal() {

            }

            @Override
            public void signalAll() {

            }
        };
    }
}
