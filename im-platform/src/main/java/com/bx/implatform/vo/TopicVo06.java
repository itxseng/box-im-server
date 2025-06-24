package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true) // 链式调用
public class TopicVo06 {

    /**
     * 回复id
     */
    @Schema(description = "回复id")
    private Long replyId;
    /**
     * 回复类型：1帖子  2用户
     */
    @Schema(description = "回复类型：1帖子  2用户")
    private Integer replyType;
    /**
     * 评论内容
     */
    @Schema(description = "评论内容")
    private String content;
    /**
     * 回复时间
     */
    @Schema(description = "回复时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date createTime;
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
     * 展示名称
     */
    @Schema(description = "展示名称")
    private String displayName;
    /**
     * 头像
     */
    @Schema(description = "头像")
    private String portrait;
    /**
     * 是否可以删除
     */
    @Schema(description = "是否可以删除Y,N")
    private String canDeleted;
    /**
     * 用户id
     */
    @Schema(description = "推送用户id")
    private Long toUserId;
    /**
     * 昵称
     */
    @Schema(description = "推送昵称")
    private String toNickName;
    /**
     * 名称
     */
    @Schema(description = "推送名称")
    private String toDisplayName;
    /**
     * 头像
     */
    @Schema(description = "推送头像")
    private String toPortrait;

}
