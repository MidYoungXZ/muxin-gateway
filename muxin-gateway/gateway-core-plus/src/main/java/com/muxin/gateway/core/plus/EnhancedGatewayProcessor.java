package com.muxin.gateway.core.plus;

import com.muxin.gateway.core.plus.config.GatewayConfig;
import com.muxin.gateway.core.plus.connect.ClientConnection;
import com.muxin.gateway.core.plus.connect.ConnectionPoolManager;

import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.MessageType;
import com.muxin.gateway.core.plus.message.ProtocolConverterManager;
import com.muxin.gateway.core.plus.message.http.HttpBody;
import com.muxin.gateway.core.plus.message.http.HttpHeaders;
import com.muxin.gateway.core.plus.message.http.HttpMessage;
import com.muxin.gateway.core.plus.message.http.HttpMetadata;
import com.muxin.gateway.core.plus.route.node.NodeManager;
import com.muxin.gateway.core.plus.route.RouteManager;
import com.muxin.gateway.core.plus.route.RequestContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 增强的网关处理器实现
 * 基于新的统一架构，提供完整的HTTP请求转发能力
 *
 * @author muxin
 */
@Slf4j
public class EnhancedGatewayProcessor extends GatewayProcessor {

    public EnhancedGatewayProcessor(GatewayConfig config,
                                    ConnectionPoolManager connectionPoolManager,
                                    RouteManager routeManager,
                                    NodeManager nodeManager,
                                    ProtocolConverterManager protocolConverterManager) {
        super(config, connectionPoolManager, routeManager, nodeManager, protocolConverterManager);

        log.info("[EnhancedGatewayProcessor] 增强网关处理器创建完成");
    }

    @Override
    protected CompletableFuture<Message> invokeBackendService(RequestContext context) {
        long startTime = System.currentTimeMillis();
        ClientConnection connection = context.getOutboundConnection();
        Message request = context.getInboundMessage();
        String requestId = request.getMessageId();

        log.debug("[EnhancedGatewayProcessor] 开始调用后端服务: {}", requestId);

        return connection.send(request)
                .orTimeout(config.getCoreConfig().getDefaultTimeout().toMillis(), TimeUnit.MILLISECONDS)
                .handle((response, throwable) -> {
                    long duration = System.currentTimeMillis() - startTime;

                    try {
                        if (throwable != null) {
                            log.error("[EnhancedGatewayProcessor] 后端服务调用失败: {} - 耗时: {}ms",
                                    requestId, duration, throwable);
                            // 处理各种异常情况
                            if (throwable instanceof TimeoutException) {
                                return createTimeoutResponse(request);
                            } else {
                                return createErrorResponse(request, throwable);
                            }
                        }

                        if (response == null) {
                            log.warn("[EnhancedGatewayProcessor] 后端服务返回空响应: {} - 耗时: {}ms",
                                    requestId, duration);
                            return createEmptyResponse(request);
                        }

                        log.info("[EnhancedGatewayProcessor] 后端服务调用成功: {} - 耗时: {}ms",
                                requestId, duration);
                        return response;

                    } finally {
                        // 归还连接到池中
                        if (connection != null) {
                            try {
                                connection.returnToPool();
                            } catch (Exception e) {
                                log.warn("[EnhancedGatewayProcessor] 归还连接失败: {}", e.getMessage());
                            }
                        }
                    }
                });
    }


    // ========== 私有辅助方法 ==========

    /**
     * 创建超时响应
     */
    private Message createTimeoutResponse(Message request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Error-Type", "TIMEOUT");

        String errorBody = """
                {
                    "error": "TIMEOUT",
                    "message": "后端服务调用超时",
                    "timestamp": %d,
                    "requestId": "%s"
                }
                """.formatted(System.currentTimeMillis(), request.getMessageId());

        HttpBody body = new HttpBody(errorBody.getBytes());
        HttpMetadata metadata = new HttpMetadata();
        metadata.setStatusCode(504);

        return new HttpMessage(
                generateResponseId(request),
                MessageType.RESPONSE,
                request.getProtocol(),
                headers,
                body,
                metadata
        );
    }

    /**
     * 创建错误响应
     */
    private Message createErrorResponse(Message request, Throwable throwable) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Error-Type", "BACKEND_ERROR");

        String errorBody = """
                {
                    "error": "BACKEND_ERROR",
                    "message": "%s",
                    "timestamp": %d,
                    "requestId": "%s"
                }
                """.formatted(
                throwable.getMessage() != null ? throwable.getMessage() : "未知错误",
                System.currentTimeMillis(),
                request.getMessageId()
        );

        HttpBody body = new HttpBody(errorBody.getBytes());
        HttpMetadata metadata = new HttpMetadata();
        metadata.setStatusCode(502);

        return new HttpMessage(
                generateResponseId(request),
                MessageType.RESPONSE,
                request.getProtocol(),
                headers,
                body,
                metadata
        );
    }

    /**
     * 创建空响应
     */
    private Message createEmptyResponse(Message request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Error-Type", "EMPTY_RESPONSE");

        String errorBody = """
                {
                    "error": "EMPTY_RESPONSE",
                    "message": "后端服务返回空响应",
                    "timestamp": %d,
                    "requestId": "%s"
                }
                """.formatted(System.currentTimeMillis(), request.getMessageId());

        HttpBody body = new HttpBody(errorBody.getBytes());
        HttpMetadata metadata = new HttpMetadata();
        metadata.setStatusCode(502);

        return new HttpMessage(
                generateResponseId(request),
                MessageType.RESPONSE,
                request.getProtocol(),
                headers,
                body,
                metadata
        );
    }

    /**
     * 生成响应ID
     */
    private String generateResponseId(Message request) {
        return "resp-" + request.getMessageId() + "-" + System.nanoTime();
    }

}