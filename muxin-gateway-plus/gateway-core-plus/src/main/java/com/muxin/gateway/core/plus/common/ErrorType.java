package com.muxin.gateway.core.plus.common;

/**
 * @projectname: muxin-gateway
 * @filename: ErrorType
 * @author: yangxz
 * @data:2025/7/16 22:35
 * @description:
 */
public enum ErrorType {
    TIMEOUT("E001", "请求超时，请稍后重试", 504),
    CONNECTION_ERROR("E002", "连接失败，请检查网络", 502),
    ROUTE_NOT_FOUND("E003", "找不到指定的服务路径", 404),
    SERVICE_UNAVAILABLE("E004", "服务暂时不可用，请稍后重试", 503),
    AUTHENTICATION_ERROR("E005", "认证失败，请检查权限", 401),
    PROTOCOL_ERROR("E006", "协议转换失败", 400),
    LOAD_BALANCE_ERROR("E007", "负载均衡失败", 502),
    INTERNAL_ERROR("E500", "系统内部错误", 500),
    UNKNOWN("E999", "未知错误", 500);

    private final String code;
    private final String userMessage;
    private final int httpStatus;

    ErrorType(String code, String userMessage, int httpStatus) {
        this.code = code;
        this.userMessage = userMessage;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}