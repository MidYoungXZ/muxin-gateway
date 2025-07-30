package com.muxin.gateway.admin.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author muxin
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造函数
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
     * 构造函数，默认错误码500
     *
     * @param message 错误消息
     */
    public BusinessException(String message) {
        this(500, message);
    }

    /**
     * 构造函数，默认错误码500
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
     * 快速创建未认证异常
     *
     * @return 业务异常
     */
    public static BusinessException unauthorized() {
        return new BusinessException(401, "未授权，请先登录");
    }

    /**
     * 快速创建无权限异常
     *
     * @return 业务异常
     */
    public static BusinessException forbidden() {
        return new BusinessException(403, "无权限访问");
    }

    /**
     * 快速创建资源不存在异常
     *
     * @return 业务异常
     */
    public static BusinessException notFound() {
        return new BusinessException(404, "资源不存在");
    }
} 