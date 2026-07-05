package com.b1.common.constant;

public final class RedisKeys {

    private RedisKeys() {
    }

    public static final String USER_INFO = "user:info:%d";
    public static final String USER_PERM = "user:perm:%d";
    public static final String TOKEN_ACCESS = "token:access:%s";
    public static final String TOKEN_REFRESH = "token:refresh:%d";
    public static final String TOKEN_BLACKLIST = "token:blacklist:%s";
    public static final String STANDARD = "standard:%d";
    public static final String COURSE = "course:%d";
    public static final String STATS_DASHBOARD = "stats:dashboard:%s";
    public static final String STATS_PROGRESS = "stats:progress:%d";
    public static final String REPORT = "report:%s:%s";
    public static final String AI_STATUS = "ai:status:%d";
    public static final String LOCK = "lock:%s";
    public static final String RATE_LIMIT = "ratelimit:%d:%s";

    public static String format(String pattern, Object... args) {
        return String.format(pattern, args);
    }
}
