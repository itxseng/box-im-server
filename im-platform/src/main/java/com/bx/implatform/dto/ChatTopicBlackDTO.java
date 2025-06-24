package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子不允许查看业务对象 chat_topic_black
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "帖子不允许查看业务对象")
public class ChatTopicBlackDTO {

    /**
     *
     */
    @NotNull(message = "不能为空")
    @Schema(description = "主键ID", required = true)
    private Long id;

    /**
     * 帖子id
     */
    @NotNull(message = "帖子id不能为空")
    @Schema(description = "帖子id", required = true)
    private Long topicId;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户id", required = true)
    private Long userId;


}
