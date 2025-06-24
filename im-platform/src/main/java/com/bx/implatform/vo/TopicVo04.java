package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true) // 链式调用
@Schema(description = "主题视图对象")
public class TopicVo04 {

    /**
     * 主键
     */
    private Long topicId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 内容类型
     */
    @Schema(description = "内容类型 1文字/表情  2图片/拍照  3视频")
    private Integer topicType;
    /**
     * 内容
     */
    @Schema(description = "使用json存储，格式{\"text\":\"文字内容\",\"imgList\":[\"http://111\",\"http://222\"],\"videoList\":[\"http:111\",\"http://222\"]}")
    private String content;

    @Schema(description = "文件地址")
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
     * 时间
     */
    @Schema(description = "时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date createTime;
    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickName;
    /**
     * 名称
     */
    @Schema(description = "名称")
    private String displayName;
    /**
     * 头像
     */
    @Schema(description = "头像")
    private String portrait;

    @Schema(description = "是否可以删除Y,N")
    private String canDeleted;

    @Schema(description = "查看类型（1开放，2私密，3部分可见，4不给谁看）")
    private Integer openType;
}
