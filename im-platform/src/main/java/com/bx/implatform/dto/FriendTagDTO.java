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
@Schema(description = "修改好友标记")
public class FriendTagDTO {

    @NotNull(message = "好友id不可为空")
    @Schema(description = "好友用户id")
    private Long friendId;

    @Schema(description = "是否标记*")
    private Boolean tag;

}
