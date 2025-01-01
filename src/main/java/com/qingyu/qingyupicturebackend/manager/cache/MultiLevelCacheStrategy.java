package com.qingyu.qingyupicturebackend.manager.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 多级缓存策略实现类，结合本地缓存（如 Caffeine）和分布式缓存（如 Redis）。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */
@Slf4j
@Component
public class MultiLevelCacheStrategy implements CacheStrategy {

    /**
     * 本地缓存策略实例，用于快速访问和临时存储数据。
     */
    private final LocalCacheStrategy localCacheStrategy;

    /**
     * 分布式缓存策略实例，用于持久化存储和跨节点共享数据。
     */
    private final RedisCacheStrategy redisCacheStrategy;

    /**
     * 构造函数，通过依赖注入初始化本地缓存和分布式缓存策略。
     *
     * @param localCacheStrategy  本地缓存策略实例
     * @param redisCacheStrategy  分布式缓存策略实例
     */
    @Autowired
    public MultiLevelCacheStrategy(LocalCacheStrategy localCacheStrategy, RedisCacheStrategy redisCacheStrategy) {
        this.localCacheStrategy = localCacheStrategy;
        this.redisCacheStrategy = redisCacheStrategy;
    }

    /**
     * 根据指定的键从多级缓存中获取字符串值。
     * <p>
     * 获取逻辑如下：
     * - 首先尝试从本地缓存中获取数据，如果命中则直接返回。
     * - 如果本地缓存未命中，则尝试从分布式缓存中获取数据。
     * - 如果分布式缓存命中，则将数据更新到本地缓存后返回。
     * - 如果两级缓存均未命中，则返回 {@code null}。
     *
     * @param key 缓存键
     * @return 如果存在则返回对应的缓存值，否则返回 {@code null}
     */
    @Override
    public String get(String key) {
        // 尝试从本地缓存中获取数据
        String localValue = localCacheStrategy.get(key);
        if (localValue != null) {
            log.debug("Hit local cache for key: {}", key);
            return localValue;
        }

        // 尝试从分布式缓存中获取数据
        String redisValue = redisCacheStrategy.get(key);
        if (redisValue != null) {
            log.debug("Hit Redis cache for key: {}", key);
            // 更新本地缓存，设置过期时间为5分钟
            localCacheStrategy.set(key, redisValue, 5, TimeUnit.MINUTES);
            return redisValue;
        }

        // 如果缓存中没有数据，返回 null
        return null;
    }

    /**
     * 将指定的键值对存储到多级缓存中。
     * <p>
     * 存储逻辑如下：
     * - 将数据同时写入本地缓存和分布式缓存。
     * - 确保数据在两级缓存中保持一致。
     *
     * @param key         缓存键
     * @param value       缓存值
     * @param expireTime  缓存过期时间（单位由 {@code timeUnit} 指定）
     * @param timeUnit    时间单位（如秒、分钟、小时等）
     */
    @Override
    public void set(String key, String value, int expireTime, TimeUnit timeUnit) {
        // 将数据写入本地缓存
        localCacheStrategy.set(key, value, expireTime, timeUnit);
        // 将数据写入分布式缓存
        redisCacheStrategy.set(key, value, expireTime, timeUnit);
        log.debug("Set cache for key: {} with expire time: {} {}", key, expireTime, timeUnit);
    }
}
