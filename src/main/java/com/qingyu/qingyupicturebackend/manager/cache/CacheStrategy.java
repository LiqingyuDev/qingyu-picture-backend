package com.qingyu.qingyupicturebackend.manager.cache;

import java.util.concurrent.TimeUnit;

/**
 * 缓存策略接口，定义了缓存的基本操作方法。
 * 该接口提供了获取缓存数据和设置缓存数据的功能，并支持设置缓存过期时间。
 * 同时，还提供了使用分布式锁进行缓存操作的方法。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */
public interface CacheStrategy {

    /**
     * 根据指定的键从缓存中获取字符串值。
     *
     * @param cacheKey 缓存键
     * @return 如果存在则返回对应的缓存值，否则返回 {@code null}
     */
    String get(String cacheKey);

    /**
     * 根据指定的键从缓存中获取字符串值，并使用分布式锁进行保护。
     * 该方法尝试获取分布式锁，如果成功获取锁，则从缓存中获取数据；如果获取锁失败，则返回 {@code null}。
     *
     * @param cacheKey 缓存键
     * @param lockKey  分布式锁键
     * @return 如果存在则返回对应的缓存值，否则返回 {@code null}
     */
    String get(String cacheKey, String lockKey);

    /**
     * 将指定的键值对存储到缓存中，并设置缓存的过期时间。
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param expireTime 缓存过期时间（单位由 {@code timeUnit} 指定）
     * @param timeUnit   时间单位（如秒、分钟、小时等）
     */
    void set(String key, String value, int expireTime, TimeUnit timeUnit);

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
    void set(String key, String lockKey, String value, int expireTime, TimeUnit timeUnit);
}
