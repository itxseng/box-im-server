package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户头像DTO")
public class UserHeadImageDTO {

//    @NotNull(message = "用户id不能为空")
//    @Schema(description = "id")
//    private Long id;

    @Schema(description = "头像")
    private String headImage;

    @Schema(description = "头像缩略图")
    private String headImageThumb;
}
