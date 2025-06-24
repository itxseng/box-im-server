package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Blue
 * @version 1.0
 * @date 2025-02-22
 */
@Data
@Schema(description = "修改好友备注")
public class FriendRemarkDTO {

    @NotNull(message = "好友id不可为空")
    @Schema(description = "好友用户id")
    private Long friendId;

    @Schema(description = "备注昵称")
    private String remarkNickName;

}
