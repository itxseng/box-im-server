package com.bx.implatform.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "群信息VO")
public class GroupVO {

    @Schema(description = "群id")
    private Long id;

    @Length(max = 20, message = "群名称长度不能大于20")
    @NotEmpty(message = "群名称不可为空")
    @Schema(description = "群名称")
    private String name;

    @Schema(description = "群主id")
    private Long ownerId;

    @Schema(description = "头像")
    private String headImage;

    @Schema(description = "头像缩略图")
    private String headImageThumb;

    @Length(max = 1024, message = "群聊显示长度不能大于1024")
    @Schema(description = "群公告")
    private String notice;

    @Length(max = 20, message = "显示昵称长度不能大于20")
    @Schema(description = "用户在群显示昵称")
    private String remarkNickName;

    @Schema(description = "群内显示名称")
    private String showNickName;

    @Schema(description = "群名显示名称")
    private String showGroupName;

    @Length(max = 20, message = "群备注长度不能大于20")
    @Schema(description = "群名备注")
    private String remarkGroupName;

    @Schema(description = "是否开启全体禁言")
    private Boolean isMuted;

    @Schema(description = "是否已解散")
    private Boolean dissolve;

    @Schema(description = "是否已退出")
    private Boolean quit;

    @Schema(description = "账号是否被封禁")
    private Boolean isBanned;

    @Schema(description = "被封禁原因")
    private String reason;

    @Schema(description = "置顶消息")
    private GroupMessageVO topMessage;

    @Schema(description = "置顶消息")
    private List<GroupMessageVO> topMessages;

    /**
     * 允许普通成员发起临时会话（false否  true是）
     */
    @Schema(description = "允许普通成员发起临时会话 false否  true是")
    private Boolean interimPerm;

    /**
     * 允许查看群成员（false否  true是）
     */
    @Schema(description = "允许查看群成员 false否  true是")
    private Boolean queryMemberPerm;

    /**
     * 加群权限（1不限制加入  2群成员可以拉人  3只能管理员拉人  4群成员拉人需要管理员验证）
     */
    @Schema(description = "加群权限（1不限制加入  2群成员可以拉人  3只能管理员拉人  4群成员拉人需要管理员验证）")
    private Integer addGroupPerm;

    /**
     * 查找方式（1允许查找  2不允许查找）
     */
    @Schema(description = "查找方式（1允许查找  2不允许查找）")
    private Integer queryGroupPerm;

    /**
     * 是否关闭群语音/视频通话（false否  true是）
     */
    @Schema(description = "是否关闭群语音/视频通话 false否  true是")
    private Boolean roomGroupPerm;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm",timezone = "GMT+8")
    private Date updateTime;

    /**
     * 群消息通知过期时间戳
     */
    @Schema(description = "群消息过期时间戳")
    private Long notifyExpireTs;

    /**
     * 群成员人数
     */
    @Schema(description = "群成员人数")
    private Integer membersCount;

    /**
     *在线人数
     */
    @Schema(description = "在线人数")
    private Integer onlineCount;
}
