package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "好友信息VO")
public class FriendVO {

    @NotNull(message = "好友id不可为空")
    @Schema(description = "好友id")
    private Long id;

    @NotNull(message = "好友昵称不可为空")
    @Schema(description = "好友昵称")
    private String nickName;

    @Schema(description = "群内显示名称")
    private String showNickName;

    @Schema(description = "群内昵称备注")
    private String remarkNickName;

    @Schema(description = "好友头像")
    private String headImage;

    @Schema(description = "好友头像缩略图")
    private String headImageThumb;

    @Schema(description = "最后登录时间")
    private Long lastLoginTime;

    @Schema(description = "是否已删除")
    private Boolean deleted;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "网页端是否在线")
    private Boolean onlineWeb;

    @Schema(description = "APP端是否在线")
    private Boolean onlineApp;

    @Schema(description = "好友在线状态谁可看（0:所有人 1:联系人 2:隐藏）")
    private Integer onlinePermStatus;

    @Schema(description = "拉群权限（1所有人  2联系人  3没有人）")
    private Integer groupPermStatus;

    @Schema(description = "总是允许用户id集合")
    private List<Long> groupPermYesUser;

    @Schema(description = "永不允许用户id集合")
    private List<Long> groupPermNoUser;

    @Schema(description = "区号")
    private String regionCode;

    @Schema(description = "是否加入黑名单")
    private Boolean blacklist;

    @Schema(description = "是否标记*")
    private Boolean tag;

    @Schema(description = "消息不通知到期时间")
    private Long notifyExpireTs;
}
