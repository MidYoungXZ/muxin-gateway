package com.muxin.gateway.core.route.filter;

import com.muxin.gateway.core.common.GatewayConstants;
import com.muxin.gateway.core.config.NettyHttpClientProperties;
import com.muxin.gateway.core.http.HttpServerRequest;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.netty.NettyHttpClient;
import com.muxin.gateway.core.utils.ResponseUtil;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 基于AsyncHttpClient实现的http请求过滤器
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


    @Override
    public void filter(ServerWebExchange exchange) {
        //两种接口类型的转换
        Request request = buildRequest(exchange);
        //代理请求
        log.info("HTTP request for URL: {}", request.getUrl());
        CompletableFuture<Response> future = nettyHttpClient.executeRequest(request);
        try {
            Response response1 = future.get(3, TimeUnit.SECONDS);
            complete(request, response1, null, exchange);
        } catch (Exception e) {
            log.error("complete", e);
        }
//        if (properties.isWhenComplete()) {
//            future.whenCompleteAsync((response, throwable) -> {
//                complete(request, response, throwable, exchange);
//            });
//        } else {
//            future.whenComplete((response, throwable) -> {
//                complete(request, response, throwable, exchange);
//            });
//        }
    }


    private void complete(Request request,
                          Response response,
                          Throwable throwable,
                          ServerWebExchange exchange) {

        try {
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("HTTP request timeout for URL: {}", url);
                    exchange.setOriginalResponse(ResponseUtil.createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                } else {
                    log.error("HTTP request error for URL: {}", url, throwable);
                    exchange.setOriginalResponse(ResponseUtil.createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR));
                }
            } else {
                exchange.setOriginalResponse(ResponseUtil.convertToFullHttpResponse(response));
            }
        } catch (Throwable t) {
            log.error("Error during HTTP request completion", t);
            exchange.setOriginalResponse(ResponseUtil.createResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, t.getMessage()));
        }
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }


    /**
     * HttpServerRequest 转为asynchttpclient request
     *
     * @param exchange
     * @return
     */
    private Request buildRequest(ServerWebExchange exchange) {
        HttpServerRequest request = exchange.getRequest();
        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setMethod(request.method().name());
        requestBuilder.setHeaders(request.requestHeaders());
        QueryStringDecoder stringDecoder = new QueryStringDecoder(exchange.getRequest().uri(), StandardCharsets.UTF_8);
        requestBuilder.setQueryParams(stringDecoder.parameters());
        if (Objects.nonNull(request.body())) {
            requestBuilder.setBody(request.body().nioBuffer());
        }

        // 从LoadBalanceFilter设置的URI属性中获取目标URL
        log.info("Built exchange: {}", exchange.getRequest().uri());
        try {
            URI targetUri = exchange.getRequiredAttribute(GatewayConstants.GATEWAY_REQUEST_URL_ATTR);
            requestBuilder.setUrl(targetUri.toString());
        } catch (Exception e) {
            log.error("getRequiredAttribute request:{}", exchange.getRequest(), e);
        }
        return requestBuilder.build();
    }


}
