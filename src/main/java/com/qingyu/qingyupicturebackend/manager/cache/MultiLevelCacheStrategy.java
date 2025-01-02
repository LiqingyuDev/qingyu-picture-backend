package com.qingyu.qingyupicturebackend.manager.cache;

import com.qingyu.qingyupicturebackend.constant.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    @Resource
    private RedisCacheStrategy redisCacheStrategy;
    @Resource
    private LocalCacheStrategy localCacheStrategy;

    /**
     * 根据指定的键从缓存中获取字符串值。
     *
     * @param cacheKey 缓存键
     * @return 如果存在则返回对应的缓存值，否则返回 {@code null}
     */
    @Override
    public String get(String cacheKey) {
        // 尝试从本地缓存获取数据
        String value = localCacheStrategy.get(cacheKey);
        if (value != null) {
            log.debug("从本地缓存获取到数据: key={}, value={}", cacheKey, value);
            return value;
        }

        // 尝试从分布式缓存获取数据
        value = redisCacheStrategy.get(cacheKey);
        if (value != null) {
            log.debug("从分布式缓存获取到数据: key={}, value={}", cacheKey, value);
            // 将数据同步到本地缓存
            localCacheStrategy.set(cacheKey, value, CacheConstants.DEFAULT_EXPIRE_TIME, CacheConstants.DEFAULT_TIME_UNIT);
        }

        return value;
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
        // 尝试从本地缓存获取数据
        String value = localCacheStrategy.get(cacheKey);
        if (value != null) {
            log.debug("从本地缓存获取到数据: key={}, value={}", cacheKey, value);
            return value;
        }

        // 尝试从分布式缓存获取数据，并使用分布式锁保护
        value = redisCacheStrategy.get(cacheKey, lockKey);
        if (value != null) {
            log.debug("从分布式缓存获取到数据: key={}, value={}", cacheKey, value);
            // 将数据同步到本地缓存
            localCacheStrategy.set(cacheKey, value, CacheConstants.DEFAULT_EXPIRE_TIME, CacheConstants.DEFAULT_TIME_UNIT);
        }

        return value;
    }

    /**
     * 将指定的键值对存储到缓存中，并设置缓存的过期时间。
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param expireTime 缓存过期时间（单位由 {@code timeUnit} 指定）
     * @param timeUnit   时间单位（如秒、分钟、小时等）
     */
    @Override
    public void set(String key, String value, int expireTime, TimeUnit timeUnit) {
        // 将数据写入本地缓存
        localCacheStrategy.set(key, value, expireTime, timeUnit);
        // 将数据写入分布式缓存
        redisCacheStrategy.set(key, value, expireTime, timeUnit);
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
        // 使用分布式锁保护
        redisCacheStrategy.set(key, lockKey, value, expireTime, timeUnit);
        // 将数据写入本地缓存
        localCacheStrategy.set(key, value, expireTime, timeUnit);
    }
}
