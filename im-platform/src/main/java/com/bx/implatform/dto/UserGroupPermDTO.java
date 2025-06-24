package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "用户拉群权限DTO")
public class UserGroupPermDTO {

//    @NotNull(message = "用户id不能为空")
//    @Schema(description = "id")
//    private Long id;

    @Schema(description = "拉群权限（1所有人  2联系人  3没有人）")
    private Integer groupPermStatus;

    @Schema(description = "总是允许用户id集合")
    private List<Long> groupPermYesUser;

    @Schema(description = "永不允许用户id集合")
    private List<Long> groupPermNoUser;
}
