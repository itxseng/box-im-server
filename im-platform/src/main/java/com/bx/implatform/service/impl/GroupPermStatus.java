package com.bx.implatform.service.impl;

/**
 * 群聊权限
 */
public enum GroupPermStatus {

    ALL(1, "所有人"),
    FRIEND(2, "联系人"),
    NO(3, "没有人");
    private Integer code;
    private String name;

    GroupPermStatus(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
