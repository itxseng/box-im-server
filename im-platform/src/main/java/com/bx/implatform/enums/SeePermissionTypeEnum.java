package com.bx.implatform.enums;

import lombok.Getter;

/**
 * 查看权限枚举
 */
@Getter
public enum SeePermissionTypeEnum {

    /**
     * 不让谁看
     */
    NO_SEED_USER(1, "不让谁看"),
    /**
     * 不看谁
     */
    NO_USER(2, "不看谁"),
    ;

    private final Integer code;
    private final String name;

    SeePermissionTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
