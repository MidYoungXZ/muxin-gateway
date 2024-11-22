package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.LifeCycle;
import com.muxin.gateway.core.config.NettyHttpClientProperties;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;

import java.io.IOException;

/**
 * [Class description]
 *
 * @author Administrator
 * @date 2024/11/22 16:25
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private final NettyHttpClientProperties properties;

    private final EventLoopGroup eventLoopGroupWorker;

    private AsyncHttpClient asyncHttpClient;

    public NettyHttpClient(NettyHttpClientProperties properties, EventLoopGroup eventLoopGroupWorker) {
        this.properties = properties;
        this.eventLoopGroupWorker = eventLoopGroupWorker;
        init();
    }

    @Override
    public void init() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroupWorker)
                .setConnectTimeout(properties.getHttpConnectTimeout())
                .setRequestTimeout(properties.getHttpRequestTimeout())
                .setMaxRedirects(properties.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT) //池化的byteBuf分配器，提升性能
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
    }
}