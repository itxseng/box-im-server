package com.bx.implatform.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "用户设备VO")
public class UserDeviceVo implements Serializable {
    @Schema(description = "当前设备信息")
    private UserDeviceListVo nowDevice;
    @Schema(description = "其他设备列表")
    private List<UserDeviceListVo> otherDeviceList;
}