package com.muxin.gateway.core.plus.message.converter.http;

import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.message.converter.ProtocolConversionException;
import com.muxin.gateway.core.plus.message.converter.ProtocolConverter;
import com.muxin.gateway.core.plus.message.http.HttpMessage;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP协议版本转换器
 * 支持HTTP/1.0、HTTP/1.1、HTTP/2.0之间的转换
 *
 * @author muxin
 * @since 1.0.0
 */
@Slf4j
public class HttpVersionConverter implements ProtocolConverter {

    @Override
    public boolean supports(Protocol fromProtocol, Protocol toProtocol) {
        if (fromProtocol == null || toProtocol == null) {
            return false;
        }

        // 只支持HTTP协议之间的转换
        if (!"HTTP".equalsIgnoreCase(fromProtocol.type()) || !"HTTP".equalsIgnoreCase(toProtocol.type())) {
            return false;
        }

        // 支持的HTTP版本
        String fromVersion = normalizeVersion(fromProtocol.getVersion());
        String toVersion = normalizeVersion(toProtocol.getVersion());

        return isSupportedVersion(fromVersion) && isSupportedVersion(toVersion);
    }

    @Override
    public Message convertRequest(Message request, Protocol fromProtocol, Protocol toProtocol) {
        if (!(request instanceof HttpMessage)) {
            throw new ProtocolConversionException(
                    "请求消息不是HTTP消息类型: " + request.getClass().getSimpleName(),
                    fromProtocol.type() + "/" + fromProtocol.getVersion(),
                    toProtocol.type() + "/" + toProtocol.getVersion()
            );
        }

        try {
            HttpMessage httpMessage = (HttpMessage) request;
            String fromVersion = normalizeVersion(fromProtocol.getVersion());
            String toVersion = normalizeVersion(toProtocol.getVersion());

            log.debug("[HttpVersionConverter] 转换HTTP请求版本: {} -> {}", fromVersion, toVersion);

            // 更新HTTP版本
            if (httpMessage instanceof com.muxin.gateway.core.plus.message.http.HttpRequestMessage) {
                com.muxin.gateway.core.plus.message.http.HttpRequestMessage httpRequest =
                        (com.muxin.gateway.core.plus.message.http.HttpRequestMessage) httpMessage;

                HttpVersion newVersion = parseHttpVersion(toVersion);
                httpRequest.setProtocolVersion(newVersion);

                // HTTP/2.0到HTTP/1.1的转换可能需要调整headers
                if ("2.0".equals(fromVersion) && "1.1".equals(toVersion)) {
                    addHttp2HeadersForHttp11(httpRequest);
                }

                log.debug("[HttpVersionConverter] HTTP请求版本转换完成: {} -> {}", fromVersion, toVersion);
            }

            return request;

        } catch (Exception e) {
            log.error("[HttpVersionConverter] HTTP请求版本转换失败", e);
            throw new ProtocolConversionException(
                    "HTTP请求版本转换失败: " + e.getMessage(),
                    fromProtocol.type() + "/" + fromProtocol.getVersion(),
                    toProtocol.type() + "/" + toProtocol.getVersion(),
                    e
            );
        }
    }

    @Override
    public Message convertResponse(Message response, Protocol fromProtocol, Protocol toProtocol) {
        if (!(response instanceof HttpMessage)) {
            throw new ProtocolConversionException(
                    "响应消息不是HTTP消息类型: " + response.getClass().getSimpleName(),
                    fromProtocol.type() + "/" + fromProtocol.getVersion(),
                    toProtocol.type() + "/" + toProtocol.getVersion()
            );
        }

        try {
            HttpMessage httpMessage = (HttpMessage) response;
            String fromVersion = normalizeVersion(fromProtocol.getVersion());
            String toVersion = normalizeVersion(toProtocol.getVersion());

            log.debug("[HttpVersionConverter] 转换HTTP响应版本: {} -> {}", fromVersion, toVersion);

            // 更新HTTP版本
            if (httpMessage instanceof com.muxin.gateway.core.plus.message.http.HttpResponseMessage) {
                com.muxin.gateway.core.plus.message.http.HttpResponseMessage httpResponse =
                        (com.muxin.gateway.core.plus.message.http.HttpResponseMessage) httpMessage;

                HttpVersion newVersion = parseHttpVersion(toVersion);
                httpResponse.setProtocolVersion(newVersion);

                // HTTP/2.0到HTTP/1.1的转换
                if ("2.0".equals(fromVersion) && "1.1".equals(toVersion)) {
                    addHttp2HeadersForHttp11(httpResponse);
                }

                log.debug("[HttpVersionConverter] HTTP响应版本转换完成: {} -> {}", fromVersion, toVersion);
            }

            return response;

        } catch (Exception e) {
            log.error("[HttpVersionConverter] HTTP响应版本转换失败", e);
            throw new ProtocolConversionException(
                    "HTTP响应版本转换失败: " + e.getMessage(),
                    fromProtocol.type() + "/" + fromProtocol.getVersion(),
                    toProtocol.type() + "/" + toProtocol.getVersion(),
                    e
            );
        }
    }

    @Override
    public String getName() {
        return "HttpVersionConverter";
    }

    @Override
    public String getDescription() {
        return "HTTP协议版本转换器，支持HTTP/1.0、HTTP/1.1、HTTP/2.0之间的转换";
    }

    // ========== 私有方法 ==========

    /**
     * 规范化版本号
     */
    private String normalizeVersion(String version) {
        if (version == null) {
            return "1.1";
        }
        // 统一格式：1.0, 1.1, 2.0
        return version.replaceAll("HTTP/", "").trim();
    }

    /**
     * 检查是否为支持的版本
     */
    private boolean isSupportedVersion(String version) {
        return "1.0".equals(version) || "1.1".equals(version) || "2.0".equals(version);
    }

    /**
     * 解析HTTP版本
     */
    private HttpVersion parseHttpVersion(String version) {
        switch (version) {
            case "1.0":
                return HttpVersion.HTTP_1_0;
            case "1.1":
                return HttpVersion.HTTP_1_1;
            default:
                throw new IllegalArgumentException("不支持的HTTP版本: " + version);
        }
    }

    /**
     * HTTP/2.0到HTTP/1.1转换时添加headers
     */
    private void addHttp2HeadersForHttp11(com.muxin.gateway.core.plus.message.http.HttpMessage httpMessage) {
        // 如果响应中没有Connection header，添加keep-alive
        if (httpMessage.headers() != null && !httpMessage.headers().contains("Connection")) {
            httpMessage.headers().set("Connection", "keep-alive");
        }

        log.debug("[HttpVersionConverter] 添加HTTP/1.1兼容headers");
    }
}
