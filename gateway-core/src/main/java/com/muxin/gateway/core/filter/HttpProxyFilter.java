package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.common.GatewayConstants;
import com.muxin.gateway.core.common.exception.GatewayException;
import com.muxin.gateway.core.config.NettyHttpClientProperties;
import com.muxin.gateway.core.http.HttpServerOperations;
import com.muxin.gateway.core.http.HttpServerRequest;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.netty.NettyHttpClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 基于AsyncHttpClient实现的HTTP请求代理过滤器。
 * 该过滤器负责将传入的HTTP请求代理到指定的目标服务器，并处理响应。
 *
 * @author Administrator
 * @date 2024/11/22 16:00
 */
@Slf4j
@Data
@AllArgsConstructor
public class HttpProxyFilter implements GlobalFilter {

    private NettyHttpClient nettyHttpClient;

    private final NettyHttpClientProperties properties;

    /**
     * 执行过滤器逻辑，将请求代理到目标服务器并处理响应。
     *
     * @param exchange 当前的 {@link ServerWebExchange} 对象
     */
    @Override
    public void filter(ServerWebExchange exchange) {
        // 将 ServerWebExchange 转换为 AsyncHttpClient 的 Request
        Request request = buildRequest(exchange);
        // 发起代理请求
        CompletableFuture<Response> future = nettyHttpClient.executeRequest(request);
        if (properties.isWhenComplete()) {
            future.whenComplete((response, throwable) -> {
                complete(request, response, throwable, exchange);
            });
        } else {
            future.whenCompleteAsync((response, throwable) -> {
                complete(request, response, throwable, exchange);
            });
        }
    }

    /**
     * 处理代理请求的响应或异常。
     *
     * @param request   代理请求
     * @param response  代理响应
     * @param throwable 异常
     * @param exchange  当前的 {@link ServerWebExchange} 对象
     */
    private void complete(Request request, Response response, Throwable throwable, ServerWebExchange exchange) {
        if (Objects.nonNull(throwable)) {
            throw new GatewayException(request.getMethod() + " " + request.getUrl(), throwable);
        } else {
            ByteBuf content;
            if (Objects.nonNull(response)) {
                content = Unpooled.wrappedBuffer(response.getResponseBodyAsByteBuffer());
            } else {
                content = Unpooled.wrappedBuffer(GatewayConstants.BLANK_SEPARATOR.getBytes());
            }
            // 创建 Netty 的 FullHttpResponse
            DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);

            if (Objects.nonNull(response)) {
                fullHttpResponse.setStatus(HttpResponseStatus.valueOf(response.getStatusCode()));
                fullHttpResponse.headers().add(response.getHeaders());
            }
            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
            // 设置网关响应
            HttpServerOperations serverRequest = (HttpServerOperations) exchange.getRequest();
            serverRequest.setResponse(fullHttpResponse);
        }
    }

    /**
     * 返回过滤器的类型，固定为 {@link FilterTypeEnum#ENDPOINT}。
     *
     * @return 过滤器类型 {@link FilterTypeEnum#ENDPOINT}
     */
    @Override
    public FilterTypeEnum filterType() {
        return FilterTypeEnum.ENDPOINT;
    }

    /**
     * 返回过滤器的顺序，固定为 {@link Integer#MAX_VALUE}。
     *
     * @return 过滤器顺序 {@link Integer#MAX_VALUE}
     */
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * 将 {@link HttpServerRequest} 转换为 AsyncHttpClient 的 {@link Request}。
     *
     * @param exchange 当前的 {@link ServerWebExchange} 对象
     * @return 转换后的 {@link Request} 对象
     */
    private Request buildRequest(ServerWebExchange exchange) {
        HttpServerRequest request = exchange.getRequest();
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod(request.method().name());
        requestBuilder.setHeaders(request.requestHeaders());
        QueryStringDecoder stringDecoder = new QueryStringDecoder(exchange.getRequest().uri(), StandardCharsets.UTF_8);
        requestBuilder.setQueryParams(stringDecoder.parameters());
        if (Objects.nonNull(request.reqBody())) {
            requestBuilder.setBody(request.reqBody().nioBuffer());
        }
        URL url = exchange.getRequiredAttribute(GatewayConstants.GATEWAY_REQUEST_URL_ATTR);
        requestBuilder.setUrl(url.toString());
        return requestBuilder.build();
    }
}
