package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.LifeCycle;
import com.muxin.gateway.core.config.NettyHttpServerProperties;
import com.muxin.gateway.core.http.ExchangeHandler;
import com.muxin.gateway.core.utils.RemotingUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * [Class description]
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

    public NettyHttpServer(ExchangeHandler exchangeHandler, NettyHttpServerProperties properties) {
        this.exchangeHandler = exchangeHandler;
        this.properties = properties;
        init();
    }


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

    public boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        // 配置写缓冲区水位
        WriteBufferWaterMark waterMark = new WriteBufferWaterMark(
                properties.getWriteBufferLowWaterMark(),
                properties.getWriteBufferHighWaterMark()
        );
        
        this.serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupWorker)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                // 使用池化的ByteBuf分配器
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_BACKLOG, properties.getBacklog())            //	sync + accept = backlog
                .option(ChannelOption.SO_REUSEADDR, properties.isReUseAddress())    //	tcp端口重绑定
                .childOption(ChannelOption.SO_KEEPALIVE, properties.isKeepAlive())    //  如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.TCP_NODELAY, properties.isTcpNoDelay())   //	该参数的左右就是禁用Nagle算法，使用小数据传输时合并
                .childOption(ChannelOption.SO_SNDBUF, properties.getSndBuf())    //	设置发送数据缓冲区大小
                .childOption(ChannelOption.SO_RCVBUF, properties.getRcvBuf())    //	设置接收数据缓冲区大小
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark)   // 设置写缓冲区水位
                .localAddress(new InetSocketAddress(properties.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpServerCodec(), //http编解码
                                new HttpObjectAggregator(properties.getMaxContentLength()), //请求报文聚合成FullHttpRequest
                                new HttpServerExpectContinueHandler()
                        );
                        
                        // 如果启用压缩，添加压缩处理器
                        if (properties.isCompressionEnabled()) {
                            ch.pipeline().addLast(new HttpContentCompressor(properties.getCompressionLevel()));
                        }
                        
                        ch.pipeline().addLast(
                                new ExchangeHandlerAdapter(exchangeHandler),
                                new NettyServerConnectManagerHandler()
                        );
                    }
                });

        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port {}", this.properties.getPort());
        } catch (Exception e) {
            log.error("Failed to start Netty HTTP server on port {}", this.properties.getPort(), e);
            throw new RuntimeException("Netty HTTP server startup failed", e);
        }
    }

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
