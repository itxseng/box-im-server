package com.bx.implatform.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限相关配置
 * @author Blue
 * @version 1.0
 * @date 2025-03-21
 */
@Data
@Component
@ConfigurationProperties(prefix = "registration")
public class RegistrationProperties {

    /**
     *  注册方式(多种)
     */
    private List<String> mode;

}
