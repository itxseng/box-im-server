package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 帖子允许查看视图对象 chat_topic_white
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "帖子允许查看视图对象")
public class ChatTopicWhiteVO implements Serializable {

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
    @Schema(description = "帖子id")
    private Long topicId;

    /**
     * 用户id
     */
    @Schema(description = "用户id")
    private Long userId;


}
