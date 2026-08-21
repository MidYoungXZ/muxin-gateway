package com.muxin.gateway.core.connect.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * HTTP协议Channel工厂
 * 简化版本：移除协议抽象，专注于HTTP
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
@Slf4j
public class HttpChannelFactory {

    private static final String CODEC_HANDLER = "http-codec";
    private static final String AGGREGATOR_HANDLER = "http-aggregator";
    private static final AttributeKey<ScheduledFuture<?>> IDLE_CLOSE_TASK =
            AttributeKey.valueOf("gateway.idleCloseTask");

    private final NettyPoolConfig config;

    public HttpChannelFactory() {
        this(NettyPoolConfig.defaultConfig());
    }

    public HttpChannelFactory(NettyPoolConfig config) {
        this.config = config;
    }

    public void configureBootstrap(Bootstrap bootstrap, NettyPoolConfig config) {
        // HTTP协议无需额外配置Bootstrap
    }

    public void configurePipeline(ChannelPipeline pipeline, NettyPoolConfig config) {
        pipeline.addLast(CODEC_HANDLER, new HttpClientCodec(
                config.getMaxInitialLineLength(),
                config.getMaxHeaderSize(),
                config.getMaxChunkSize(),
                false,
                true
        ));

        pipeline.addLast(AGGREGATOR_HANDLER, new HttpObjectAggregator(config.getMaxContentLength()));

        pipeline.addLast("response-handler", new HttpResponseHandler());

        log.debug("HTTP pipeline configured: maxContentLength={}", config.getMaxContentLength());
    }

    public ChannelHealthChecker createHealthChecker() {
        return channel -> channel.eventLoop().newSucceededFuture(isChannelHealthy(channel));
    }

    public ChannelPoolHandler createPoolHandler() {
        return new ChannelPoolHandler() {
            @Override
            public void channelReleased(Channel ch) {
                scheduleIdleClose(ch);
                log.debug("HTTP channel released: {}", ch);
            }

            @Override
            public void channelAcquired(Channel ch) {
                cancelIdleClose(ch);
                log.debug("HTTP channel acquired: {}", ch);
            }

            @Override
            public void channelCreated(Channel ch) {
                log.debug("HTTP channel created: {}", ch);
                configurePipeline(ch.pipeline(), config);
            }
        };
    }

    private void scheduleIdleClose(Channel channel) {
        Long idleTimeout = config.getIdleTimeout();
        if (idleTimeout == null || idleTimeout <= 0) {
            return;
        }
        cancelIdleClose(channel);
        channel.attr(IDLE_CLOSE_TASK).set(channel.eventLoop().schedule(() -> channel.close(), idleTimeout, TimeUnit.MILLISECONDS));
    }

    private void cancelIdleClose(Channel channel) {
        ScheduledFuture<?> task = channel.attr(IDLE_CLOSE_TASK).getAndSet(null);
        if (task != null) {
            task.cancel(false);
        }
    }

    private boolean isChannelHealthy(Channel channel) {
        return channel != null && channel.isActive() && channel.isWritable();
    }
}
