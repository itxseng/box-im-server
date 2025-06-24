package com.bx.implatform.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wx
 */
@Data
@Component
@ConfigurationProperties(prefix = "storage.s3")
public class S3Properties implements StorageProperties{

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String region;

    private String domain;

    private String imagePath;

    private String filePath;

    private String videoPath;

}
