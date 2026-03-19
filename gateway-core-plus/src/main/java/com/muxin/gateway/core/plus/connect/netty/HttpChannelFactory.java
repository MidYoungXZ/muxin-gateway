package com.muxin.gateway.core.plus.connect.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.extern.slf4j.Slf4j;

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
                log.debug("HTTP channel released: {}", ch);
            }

            @Override
            public void channelAcquired(Channel ch) {
                log.debug("HTTP channel acquired: {}", ch);
            }

            @Override
            public void channelCreated(Channel ch) {
                log.debug("HTTP channel created: {}", ch);
                configurePipeline(ch.pipeline(), config);
            }
        };
    }

    private boolean isChannelHealthy(Channel channel) {
        return channel != null && channel.isActive() && channel.isWritable();
    }
}
