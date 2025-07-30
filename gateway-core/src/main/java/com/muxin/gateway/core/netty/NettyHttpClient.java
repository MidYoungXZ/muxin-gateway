package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.LifeCycle;
import com.muxin.gateway.core.config.NettyHttpClientProperties;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.util.concurrent.CompletableFuture;

/**
 * Netty HTTP客户端
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private final NettyHttpClientProperties properties;

    private AsyncHttpClient asyncHttpClient;

    private EventLoopGroup eventLoopGroup;

    public NettyHttpClient(NettyHttpClientProperties properties) {
        this.properties = properties;
        init();
    }

    @Override
    public void init() {
        // 创建专用的EventLoopGroup
        this.eventLoopGroup = new NioEventLoopGroup(properties.getIoThreadsCount());
        
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroup)
                .setAllocator(PooledByteBufAllocator.DEFAULT)
                .setConnectTimeout(properties.getHttpConnectTimeout())
                .setRequestTimeout(properties.getHttpRequestTimeout())
                .setReadTimeout(properties.getHttpReadTimeout())
                .setMaxRequestRetry(properties.getHttpMaxRequestRetry())
                .setMaxConnections(properties.getHttpMaxConnections())
                .setMaxConnectionsPerHost(properties.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(properties.getHttpPooledConnectionIdleTimeout())
                .setConnectionTtl(properties.getHttpConnectionTtl())
                .setIoThreadsCount(properties.getIoThreadsCount())
                .setCompressionEnforced(properties.isCompressionEnabled())
                .setFollowRedirect(properties.isFollowRedirect())
                .setMaxRedirects(properties.getMaxRedirects())
                .setTcpNoDelay(properties.isTcpNoDelay())
                .setKeepAlive(properties.isSoKeepAlive())
                .setSoSndBuf(properties.getSoSndBuf())
                .setSoRcvBuf(properties.getSoRcvBuf())
                // 使用池化连接
                .setUseNativeTransport(false);

        this.asyncHttpClient = new DefaultAsyncHttpClient(builder.build());
    }

    @Override
    public void start() {
        log.info("NettyHttpClient started with config: maxConnections={}, connectionsPerHost={}, ioThreads={}",
                properties.getHttpMaxConnections(),
                properties.getHttpConnectionsPerHost(),
                properties.getIoThreadsCount());
    }

    @Override
    public void shutdown() {
        try {
            if (asyncHttpClient != null) {
                asyncHttpClient.close();
            }
            if (eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            log.error("NettyHttpClient shutdown error", e);
        }
    }

    public CompletableFuture<Response> executeRequest(Request request) {
        if (properties.isWhenComplete()) {
            return asyncHttpClient.executeRequest(request).toCompletableFuture();
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return asyncHttpClient.executeRequest(request).get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}