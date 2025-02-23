package com.muxin.gateway.core.http;

/**
 * 定义了一个处理HTTP请求的接口，所有具体的HTTP请求处理器都需要实现这个接口。
 * 该接口的主要职责是处理传入的ServerWebExchange对象，完成请求的路由、过滤和响应。
 *
 * @author Administrator
 * @date 2024/11/21 10:11
 */
public interface ExchangeHandler {
   /**
    * 处理HTTP请求的方法。
    *
    * @param exchange 当前的ServerWebExchange对象，包含了请求和响应的相关信息。
    */
   void handle(ServerWebExchange exchange);
}
