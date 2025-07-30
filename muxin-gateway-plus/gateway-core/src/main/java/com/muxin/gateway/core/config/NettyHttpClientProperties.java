package com.muxin.gateway.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Netty HTTP客户端配置属性
 */
@Data
@ConfigurationProperties(prefix = "muxin.gateway.netty.client")
public class NettyHttpClientProperties {
    //	连接超时时间 - 优化为5秒
    private int httpConnectTimeout = 5000;

    //	请求超时时间
    private int httpRequestTimeout = 30000;

    //	读取超时时间
    private int httpReadTimeout = 30000;

    //	客户端请求重试次数
    private int httpMaxRequestRetry = 3;

    //	客户端请求最大连接数 - 优化为2000
    private int httpMaxConnections = 2000;

    //	客户端每个地址支持的最大连接数 - 优化为500
    private int httpConnectionsPerHost = 500;

    //	客户端空闲连接超时时间, 默认60秒
    private int httpPooledConnectionIdleTimeout = 60000;

    //	连接TTL（生存时间）- 5分钟
    private int httpConnectionTtl = 300000;

    //	IO线程数 - 默认为CPU核心数的2倍
    private int ioThreadsCount = Runtime.getRuntime().availableProcessors() * 2;

    //	是否启用连接池
    private boolean poolingEnabled = true;

    //	是否启用压缩
    private boolean compressionEnabled = true;

    //	是否跟随重定向
    private boolean followRedirect = false;

    //	最大重定向次数
    private int maxRedirects = 5;

    //	是否异步
    private boolean whenComplete = true;

    //	是否启用TCP NoDelay
    private boolean tcpNoDelay = true;

    //	是否启用SO_KEEPALIVE
    private boolean soKeepAlive = false;

    //	发送缓冲区大小
    private int soSndBuf = 1048576;

    //	接收缓冲区大小
    private int soRcvBuf = 1048576;
}
