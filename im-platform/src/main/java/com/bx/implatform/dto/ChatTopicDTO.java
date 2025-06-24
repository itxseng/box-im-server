package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 主题业务对象 chat_topic
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "主题业务对象")
public class ChatTopicDTO {

    /**
     * 主键
     */
    @Schema(description = "主键", required = true)
    private Long id;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户id", required = true)
    private Long userId;

    /**
     * 类型（1文字/表情  2图片/拍照  3视频）
     */
    @NotNull(message = "类型不能为空")
    @Schema(description = "类型（1文字/表情  2图片/拍照  3视频）", required = true)
    private Integer topicType;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    @Schema(description = "内容", required = true)
    private String content;

    /**
     * 经纬度
     */
    @Schema(description = "经纬度")
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

    /**
     * 查看类型（1开放，2私密，3部分可见，4不给谁看）
     */
    @NotNull(message = "查看类型不能为空")
    @Schema(description = "查看类型（1开放，2私密，3部分可见，4不给谁看）", required = true)
    private Integer openType;


}
