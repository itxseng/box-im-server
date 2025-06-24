package com.bx.implatform.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * @author wx
 */
@Component
@RequiredArgsConstructor
public class MessageIdGenerator {

    private static final long EPOCH = 1_750_000_000_000L; // 自定义起始时间戳（毫秒）
    private static final int  MAX_SEQUENCE = 999;         // 每毫秒最多 1000 条消息

    private static long lastTimestamp = -1L;
    private static int  sequence = 0;

    public static synchronized Long nextId() {
        long now = System.currentTimeMillis();

        if (now == lastTimestamp) {
            sequence++;
            if (sequence > MAX_SEQUENCE) {
                // 等待下一毫秒
                while ((now = System.currentTimeMillis()) <= lastTimestamp);
                sequence = 0;
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = now;

        // 生成格式：去掉前缀时间戳，保留 13 位 + 3 位序列（共最多 16 位）
        return now * 1000 + sequence;
    }
}
