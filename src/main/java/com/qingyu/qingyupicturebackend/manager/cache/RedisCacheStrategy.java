package com.qingyu.qingyupicturebackend.manager.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存策略实现类，使用 Spring Data Redis 提供的 {@code StringRedisTemplate} 进行缓存操作。
 * 该类实现了 {@link CacheStrategy} 接口，并提供了使用分布式锁进行缓存操作的方法。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */
@Component
@Slf4j
public class RedisCacheStrategy implements CacheStrategy {

    private final StringRedisTemplate stringRedisTemplate;
    /**
     * Redisson 客户端实例，用于分布式锁。
     */
    @Resource
    public RedissonClient redissonClient;

    @Autowired
    public RedisCacheStrategy(StringRedisTemplate stringRedisTemplate, RedissonClient redissonClient) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public String get(String cacheKey) {
        return stringRedisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * 根据指定的键从缓存中获取字符串值，并使用分布式锁进行保护。
     * 该方法尝试获取分布式锁，如果成功获取锁，则从缓存中获取数据；如果获取锁失败，则返回 {@code null}。
     *
     * @param cacheKey 缓存键
     * @param lockKey  分布式锁键
     * @return 如果存在则返回对应的缓存值，否则返回 {@code null}
     */
    @Override
    public String get(String cacheKey, String lockKey) {
        if (cacheKey == null || lockKey == null) {
            log.error("cacheKey or lockKey is null");
            return null;
        }
        // 创建锁
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁（等待3秒，5秒自动释放）
            boolean triedLock = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (triedLock) {
                try {
                    return stringRedisTemplate.opsForValue().get(cacheKey);

                } catch (Exception e) {
                    log.error("获取缓存数据异常", e);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁失败", e);
        } finally {
            // 确保锁已经被成功获取才释放
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return null; // 如果获取锁失败，返回 null
    }

    @Override
    public void set(String key, String value, int expireTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    /**
     * 将指定的键值对存储到缓存中，并设置缓存的过期时间。
     * 该方法使用分布式锁进行保护，确保在写入缓存时只有一个线程可以执行。
     *
     * @param key        缓存键
     * @param lockKey    分布式锁键
     * @param value      缓存值
     * @param expireTime 缓存过期时间（单位由 {@code timeUnit} 指定）
     * @param timeUnit   时间单位（如秒、分钟、小时等）
     */
    @Override
    public void set(String key, String lockKey, String value, int expireTime, TimeUnit timeUnit) {
        // 创建锁
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 尝试获取锁（等待3秒，5秒自动释放）
            boolean triedLock = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (triedLock) {
                stringRedisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁失败", e);
        } finally {
            // 确保锁已经被成功获取才释放
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
