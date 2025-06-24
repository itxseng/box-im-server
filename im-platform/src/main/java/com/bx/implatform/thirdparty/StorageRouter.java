package com.bx.implatform.thirdparty;


import com.bx.implatform.config.props.MinioProperties;
import com.bx.implatform.config.props.S3Properties;
import com.bx.implatform.config.props.StorageProperties;
import com.bx.implatform.enums.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StorageRouter {

    private final Map<String, CloudStorageService> strategyMap;

    private final MinioProperties minioProperties;
    private final S3Properties s3Properties;

    @Value("${storage.type:minio}")
    private String type;

    public CloudStorageService getService() {
        CloudStorageService service = strategyMap.get(type.toLowerCase());
        if (service == null) {
            throw new IllegalArgumentException("不支持的存储类型: " + type);
        }
        return service;
    }

    public StorageProperties getProperties() {
        if ("s3".equalsIgnoreCase(type)) {
            return s3Properties;
        }
        return minioProperties;
    }

    public String getType() {
        return type;
    }
}
