package com.bx.imcommon.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类，封装常用的 Redis 操作方法，支持静态调用。
 */
@Component
public class RedisUtils {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);
    private static RedisTemplate<String, Object> staticRedisTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 在 Bean 初始化完成后，将注入的 RedisTemplate 赋值给静态变量
     */
    @PostConstruct
    public void init() {
        logger.info("Starting RedisUtils initialization...");
        try {
            if (redisTemplate == null) {
                logger.error("Injected RedisTemplate is null.");
                throw new IllegalStateException("RedisTemplate injection failed");
            }

            // 配置序列化方式
            logger.info("Configuring serializers...");
            StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
            Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = createJackson2JsonRedisSerializer();

            logger.info("Setting key serializer...");
            redisTemplate.setKeySerializer(stringRedisSerializer);
            logger.info("Setting value serializer...");
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            logger.info("Setting hash key serializer...");
            redisTemplate.setHashKeySerializer(stringRedisSerializer);
            logger.info("Setting hash value serializer...");
            redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

            logger.info("Calling afterPropertiesSet()...");
            redisTemplate.afterPropertiesSet();

            staticRedisTemplate = redisTemplate;
            logger.info("RedisUtils initialization completed successfully.");
        } catch (Exception e) {
            logger.error("RedisUtils initialization failed: ", e);
            throw new RuntimeException("RedisUtils initialization failed", e);
        }
    }

    private Jackson2JsonRedisSerializer<Object> createJackson2JsonRedisSerializer() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            // 解决 jackson2 无法反序列化 LocalDateTime 的问题
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.registerModule(new JavaTimeModule());
            // 忽略空值
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            // 使用 BasicPolymorphicTypeValidator 替代已弃用的方法
            PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(Object.class)
                    .build();
            objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
            // 忽略无效字段
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        } catch (Exception e) {
            logger.error("Failed to create Jackson2JsonRedisSerializer: ", e);
            throw e;
        }
    }
    /**
     * 存储在list头部
     *
     * @param key   键
     * @param value 值
     * @return 成功返回 1，失败返回 0
     */
    public static Long lLeftPush(String key, String value) {
        return staticRedisTemplate.opsForList().leftPush(key, value);
    }
    /**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间(秒)
     * @return 是否设置成功
     */
    public static boolean expire(String key, long time) {
        try {
            if (time > 0) {
                return Boolean.TRUE.equals(staticRedisTemplate.expire(key, time, TimeUnit.SECONDS));
            }
            return true;
        } catch (Exception e) {
            logger.error("设置缓存过期时间失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 根据 key 获取过期时间
     * @param key 键 不能为 null
     * @return 时间(秒) 返回 0 代表为永久有效
     */
    public static long getExpire(String key) {
        try {
            return Objects.requireNonNullElse(staticRedisTemplate.getExpire(key, TimeUnit.SECONDS), 0L);
        } catch (Exception e) {
            logger.error("获取缓存过期时间失败，key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 判断 key 是否存在
     * @param key 键
     * @return true 存在 false 不存在
     */
    public static boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(staticRedisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("判断缓存 key 是否存在失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key 可以传一个值 或多个
     */
    @SuppressWarnings("unchecked")
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            try {
                if (key.length == 1) {
                    staticRedisTemplate.delete(key[0]);
                } else {
                    staticRedisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
                }
            } catch (Exception e) {
                logger.error("删除缓存失败，keys: {}", (Object) key, e);
            }
        }
    }

    /**
     * 删除缓存
     * @param keys 键集合
     */
    public static void del(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        try {
            staticRedisTemplate.delete(keys);
        } catch (Exception e) {
            logger.error("删除缓存失败，keys: {}", keys, e);
        }
    }

    // ============================String=============================

    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public static Object get(String key) {
        try {
            return key == null ? null : staticRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("获取缓存值失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 获取列表长度
     *
     * @param key
     * @return
     */
    public static Long lLen(String key) {
        try {
            return Objects.requireNonNullElse(staticRedisTemplate.opsForList().size(key), 0L);
        } catch (Exception e) {
            logger.error("获取列表长度失败，key: {}", key, e);
            return 0L;
        }
    }

    /**
     * 获取列表长度
     *
     * @param key
     * @return
     */
    public static List<Object> lAll(String key) {
        try {
            Long size = lLen(key);
            if (size.equals(0L)) {
                return new ArrayList<>();
            }
            return staticRedisTemplate.opsForList().range(key, 0, size - 1);
        } catch (Exception e) {
            logger.error("获取列表所有元素失败，key: {}", key, e);
            return new ArrayList<>();
        }
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public static boolean set(String key, Object value) {
        try {
            staticRedisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("设置缓存值失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time 要大于 0 如果 time 小于等于 0 将设置无限期
     * @return true成功 false 失败
     */
    public static boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                staticRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置缓存值并设置时间失败，key: {}, time: {}", key, time, e);
            return false;
        }
    }

    /**
     * 递增
     * @param key 键
     * @param delta 要增加几(大于 0)
     * @return 递增后的值
     */
    public static long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于 0");
        }
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForValue().increment(key, delta));
        } catch (Exception e) {
            logger.error("递增缓存值失败，key: {}, delta: {}", key, delta, e);
            throw e;
        }
    }

    /**
     * 递减
     * @param key 键
     * @param delta 要减少几(大于 0)
     * @return 递减后的值
     */
    public static long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于 0");
        }
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForValue().increment(key, -delta));
        } catch (Exception e) {
            logger.error("递减缓存值失败，key: {}, delta: {}", key, delta, e);
            throw e;
        }
    }

    // ================================Map=================================

    /**
     * HashGet
     * @param key 键 不能为 null
     * @param item 项 不能为 null
     * @return 值
     */
    public static Object hget(String key, String item) {
        try {
            return staticRedisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            logger.error("获取 hash 缓存值失败，key: {}, item: {}", key, item, e);
            return null;
        }
    }

    /**
     * 获取 hashKey 对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public static Map<Object, Object> hmget(String key) {
        try {
            return staticRedisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            logger.error("获取 hash 缓存所有键值失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public static boolean hmset(String key, Map<String, Object> map) {
        try {
            staticRedisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            logger.error("设置 hash 缓存所有键值失败，key: {}", key, e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public static boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            staticRedisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("设置 hash 缓存所有键值并设置时间失败，key: {}, time: {}", key, time, e);
            return false;
        }
    }

    /**
     * 向一张 hash 表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true 成功 false失败
     */
    public static boolean hset(String key, String item, Object value) {
        try {
            staticRedisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            logger.error("向 hash 表中放入数据失败，key: {}, item: {}", key, item, e);
            return false;
        }
    }

    /**
     * 向一张 hash 表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 时间(秒) 注意:如果已存在的 hash 表有时间,这里将会替换原有的时间
     * @return true 成功 false失败
     */
    public static boolean hset(String key, String item, Object value, long time) {
        try {
            staticRedisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("向 hash 表中放入数据并设置时间失败，key: {}, item: {}, time: {}", key, item, time, e);
            return false;
        }
    }

    /**
     * 删除 hash 表中的值
     * @param key 键 不能为 null
     * @param item 项 可以使多个 不能为 null
     */
    public static void hdel(String key, Object... item) {
        try {
            staticRedisTemplate.opsForHash().delete(key, item);
        } catch (Exception e) {
            logger.error("删除 hash 表中的值失败，key: {}, items: {}", key, item, e);
        }
    }

    /**
     * 判断 hash 表中是否有该项的值
     * @param key 键 不能为 null
     * @param item 项 不能为 null
     * @return true 存在 false不存在
     */
    public static boolean hHasKey(String key, String item) {
        try {
            return Boolean.TRUE.equals(staticRedisTemplate.opsForHash().hasKey(key, item));
        } catch (Exception e) {
            logger.error("判断 hash 表中是否有该项的值失败，key: {}, item: {}", key, item, e);
            return false;
        }
    }

    /**
     * hash 递增 如果不存在,就会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 要增加几(大于 0)
     * @return 递增后的值
     */
    public static double hincr(String key, String item, double by) {
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForHash().increment(key, item, by));
        } catch (Exception e) {
            logger.error("hash 递增失败，key: {}, item: {}, by: {}", key, item, by, e);
            throw e;
        }
    }

    /**
     * hash 递减
     * @param key 键
     * @param item 项
     * @param by 要减少记(大于 0)
     * @return 递减后的值
     */
    public static double hdecr(String key, String item, double by) {
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForHash().increment(key, item, -by));
        } catch (Exception e) {
            logger.error("hash 递减失败，key: {}, item: {}, by: {}", key, item, by, e);
            throw e;
        }
    }

    // ============================set=============================

    /**
     * 根据 key 获取 Set 中的所有值
     * @param key 键
     * @return 值集合
     */
    public static Set<Object> sGet(String key) {
        try {
            return staticRedisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            logger.error("根据 key 获取 Set 中的所有值失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 根据 value 从一个 set 中查询,是否存在
     * @param key 键
     * @param value 值
     * @return true 存在 false不存在
     */
    public static boolean sHasKey(String key, Object value) {
        try {
            return Boolean.TRUE.equals(staticRedisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            logger.error("根据 value 从一个 set 中查询是否存在失败，key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 将数据放入 set 缓存
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public static long sSet(String key, Object... values) {
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForSet().add(key, values));
        } catch (Exception e) {
            logger.error("将数据放入 set 缓存失败，key: {}, values: {}", key, values, e);
            return 0;
        }
    }

    /**
     * 将 set 数据放入缓存
     * @param key 键
     * @param time 时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public static long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = staticRedisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return Objects.requireNonNull(count);
        } catch (Exception e) {
            logger.error("将 set 数据放入缓存并设置时间失败，key: {}, time: {}, values: {}", key, time, values, e);
            return 0;
        }
    }

    /**
     * 获取 set 缓存的长度
     * @param key 键
     * @return 长度
     */
    public static long sGetSetSize(String key) {
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForSet().size(key));
        } catch (Exception e) {
            logger.error("获取 set 缓存的长度失败，key: {}", key, e);
            return 0;
        }
    }

    /**
     * 移除值为 value 的元素
     * @param key 键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public static long setRemove(String key, Object... values) {
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForSet().remove(key, values));
        } catch (Exception e) {
            logger.error("移除 set 缓存中值为 value 的元素失败，key: {}, values: {}", key, values, e);
            return 0;
        }
    }

    // ===============================list=================================

    /**
     * 获取 list 缓存的内容
     * @param key 键
     * @param start 开始
     * @param end 结束 0 到 -1 代表所有值
     * @return 值列表
     */
    public static List<Object> lGet(String key, long start, long end) {
        try {
            return staticRedisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            logger.error("获取 list 缓存的内容失败，key: {}, start: {}, end: {}", key, start, end, e);
            return null;
        }
    }

    /**
     * 获取 list 缓存的长度
     * @param key 键
     * @return 长度
     */
    public static long lGetListSize(String key) {
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForList().size(key));
        } catch (Exception e) {
            logger.error("获取 list 缓存的长度失败，key: {}", key, e);
            return 0;
        }
    }

    /**
     * 通过索引 获取 list 中的值
     * @param key 键
     * @param index 索引 index>=0 时， 0 表头，1 第二个元素，依次类推；index<0 时，-1，表尾，-2 倒数第二个元素，依次类推
     * @return 值
     */
    public static Object lGetIndex(String key, long index) {
        try {
            return staticRedisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            logger.error("通过索引获取 list 中的值失败，key: {}, index: {}", key, index, e);
            return null;
        }
    }

    /**
     * 将 list 放入缓存
     * @param key 键
     * @param value 值
     * @return 是否成功
     */
    public static boolean lSet(String key, Object value) {
        try {
            staticRedisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            logger.error("将 list 放入缓存失败，key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 将 list 放入缓存
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return 是否成功
     */
    public static boolean lSet(String key, Object value, long time) {
        try {
            staticRedisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("将 list 放入缓存并设置时间失败，key: {}, value: {}, time: {}", key, value, time, e);
            return false;
        }
    }

    /**
     * 将 list 放入缓存
     * @param key 键
     * @param value 值列表
     * @return 是否成功
     */
    public static boolean lSet(String key, List<Object> value) {
        try {
            staticRedisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            logger.error("将 list 列表放入缓存失败，key: {}, value: {}", key, value, e);
            return false;
        }
    }

    /**
     * 将 list 放入缓存
     * @param key 键
     * @param value 值列表
     * @param time 时间(秒)
     * @return 是否成功
     */
    public static boolean lSet(String key, List<Object> value, long time) {
        try {
            staticRedisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("将 list 列表放入缓存并设置时间失败，key: {}, value: {}, time: {}", key, value, time, e);
            return false;
        }
    }

    /**
     * 根据索引修改 list 中的某条数据
     * @param key 键
     * @param index 索引
     * @param value 值
     * @return 是否成功
     */
    public static boolean lUpdateIndex(String key, long index, Object value) {
        try {
            staticRedisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            logger.error("根据索引修改 list 中的某条数据失败，key: {}, index: {}, value: {}", key, index, value, e);
            return false;
        }
    }

    /**
     * 移除 N 个值为 value 的元素
     * @param key 键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public static long lRemove(String key, long count, Object value) {
        try {
            return Objects.requireNonNull(staticRedisTemplate.opsForList().remove(key, count, value));
        } catch (Exception e) {
            logger.error("移除 N 个值为 value 的元素失败，key: {}, count: {}, value: {}", key, count, value, e);
            return 0;
        }
    }


    public static boolean tryLock(String key, long expireMillis) {
        Boolean success = staticRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMillis(expireMillis));
        return Boolean.TRUE.equals(success);
    }

    public static void unlock(String key) {
        staticRedisTemplate.delete(key);
    }
}