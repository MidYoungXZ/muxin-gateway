package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.LifeCycle;
import com.muxin.gateway.core.config.NettyHttpServerProperties;
import com.muxin.gateway.core.http.ExchangeHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 实现了一个基于Netty的HTTP服务器，继承自 {@link LifeCycle} 接口。
 * 该类负责初始化、启动和关闭HTTP服务器，并配置Netty的通道和处理器。
 *
 * @author Administrator
 * @date 2024/11/20 16:50
 */
@Slf4j
@Data
public class NettyHttpServer implements LifeCycle {

    private ServerBootstrap serverBootstrap;
    private EventLoopGroup eventLoopGroupBoss;
    private EventLoopGroup eventLoopGroupWorker;

    private NettyHttpServerProperties properties;

    private ExchangeHandler exchangeHandler;

    /**
     * 构造函数，用于初始化Netty HTTP服务器。
     *
     * @param exchangeHandler 交换处理器
     * @param properties HTTP服务器配置属性
     */
    public NettyHttpServer(ExchangeHandler exchangeHandler, NettyHttpServerProperties properties) {
        this.exchangeHandler = exchangeHandler;
        this.properties = properties;
        init();
    }

    /**
     * 初始化HTTP服务器。
     */
    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (useEpoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(properties.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory(properties.getEventLoopGroupBossThreadPoolName()));
            this.eventLoopGroupWorker = new EpollEventLoopGroup(properties.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory(properties.getEventLoopGroupWorkerThreadPoolName()));
        } else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(properties.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory(properties.getEventLoopGroupBossThreadPoolName()));
            this.eventLoopGroupWorker = new NioEventLoopGroup(properties.getEventLoopGroupWorkerNum(),
                    new DefaultThreadFactory(properties.getEventLoopGroupWorkerThreadPoolName()));
        }
    }

    /**
     * 检查是否可以使用Epoll。
     *
     * @return 如果可以使用Epoll，返回 true；否则返回 false
     */
    public boolean useEpoll() {
        return Epoll.isAvailable();
    }

    /**
     * 启动HTTP服务器。
     */
    @Override
    public void start() {
        this.serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupWorker)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, properties.getBacklog())            // 设置同步和接受连接的队列大小
                .option(ChannelOption.SO_REUSEADDR, properties.isReUseAddress())    // 允许端口重绑定
                .option(ChannelOption.SO_KEEPALIVE, properties.isKeepAlive())    // 启用TCP keep-alive机制
                .childOption(ChannelOption.TCP_NODELAY, properties.isTcpNoDelay())   // 禁用Nagle算法
                .childOption(ChannelOption.SO_SNDBUF, properties.getSndBuf())    // 设置发送缓冲区大小
                .childOption(ChannelOption.SO_RCVBUF, properties.getRcvBuf())    // 设置接收缓冲区大小
                .localAddress(new InetSocketAddress(properties.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpServerCodec(), // HTTP编解码器
                                new HttpObjectAggregator(properties.getMaxContentLength()), // 请求报文聚合为FullHttpRequest
                                new HttpServerExpectContinueHandler(),
                                new ExchangeHandlerAdapter(exchangeHandler), // 交换处理器适配器
                                new NettyServerConnectManagerHandler()
                        );
                    }
                });

        try {
            this.serverBootstrap.bind().sync();
            log.info("Server started on port {}", this.properties.getPort());
        } catch (Exception e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }

    /**
     * 关闭HTTP服务器。
     */
    @Override
    public void shutdown() {
        if (eventLoopGroupBoss != null) {
            eventLoopGroupBoss.shutdownGracefully();
        }
        if (eventLoopGroupWorker != null) {
            eventLoopGroupWorker.shutdownGracefully();
        }
    }
}
