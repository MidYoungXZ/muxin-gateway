package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.LifeCycle;
import com.muxin.gateway.core.config.NettyHttpClientProperties;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/22 16:25
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private final NettyHttpClientProperties properties;

    private AsyncHttpClient asyncHttpClient;

    private EventLoopGroup eventLoopGroupWork;

    public NettyHttpClient(NettyHttpClientProperties properties) {
        this.properties = properties;
        init();
    }

    @Override
    public void init() {
        this.eventLoopGroupWork = new NioEventLoopGroup(16);

        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroupWork)
                .setConnectTimeout(properties.getHttpConnectTimeout())
                .setRequestTimeout(properties.getHttpRequestTimeout())
                .setMaxRequestRetry(properties.getHttpMaxRequestRetry())
                .setAllocator(io.netty.buffer.PooledByteBufAllocator.DEFAULT)
                .setCompressionEnforced(true)
                .setMaxConnections(properties.getHttpMaxConnections())
                .setMaxConnectionsPerHost(properties.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(properties.getHttpPooledConnectionIdleTimeout());

        this.asyncHttpClient = new DefaultAsyncHttpClient(builder.build());
    }

    @Override
    public void start() {
        init();
    }

    @Override
    public void shutdown() {
        if (asyncHttpClient != null) {
            try {
                this.asyncHttpClient.close();
            } catch (IOException e) {
                log.error("NettyHttpClient shutdown error", e);
            }
        }
        if (eventLoopGroupWork != null) {
            eventLoopGroupWork.shutdownGracefully();
        }
    }

    public CompletableFuture<Response> executeRequest(Request request) {
        ListenableFuture<Response> future = asyncHttpClient.executeRequest(request);
        return future.toCompletableFuture();
    }

    public <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
        ListenableFuture<T> future = asyncHttpClient.executeRequest(request, handler);
        return future.toCompletableFuture();
    }

    public AsyncHttpClient getAsyncHttpClient() {
        return asyncHttpClient;
    }
}