package com.b1.common.result;

import lombok.Data;
import org.slf4j.MDC;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private boolean success;
    private long timestamp;
    private String traceId;

    protected Result() {
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "success";
        r.data = data;
        r.success = true;
        r.timestamp = System.currentTimeMillis();
        r.traceId = MDC.get("traceId");
        return r;
    }

    public static <T> Result<T> err(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = null;
        r.success = false;
        r.timestamp = System.currentTimeMillis();
        r.traceId = MDC.get("traceId");
        return r;
    }
}
