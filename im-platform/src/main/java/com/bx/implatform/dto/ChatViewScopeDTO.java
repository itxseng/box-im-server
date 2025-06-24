package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 朋友圈权限范围业务对象 chat_view_scope
 *
 * @author Blue
 * @date 2025-05-29
 */
@Data
@Schema(description = "朋友圈权限范围业务对象")
public class ChatViewScopeDTO {

    /**
     * 用户表ID
     */
    @NotNull(message = "用户表ID不能为空")
    @Schema(description = "用户表ID")
    private Long userId;

    /**
     * 0-全部，1-私密，2-最近三天，3-最近7天
     */
    @NotNull(message = "0-全部，1-私密，2-最近三天，3-最近7天不能为空")
    @Schema(description = "0-全部，1-私密，2-最近三天，3-最近7天")
    private Long viewType;

    /**
     * 1 朋友 2 陌生人
     */
    @NotNull(message = "1 朋友 2 陌生人不能为空")
    @Schema(description = "1 朋友 2 陌生人")
    private Long isFriend;


}
