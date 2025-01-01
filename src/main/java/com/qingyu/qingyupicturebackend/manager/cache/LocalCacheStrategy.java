package com.qingyu.qingyupicturebackend.manager.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Data;
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

    /**
     * 本地缓存配置，基于 Caffeine 构建的加载型缓存（LoadingCache）。
     * <p>
     * 缓存策略如下：
     * - 缓存条目在写入后 5 分钟后过期（expireAfterWrite）。
     * - 缓存的最大容量为 1024 个条目（maximumSize）。
     * - 当缓存未命中时，默认返回当前时间的字符串表示。
     */
    private final LoadingCache<String, String> CAFFEINE_CACHE;

    /**
     * 构造函数，初始化 Caffeine 缓存。
     */
    public LocalCacheStrategy() {
        this.CAFFEINE_CACHE = Caffeine.newBuilder()
                .maximumSize(1024) // 设置最大缓存条目数为 1024
                .expireAfterWrite(5, TimeUnit.MINUTES) // 写入后 5 分钟过期
                .build(key -> new Date().toString()); // 缓存未命中时默认返回当前时间的字符串表示
    }

    /**
     * 根据指定的键从缓存中获取字符串值。
     *
     * @param key 缓存键
     * @return 如果存在则返回对应的缓存值，否则返回默认值（当前时间的字符串表示）
     */
    @Override
    public String get(String key) {
        return CAFFEINE_CACHE.get(key);
    }

    /**
     * 将指定的键值对存储到缓存中。
     * 注意：此方法不会设置自定义的过期时间，而是遵循构造时设定的全局过期策略。
     *
     * @param key         缓存键
     * @param value       缓存值
     * @param expireTime  过期时间（单位由 {@code timeUnit} 指定），此参数在此方法中未使用
     * @param timeUnit    时间单位（如秒、分钟、小时等），此参数在此方法中未使用
     */
    @Override
    public void set(String key, String value, int expireTime, TimeUnit timeUnit) {
        CAFFEINE_CACHE.put(key, value);
    }
}
