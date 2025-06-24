package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 帖子点赞视图对象 chat_topic_like
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "帖子点赞视图对象")
public class ChatTopicLikeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private Long id;

    /**
     * 帖子id
     */
    @Schema(description = "帖子不允许查看视图对象")
    private Long topicId;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 是否点赞
     */
    @Schema(description = "是否点赞")
    private String hasLike;


}
