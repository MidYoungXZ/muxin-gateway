package com.muxin.gateway.core.plus.server.http;

import com.muxin.gateway.core.plus.DefaultRequestContext;
import com.muxin.gateway.core.plus.GatewayProcessor;
import com.muxin.gateway.core.plus.connect.NettyServerConnection;
import com.muxin.gateway.core.plus.common.Constant;
import com.muxin.gateway.core.plus.protocol.message.*;
import com.muxin.gateway.core.plus.protocol.message.http.HttpMessage;
import com.muxin.gateway.core.plus.route.RequestContext;
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
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * 简化的 HTTP 服务器实现
 * 专注于网络层功能，业务逻辑委托给 GatewayProcessor
 *
 * @author muxin
 */
@Slf4j
public class NettyHttpServer {

    private final int port;
    private final HttpServerConfig httpConfig;
    private final GatewayProcessor gatewayProcessor;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean running = false;

    public NettyHttpServer(int port, HttpServerConfig httpConfig, GatewayProcessor gatewayProcessor) {
        this.port = port;
        this.httpConfig = httpConfig != null ? httpConfig : HttpServerConfig.builder().build();
        this.gatewayProcessor = gatewayProcessor;

        if (gatewayProcessor == null) {
            throw new IllegalArgumentException("GatewayProcessor 不能为空");
        }

        log.info("[NettyHttpServer] 创建简化HTTP服务器 - 端口: {}, 配置: {}", port, this.httpConfig);
    }

    public NettyHttpServer(int port, GatewayProcessor gatewayProcessor) {
        this(port, HttpServerConfig.builder().build(), gatewayProcessor);
    }

    /**
     * 启动服务器
     */
    public void start() {
        if (running) {
            log.warn("[NettyHttpServer] 服务器已在运行中，忽略启动请求");
            return;
        }

        try {
            log.info("[NettyHttpServer] 开始启动HTTP服务器 - 端口: {}", port);

            // 初始化线程组
            bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
            workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("worker"));

            // 配置ServerBootstrap
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(new HttpChannelInitializer());

            // 绑定端口并启动
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(port)).sync();
            serverChannel = future.channel();

            running = true;
            log.info("[NettyHttpServer] HTTP服务器启动成功 - 监听端口: {}", port);

        } catch (Exception e) {
            log.error("[NettyHttpServer] HTTP服务器启动失败 - 端口: {}", port, e);
            throw new RuntimeException("HTTP服务器启动失败", e);
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (!running) {
            log.warn("[NettyHttpServer] 服务器未运行，忽略停止请求");
            return;
        }

        try {
            log.info("[NettyHttpServer] 开始停止HTTP服务器 - 端口: {}", port);

            if (serverChannel != null) {
                serverChannel.close().sync();
                serverChannel = null;
            }

            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
                workerGroup = null;
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
                bossGroup = null;
            }

            running = false;
            log.info("[NettyHttpServer] HTTP服务器停止完成 - 端口: {}", port);

        } catch (Exception e) {
            log.error("[NettyHttpServer] HTTP服务器停止失败 - 端口: {}", port, e);
        }
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
     */
    private class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
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

            // 简化的HTTP处理器
            pipeline.addLast(new DefaultHttpServerHandler(gatewayProcessor));

            log.debug("[NettyHttpServer] HTTP Channel管道初始化完成 - 远程地址: {}", ch.remoteAddress());
        }
    }

    /**
     * 简化的HTTP服务器处理器
     * 直接与GatewayProcessor对接，处理HTTP请求到网关处理的完整流程
     */
    private static class DefaultHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private final GatewayProcessor gatewayProcessor;

        public DefaultHttpServerHandler(GatewayProcessor gatewayProcessor) {
            this.gatewayProcessor = gatewayProcessor;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            // 存储请求到Channel属性中，用于后续判断Keep-Alive
            ctx.channel().attr(AttributeKey.<FullHttpRequest>valueOf("request")).set(request);
            try {
                DefaultRequestContext context = new DefaultRequestContext(new ProtocolData(ProtocolEnum.HTTP, request));
                NettyServerConnection connection = new NettyServerConnection(ctx, ProtocolEnum.HTTP);
                context.setServerConnection(connection);
                context.setAttribute(Constant.CTX, ctx);
                gatewayProcessor.processRequest(context);
            } catch (Exception e) {
                log.error("[SimpleHttpServerHandler] 处理请求异常", e);
                writeErrorResponse(e, ctx);
            }
        }

        /**
         * 创建HTTP消息对象
         */
        private Message createHttpMessage(FullHttpRequest request) {
            // 使用HttpMessage实现
            String messageId = UUID.randomUUID().toString();
            MessageType type = MessageType.REQUEST;
            Protocol protocol = ProtocolEnum.LB;

            return new HttpMessage(
                    messageId,
                    type,
                    protocol,
                    null, // headers
                    null,
                    null,
                    null, // body
                    null  // metadata
            );
        }

        /**
         * 创建请求上下文
         */
        private RequestContext createRequestContext(FullHttpRequest request, ChannelHandlerContext ctx) {
            // 创建HTTP协议
            Protocol httpProtocol = ProtocolEnum.LB;

            // 创建服务器连接
            NettyServerConnection connection = new NettyServerConnection(ctx, httpProtocol);

            // 先创建一个简单的Message对象
            Message httpMessage = createHttpMessage(request);

            // 创建上下文
            RequestContext context = new DefaultRequestContext(httpMessage, connection);

            // 设置原始请求信息到上下文属性中
            context.setAttribute("nettyRequest", request);
            context.setAttribute("nettyContext", ctx);
            context.setAttribute("protocol", httpProtocol);
            context.setAttribute("startTime", System.currentTimeMillis());

            // 从HTTP请求中提取基本信息
            context.setAttribute("method", request.method().name());
            context.setAttribute("uri", request.uri());
            context.setAttribute("path", extractPath(request.uri()));
            context.setAttribute("queryString", extractQueryString(request.uri()));

            return context;
        }

        /**
         * 写入HTTP响应
         */
        private void writeHttpResponse(Message response, ChannelHandlerContext ctx, FullHttpRequest originalRequest) {
            try {
                // 创建Netty HTTP响应
                FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK
                );

                // 设置响应内容
                if (response != null && response.getBody() != null) {
                    byte[] content = response.getBody().getBytes();
                    httpResponse.content().writeBytes(content);
                    httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
                }

                // 设置响应头
                httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
                if (response != null && response.getHeaders() != null) {
                    response.getHeaders().asMap().forEach((name, value) -> {
                        if (value != null) {
                            httpResponse.headers().set(name, value.toString());
                        }
                    });
                }

                // 检查Keep-Alive
                boolean keepAlive = HttpUtil.isKeepAlive(originalRequest);

                if (keepAlive) {
                    httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.writeAndFlush(httpResponse);
                } else {
                    ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
                }

                log.debug("[SimpleHttpServerHandler] HTTP响应写入完成 - keepAlive: {}", keepAlive);

            } catch (Exception e) {
                log.error("[SimpleHttpServerHandler] 写入响应失败", e);
                ctx.close();
            }
        }

        /**
         * 写入错误响应
         */
        private void writeErrorResponse(Throwable ex, ChannelHandlerContext ctx) {
            try {
                String errorMessage = String.format(
                        "{\"error\":{\"code\":500,\"message\":\"%s\",\"timestamp\":%d}}",
                        ex.getMessage(), System.currentTimeMillis()
                );

                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        ctx.alloc().buffer().writeBytes(errorMessage.getBytes())
                );

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, errorMessage.length());

                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

            } catch (Exception e) {
                log.error("[SimpleHttpServerHandler] 写入错误响应失败", e);
                ctx.close();
            }
        }

        /**
         * 从URI中提取路径
         */
        private String extractPath(String uri) {
            if (uri == null) return "/";
            int queryIndex = uri.indexOf('?');
            return queryIndex > 0 ? uri.substring(0, queryIndex) : uri;
        }

        /**
         * 从URI中提取查询字符串
         */
        private String extractQueryString(String uri) {
            if (uri == null) return null;
            int queryIndex = uri.indexOf('?');
            return queryIndex > 0 && queryIndex < uri.length() - 1 ? uri.substring(queryIndex + 1) : null;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.debug("[SimpleHttpServerHandler] 连接建立 - 远程地址: {}", ctx.channel().remoteAddress());
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.debug("[SimpleHttpServerHandler] 连接关闭 - 远程地址: {}", ctx.channel().remoteAddress());
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("[SimpleHttpServerHandler] 连接异常 - 远程地址: {}", ctx.channel().remoteAddress(), cause);
            ctx.close();
        }
    }

    // ========== 访问器方法 ==========

    public int getPort() {
        return port;
    }

    public HttpServerConfig getHttpConfig() {
        return httpConfig;
    }

    public GatewayProcessor getGatewayProcessor() {
        return gatewayProcessor;
    }

    public boolean isRunning() {
        return running;
    }
} 