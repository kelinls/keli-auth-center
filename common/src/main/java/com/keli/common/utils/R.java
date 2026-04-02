package com.keli.common.utils;


import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回结果类
 * @param <T> 数据类型
 */
@Data
//@ApiModel(description = "统一响应结果")
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    //@ApiModelProperty(value = "状态码（200=成功，其他=失败）", example = "200")
    private Integer code;
   // @ApiModelProperty(value = "响应消息", example = "操作成功")
    private String message;   // 消息
    //@ApiModelProperty(value = "响应数据（成功时返回）")
    private T data;       // 数据

    // 私有构造方法
    private R() {}

    // 成功返回结果
    public static <T> R<T> success(String message) {
        R<T> r = new R<>();
        r.code = 200;
        r.data = null;
        r.message = message;
        return r;
    }

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.data = data;
        r.message = "操作成功";
        return r;
    }

    // 成功返回结果（自定义消息）
    public static <T> R<T> success(String msg, T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.data = data;
        r.message = msg;
        return r;
    }

    // 失败返回结果
    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.code = 0;
        r.message = msg;
        return r;
    }

    // 指定状态码的错误返回结果
    public static <T> R<T> error(Integer code, String msg) {
        R<T> r = new R<>();
        r.code = code;
        r.message = msg;
        return r;
    }

    // 设置消息
    public R<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    // 设置状态码
    public R<T> setCode(Integer code) {
        this.code = code;
        return this;
    }
}