package com.qingyu.qingyupicturebackend.manager.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.qingyu.qingyupicturebackend.constant.CacheConstants;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存策略实现类，使用 Caffeine 作为缓存引擎。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */
@Component
public class LocalCacheStrategy implements CacheStrategy {

    private final LoadingCache<String, String> CAFFEINE_CACHE;

    public LocalCacheStrategy() {
        this.CAFFEINE_CACHE = Caffeine.newBuilder().maximumSize(1024) // 设置最大缓存条目数为 1024
                .expireAfterWrite(CacheConstants.DEFAULT_EXPIRE_TIME, CacheConstants.DEFAULT_TIME_UNIT) // 写入后 5 分钟过期
                .build(key -> new Date().toString()); // 缓存未命中时默认返回当前时间的字符串表示
    }

    @Override
    public String get(String key) {
        return CAFFEINE_CACHE.get(key);
    }

    @Override
    public String get(String key, String lockKey) {
        // 本地缓存不需要分布式锁
        return CAFFEINE_CACHE.get(key);
    }

    @Override
    public void set(String key, String value, int expireTime, TimeUnit timeUnit) {
        CAFFEINE_CACHE.put(key, value);
    }

    @Override
    public void set(String key, String lockKey, String value, int expireTime, TimeUnit timeUnit) {
        // 本地缓存不需要分布式锁
        CAFFEINE_CACHE.put(key, value);
    }
}