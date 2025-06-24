package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

/**
 * @author wx
 */
@Data
@Schema(description = "用户修改信息DTO")
public class UserDTO {

    @NotNull(message = "用户id不能为空")
    @Schema(description = "用户ID")
    private Long id;

    @Size(max = 20, message = "用户名不能大于20个字符")
    @Schema(description = "用户名")
    private String userName;

    @Size(max = 20, message = "昵称不能大于20个字符")
    @Schema(description = "用户昵称")
    private String nickName;

    @Schema(description = "性别（0：男，1：女）")
    private Integer sex;

    @Size(max = 128, message = "个性签名不能大于128个字符")
    @Schema(description = "个性签名")
    private String signature;

    @Schema(description = "头像地址")
    private String headImage;

    @Schema(description = "头像缩略图地址")
    private String headImageThumb;

    @Schema(description = "是否开启好友验证审核")
    private Boolean isManualApprove;

    @Schema(description = "用户在线状态设置（0：所有人可见，1：仅联系人，2：隐藏）")
    private Integer onlinePermStatus;

    @Schema(description = "生日（格式：yyyy-MM-dd）")
    private Date birthday;

    @Size(max = 64, message = "国家名称不能超过64个字符")
    @Schema(description = "国家")
    private String country;
}
