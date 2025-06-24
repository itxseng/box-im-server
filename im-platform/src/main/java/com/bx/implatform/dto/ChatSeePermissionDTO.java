package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 屏蔽统一权限业务对象 chat_see_permission
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "屏蔽统一权限业务对象")
public class ChatSeePermissionDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 权限类型：1 不让谁看 2 不看谁
     */
    @NotNull(message = "权限类型：1 不让谁看 2 不看谁不能为空")
    @Schema(description = "权限类型：1 不让谁看 2 不看谁")
    private Long permissionType;

    /**
     * 目标用户ID
     */
    @NotNull(message = "目标用户ID不能为空")
    @Schema(description = "目标用户ID")
    private Long targetId;


}
