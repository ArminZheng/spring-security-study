package com.armin.security.domain;

import java.io.Serializable;

/**
 * @author zy
 * @version 2021/12/15
 */
public class Result<T> implements Serializable {
    public static final String OPERATE_SUCCESS = "操作成功！";
    private static final long serialVersionUID = 1L;
    /** 时间戳 */
    private final long timestamp = System.currentTimeMillis();
    /** 成功标志 */
    private Boolean success = true;
    /** 返回处理消息 */
    private String message = OPERATE_SUCCESS;
    /** 返回代码 */
    private Integer code = 0;
    /** 返回数据对象 data */
    private T data;

    public Result() {}

    public static <T> Result<T> ok(T data) {
        return ok("成功", data);
    }

    public static <T> Result<T> ok() {
        return ok("成功");
    }

    public static <T> Result<T> ok(String msg) {
        return ok(msg, null);
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> r = new Result<>();
        r.setMessage(msg);
        r.setCode(200);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(500, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        return error(code, msg, null);
    }

    public static <T> Result<T> error(String msg, T data) {
        return error(500, msg, data);
    }

    public static <T> Result<T> error(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setSuccess(false);
        r.setMessage(msg);
        r.setCode(code);
        r.setData(data);
        return r;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String toString() {
        return "Result(success="
                + this.getSuccess()
                + ", message="
                + this.getMessage()
                + ", code="
                + this.getCode()
                + ", data="
                + this.getData()
                + ", timestamp="
                + this.getTimestamp()
                + ")";
    }
}
