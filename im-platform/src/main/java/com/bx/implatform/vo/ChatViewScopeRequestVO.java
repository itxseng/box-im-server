package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户权限视图对象")
public class ChatViewScopeRequestVO {

    /**
     * 1 朋友 2 陌生人
     */
    @NotNull(message = "类型不能为空")
    @Schema(description = "帖子允许查看视图对象")
    private Integer isFriend;

}
