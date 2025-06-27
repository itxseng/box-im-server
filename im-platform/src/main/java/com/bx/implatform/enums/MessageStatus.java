package com.bx.implatform.enums;

import lombok.AllArgsConstructor;

/**
 * @author Administrator
 */

@AllArgsConstructor
public enum MessageStatus {

    /**
     * 文件
     */
    UNSEND(0, "未送达"),
    /**
     * 文件
     */
    SENDED(1, "送达"),
    /**
     * 撤回
     */
    RECALL(2, "撤回"),
    /**
     * 已读
     */
    READED(3, "已读"),

    /**
     * 编辑
     */
    EDITED(4, "已编辑"),
    /**
     * 编辑
     */
    DELETED(5, "已删除"),

    ;

    private final Integer code;

    private final String desc;


    public Integer code() {
        return this.code;
    }
}
