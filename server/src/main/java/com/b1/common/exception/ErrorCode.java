package com.b1.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // Success
    SUCCESS(0, "success"),

    // Business (1000-1999)
    NOT_FOUND(1001, "资源不存在"),
    ALREADY_EXISTS(1002, "资源已存在"),
    OPERATION_NOT_ALLOWED(1003, "操作不允许"),
    DATA_MODIFIED(1004, "数据已被修改，请刷新后重试"),
    SUBMIT_LIMIT_EXCEEDED(1005, "提交次数已达上限"),
    DEADLINE_PASSED(1006, "任务已截止"),
    ACCOUNT_NOT_FOUND(1007, "账号不存在"),
    SCORE_ALREADY_PUBLISHED(1008, "成绩已发布不可修改"),

    // Auth (2000-2999)
    NOT_LOGGED_IN(2001, "请先登录"),
    TOKEN_EXPIRED(2002, "Token 已过期"),
    TOKEN_INVALID(2003, "登录凭证无效"),
    WRONG_PASSWORD(2004, "用户名或密码错误"),
    ACCOUNT_LOCKED(2005, "账号已被锁定，请15分钟后再试"),
    ACCOUNT_DISABLED(2006, "账号已被禁用"),
    CAPTCHA_ERROR(2007, "验证码错误"),
    REFRESH_TOKEN_EXPIRED(2008, "登录已过期，请重新登录"),

    // Permission (3000-3999)
    NO_PERMISSION(3001, "您没有权限访问此功能"),
    ROLE_NOT_FOUND(3002, "角色不存在"),
    RESOURCE_ACCESS_DENIED(3003, "您没有权限访问该资源"),
    IP_RESTRICTED(3004, "IP受限"),

    // Validation (4000-4999)
    PARAM_ERROR(4001, "参数校验失败"),
    PARAM_FORMAT_ERROR(4002, "参数格式错误"),
    PARAM_VALUE_ILLEGAL(4003, "参数值非法"),
    PARAM_TOO_LONG(4004, "字段长度超限"),
    PARAM_REQUIRED(4005, "必填字段为空"),
    DATA_STALE(4006, "数据已过期"),

    // System (5000-5999)
    INTERNAL_ERROR(5001, "服务器繁忙，请稍后重试"),
    DB_ERROR(5002, "数据服务异常"),
    REDIS_ERROR(5003, "缓存服务异常"),
    EXTERNAL_SERVICE_DOWN(5004, "第三方服务不可用"),
    REQUEST_TIMEOUT(5005, "请求超时"),
    RATE_LIMIT_HIT(5006, "操作过于频繁，请稍后重试"),
    SERVICE_DEGRADED(5007, "服务降级中"),

    // AI (6000-6999)
    AI_SERVICE_UNAVAILABLE(6001, "AI 分析服务暂时不可用"),
    AI_MODEL_CALL_FAILED(6002, "AI 模型调用失败"),
    AI_ANALYSIS_TIMEOUT(6003, "AI 分析超时"),
    AI_RESULT_FORMAT_ERROR(6004, "AI 返回格式异常"),
    AI_TOKEN_INSUFFICIENT(6005, "Token 额度不足"),
    AI_PROMPT_ERROR(6006, "Prompt 模板异常"),
    AI_MODEL_NOT_FOUND(6007, "模型不存在"),
    AI_ALREADY_ANALYZING(6008, "AI 正在分析中，请等待分析完成"),
    AI_ANALYSIS_CANCELLED(6009, "AI 分析已取消"),

    // File (7000-7999)
    FILE_TOO_LARGE(7001, "文件大小超过限制"),
    FILE_TYPE_UNSUPPORTED(7002, "不支持的文件类型"),
    FILE_UPLOAD_FAILED(7003, "文件上传失败"),
    FILE_NOT_FOUND(7004, "文件不存在或已被删除"),
    FILE_CORRUPTED(7005, "文件已损坏"),
    FILE_COUNT_EXCEEDED(7006, "文件数量超限"),

    // Git (8000-8999)
    GIT_REPO_NOT_FOUND(8001, "仓库地址无效，请检查是否为有效的公开仓库地址"),
    GIT_NO_PERMISSION(8002, "无法访问该仓库，请检查仓库权限"),
    GIT_CLONE_FAILED(8003, "仓库克隆失败"),
    GIT_BRANCH_NOT_FOUND(8004, "指定的分支不存在"),

    // Export (9000-9999)
    EXPORT_FAILED(9001, "报表导出失败"),
    EXPORT_DATA_TOO_LARGE(9002, "导出数据量过大"),
    EXPORT_TIMEOUT(9003, "报表导出超时"),
    EXPORT_FORMAT_UNSUPPORTED(9004, "导出格式不支持");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
