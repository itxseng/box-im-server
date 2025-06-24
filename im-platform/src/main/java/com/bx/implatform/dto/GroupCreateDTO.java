package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@Schema(description = "创建群信息")
public class GroupCreateDTO {

    @Length(max = 20, message = "群名称长度不能大于20")
    @Schema(description = "群名称")
    private String name;

    @Schema(description = "群成员id")
    private List<Long> memberIds;

    @Schema(description = "头像")
    private String headImage;

    @Schema(description = "头像缩略图")
    private String headImageThumb;

}
