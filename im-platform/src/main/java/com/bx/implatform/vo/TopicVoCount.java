package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true) // 链式调用
@Schema(description = "帖子详情")
public class TopicVoCount {

    @Schema(description = "帖子数量")
    private Long count;

    @Schema(description = "头像")
    private String portrait;


}
