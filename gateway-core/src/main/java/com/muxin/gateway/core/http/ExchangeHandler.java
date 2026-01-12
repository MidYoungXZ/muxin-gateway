package com.muxin.gateway.core.http;

/**
 * 交换处理器接口
 * 
 * 定义HTTP请求处理的接口
 *
 * @author Administrator
 * @since 1.0.0
 */
public interface ExchangeHandler {

   void handle(ServerWebExchange exchange);

}
