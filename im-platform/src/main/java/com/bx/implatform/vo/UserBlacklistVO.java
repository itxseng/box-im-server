package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 用户黑名单VO
 *
 * @author: Blue
 * @date: 2024-09-22
 * @version: 1.0
 */
@Data
@Schema(description = "用户黑名单VO")
public class UserBlacklistVO {

    /**
     * 被拉黑用户id
     */
    @Schema(description = "被拉黑用户id")
    private Long toUserId;
    /**
     * 被拉黑用户编号
     */
    @Schema(description = "被拉黑用户编号")
    private String userName;
    /**
     * 被拉黑用户昵称
     */
    @Schema(description = "被拉黑用户昵称")
    private String nickName;
    /**
     * 被拉黑用户头像
     */
    @Schema(description = "被拉黑用户头像")
    private String headImageThumb;

    /**
     * 拉黑时间
     */
    @Schema(description = "拉黑时间")
    private Date createTime;
}
