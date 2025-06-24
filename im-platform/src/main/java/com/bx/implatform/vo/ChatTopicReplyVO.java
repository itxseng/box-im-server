package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 帖子回复视图对象 chat_topic_reply
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "帖子回复视图对象")
public class ChatTopicReplyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long replyId;

    /**
     * 回复类型：1帖子  2用户
     */
    @Schema(description = "回复类型：1帖子  2用户")
    private Integer replyType;

    /**
     * 回复状态（Y是  N否）
     */
    @Schema(description = "回复状态")
    private String replyStatus;

    /**
     * 回复内容
     */
    @Schema(description = "回复内容")
    private String content;

    /**
     * 帖子id
     */
    @Schema(description = "帖子id")
    private Long topicId;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 目标id
     */
    @Schema(description = "目标id")
    private Long targetId;


}
