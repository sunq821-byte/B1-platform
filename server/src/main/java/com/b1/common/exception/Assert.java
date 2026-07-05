package com.b1.common.exception;

import cn.hutool.core.util.StrUtil;

public final class Assert {

    private Assert() {
    }

    public static void notNull(Object obj, ErrorCode errorCode) {
        if (obj == null) {
            throw new BusinessException(errorCode);
        }
    }

    public static void notBlank(String str, ErrorCode errorCode) {
        if (StrUtil.isBlank(str)) {
            throw new BusinessException(errorCode);
        }
    }

    public static void isTrue(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode);
        }
    }

    public static void isTrue(boolean condition, ErrorCode errorCode, String detail) {
        if (!condition) {
            throw new BusinessException(errorCode, detail);
        }
    }
}
