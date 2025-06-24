package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 主题视图对象 chat_topic
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "主题视图对象")
public class ChatTopicVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 类型（1文字/表情  2图片/拍照  3视频）
     */
    @Schema(description = "类型")
    private Integer topicType;

    /**
     * 经纬度
     */
    @Schema(description = "经纬度")
    private String location;

    /**
     * 时间
     */
    @Schema(description = "时间")
    private Date createTime;

    /**
     * 地址
     */
    @Schema(description = "地址")
    private String address;

    /**
     * 查看类型（1开放，2私密，3部分可见，4不给谁看）
     */
    @Schema(description = "查看类型")
   private Integer openType;


}
