package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.config.NettyHttpClientProperties;
import com.muxin.gateway.core.http.ServerWebExchange;
import com.muxin.gateway.core.netty.NettyHttpClient;
import com.muxin.gateway.core.utils.ResponseUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * [Class description]
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
}
