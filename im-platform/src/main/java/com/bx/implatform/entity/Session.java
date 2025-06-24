package com.bx.implatform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("im_session")
public class Session {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer sessionType;

    private Long targetId;

    private String lastMessage;

    private LocalDateTime lastTime;

    private Integer unreadCount;

    private Integer topFlag;

    private Integer blockedFlag;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
