package com.muxin.gateway.refactory.server.http;

import com.muxin.gateway.refactory.connect.NettyServerConnection;
import com.muxin.gateway.refactory.message.Protocol;
import com.muxin.gateway.refactory.message.ProtocolAdapter;
import com.muxin.gateway.refactory.message.http.HttpProtocolAdapter;
import com.muxin.gateway.refactory.server.GenericProtocolServer;
import com.muxin.gateway.refactory.server.MessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * 基于泛型设计的HTTP协议服务器实现
 * 继承GenericProtocolServer，支持类型安全的HTTP处理
 * 
 * 泛型参数：
 * - REQ: FullHttpRequest (HTTP请求)
 * - RESP: FullHttpResponse (HTTP响应)  
 * - CTX: ChannelHandlerContext (Netty上下文)
 * - CONN: NettyServerConnection (服务器连接)
 * 
 * @author muxin
 */
@Slf4j
public class NettyHttpServer 
    extends GenericProtocolServer<FullHttpRequest, FullHttpResponse, ChannelHandlerContext, NettyServerConnection> {
    
    private final HttpServerConfig httpConfig;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    
    public NettyHttpServer(int port, HttpServerConfig httpConfig) {
        super(new Protocol.HttpProtocol(), port, new HttpProtocolAdapter());
        this.httpConfig = httpConfig != null ? httpConfig : HttpServerConfig.builder().build();
        
        log.info("[NettyHttpServer] 创建泛型HTTP服务器 - 端口: {}, 配置: {}", port, this.httpConfig);
    }
    
    public NettyHttpServer(int port) {
        this(port, HttpServerConfig.builder().build());
    }
    
    @Override
    public void init() {
        // 初始化方法 - 可以在这里进行一些预处理
        log.info("[NettyHttpServer] 初始化HTTP服务器 - 端口: {}", port);
    }
    
    @Override
    protected void doStart() throws Exception {
        // 检查MessageHandler
        if (messageHandler == null) {
            throw new IllegalStateException("MessageHandler未设置，无法启动服务器");
        }
        
        // 初始化线程组
        initEventLoopGroups();
        
        // 配置ServerBootstrap
        bootstrap = new ServerBootstrap();
        configureBootstrap();
        
        // 绑定端口并启动
        ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
        serverChannel = future.channel();
        
        log.info("[NettyHttpServer] HTTP服务器启动成功 - 监听端口: {}", port);
    }
    
    @Override
    protected void doStop() throws Exception {
        try {
            // 关闭服务器通道
            if (serverChannel != null) {
                serverChannel.close().sync();
                serverChannel = null;
            }
        } finally {
            // 优雅关闭线程组
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
                workerGroup = null;
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
                bossGroup = null;
            }
        }
        
        log.info("[NettyHttpServer] HTTP服务器停止完成 - 端口: {}", port);
    }
    
    // ========== 实现GenericProtocolServer抽象方法 ==========
    
    @Override
    protected void writeResponse(FullHttpResponse response, ChannelHandlerContext context) {
        // 检查是否需要保持连接
        boolean keepAlive = HttpUtil.isKeepAlive((HttpMessage) context.channel().attr(
            io.netty.util.AttributeKey.valueOf("request")).get());
        
        if (keepAlive) {
            context.writeAndFlush(response);
        } else {
            context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
        
        log.debug("[NettyHttpServer] HTTP响应写入完成 - keepAlive: {}", keepAlive);
    }
    
    @Override
    protected void closeConnection(ChannelHandlerContext context) {
        log.debug("[NettyHttpServer] 关闭HTTP连接 - 远程地址: {}", context.channel().remoteAddress());
        context.close();
    }
    
    // ========== 私有方法 ==========
    
    private void initEventLoopGroups() {
        if (useEpoll()) {
            bossGroup = new EpollEventLoopGroup(
                httpConfig.getBossThreads(),
                new DefaultThreadFactory(httpConfig.getBossThreadName())
            );
            workerGroup = new EpollEventLoopGroup(
                httpConfig.getWorkerThreads(),
                new DefaultThreadFactory(httpConfig.getWorkerThreadName())
            );
            log.debug("[NettyHttpServer] 使用Epoll事件循环组");
        } else {
            bossGroup = new NioEventLoopGroup(
                httpConfig.getBossThreads(),
                new DefaultThreadFactory(httpConfig.getBossThreadName())
            );
            workerGroup = new NioEventLoopGroup(
                httpConfig.getWorkerThreads(),
                new DefaultThreadFactory(httpConfig.getWorkerThreadName())
            );
            log.debug("[NettyHttpServer] 使用NIO事件循环组");
        }
    }
    
    private boolean useEpoll() {
        return Epoll.isAvailable() && httpConfig.isUseNativeTransport();
    }
    
    private void configureBootstrap() {
        // 配置写缓冲区水位
        WriteBufferWaterMark waterMark = new WriteBufferWaterMark(
            httpConfig.getWriteBufferLowWaterMark(),
            httpConfig.getWriteBufferHighWaterMark()
        );
        
        bootstrap.group(bossGroup, workerGroup)
            .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
            // 服务器Socket配置
            .option(ChannelOption.SO_BACKLOG, httpConfig.getBacklog())
            .option(ChannelOption.SO_REUSEADDR, httpConfig.isReuseAddr())
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            // 子Channel配置
            .childOption(ChannelOption.SO_KEEPALIVE, httpConfig.isKeepAlive())
            .childOption(ChannelOption.TCP_NODELAY, httpConfig.isTcpNoDelay())
            .childOption(ChannelOption.SO_SNDBUF, httpConfig.getSendBufferSize())
            .childOption(ChannelOption.SO_RCVBUF, httpConfig.getReceiveBufferSize())
            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, waterMark)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childHandler(new HttpChannelInitializer());
        
        log.debug("[NettyHttpServer] ServerBootstrap配置完成");
    }
    
    /**
     * HTTP Channel初始化器
     * 使用泛型设计的HTTP处理器
     */
    private class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            
            // HTTP编解码器
            pipeline.addLast(new HttpServerCodec(
                httpConfig.getMaxInitialLineLength(),
                httpConfig.getMaxHeaderSize(),
                httpConfig.getMaxChunkSize()
            ));
            
            // HTTP消息聚合器
            pipeline.addLast(new HttpObjectAggregator(httpConfig.getMaxContentLength()));
            
            // Expect: 100-continue处理
            pipeline.addLast(new HttpServerExpectContinueHandler());
            
            // 支持分块传输
            pipeline.addLast(new ChunkedWriteHandler());
            
            // 压缩处理器(可选)
            if (httpConfig.isCompressionEnabled()) {
                pipeline.addLast(new HttpContentCompressor(
                    httpConfig.getCompressionLevel(),
                    httpConfig.getCompressionWindowBits(),
                    httpConfig.getCompressionMemLevel()
                ));
            }
            
            // ===== 关键：使用泛型设计的HTTP处理器 =====
            pipeline.addLast(new GenericHttpServerHandler(NettyHttpServer.this));
            
            log.debug("[NettyHttpServer] HTTP Channel管道初始化完成 - 远程地址: {}", 
                ch.remoteAddress());
        }
    }
    
    /**
     * 泛型HTTP服务器处理器
     * 继承SimpleChannelInboundHandler，处理FullHttpRequest
     */
    private static class GenericHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        
        private final NettyHttpServer server;
        
        public GenericHttpServerHandler(NettyHttpServer server) {
            this.server = server;
        }
        
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
            // 存储请求到Channel属性中，用于后续判断Keep-Alive
            ctx.channel().attr(io.netty.util.AttributeKey.valueOf("request")).set(request);
            
            // 调用父类的模板方法处理请求
            server.handleInboundRequest(request, ctx);
        }
        
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            server.handleConnectionEstablished(ctx);
            super.channelActive(ctx);
        }
        
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            server.handleConnectionClosed(ctx);
            super.channelInactive(ctx);
        }
        
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            server.handleConnectionException(ctx, cause);
            // 异常时关闭连接
            ctx.close();
        }
    }
    
    /**
     * 获取HTTP配置
     */
    public HttpServerConfig getHttpConfig() {
        return httpConfig;
    }
    
    /**
     * 将HttpServerConfig转换为Map用于父类
     */
    private static Map<String, Object> convertConfigToMap(HttpServerConfig config) {
        if (config == null) {
            return java.util.Collections.emptyMap();
        }
        
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("bossThreads", config.getBossThreads());
        map.put("workerThreads", config.getWorkerThreads());
        map.put("backlog", config.getBacklog());
        map.put("keepAlive", config.isKeepAlive());
        map.put("tcpNoDelay", config.isTcpNoDelay());
        map.put("maxContentLength", config.getMaxContentLength());
        map.put("compressionEnabled", config.isCompressionEnabled());
        return map;
    }
} 