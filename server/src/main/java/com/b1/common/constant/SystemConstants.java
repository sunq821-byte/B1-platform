package com.b1.common.constant;

public final class SystemConstants {

    private SystemConstants() {
    }

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static final int LOGIN_FAIL_MAX = 5;
    public static final int LOGIN_LOCK_MINUTES = 15;

    public static final int TOKEN_ACCESS_TTL = 7200;
    public static final int TOKEN_REFRESH_TTL = 604800;

    public static final int MAX_UPLOAD_SIZE_MB = 50;

    /** 系统默认评价标准 ID（固定四维度基线，见 V5 迁移）。所有新建任务固定引用此标准。 */
    public static final long DEFAULT_STANDARD_ID = 1000L;
}
