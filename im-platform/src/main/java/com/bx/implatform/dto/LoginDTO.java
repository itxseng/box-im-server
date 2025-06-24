package com.bx.implatform.dto;

import com.bx.implatform.enums.RegisterMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "用户登录DTO")
public class LoginDTO {

    @Max(value = 2, message = "登录终端类型取值范围:0,2")
    @Min(value = 0, message = "登录终端类型取值范围:0,2")
    @NotNull(message = "登录终端类型不可为空")
    @Schema(description = "登录终端 0:web 1:app 2:pc")
    private Integer terminal;

    @Schema(description = "登录方式,  password:密码登录,phone:手机登录,email: 邮箱登录")
    @NotNull(message = "登录终端类型不可为空")
    private String mode = RegisterMode.PHONE.getCode() ;

    @Schema(description = "验证码")
    private String  code;

    @Schema(description = "区号")
    private String regionCode;

    @NotEmpty(message = "登陆名不可为空")
    @Schema(description = "登陆名: 手机号/邮箱")
    private String userName;

    @Schema(description = "用户密码")
    private String password;

    @NotEmpty(message = "登录设备ID不可为空")
    @Schema(description = "设备唯一标识")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

}
