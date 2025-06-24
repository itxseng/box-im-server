package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Data
@Schema(description = "朋友圈权限设置")
public class ChatSeePermissionVO {


    @Schema(description = "权限类型：1 不让谁看 2 不看谁")
    private Integer permissionType;

    @Schema(description = "目标用户ID")
    private List<Long> targetIds;

}
