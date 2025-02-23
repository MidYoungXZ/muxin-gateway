package com.muxin.gateway.core.config;

import lombok.Data;

/**
 * 定义了Netty HTTP服务器的配置属性。这些属性用于配置Netty服务器的行为，包括端口、线程池设置、缓冲区大小、超时设置等。
 *
 * @author Administrator
 * @date 2024/11/20 16:57
 */
@Data
public class NettyHttpServerProperties {

    /**
     * 服务器监听的端口号。
     */
    private int port;

    /**
     * Boss线程组的数量，默认为1。
     */
    private int eventLoopGroupBossNum = 1;

    /**
     * Boss线程组的线程池名称，默认为"netty-boss-nio"。
     */
    private String eventLoopGroupBossThreadPoolName = "netty-boss-nio";

    /**
     * Worker线程组的数量，默认为可用处理器的数量。
     */
    private int eventLoopGroupWorkerNum = Runtime.getRuntime().availableProcessors();

    /**
     * Worker线程组的线程池名称，默认为"netty-worker-nio"。
     */
    private String eventLoopGroupWorkerThreadPoolName = "netty-worker-nio";

    /**
     * 请求内容的最大长度，默认为64MB。
     */
    private int maxContentLength = 64 * 1024 * 1024;

    /**
     * 服务器套接字的等待队列长度，默认为1024。
     */
    private int backlog = 1024;

    /**
     * 是否重用地址，默认为true。
     */
    private boolean reUseAddress = true;

    /**
     * 是否禁用Nagle算法，默认为true。
     */
    private boolean tcpNoDelay = true;

    /**
     * 是否启用TCP keep-alive机制，默认为true。
     */
    private boolean keepAlive = true;

    /**
     * 发送缓冲区大小，默认为65535字节。
     */
    private int sndBuf = 65535;

    /**
     * 接收缓冲区大小，默认为65535字节。
     */
    private int rcvBuf = 65535;

    /**
     * SO_LINGER选项的值，表示延迟关闭连接的时间（秒）。
     */
    private int soLinger;

    /**
     * SO_TIMEOUT选项的值，表示读取操作的超时时间（毫秒）。
     */
    private int soTimeout;
}
