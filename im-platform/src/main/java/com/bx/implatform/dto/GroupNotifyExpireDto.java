package com.bx.implatform.dto;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class GroupNotifyExpireDto {
    /**
     * 群id
     */
    private Long groupId;

    /**
     * 群消息过期时间戳
     */
    private Long notifyExpireTs;
}
