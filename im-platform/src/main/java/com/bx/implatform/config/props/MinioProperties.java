package com.bx.implatform.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: wx
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage.minio")
public class MinioProperties implements StorageProperties{

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String domain;

    private String bucketName;

    private String imagePath;

    private String filePath;

    private String videoPath;

    private Integer expireIn;

    @Override
    public String getRegion() {
        return "";
    }
}
