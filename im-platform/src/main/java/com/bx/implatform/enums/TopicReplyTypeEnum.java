package com.bx.implatform.enums;

import lombok.Getter;

/**
 * 帖子回复类型枚举
 */
@Getter
public enum TopicReplyTypeEnum {

    /**
     * 帖子
     */
    TOPIC(1, "帖子"),
    /**
     * 用户
     */
    USER(2, "用户"),
    ;

    private final Integer code;
    private final String name;

    TopicReplyTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
