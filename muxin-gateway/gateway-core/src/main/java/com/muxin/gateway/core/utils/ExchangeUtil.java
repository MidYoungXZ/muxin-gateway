package com.muxin.gateway.core.utils;

import com.muxin.gateway.core.http.HttpServerResponse;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.registry.ServiceInstance;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

import static com.muxin.gateway.core.common.GatewayConstants.GATEWAY_STRIPPED_PATH_ATTR;

/**
 * 请求交换工具类
 */
@Slf4j
public class ExchangeUtil {


    public static void writeAndFlush(ServerWebExchange exchange) {
        if (exchange == null) {
            return;
        }
        if (exchange.getResponse() != null) {
            if (exchange.isKeepAlive()) {
                HttpServerResponse httpServerResponse = exchange.getResponse();
                httpServerResponse.header(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(httpServerResponse.content().readableBytes()));
                httpServerResponse.header(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                exchange.inboundContext()
                        .writeAndFlush(exchange.getOriginalResponse())
                        .addListener((ChannelFuture future) -> {
                            if (!future.isSuccess()) {
                                log.error("Failed to write response", future.cause());
                            }
                            // 在响应写入完成后释放资源
                            try {
                                exchange.release();
                            } catch (Exception e) {
                                log.warn("Failed to release exchange resources", e);
                            }
                        });
            } else {
                exchange.inboundContext()
                        .writeAndFlush(exchange.getResponse()
                                .content())
                        .addListener((ChannelFuture future) -> {
                            if (!future.isSuccess()) {
                                log.error("Failed to write response", future.cause());
                            }
                            // 在响应写入完成后释放资源
                            try {
                                exchange.release();
                            } catch (Exception e) {
                                log.warn("Failed to release exchange resources", e);
                            }
                            // 关闭连接
                            future.channel().close();
                        });
            }
        }
    }


    /**
     * URL重建 - 将原始请求URL重建为指向具体服务实例的URL
     *
     * @param serviceInstance 服务实例
     * @param original        原始URI
     * @return 重建后的URI
     */
    public static URI doReconstructURI(ServiceInstance serviceInstance, URI original) {
        if (serviceInstance == null || original == null) {
            throw new IllegalArgumentException("ServiceInstance and original URI cannot be null");
        }

        try {
            String scheme = serviceInstance.isSecure() ? "https" : "http";
            String host = serviceInstance.getHost();
            int port = serviceInstance.getPort();
            String path = original.getPath();
            String query = original.getQuery();
            String fragment = original.getFragment();

            // 构建新的URI
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(scheme).append("://").append(host);

            // 只有当端口不是默认端口时才添加端口
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                uriBuilder.append(":").append(port);
            }

            if (path != null) {
                uriBuilder.append(path);
            }

            if (query != null && !query.isEmpty()) {
                uriBuilder.append("?").append(query);
            }

            if (fragment != null && !fragment.isEmpty()) {
                uriBuilder.append("#").append(fragment);
            }

            URI reconstructedURI = new URI(uriBuilder.toString());
            log.debug("Reconstructed URI: {} -> {}", original, reconstructedURI);

            return reconstructedURI;

        } catch (URISyntaxException e) {
            log.error("Failed to reconstruct URI for service instance: {}, original: {}",
                    serviceInstance, original, e);
            throw new RuntimeException("URL reconstruction failed", e);
        }
    }

    /**
     * URL重建 - 使用ServerWebExchange中的处理过的路径进行重建
     *
     * @param serviceInstance 服务实例
     * @param exchange        ServerWebExchange，包含可能被StripPrefix处理过的路径
     * @return 重建后的URI
     */
    public static URI doReconstructURI(ServiceInstance serviceInstance, ServerWebExchange exchange) {
        if (serviceInstance == null || exchange == null) {
            throw new IllegalArgumentException("ServiceInstance and ServerWebExchange cannot be null");
        }

        try {
            String scheme = serviceInstance.isSecure() ? "https" : "http";
            String host = serviceInstance.getHost();
            int port = serviceInstance.getPort();

            // 优先使用被StripPrefix处理过的路径
            String path = exchange.getAttribute(GATEWAY_STRIPPED_PATH_ATTR);
            if (path == null || path.isEmpty()) {
                // 如果没有被StripPrefix处理，使用原始路径
                URI originalUri = URI.create(exchange.getRequest().uri());
                path = originalUri.getPath();
            }

            URI originalUri = URI.create(exchange.getRequest().uri());
            String query = originalUri.getQuery();
            String fragment = originalUri.getFragment();

            // 构建新的URI
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(scheme).append("://").append(host);

            // 只有当端口不是默认端口时才添加端口
            if ((scheme.equals("http") && port != 80) || (scheme.equals("https") && port != 443)) {
                uriBuilder.append(":").append(port);
            }

            if (path != null) {
                uriBuilder.append(path);
            }

            if (query != null && !query.isEmpty()) {
                uriBuilder.append("?").append(query);
            }

            if (fragment != null && !fragment.isEmpty()) {
                uriBuilder.append("#").append(fragment);
            }

            URI reconstructedURI = new URI(uriBuilder.toString());
            log.debug("Reconstructed URI with exchange: {} -> {} (stripped path: {})",
                    exchange.getRequest().uri(), reconstructedURI, path);

            return reconstructedURI;

        } catch (URISyntaxException e) {
            log.error("Failed to reconstruct URI for service instance: {}, exchange: {}",
                    serviceInstance, exchange.getRequest().uri(), e);
            throw new RuntimeException("URL reconstruction failed", e);
        }
    }

    /**
     * 提取服务ID从lb://协议的URI中
     *
     * @param uri lb://service-name格式的URI
     * @return 服务名称
     */
    public static String extractServiceId(URI uri) {
        if (uri == null) {
            return null;
        }

        String scheme = uri.getScheme();
        if (!"lb".equalsIgnoreCase(scheme)) {
            return null;
        }

        return uri.getHost();
    }

    /**
     * 判断是否为负载均衡URI
     *
     * @param uri URI
     * @return 是否为lb://格式
     */
    public static boolean isLoadBalanceUri(URI uri) {
        return uri != null && "lb".equalsIgnoreCase(uri.getScheme());
    }
}
