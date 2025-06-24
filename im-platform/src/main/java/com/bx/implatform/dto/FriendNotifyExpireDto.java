package com.bx.implatform.dto;

import lombok.Data;

@Data
public class FriendNotifyExpireDto {

    /**
     * 朋友id
     */
    private Long friendId;

    /**
     * 通知过期时间戳
     */
    private Long notifyExpireTs;
}
