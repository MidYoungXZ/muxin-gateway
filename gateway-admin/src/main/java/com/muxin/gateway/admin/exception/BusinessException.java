package com.muxin.gateway.admin.exception;

import lombok.Getter;

/**
 * 业务异常类.
 * <p>
 * 该类用于封装业务逻辑中的异常情况，支持自定义错误码和错误消息。
 * 继承自RuntimeException，为非受检异常。
 * </p>
 * <p>
 * 提供多个静态工厂方法，方便创建常见类型的业务异常。
 * </p>
 *
 * @author muxin
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码.
     * <p>
     * 用于标识具体的业务错误类型，如401表示未授权，403表示无权限等。
     * </p>
     */
    private final Integer code;

    /**
     * 错误消息.
     * <p>
     * 详细的错误描述信息，用于向调用者说明具体的错误原因。
     * </p>
     */
    private final String message;

    /**
     * 构造函数.
     * <p>
     * 创建包含指定错误码和错误消息的业务异常。
     * </p>
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造函数（默认错误码500）.
     * <p>
     * 创建服务器内部错误（500）的业务异常。
     * </p>
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 构造函数（带原始异常）.
     * <p>
     * 创建包含原始异常链的业务异常，便于追踪错误来源。
     * </p>
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    /**
     * 快速创建未认证异常.
     * <p>
     * 创建状态码为401的未授权业务异常。
     * </p>
     *
     * @return 业务异常实例
     */
    public static BusinessException unauthorized() {
        return new BusinessException(401, "未授权，请先登录");
    }

    /**
     * 快速创建无权限异常.
     * <p>
     * 创建状态码为403的禁止访问业务异常。
     * </p>
     *
     * @return 业务异常实例
     */
    public static BusinessException forbidden() {
        return new BusinessException(403, "无权限访问");
    }

    /**
     * 快速创建资源不存在异常.
     * <p>
     * 创建状态码为404的资源不存在业务异常。
     * </p>
     *
     * @return 业务异常实例
     */
    public static BusinessException notFound() {
        return new BusinessException(404, "资源不存在");
    }
} 