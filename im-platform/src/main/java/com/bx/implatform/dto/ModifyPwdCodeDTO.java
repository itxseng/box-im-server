package com.bx.implatform.dto;

import com.bx.implatform.enums.RegisterMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "验证码修改密码DTO")
public class ModifyPwdCodeDTO {

    @Schema(description = "修改方式,  phone:手机,email: 邮箱")
    @NotNull(message = "登录终端类型不可为空")
    private String mode = RegisterMode.PHONE.getCode() ;

    @NotEmpty(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String code;

    private String phone;

    private String email;

    @NotEmpty(message = "新用户密码不可为空")
    @Schema(description = "新用户密码")
    private String newPassword;


}
