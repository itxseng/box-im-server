package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "朋友圈权限设置")
public class ChatPermissionVO {

    @Schema(description = "好友查看权限：0-全部，1-私密，2-最近三天，3-最近7天")
    private Integer isFriend;

    @Schema(description = "陌生人查看权限：0-全部，1-私密，2-最近三天，3-最近7天")
    private Integer stranger;

    @Schema(description = "不让谁看")
    private List<Long> notSee;

    @Schema(description = "不看谁")
    private List<Long> notSeeMe;

}
