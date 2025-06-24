package com.bx.implatform.vo;

import com.bx.implatform.dto.FindMyWayDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;

@Data
@Schema(description = "用户信息VO")
public class UserVO {

    @NotNull(message = "用户id不能为空")
    @Schema(description = "id")
    private Long id;

    //    @NotEmpty(message = "用户名不能为空")
    @Length(max = 20, message = "用户名不能大于20字符")
    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "邮箱")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    //    @NotEmpty(message = "用户昵称不能为空")
    @Length(max = 20, message = "昵称不能大于20字符")
    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "用户类型 1:普通用户 2:审核账户")
    private Integer type;

    @Length(max = 128, message = "个性签名不能大于128个字符")
    @Schema(description = "个性签名")
    private String signature;

    @Schema(description = "头像")
    private String headImage;

    @Schema(description = "头像缩略图")
    private String headImageThumb;

    @Schema(description = "账号是否被封禁")
    private Boolean isBanned;

    @Schema(description = "被封禁原因")
    private String reason;

    @Schema(description = "是否开启好友验证审核")
    private Boolean isManualApprove;

    @Schema(description = "是否在黑名单中")
    private Boolean isInBlacklist;

    @Schema(description = "状态 0:正常 1:已注销")
    private Integer status;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "最后登录时间")
    private Long lastLoginTime;

    @Schema(description = "拉群权限（1所有人  2联系人  3没有人）")
    private Integer groupPermStatus;

    @Schema(description = "总是允许用户id集合")
    private List<Long> groupPermYesUser;

    @Schema(description = "永不允许用户id集合")
    private List<Long> groupPermNoUser;

    @Schema(description = "用户在线状态设置")
    private Integer onlinePermStatus;

    @Schema(description = "区号")
    private String regionCode;

    @Schema(description = "是否修改过用户名")
    private Integer isModifiedUsername;

    @Schema(description = "搜索到我的方式")
    private FindMyWayDto findMyWay;

    @Schema(description = "是否是朋友")
    private Boolean isFriend;

    @Schema(description = "关闭消息通知过期时间")
    private Long  notifyExpireTs;

    @Schema(description = "生日")
    private Date birthday;

    @Schema(description = "国家")
    private String country;
}
