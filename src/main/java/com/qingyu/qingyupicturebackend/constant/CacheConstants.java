package com.qingyu.qingyupicturebackend.constant;

import cn.hutool.json.JSONUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 缓存相关的常量类。
 *
 * @author liqingyu.dev@gmail.com
 * @since 2025/1/1
 */

public interface CacheConstants {

    // 最大过期时间（单位：秒）
    public static final int MAX_EXPIRE_TIME = 600; // 10分钟

    // 最小过期时间（单位：秒）
    public static final int MIN_EXPIRE_TIME = 300; // 5分钟

    // 默认过期时间（单位：秒）
    public static final int DEFAULT_EXPIRE_TIME = 300; // 5分钟

    // 缓存键前缀
    public static final String CACHE_KEY_PREFIX = "qingyu-picture:cache:%s";

    // 分布式锁键前缀
    public static final String LOCK_KEY_PREFIX = "qingyu-picture:lock:%s";

    // 默认过期时间单位
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;
}
