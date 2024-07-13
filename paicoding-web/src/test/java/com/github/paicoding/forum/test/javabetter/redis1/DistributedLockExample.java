package com.github.paicoding.forum.test.javabetter.redis1;

/**
 * 微信搜索「沉默王二」，回复 Java
 *
 * @author 沉默王二
 * @date 4/25/24
 */
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class DistributedLockExample {

    public static void main(String[] args) {
        try {
            RedissonManager.init();
            RedissonClient redisson = RedissonManager.getClient();

            // 获取一个锁对象
            RLock lock = redisson.getLock("myLock");

            // 尝试获取锁
            if (lock.tryLock()) {
                try {
                    // 执行一些受保护的代码
                    System.out.println("Lock acquired and operations are safe to perform");
                    // 模拟一些处理工作
                    Thread.sleep(2000);
                } finally {
                    // 确保在操作完成后释放锁
                    lock.unlock();
                    System.out.println("Lock released");
                }
            } else {
                System.out.println("Unable to acquire lock");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } finally {
            RedissonManager.getClient().shutdown();
        }
    }
}

