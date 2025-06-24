package com.bx.imcommon.mq;

import com.alibaba.fastjson.JSONObject;
import com.bx.imcommon.util.ThreadPoolExecutorFactory;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Redis 队列拉取定时任务
 *
 * 该类会在 Spring Boot 启动后执行，遍历所有实现了 RedisMQConsumer 接口的 Bean，
 * 从 Redis 中定时拉取消息，进行消费处理。
 *
 * 特性：
 * 1. 支持多个队列消费者同时运行；
 * 2. 每个消费者使用线程池异步拉取数据；
 * 3. 支持批量拉取、自动重试、消费失败延迟机制。
 *
 * @author: Blue
 * @date: 2024-07-15
 * @version: 1.0
 */
@Slf4j
@Component
public class RedisMQPullTask implements CommandLineRunner {

    // 使用统一的线程池执行拉取任务（可通过 ThreadPoolExecutorFactory 进行线程复用管理）
    private static final ScheduledThreadPoolExecutor EXECUTOR = ThreadPoolExecutorFactory.getThreadPoolExecutor();

    // 注入所有 RedisMQConsumer 的实现类，自动装配，容器中不存在时为空列表
    @Autowired(required = false)
    private List<RedisMQConsumer> consumers = Collections.emptyList();

    // Redis 模板封装类，用于对接队列操作
    @Autowired
    private RedisMQTemplate redisMQTemplate;

    /**
     * 项目启动时执行，初始化每个消费者的任务
     */
    @Override
    public void run(String... args) {
        consumers.forEach((consumer -> {
            // 获取消费者类上的注解参数（配置队列名、批次大小、拉取周期等）
            RedisMQListener annotation = consumer.getClass().getAnnotation(RedisMQListener.class);
            String queue = annotation.queue();        // 队列 key
            int batchSize = annotation.batchSize();   // 每批拉取条数
            int period = annotation.period();         // 周期（毫秒）

            // 获取当前消费者的泛型参数类型（即要反序列化成的消息类型）
            Type superClass = consumer.getClass().getGenericSuperclass();
            Type type = ((ParameterizedType)superClass).getActualTypeArguments()[0];

            // 启动一个线程拉取并处理消息
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    List<Object> datas = new LinkedList<>();
                    try {
                        // 如果消费者准备就绪
                        if (consumer.isReady()) {
                            String key = consumer.generateKey(); // 获取 Redis 队列 key
                            // 从队列拉取一个批次的数据
                            List<Object> objects = pullBatch(key, batchSize);
                            for (Object obj : objects) {
                                if (obj instanceof JSONObject) {
                                    // 将 JSON 对象转换为目标类型
                                    JSONObject jsonObject = (JSONObject)obj;
                                    Object data = jsonObject.toJavaObject(type);
                                    // 单条消息处理
                                    consumer.onMessage(data);
                                    datas.add(data);
                                }
                            }
                            // 如果有数据，执行批量处理（可选）
                            if (!datas.isEmpty()) {
                                consumer.onMessage(datas);
                            }
                        }
                    } catch (Exception e) {
                        log.error("数据消费异常, 队列: {}", queue, e);
                        // 如果出现异常，延迟 10 秒后重试本任务
                        EXECUTOR.schedule(this, 10, TimeUnit.SECONDS);
                        return;
                    }

                    // 判断是否继续消费下一批数据
                    if (!EXECUTOR.isShutdown()) {
                        if (datas.size() < batchSize) {
                            // 拉取的数据已处理完，延迟一段时间再拉
                            EXECUTOR.schedule(this, period, TimeUnit.MILLISECONDS);
                        } else {
                            // 当前批次未消费完，立即拉下一批
                            EXECUTOR.execute(this);
                        }
                    }
                }
            });
        }));
    }

    /**
     * 从 Redis 队列中拉取数据（支持批量或逐条拉取）
     * @param key Redis 列表 key
     * @param batchSize 批次大小
     * @return 返回拉取的数据列表
     */
    private List<Object> pullBatch(String key, Integer batchSize) {
        List<Object> objects = new LinkedList<>();
        int retry = 3; // 最多重试3次
        while (retry-- > 0) {
            try {
                // 如果底层支持批量拉取
                if (redisMQTemplate.isSupportBatchPull()) {
                    objects = redisMQTemplate.opsForList().leftPop(key, batchSize);
                } else {
                    // 否则使用循环逐条拉取
                    Object obj = redisMQTemplate.opsForList().leftPop(key);
                    while (!Objects.isNull(obj) && objects.size() < batchSize) {
                        objects.add(obj);
                        obj = redisMQTemplate.opsForList().leftPop(key);
                    }
                    if (!Objects.isNull(obj)){
                        objects.add(obj); // 多余一个也拉上
                    }
                }
                break; // 拉取成功跳出重试
            } catch (Exception e) {
                log.warn("Redis队列拉取失败，重试次数: {}, 队列: {}", 3 - retry, key, e);
                try {
                    Thread.sleep(200); // 简单退避等待
                } catch (InterruptedException ignored) {}
            }
        }
        return objects;
    }

    /**
     * 应用关闭时关闭线程池
     */
    @PreDestroy
    public void destory() {
        log.info("消费线程停止...");
        ThreadPoolExecutorFactory.shutDown();
    }
}
