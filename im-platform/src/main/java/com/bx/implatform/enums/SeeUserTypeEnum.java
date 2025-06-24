package com.bx.implatform.enums;

import lombok.Getter;

/**
 * 查看用户权限枚举
 */
@Getter
public enum SeeUserTypeEnum {

    /**
     * 朋友
     */
    FRIEND(1, "朋友"),
    /**
     * 陌生人
     */
    NO_FRIEND(2, "陌生人"),
    ;

    private final Integer code;
    private final String name;

    SeeUserTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
