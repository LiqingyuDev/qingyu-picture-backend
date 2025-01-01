package com.qingyu.qingyupicturebackend.manager.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存策略实现类，使用 Spring Data Redis 提供的 {@code StringRedisTemplate} 进行缓存操作。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */
@Component
public class RedisCacheStrategy implements CacheStrategy {

    /**
     * Redis 操作模板，用于执行 Redis 缓存操作。
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造函数，通过依赖注入初始化 Redis 操作模板。
     *
     * @param stringRedisTemplate Redis 操作模板实例
     */
    public RedisCacheStrategy(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 根据指定的键从 Redis 缓存中获取字符串值。
     *
     * @param key 缓存键
     * @return 如果存在则返回对应的缓存值，否则返回 {@code null}
     */
    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 将指定的键值对存储到 Redis 缓存中，并设置缓存的过期时间。
     *
     * @param key         缓存键
     * @param value       缓存值
     * @param expireTime  缓存过期时间（单位由 {@code timeUnit} 指定）
     * @param timeUnit    时间单位（如秒、分钟、小时等）
     */
    @Override
    public void set(String key, String value, int expireTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }
}
