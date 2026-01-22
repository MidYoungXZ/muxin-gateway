package com.muxin.gateway.admin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举类.
 * <p>
 * 定义系统中使用的所有错误码和错误消息，按功能模块分类：
 * <ul>
 *     <li>通用错误码 (1000-1999)</li>
 *     <li>认证相关 (2000-2999)</li>
 *     <li>业务相关 (3000-3999)</li>
 *     <li>系统相关 (4000-4999)</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 通用错误码 (1000-1999)
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 认证相关 (2000-2999)
    INVALID_CREDENTIALS(2001, "用户名或密码错误"),
    ACCOUNT_LOCKED(2002, "账号已锁定"),
    TOKEN_EXPIRED(2003, "Token已过期"),
    TOKEN_INVALID(2004, "无效的Token"),

    // 业务相关 (3000-3999)
    ROUTE_EXISTS(3001, "路由已存在"),
    ROUTE_IN_USE(3002, "路由正在使用中"),
    INVALID_CONFIG(3003, "配置格式错误"),
    PREDICATE_IN_USE(3004, "断言正在使用中"),
    FILTER_IN_USE(3005, "过滤器正在使用中"),

    // 系统相关 (4000-4999)
    DB_ERROR(4001, "数据库操作失败"),
    CACHE_ERROR(4002, "缓存操作失败"),
    RPC_ERROR(4003, "远程调用失败");

    /**
     * 错误码.
     * <p>
     * 用于标识具体的错误类型。
     * </p>
     */
    private final int code;

    /**
     * 错误消息.
     * <p>
     * 错误的详细描述信息，用于向用户展示。
     * </p>
     */
    private final String message;
} 