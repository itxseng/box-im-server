package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
@Schema(description = "聊天视图范围")
public class ChatViewScopeVO {


    /**
     * 0-全部，1-私密，2-最近三天，3-最近7天
     */
    @NotNull(message = "请选择范围")
    @Schema(description = "范围: 0-全部，1-私密，2-最近三天，3-最近7天")
    private Integer viewType;

    /**
     * 1 朋友 2 陌生人
     */
    @NotNull(message = "类型不能为空")
    @Schema(description = "1 朋友 2 陌生人")
    private Integer isFriend;

}
