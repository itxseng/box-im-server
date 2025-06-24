package com.bx.implatform.config;

import com.bx.implatform.util.SnowflakeIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wx
 */
@Configuration
public class IdGeneratorConfig {

    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        // workerId = 1, datacenterId = 1，可根据部署环境配置
        return new SnowflakeIdGenerator(1L, 1L);
    }
}
