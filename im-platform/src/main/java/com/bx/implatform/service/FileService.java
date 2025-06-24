package com.bx.implatform.service;

import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bx.implatform.entity.FileInfo;
import com.bx.implatform.vo.UploadFileVO;
import com.bx.implatform.vo.UploadImageVO;
import com.bx.implatform.vo.UploadVideoVO;
import org.springframework.web.multipart.MultipartFile;


public interface FileService extends IService<FileInfo> {

    UploadFileVO uploadFile(MultipartFile file);

    UploadImageVO uploadImage(MultipartFile file,Boolean isPermanent);

    UploadVideoVO uploadVideo(MultipartFile file);

    Dict getToken(String fileName);
}
