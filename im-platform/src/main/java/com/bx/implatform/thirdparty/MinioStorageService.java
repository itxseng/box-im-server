package com.bx.implatform.thirdparty;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import com.bx.implatform.config.props.StorageProperties;
import com.bx.implatform.util.DateTimeUtils;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component("minio")
@RequiredArgsConstructor
public class MinioStorageService implements CloudStorageService{

    private final MinioClient minioClient;

    /**
     * 查看存储bucket是否存在
     *
     * @return boolean
     */
    @Override
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("查询bucket失败", e);
            return false;
        }
    }

    /**
     * 创建存储bucket
     */
    @Override
    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("创建bucket失败,", e);
        }
    }

    /**
     * 设置bucket权限为public
     */
    @Override
    public void setBucketPublic(String bucketName) {
        try {
            // 设置公开
            String sb = "{\"Version\":\"2012-10-17\"," +
                    "\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":" +
                    "{\"AWS\":[\"*\"]},\"Action\":[\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"," +
                    "\"s3:GetBucketLocation\"],\"Resource\":[\"arn:aws:s3:::" + bucketName +
                    "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::" +
                    bucketName +
                    "/*\"]}]}";
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(sb)
                            .build());
        } catch (Exception e) {
            log.error("创建bucket失败,", e);
        }
    }

    @Override
    public Dict getToken(String fileName, StorageProperties properties) {
        try {
            // 1. 创建 POST 策略
            PostPolicy policy = new PostPolicy(properties.getBucketName(), ZonedDateTime.now().plusMinutes(10));
            fileName = DateUtil.format(DateUtil.date(), "yyyy/MM/dd")+"/"+ fileName;
            policy.addEqualsCondition("key", fileName);  // 必须设置key条件

            // 2. 获取带签名的表单数据
            Map<String, String> formData = minioClient.getPresignedPostFormData(policy);
            Dict set = Dict.create()
                    .set("uploadType", "MINIO")
                    .set("key", fileName)
                    .set("url", properties.getDomain());
            for (String string : formData.keySet()) {
                set.set(string.replaceAll("-","_"), formData.get(string));
            }
            // 3. 返回给前端
            return set;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取临时凭证失败");
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName bucket名称
     * @param path       路径
     * @param file       文件
     * @return Boolean
     */
    @Override
    public String upload(String bucketName, String path, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new RuntimeException();
        }
        String fileName = System.currentTimeMillis() + "";
        if (originalFilename.lastIndexOf(".") >= 0) {
            fileName += originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = DateTimeUtils.getFormatDate(new Date(), DateTimeUtils.PARTDATEFORMAT) + "/" + fileName;
        try {
            InputStream stream = new ByteArrayInputStream(file.getBytes());
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(path + "/" + objectName)
                .stream(stream, file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("上传图片失败,", e);
            return null;
        }
        return objectName;
    }

    /**
     * 文件上传
     *
     * @param bucketName  bucket名称
     * @param path        路径
     * @param name        文件名
     * @param fileByte    文件内容
     * @param contentType contentType
     * @return objectName
     */
    @Override
    public String upload(String bucketName, String path, String name, byte[] fileByte, String contentType) {

        String fileName = System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        String objectName = DateTimeUtils.getFormatDate(new Date(), DateTimeUtils.PARTDATEFORMAT) + "/" + fileName;
        try {
            InputStream stream = new ByteArrayInputStream(fileByte);
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(path + "/" + objectName)
                    .stream(stream, fileByte.length, -1).contentType(contentType).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("上传文件失败,", e);
            return null;
        }
        return objectName;
    }


    /**
     * 删除
     *
     * @param bucketName bucket名称
     * @param path       路径
     * @param fileName   文件名
     * @return true/false
     */
    @Override
    public boolean remove(String bucketName, String path, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(path + "/"  + fileName).build());
        } catch (Exception e) {
            log.error("删除文件失败,", e);
            return false;
        }
        return true;
    }

    /**
     * 判断文件是否存在
     *
     * @param bucketName bucket名称
     * @param path       路径
     * @param fileName   文件名
     * @return
     */
    @Override
    public boolean isExist(String bucketName, String path, String fileName) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(path + "/"  + fileName).build());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
