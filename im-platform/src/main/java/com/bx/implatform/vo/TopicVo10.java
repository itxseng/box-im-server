package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "帖子权限")
public class TopicVo10 {

    @NotNull(message = "帖子ID")
    @Schema(description = "帖子ID")
    private Long topicId;

    @Schema(description = "1开放，2私密，3部分可见，4不给谁看")
    private Integer openType;

    @Schema(description = "可见或不给谁看的用户id")
    private List<String> userIdList;

}
