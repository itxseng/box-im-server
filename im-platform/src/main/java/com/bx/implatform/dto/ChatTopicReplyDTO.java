package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 帖子回复业务对象 chat_topic_reply
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "帖子回复业务对象")
public class ChatTopicReplyDTO {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空")
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long replyId;

    /**
     * 回复类型：1帖子  2用户
     */
    @NotNull(message = "回复类型：1帖子  2用户不能为空")
    @Schema(description = "回复类型：1帖子  2用户", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer replyType;

    /**
     * 回复状态（Y是  N否）
     */
    @NotBlank(message = "回复状态（Y是  N否）不能为空")
    @Schema(description = "回复状态（Y是  N否）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String replyStatus;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /**
     * 帖子id
     */
    @NotNull(message = "帖子id不能为空")
    @Schema(description = "帖子id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topicId;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    /**
     * 目标id
     */
    @NotNull(message = "目标id不能为空")
    @Schema(description = "目标id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;


}
