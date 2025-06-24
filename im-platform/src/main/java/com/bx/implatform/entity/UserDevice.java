package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("im_user_device")
public class UserDevice implements Serializable {
    private Long id;
    private Long userId;
    private String deviceId;
    private String deviceName;
    private String platform;
    private Date lastLoginTime;
    private String lastIp;
    private Integer status;
    private Date createTime;
}