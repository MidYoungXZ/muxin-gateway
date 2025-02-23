package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.LifeCycle;
import com.muxin.gateway.core.config.NettyHttpClientProperties;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * 实现了一个基于Netty的HTTP客户端，继承自 {@link LifeCycle} 接口。
 * 该类负责初始化、启动和关闭HTTP客户端，并提供执行HTTP请求的方法。
 *
 * @author Administrator
 * @date 2024/11/22 16:25
 */
@Slf4j
public class NettyHttpClient implements LifeCycle {

    private final NettyHttpClientProperties properties;

    private final EventLoopGroup eventLoopGroupWorker;

    private AsyncHttpClient asyncHttpClient;

    /**
     * 构造函数，用于初始化Netty HTTP客户端。
     *
     * @param properties HTTP客户端配置属性
     * @param eventLoopGroupWorker Netty的Worker线程组
     */
    public NettyHttpClient(NettyHttpClientProperties properties, EventLoopGroup eventLoopGroupWorker) {
        this.properties = properties;
        this.eventLoopGroupWorker = eventLoopGroupWorker;
        init();
    }

    /**
     * 初始化HTTP客户端。
     */
    @Override
    public void init() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder()
                .setEventLoopGroup(eventLoopGroupWorker)
                .setConnectTimeout(properties.getHttpConnectTimeout())
                .setRequestTimeout(properties.getHttpRequestTimeout())
                .setMaxRedirects(properties.getHttpMaxRequestRetry())
                .setAllocator(PooledByteBufAllocator.DEFAULT) // 使用池化的ByteBuf分配器，提升性能
                .setCompressionEnforced(true)
                .setMaxConnections(properties.getHttpMaxConnections())
                .setMaxConnectionsPerHost(properties.getHttpConnectionsPerHost())
                .setPooledConnectionIdleTimeout(properties.getHttpPooledConnectionIdleTimeout());
        this.asyncHttpClient = new DefaultAsyncHttpClient(builder.build());
    }

    /**
     * 启动HTTP客户端。
     */
    @Override
    public void start() {
        init();
    }

    /**
     * 关闭HTTP客户端。
     */
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

    /**
     * 执行HTTP请求并返回响应的CompletableFuture。
     *
     * @param request HTTP请求
     * @return 响应的CompletableFuture
     */
    public CompletableFuture<Response> executeRequest(Request request) {
        ListenableFuture<Response> future = asyncHttpClient.executeRequest(request);
        return future.toCompletableFuture();
    }

    /**
     * 执行HTTP请求并使用指定的异步处理器处理响应，返回结果的CompletableFuture。
     *
     * @param request HTTP请求
     * @param handler 异步处理器
     * @param <T> 结果类型
     * @return 结果的CompletableFuture
     */
    public <T> CompletableFuture<T> executeRequest(Request request, AsyncHandler<T> handler) {
        ListenableFuture<T> future = asyncHttpClient.executeRequest(request, handler);
        return future.toCompletableFuture();
    }
}
