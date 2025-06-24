package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class MessageDeleteNoticeVO {

    @Schema(description = " 发送者id")
    private Long sendId;

    @Schema(description = " 接收者id")
    private Long recvId;

    @Schema(description = "消息id")
    private List<Long> msgIds;

    @Schema(description = "是否清空聊天记录")
    private Boolean isClear;

    @Schema(description = "消息类型")
    private Integer type;

}
