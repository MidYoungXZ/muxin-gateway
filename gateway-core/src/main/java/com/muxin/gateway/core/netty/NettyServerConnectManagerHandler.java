package com.muxin.gateway.core.netty;

import com.muxin.gateway.core.utils.RemotingUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty服务器连接管理器
 */
@Slf4j
public class NettyServerConnectManagerHandler extends ChannelDuplexHandler {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        //当Channel注册到它的EventLoop并且能够处理I/O时调用
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelRegistered {}", remoteAddr);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        //当Channel从它的EventLoop中注销并且无法处理任何I/O时调用
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelUnregistered {}", remoteAddr);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //当Channel处理于活动状态时被调用，可以接收与发送数据
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelActive {}", remoteAddr);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //不再是活动状态且不再连接它的远程节点时被调用
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.debug("NETTY SERVER PIPLINE: channelInactive {}", remoteAddr);
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //当ChannelInboundHandler.fireUserEventTriggered()方法被调用时触发
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.ALL_IDLE)) { //有一段时间没有收到或发送任何数据
                final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
                log.warn("NETTY SERVER PIPLINE: userEventTriggered: IDLE {}", remoteAddr);
                ctx.channel().close();
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        //当ChannelHandler在处理过程中出现异常时调用
        final String remoteAddr = RemotingUtil.parseChannelRemoteAddr(ctx.channel());
        log.error("NETTY SERVER PIPELINE: remoteAddr: {}, exceptionCaught", remoteAddr, cause);
        ctx.channel().close();
    }

}