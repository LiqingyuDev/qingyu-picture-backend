package com.qingyu.qingyupicturebackend.manager.cache;

import cn.hutool.json.JSONUtil;
import com.qingyu.qingyupicturebackend.constant.CacheConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * 缓存配置类，用于配置和初始化缓存策略。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */
@Configuration
public class CacheManager {

    /**
     * 构建缓存 key。
     *
     * @param interfaceName       接口名称
     * @param pictureQueryRequest 查询请求对象
     * @return 缓存键
     */
    public static String buildCacheKey(String interfaceName, Object pictureQueryRequest) {
        final String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        final String queryHashKey = DigestUtils.md5Hex(queryCondition);
        return String.format(CacheConstants.CACHE_KEY_PREFIX, interfaceName + queryHashKey);
    }

    /**
     * 构建分布式锁 key。
     *
     * @param interfaceName       接口名称
     * @param pictureQueryRequest 查询请求对象
     * @return 分布式锁键
     */
    public static String buildLockKey(String interfaceName, Object pictureQueryRequest) {
        final String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        final String queryHashKey = DigestUtils.md5Hex(queryCondition);
        return String.format(CacheConstants.LOCK_KEY_PREFIX, interfaceName + queryHashKey);
    }
    /**
     * 从配置文件中读取缓存策略类型，默认值为 "local"、"redis" 或 "multi-level"。
     */
    @Value("${cache.strategy}")
    private String cacheStrategy;

    /**
     * 根据配置文件中的缓存策略类型创建并返回相应的缓存策略实例。
     * <p>
     * 支持的缓存策略类型：
     * - {@code local}：使用本地缓存（如 Caffeine）。
     * - {@code redis}：使用分布式 Redis 缓存。
     * - {@code multi-level}：使用多级缓存策略，结合本地缓存和 Redis 缓存。
     *
     * @param localCacheStrategy  本地缓存策略实例
     * @param redisCacheStrategy  分布式 Redis 缓存策略实例
     * @return 根据配置选择的缓存策略实例
     * @throws IllegalArgumentException 如果配置的缓存策略类型无效
     */
    @Bean
    public CacheStrategy cacheStrategy(LocalCacheStrategy localCacheStrategy, RedisCacheStrategy redisCacheStrategy, MultiLevelCacheStrategy multiLevelCacheStrategy) {
        switch (cacheStrategy.toLowerCase()) {
            case "local":
                return localCacheStrategy;
            case "redis":
                return redisCacheStrategy;
            case "multi-level":
                return  multiLevelCacheStrategy;
            default:
                throw new IllegalArgumentException("Unknown cache strategy: " + cacheStrategy);
        }
    }
}
