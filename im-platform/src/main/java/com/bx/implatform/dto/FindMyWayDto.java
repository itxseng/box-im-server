package com.bx.implatform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FindMyWayDto {

    /**
     * 只能是零和一
     */
    @Max(value = 1,message = "参数错误")
    @Min(value = 0,message = "参数错误")
    private Integer username;

    @Max(value = 1,message = "参数错误")
    @Min(value = 0,message = "参数错误")
    private Integer nickname;

    @Max(value = 1,message = "参数错误")
    @Min(value = 0,message = "参数错误")
    private Integer phone;

    @Max(value = 1,message = "参数错误")
    @Min(value = 0,message = "参数错误")
    private Integer email;

    public FindMyWayDto() {
        this.username = 1;
        this.nickname = 1;
        this.phone = 1;
        this.email = 1;
    }
}
