package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "好友共同群信息VO")
public class FriendGroupVO {

    @Schema(description = "群id")
    private Long id;

    @Length(max = 20, message = "群名称长度不能大于20")
    @NotEmpty(message = "群名称不可为空")
    @Schema(description = "群名称")
    private String name;

    @Schema(description = "头像缩略图")
    private String headImageThumb;

    @Length(max = 20, message = "群备注长度不能大于20")
    @Schema(description = "群名备注")
    private String remarkGroupName;
}
