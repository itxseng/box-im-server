package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

@Data
@Schema(description = "朋友圈消息VO")
public class ChatMessageVO {

    @Schema(description = " 帖子id")
    private Long topicId;

    @Schema(description = "发起方用户ID")
    private Long sendId;

    @Schema(description = "发起方昵称")
    private String sendNickName;

    @Schema(description = "发起方头像")
    private String sendHeadImage;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "消息内容类型 MessageType")
    private Integer type;

    @Schema(description = " 发送时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date sendTime;
}
