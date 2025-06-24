package com.bx.implatform.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bx.implatform.config.props.StorageProperties;
import com.bx.implatform.contant.Constant;
import com.bx.implatform.entity.FileInfo;
import com.bx.implatform.enums.FileType;
import com.bx.implatform.enums.ResultCode;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.mapper.FileInfoMapper;
import com.bx.implatform.service.FileService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.thirdparty.CloudStorageService;
import com.bx.implatform.thirdparty.StorageRouter;
import com.bx.implatform.util.FileUtil;
import com.bx.implatform.util.ImageUtil;
import com.bx.implatform.vo.UploadFileVO;
import com.bx.implatform.vo.UploadImageVO;
import com.bx.implatform.vo.UploadVideoVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileService {

    private final StorageRouter storageRouter;

    @PostConstruct
    public void init() {
        CloudStorageService storage = storageRouter.getService();
        StorageProperties props = storageRouter.getProperties();
        if (!storage.bucketExists(props.getBucketName())) {
            storage.makeBucket(props.getBucketName());
            storage.setBucketPublic(props.getBucketName());
        }
    }

    @Override
    public UploadFileVO uploadFile(MultipartFile file) {
        try {
            UploadFileVO vo = new UploadFileVO();
            CloudStorageService storage = storageRouter.getService();
            StorageProperties props = storageRouter.getProperties();
            Long userId = SessionContext.getSession().getUserId();

            if (file.getSize() > Constant.MAX_FILE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件大小不能超过20M");
            }

            String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            FileInfo fileInfo = findByMd5(md5);
            if (fileInfo != null) {
                fileInfo.setUploadTime(new Date());
                this.updateById(fileInfo);
                vo.setOriginUrl(fileInfo.getFilePath());
                return vo;
            }

            String fileName = storage.upload(props.getBucketName(), props.getFilePath(), file);
            if (StringUtils.isEmpty(fileName)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "文件上传失败");
            }

            String url = generateUrl(props, FileType.FILE, fileName);
            saveFileInfo(file, md5, url);
            log.info("文件上传成功，用户id:{}, url:{}", userId, url);
            vo.setOriginUrl(url);
            return vo;

        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "上传文件失败");
        }
    }

    @Transactional
    @Override
    public UploadImageVO uploadImage(MultipartFile file, Boolean isPermanent) {
        try {
            CloudStorageService storage = storageRouter.getService();
            StorageProperties props = storageRouter.getProperties();
            Long userId = SessionContext.getSession().getUserId();

            if (file.getSize() > Constant.MAX_IMAGE_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片大小不能超过20M");
            }

            if (!FileUtil.isImage(file.getOriginalFilename())) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片格式不合法");
            }

            UploadImageVO vo = new UploadImageVO();
            String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            FileInfo fileInfo = findByMd5(md5);
            if (fileInfo != null) {
                fileInfo.setIsPermanent(isPermanent || fileInfo.getIsPermanent());
                fileInfo.setUploadTime(new Date());
                this.updateById(fileInfo);
                vo.setOriginUrl(fileInfo.getFilePath());
                vo.setThumbUrl(fileInfo.getCompressedPath());
                return vo;
            }

            String fileName = storage.upload(props.getBucketName(), props.getImagePath(), file);
            if (StringUtils.isEmpty(fileName)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "图片上传失败");
            }
            vo.setOriginUrl(generateUrl(props, FileType.IMAGE, fileName));

            if (file.getSize() > 50 * 1024) {
                byte[] thumb = ImageUtil.compressForScale(file.getBytes(), 30);
                String thumbFileName = storage.upload(props.getBucketName(), props.getImagePath(),
                        file.getOriginalFilename(), thumb, file.getContentType());
                if (StringUtils.isEmpty(thumbFileName)) {
                    throw new GlobalException(ResultCode.PROGRAM_ERROR, "缩略图上传失败");
                }
                vo.setThumbUrl(generateUrl(props, FileType.IMAGE, thumbFileName));
                saveImageFileInfo(file, md5, vo.getOriginUrl(), vo.getThumbUrl(), isPermanent);
            } else {
                vo.setThumbUrl(generateUrl(props, FileType.IMAGE, fileName));
                saveImageFileInfo(file, md5, vo.getOriginUrl(), vo.getThumbUrl(), true);
            }

            log.info("图片上传成功，用户id:{}, url:{}", userId, vo.getOriginUrl());
            return vo;

        } catch (IOException e) {
            log.error("上传图片失败", e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "上传图片失败");
        }
    }

    @Override
    public UploadVideoVO uploadVideo(MultipartFile file) {
        try {
            CloudStorageService storage = storageRouter.getService();
            StorageProperties props = storageRouter.getProperties();
            Long userId = SessionContext.getSession().getUserId();

            if (file.getSize() > Constant.MAX_VIDEO_SIZE) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "视频大小不能超过50M");
            }

            UploadVideoVO vo = new UploadVideoVO();
            String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            FileInfo fileInfo = findByMd5(md5);
            if (fileInfo != null) {
                fileInfo.setUploadTime(new Date());
                this.updateById(fileInfo);
                vo.setVideoUrl(fileInfo.getFilePath());
                vo.setCoverUrl(fileInfo.getCoverPath());
                return vo;
            }

            String fileName = storage.upload(props.getBucketName(), props.getVideoPath(), file);
            if (StringUtils.isEmpty(fileName)) {
                throw new GlobalException(ResultCode.PROGRAM_ERROR, "视频上传失败");
            }

            String url = generateUrl(props, FileType.VIDEO, fileName);
            String coverName = createCoverImage(file, props);
            vo.setVideoUrl(url);
            vo.setCoverUrl(generateUrl(props, FileType.VIDEO, coverName));
            saveVideoFileInfo(file, md5, vo.getVideoUrl(), vo.getCoverUrl());
            log.info("视频上传成功，用户id:{}, url:{}", userId, url);
            return vo;

        } catch (IOException e) {
            log.error("上传视频失败", e);
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "上传视频失败");
        }
    }

    @Override
    public Dict getToken(String fileName) {
        StorageProperties properties = storageRouter.getProperties();
        return storageRouter.getService().getToken(fileName,properties);
    }

    private String createCoverImage(MultipartFile file, StorageProperties props) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file.getInputStream());
            grabber.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage frame = converter.convert(grabber.grabImage());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(frame, "PNG", outputStream);
            byte[] imageByte = ImageUtil.compressForScale(outputStream.toByteArray(), 150);

            String fileName = FileUtil.excludeExtension(file.getOriginalFilename()) + ".png";
            String imageName = storageRouter.getService().upload(props.getBucketName(), props.getVideoPath(),
                    fileName, imageByte, "image/png");

            grabber.stop();
            outputStream.close();
            return imageName;
        } catch (IOException e) {
            throw new GlobalException(ResultCode.PROGRAM_ERROR, "视频封面上传失败");
        }
    }

    private String generateUrl(StorageProperties props, FileType type, String fileName) {
        String routerType = storageRouter.getType();
        return switch (routerType) {
            case "minio" -> generateMinioUrl(props, type, fileName);
            case "s3" -> generateS3Url(props, fileName);
            default -> throw new GlobalException(ResultCode.PROGRAM_ERROR, "未知存储类型");
        };
    }

    private String generateS3Url(StorageProperties props, String fileName) {
        return StrUtil.join("/", props.getDomain(), fileName);
    }

    private String generateMinioUrl(StorageProperties props, FileType type, String fileName) {
        String path = switch (type) {
            case FILE -> props.getFilePath();
            case IMAGE -> props.getImagePath();
            case VIDEO -> props.getVideoPath();
        };
        return StrUtil.join("/", props.getDomain(), props.getBucketName(), path, fileName);
    }

    private FileInfo findByMd5(String md5) {
        LambdaQueryWrapper<FileInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(FileInfo::getMd5, md5);
        return getOne(wrapper);
    }

    private void saveImageFileInfo(MultipartFile file, String md5, String filePath, String compressedPath, Boolean isPermanent) throws IOException {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setFileType(FileType.IMAGE.code());
        fileInfo.setFilePath(filePath);
        fileInfo.setCompressedPath(compressedPath);
        fileInfo.setMd5(md5);
        fileInfo.setIsPermanent(isPermanent);
        fileInfo.setUploadTime(new Date());
        this.save(fileInfo);
    }

    private void saveVideoFileInfo(MultipartFile file, String md5, String filePath, String coverPath) throws IOException {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setFileType(FileType.VIDEO.code());
        fileInfo.setFilePath(filePath);
        fileInfo.setCoverPath(coverPath);
        fileInfo.setMd5(md5);
        fileInfo.setIsPermanent(false);
        fileInfo.setUploadTime(new Date());
        this.save(fileInfo);
    }

    private void saveFileInfo(MultipartFile file, String md5, String filePath) throws IOException {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(file.getOriginalFilename());
        fileInfo.setFileSize(file.getSize());
        fileInfo.setFileType(FileType.FILE.code());
        fileInfo.setFilePath(filePath);
        fileInfo.setMd5(md5);
        fileInfo.setIsPermanent(false);
        fileInfo.setUploadTime(new Date());
        this.save(fileInfo);
    }
}
