package com.b1.common.util;

import cn.hutool.core.util.IdUtil;

public final class SnowflakeUtil {

    private SnowflakeUtil() {
    }

    public static long nextId() {
        return IdUtil.getSnowflake(1, 1).nextId();
    }

    public static String nextIdStr() {
        return String.valueOf(IdUtil.getSnowflake(1, 1).nextId());
    }
}
