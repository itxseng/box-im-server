package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 好友添加申请
 * @author Blue
 * @version 1.0
 */
@Data
@Schema(description = "好友添加申请")
public class FriendRequestApplyDTO {

    @Schema(description = "来源  1:名称搜索  2:手机号搜索 3:邮箱搜索")
    private Integer  source;

    @NotNull(message = "好友id不可为空")
    @Schema(description = "好友用户id")
    private Long  friendId;

    @Schema(description = "申请备注")
    private String remark;

}
