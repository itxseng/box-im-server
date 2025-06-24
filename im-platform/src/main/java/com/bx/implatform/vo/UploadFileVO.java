package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wx
 */
@Data
@Schema(description = "文件上传VO")
public class UploadFileVO {

    @Schema(description = "原url")
    private String originUrl;

}
