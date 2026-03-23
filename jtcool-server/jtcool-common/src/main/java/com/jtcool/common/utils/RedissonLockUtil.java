package com.jtcool.common.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, LockCallback<T> callback) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(waitTime, leaseTime, unit)) {
                try {
                    return callback.execute();
                } finally {
                    lock.unlock();
                }
            }
            throw new RuntimeException("获取锁失败: " + lockKey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断: " + lockKey, e);
        }
    }

    @FunctionalInterface
    public interface LockCallback<T> {
        T execute();
    }
}
