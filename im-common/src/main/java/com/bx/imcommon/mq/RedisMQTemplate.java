package com.bx.imcommon.mq;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Properties;

/**
 * Redis 消息队列模板类（扩展自 RedisTemplate）
 *
 * 功能说明：
 * 1. 基于 Spring 的 RedisTemplate 进行拓展；
 * 2. 提供 Redis 版本获取能力；
 * 3. 提供是否支持批量拉取判断（Redis >= 6.2 才支持 LMPOP 等指令）；
 *
 * 用于配合 RedisMQPullTask 判断是否可以使用批量拉取提升拉取效率。
 *
 * @author: Blue
 * @date: 2024-07-16
 * @version: 1.0
 */
public class RedisMQTemplate extends RedisTemplate<String, Object> {

    // 缓存 Redis 的版本号，避免重复获取连接
    private String version = Strings.EMPTY;

    /**
     * 获取 Redis 的版本号（如 "6.2.7"）
     * - 首次访问会通过连接查询并缓存；
     * - 后续直接返回缓存值。
     *
     * @return Redis 版本字符串
     */
    public String getVersion() {
        if (version.isEmpty()) {
            // 获取 Redis 原生连接
            RedisConnection connection = RedisConnectionUtils.getConnection(getConnectionFactory());
            // 通过 info 命令获取信息
            Properties properties = connection.info();
            // 提取版本信息
            version = properties.getProperty("redis_version");
            // 释放连接资源
            RedisConnectionUtils.releaseConnection(connection, getConnectionFactory());
        }
        return version;
    }

    /**
     * 判断当前 Redis 是否支持批量拉取操作
     * - Redis 从 6.2 起引入 LPOP count 参数（或 LMPOP）
     * - 如果版本 >= 6.2，则返回 true
     *
     * @return true 表示支持批量拉取；false 表示不支持
     */
    Boolean isSupportBatchPull() {
        String version = getVersion();
        String[] arr = version.split("\\.");
        // 版本号格式异常，无法判断
        if (arr.length < 2) {
            return false;
        }
        // 主版本号
        Integer firVersion = Integer.valueOf(arr[0]);
        // 次版本号
        Integer secVersion = Integer.valueOf(arr[1]);
        // Redis >= 6.2 才支持批量拉取
        return firVersion > 6 || (firVersion == 6 && secVersion >= 2);
    }

}
