package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true) // 链式调用
@Schema(description = "帖子详情")
public class TopicVo09 {

    /**
     * 帖子id
     */
    @Schema(description = "帖子id")
    private Long topicId;
    /**
     * 帖子类型
     */
    @Schema(description = "帖子类型")
    private Integer topicType;
    /**
     * 帖子内容
     */
    @Schema(description = "帖子内容")
    private String topicContent;
    /**
     * 通知类型 1点赞 2回复
     */
    @Schema(description = "通知类型 1点赞 2回复")
    private Integer noticeType;
    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;
    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickName;
    /**
     * 头像
     */
    @Schema(description = "头像")
    private String portrait;
    /**
     * 回复内容
     */
    @Schema(description = "回复内容")
    private String replyContent;
    /**
     * 回复时间
     */
    @Schema(description = "回复时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date replyTime;

}
