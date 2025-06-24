package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 帖子不允许查看视图对象 chat_topic_black
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "帖子不允许查看视图对象")
public class ChatTopicBlackVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @Schema(description = "主键ID")
    private Long id;

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


}
