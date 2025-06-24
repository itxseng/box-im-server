package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "发布话题")
public class TopicVo02 {

    @NotBlank(message = "封面不能为空")
    @Size(max = 2000, message = "封面长度不能大于2000")
    @Schema(description = "封面")
    private String cover;

}
