package com.bx.implatform.vo;

import com.bx.imcommon.serializer.DateToLongSerializer;
import com.bx.implatform.entity.UserDevice;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "用户设备列表VO")
public class UserDeviceListVo implements Serializable {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "设备id")
    private String deviceId;
    @Schema(description = "设备名称")
    private String deviceName;
    @Schema(description = "平台")
    private String platform;
    @Schema(description = "最后登录时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date lastLoginTime;
    @Schema(description = "最后登录ip")
    private String lastIp;
    @Schema(description = "在线状态 0:禁用  1:正常 ")
    private Integer status;
    @Schema(description = "创建时间")
    @JsonSerialize(using = DateToLongSerializer.class)
    private Date createTime;
    @Schema(description = "是否在线")
    private Boolean online;


    public UserDeviceListVo() {
    }

    public UserDeviceListVo(UserDevice userDevice) {
        this.id = userDevice.getId();
        this.userId = userDevice.getUserId();
        this.deviceId = userDevice.getDeviceId();
        this.deviceName = userDevice.getDeviceName();
        this.platform = userDevice.getPlatform();
        this.lastLoginTime = userDevice.getLastLoginTime();
        this.lastIp = userDevice.getLastIp();
        this.status = userDevice.getStatus();
        this.createTime = userDevice.getCreateTime();
    }
}