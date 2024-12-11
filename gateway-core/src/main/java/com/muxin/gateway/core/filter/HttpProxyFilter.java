package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.common.GatewayConstants;
import com.muxin.gateway.core.config.NettyHttpClientProperties;
import com.muxin.gateway.core.http.HttpServerRequest;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.netty.NettyHttpClient;
import com.muxin.gateway.core.utils.ResponseUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
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


    private void complete(Request request,
                          Response response,
                          Throwable throwable,
                          ServerWebExchange exchange) {
        //释放请求资源

        try {
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time out {}", url);
                    // todo

                } else {
                    log.error("complete error {}", url, throwable);
                    //todo

                }
            } else {
                exchange.setResponse(ResponseUtil.clientResponseToHttpServerResponse(response));
            }
        } catch (Throwable t) {
            log.error("complete error", t);
            //todo

        } finally {


        }
    }


    @Override
    public FilterTypeEnum filterType() {
        return FilterTypeEnum.ENDPOINT;
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
        if(Objects.nonNull(request.body())){
            requestBuilder.setBody(request.body().nioBuffer());
        }
        URL url = exchange.getRequiredAttribute(GatewayConstants.GATEWAY_REQUEST_URL_ATTR);
        requestBuilder.setUrl(url.toString());
        return requestBuilder.build();
    }


}
