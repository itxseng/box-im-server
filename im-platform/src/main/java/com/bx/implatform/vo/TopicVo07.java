package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true) // 链式调用
public class TopicVo07 {

    /**
     * 回复id
     */
    @NotNull(message = "回复id不能为空")
    @Schema(description = "回复id")
    private Long replyId;

    /**
     * 回复类型
     */
    @NotNull(message = "回复类型不能为空")
    @Schema(description = "回复类型：1帖子  2用户")
    private Integer replyType;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容长度不能大于2000")
    @Schema(description = "内容")
    private String content;

}
