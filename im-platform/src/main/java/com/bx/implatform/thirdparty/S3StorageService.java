package com.bx.implatform.thirdparty;

import cn.hutool.core.lang.Dict;
import com.bx.implatform.config.props.StorageProperties;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component("s3")
@RequiredArgsConstructor
public class S3StorageService implements CloudStorageService {

    private final S3Client s3Client;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            log.error("S3 查询 bucket 失败: {}", e.awsErrorDetails().errorMessage(), e);
            return false;
        }
    }

    @Override
    public void makeBucket(String bucketName) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            log.warn("Bucket [{}] 已存在", bucketName);
        } catch (S3Exception e) {
            log.error("S3 创建 bucket 失败: {}", e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public void setBucketPublic(String bucket) {
        // 暂不实现，S3 推荐使用预签名或 IAM 策略控制访问
        log.info("S3 不推荐直接设置 bucket 为 public，建议使用预签名或 IAM 控制");
    }

    @Override
    public Dict getToken(String fileName, StorageProperties properties) {
        try {
            String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String key = datePrefix + "/" + fileName;

            S3Presigner presigner = S3Presigner.builder()
                    .region(Region.of(properties.getRegion()))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                            )
                    )
                    .build();


            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucketName())
                    .key(key)
                    .contentType("application/octet-stream")
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            presigner.close();
            return Dict.create()
                    .set("uploadType", "aws_s3")
                    .set("fileName", key)
                    .set("fileUrl", properties.getDomain() + "/" + key)
                    .set("url", presignedRequest.url().toString())
                    .set("method", "PUT")
                    .set("expire", 600);

        } catch (Exception e) {
            log.error("生成 S3 预签名上传 URL 失败", e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "生成上传地址失败");
        }
    }

    @Override
    public String upload(String bucketName, String path, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("上传文件为空");
            return null;
        }
        try {
            String objectKey = buildObjectKey(path, file.getOriginalFilename());
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return objectKey;

        } catch (IOException | S3Exception e) {
            log.error("S3 上传文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String upload(String bucketName, String path, String name, byte[] fileBytes, String contentType) {
        try {
            String objectKey = buildObjectKey(path, name);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(fileBytes));
            return objectKey;

        } catch (S3Exception e) {
            log.error("S3 上传字节文件失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean remove(String bucketName, String path, String fileName) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path + "/" + fileName)
                    .build());
            return true;
        } catch (S3Exception e) {
            log.error("S3 删除对象失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isExist(String bucketName, String path, String fileName) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(path + "/" + fileName)
                    .build());
            return true;
        } catch (S3Exception e) {
            if (e instanceof NoSuchKeyException || e.statusCode() == 404) {
                return false;
            }
            log.warn("S3 检查文件存在异常: {}", e.getMessage(), e);
            return false;
        }
    }

    private String buildObjectKey(String path, String originalFilename) {
        String suffix = "";
        if (StringUtils.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String datePrefix = LocalDate.now().format(DATE_FORMAT);
        String fileName = System.currentTimeMillis() + suffix;
        return path + "/" + datePrefix + "/" + fileName;
    }
}
