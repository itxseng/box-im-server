package com.bx.implatform.enums;

import lombok.Getter;

/**
 * 帖子类型枚举
 */
@Getter
public enum TopicTypeEnum {

    /**
     * 文字/表情
     */
    TEXT(1, "文字/表情"),
    /**
     * 图片/拍照
     */
    IMAGE(2, "图片/拍照"),
    /**
     * 视频
     */
    VIDEO(3, "视频"),
    ;


    private final Integer code;
    private final String name;

    TopicTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
