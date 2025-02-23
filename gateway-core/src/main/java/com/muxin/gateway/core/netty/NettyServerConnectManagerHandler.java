package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.utils.RemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 实现了一个Netty的双向通道处理器，继承自 {@link ChannelDuplexHandler}。
 * 该类用于管理Netty服务器的连接状态，包括注册、注销、激活、非激活以及异常处理。
 *
 * @author Administrator
 * @date 2024/11/20 17:22
 */
@Slf4j
public class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

    /**
     * 当Channel注册到它的EventLoop并且能够处理I/O时调用。
     *
     * @param ctx 通道上下文
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelRegistered {}", remoteAddr);
        super.channelRegistered(ctx);
    }

    /**
     * 当Channel从它的EventLoop中注销并且无法处理任何I/O时调用。
     *
     * @param ctx 通道上下文
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelUnregistered {}", remoteAddr);
        super.channelUnregistered(ctx);
    }

    /**
     * 当Channel处理于活动状态时被调用，可以接收与发送数据。
     *
     * @param ctx 通道上下文
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelActive {}", remoteAddr);
        super.channelActive(ctx);
    }

    /**
     * 当Channel不再是活动状态且不再连接它的远程节点时被调用。
     *
     * @param ctx 通道上下文
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelInactive {}", remoteAddr);
        super.channelInactive(ctx);
    }

    /**
     * 当ChannelInboundHandler.fireUserEventTriggered()方法被调用时触发。
     *
     * @param ctx 通道上下文
     * @param evt 用户事件
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) { // 有一段时间没有收到或发送任何数据
                final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
                log.warn("NETTY SERVER PIPLINE: userEventTriggered: IDLE {}", remoteAddr);
                ctx.channel().close();
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    /**
     * 当ChannelHandler在处理过程中出现异常时调用。
     *
     * @param ctx 通道上下文
     * @param cause 异常
     * @throws Exception 如果处理过程中发生异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.warn("NETTY SERVER PIPLINE: remoteAddr: {}, exceptionCaught {}", remoteAddr, cause);
        ctx.channel().close();
    }
}
