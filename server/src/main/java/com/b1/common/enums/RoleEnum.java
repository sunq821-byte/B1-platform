package com.b1.common.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {

    ADMIN("admin", "管理员"),
    TEACHER("teacher", "教师"),
    STUDENT("student", "学生");

    private final String code;
    private final String name;

    RoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RoleEnum fromCode(String code) {
        for (RoleEnum r : values()) {
            if (r.code.equals(code)) {
                return r;
            }
        }
        return null;
    }
}
