package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发布话题")
public class TopicVo01 {

    @NotNull(message = "内容类型不能为空")
    @Schema(description = "内容类型 1文字/表情  2图片/拍照  3视频")
    private Integer topicType;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容长度不能大于2000")
    @Schema(description = "内容")
    private String content;

    @Size(max = 2000, message = "文件地址不能大于2000")
    @Schema(description = "件地址")
    private String location;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private String latitude;
    /**
     * 经度
     */
    @Schema(description = "经度")
    private String longitude;

    /**
     * 地址
     */
    @Schema(description = "地址")
    private String address;

    @Schema(description = "查看类型：1开放，2私密，3部分可见，4不给谁看")
    private Integer openType;

    @Schema(description ="可见或不给谁看的用户id")
    private List<Long> userIdList;

}
