package com.qingyu.qingyupicturebackend.manager.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.qingyu.qingyupicturebackend.constant.CacheConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存策略实现类，使用 Caffeine 作为缓存引擎。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */
@Component
public class LocalCacheStrategy implements CacheStrategy {

    private static final Logger log = LoggerFactory.getLogger(LocalCacheStrategy.class);
    private final LoadingCache<String, String> CAFFEINE_CACHE;

    public LocalCacheStrategy() {
        this.CAFFEINE_CACHE = Caffeine.newBuilder().maximumSize(1024) // 设置最大缓存条目数为 1024
                .expireAfterWrite(CacheConstants.DEFAULT_EXPIRE_TIME, CacheConstants.DEFAULT_TIME_UNIT) // 写入后 5 分钟过期
                .build(key -> new Date().toString()); // 缓存未命中时默认返回当前时间的字符串表示
    }

    @Override
    public String get(String key) {
        return CAFFEINE_CACHE.getIfPresent(key);
    }

    @Override
    public String get(String key, String lockKey) {
        // 本地缓存不需要分布式锁
        return CAFFEINE_CACHE.getIfPresent(key);
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


    /**
     * 根据前缀清除缓存。
     *
     * @param prefix 缓存键的前缀
     * @return 如果成功清除至少一个缓存条目，则返回 true；否则返回 false
     */
    @Override
    public boolean clearCacheByPrefix(String prefix) {
        // 获取缓存中的所有键值对
        Map<String, String> asMap = CAFFEINE_CACHE.asMap();
        // 遍历所有键值对，删除匹配前缀的键
        boolean removed = asMap.keySet().removeIf(key -> key.startsWith(prefix));
        log.info("清除前缀为 {} 的本地缓存条目，结果为 {}", prefix, removed);
        return removed;
    }

}