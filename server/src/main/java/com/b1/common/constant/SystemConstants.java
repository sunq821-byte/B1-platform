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
}
