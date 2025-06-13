package com.muxin.gateway.core.config;

import lombok.Data;

/**
 * Netty HTTP客户端配置属性
 */
@Data
public class NettyHttpClientProperties {
    //	连接超时时间
    private int httpConnectTimeout = 10000;

    //	请求超时时间
    private int httpRequestTimeout = 30000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 3;

    //	客户端请求最大连接数
    private int httpMaxConnections = 1000;

    //	客户端每个地址支持的最大连接数
    private int httpConnectionsPerHost = 100;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60000;

    //是否异步
    private boolean whenComplete = false;

}
