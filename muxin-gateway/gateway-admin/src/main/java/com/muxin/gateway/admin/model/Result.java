package com.muxin.gateway.admin.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 构造方法私有化，只能通过静态方法创建
     */
    private Result() {
    }

    /**
     * 构造成功结果
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    /**
     * 构造成功结果，无数据
     *
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 构造成功结果，自定义消息
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 结果
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }

    /**
     * 构造失败结果
     *
     * @param code    状态码
     * @param message 消息
     * @param <T>     数据类型
     * @return 结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    /**
     * 构造失败结果，默认状态码500
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return 结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 构造未授权结果
     *
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> unauthorized() {
        return error(401, "未授权，请先登录");
    }

    /**
     * 构造禁止访问结果
     *
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> forbidden() {
        return error(403, "无权限访问");
    }

    /**
     * 构造资源不存在结果
     *
     * @param <T> 数据类型
     * @return 结果
     */
    public static <T> Result<T> notFound() {
        return error(404, "资源不存在");
    }
} 