package com.bx.implatform.thirdparty;

import cn.hutool.core.lang.Dict;
import com.bx.implatform.config.props.StorageProperties;
import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {
    String upload(String bucket, String path, MultipartFile file);
    String upload(String bucket, String path, String name, byte[] fileBytes, String contentType);
    boolean remove(String bucket, String path, String fileName);
    boolean isExist(String bucket, String path, String fileName);
    void makeBucket(String bucket);
    boolean bucketExists(String bucket);
    void setBucketPublic(String bucket);
    Dict getToken(String fileName, StorageProperties properties);
}
