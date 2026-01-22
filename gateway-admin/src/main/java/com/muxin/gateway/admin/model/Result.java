package com.muxin.gateway.admin.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一API响应结果封装类.
 * <p>
 * 该类用于封装所有API接口的返回结果，提供统一的响应格式。
 * 包含状态码、消息、成功标志、数据和时间戳等字段。
 * </p>
 * <p>
 * 支持泛型，可以适配任意类型的返回数据。
 * </p>
 *
 * @param <T> 数据类型
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码.
     * <p>
     * 200表示成功，4xx表示客户端错误，5xx表示服务器错误。
     * </p>
     */
    private Integer code;

    /**
     * 返回消息.
     * <p>
     * 用于描述操作结果或错误信息。
     * </p>
     */
    private String message;

    /**
     * 是否成功.
     * <p>
     * true表示操作成功，false表示操作失败。
     * </p>
     */
    private boolean success;

    /**
     * 返回数据.
     * <p>
     * 泛型数据，可以是任意类型的业务数据。
     * </p>
     */
    private T data;

    /**
     * 时间戳.
     * <p>
     * 响应生成的时间戳（毫秒）。
     * </p>
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 构造方法私有化，只能通过静态方法创建实例.
     */
    private Result() {
    }

    /**
     * 构造成功结果.
     * <p>
     * 创建包含数据的成功响应，状态码为200。
     * </p>
     *
     * @param <T>  数据类型
     * @param data 返回数据
     * @return 成功结果对象
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
     * 构造成功结果（无数据）.
     * <p>
     * 创建不包含数据的成功响应，适用于删除、更新等操作。
     * </p>
     *
     * @param <T> 数据类型
     * @return 成功结果对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 构造成功结果（自定义消息）.
     * <p>
     * 创建包含数据和自定义消息的成功响应。
     * </p>
     *
     * @param <T>     数据类型
     * @param message 自定义消息
     * @param data    返回数据
     * @return 成功结果对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = success(data);
        result.setMessage(message);
        return result;
    }

    /**
     * 构造失败结果.
     * <p>
     * 创建包含错误码和错误消息的失败响应。
     * </p>
     *
     * @param <T>     数据类型
     * @param code    状态码
     * @param message 错误消息
     * @return 失败结果对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    /**
     * 构造失败结果（默认状态码500）.
     * <p>
     * 创建服务器内部错误响应。
     * </p>
     *
     * @param <T>     数据类型
     * @param message 错误消息
     * @return 失败结果对象
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 构造未授权结果.
     * <p>
     * 创建状态码401的未授权响应。
     * </p>
     *
     * @param <T> 数据类型
     * @return 失败结果对象
     */
    public static <T> Result<T> unauthorized() {
        return error(401, "未授权，请先登录");
    }

    /**
     * 构造禁止访问结果.
     * <p>
     * 创建状态码403的禁止访问响应。
     * </p>
     *
     * @param <T> 数据类型
     * @return 失败结果对象
     */
    public static <T> Result<T> forbidden() {
        return error(403, "无权限访问");
    }

    /**
     * 构造资源不存在结果.
     * <p>
     * 创建状态码404的资源不存在响应。
     * </p>
     *
     * @param <T> 数据类型
     * @return 失败结果对象
     */
    public static <T> Result<T> notFound() {
        return error(404, "资源不存在");
    }
} 