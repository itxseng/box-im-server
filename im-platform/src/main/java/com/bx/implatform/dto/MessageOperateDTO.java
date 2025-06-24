package com.bx.implatform.dto;

import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class MessageOperateDTO {

    /** 对端用户 ID；清空必传，删除操作可不传 */
    private Long peerId;

    /** 待删除的消息 ID 列表；DELETE 必传，CLEAR 可为空 */
    private List<Long> ids;

    /** 操作类型：DELETE=删除（默认），CLEAR=清空 */
    private ActionType action = ActionType.DELETE;

    /** 是否同步删除 / 清空对方 */
    private boolean deleteBoth;

    public enum ActionType { DELETE, CLEAR }
}
