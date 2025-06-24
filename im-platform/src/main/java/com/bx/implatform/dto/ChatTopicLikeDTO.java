package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子点赞业务对象 chat_topic_like
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "帖子点赞业务对象")
public class ChatTopicLikeDTO {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空")
    @Schema(description = "主键")
    private Long id;

    /**
     * 帖子id
     */
    @NotNull(message = "帖子id不能为空")
    @Schema(description = "帖子id")
    private Long topicId;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户id")
    private Long userId;

    /**
     * 是否点赞
     */
    @NotBlank(message = "是否点赞不能为空")
    @Schema(description = "是否点赞")
    private String hasLike;


}
