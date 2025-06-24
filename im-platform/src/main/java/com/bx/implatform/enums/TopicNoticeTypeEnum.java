package com.bx.implatform.enums;

import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
public enum TopicNoticeTypeEnum {

    /**
     * 点赞
     */
    LIKE(1, "点赞"),
    /**
     * 回复
     */
    REPLY(2, "回复"),
    ;

    private final Integer code;
    private final String name;

    TopicNoticeTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
