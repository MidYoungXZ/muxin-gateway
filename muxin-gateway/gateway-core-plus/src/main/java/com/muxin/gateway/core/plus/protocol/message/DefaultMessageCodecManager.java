package com.muxin.gateway.core.plus.protocol.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认消息编解码管理器实现
 * 负责管理不同协议的编解码器，提供统一的编解码服务
 *
 * @author muxin
 */
@Slf4j
@Component
public class DefaultMessageCodecManager implements MessageCodecManager {

    // ========== 编解码器注册表 ==========
    /**
     * 协议到编解码器的映射
     * Key: Protocol对象，Value: MessageCodec实现
     */
    private final ConcurrentHashMap<Protocol, MessageCodec> codecRegistry;



    // ========== 状态管理 ==========
    private volatile boolean initialized = false;
    private volatile boolean running = false;

    public DefaultMessageCodecManager() {
        this.codecRegistry = new ConcurrentHashMap<>();
        
        log.info("[DefaultMessageCodecManager] 消息编解码管理器创建完成");
    }

    // ========== Repository 接口实现 ==========

    @Override
    public MessageCodec insert(MessageCodec codec) {
        if (codec == null) {
            throw new IllegalArgumentException("编解码器不能为空");
        }

        // 检查编解码器支持的协议
        Protocol protocol = findSupportedProtocol(codec);
        if (protocol == null) {
            throw new IllegalArgumentException("编解码器必须支持至少一个协议");
        }

        MessageCodec existing = codecRegistry.put(protocol, codec);
        log.info("[DefaultMessageCodecManager] 注册编解码器: {} -> {}", 
                protocol.type(), codec.getClass().getSimpleName());
        
        return existing;
    }

    @Override
    public void deleteById(Protocol protocol) {
        if (protocol == null) {
            log.warn("[DefaultMessageCodecManager] 尝试删除null协议的编解码器");
            return;
        }

        MessageCodec removed = codecRegistry.remove(protocol);
        if (removed != null) {
            log.info("[DefaultMessageCodecManager] 移除编解码器: {} -> {}", 
                    protocol.type(), removed.getClass().getSimpleName());
        } else {
            log.warn("[DefaultMessageCodecManager] 未找到协议的编解码器: {}", protocol.type());
        }
    }

    @Override
    public MessageCodec selectById(Protocol protocol) {
        if (protocol == null) {
            return null;
        }
        
        MessageCodec codec = codecRegistry.get(protocol);
        if (codec == null) {
            log.debug("[DefaultMessageCodecManager] 未找到协议的编解码器: {}", protocol.type());
        }
        
        return codec;
    }

    @Override
    public Collection<MessageCodec> selectAll() {
        return codecRegistry.values();
    }

    // ========== MessageCodecManager 特有方法 ==========

    @Override
    public boolean supports(Protocol sourceProtocol) {
        if (sourceProtocol == null) {
            return false;
        }

        // 检查是否有直接匹配的编解码器
        if (codecRegistry.containsKey(sourceProtocol)) {
            return true;
        }

        // 检查是否有支持该协议的编解码器
        return codecRegistry.values().stream()
                .anyMatch(codec -> codec.supports(sourceProtocol));
    }

    // ========== LifeCycle 接口实现 ==========

    @Override
    public void init() {
        if (initialized) {
            log.warn("[DefaultMessageCodecManager] 消息编解码管理器已初始化");
            return;
        }

        log.info("[DefaultMessageCodecManager] 开始初始化消息编解码管理器");

        try {
            // 注册默认的 HTTP 编解码器
            registerDefaultCodecs();
            
            initialized = true;
            log.info("[DefaultMessageCodecManager] 消息编解码管理器初始化完成，注册编解码器数量: {}", 
                    codecRegistry.size());
            
        } catch (Exception e) {
            log.error("[DefaultMessageCodecManager] 消息编解码管理器初始化失败", e);
            throw new RuntimeException("消息编解码管理器初始化失败", e);
        }
    }

    @Override
    public void start() {
        if (!initialized) {
            init();
        }

        if (running) {
            log.warn("[DefaultMessageCodecManager] 消息编解码管理器已启动");
            return;
        }

        log.info("[DefaultMessageCodecManager] 启动消息编解码管理器");
        
        // 验证所有编解码器状态
        validateCodecs();
        
        running = true;
        log.info("[DefaultMessageCodecManager] 消息编解码管理器启动完成，可用编解码器: {}", 
                codecRegistry.size());
    }

    @Override
    public void shutdown() {
        if (!running) {
            log.warn("[DefaultMessageCodecManager] 消息编解码管理器未启动或已关闭");
            return;
        }

        log.info("[DefaultMessageCodecManager] 开始关闭消息编解码管理器");

        try {
            // 清理编解码器
            codecRegistry.clear();
            
            running = false;
            log.info("[DefaultMessageCodecManager] 消息编解码管理器关闭完成");
            
        } catch (Exception e) {
            log.error("[DefaultMessageCodecManager] 消息编解码管理器关闭异常", e);
        }
    }

    // ========== 内部辅助方法 ==========

    /**
     * 注册默认编解码器
     */
    private void registerDefaultCodecs() {
        try {
            // 注册 HTTP 编解码器
            HttpMessageCodec httpCodec = new HttpMessageCodec();
            insert(httpCodec);
            
            log.info("[DefaultMessageCodecManager] 默认编解码器注册完成");
            
        } catch (Exception e) {
            log.error("[DefaultMessageCodecManager] 注册默认编解码器失败", e);
            throw e;
        }
    }

    /**
     * 查找编解码器支持的协议
     */
    private Protocol findSupportedProtocol(MessageCodec codec) {
        // 尝试常见协议
        Protocol[] commonProtocols = {ProtocolEnum.HTTP, ProtocolEnum.LB};
        
        for (Protocol protocol : commonProtocols) {
            if (codec.supports(protocol)) {
                return protocol;
            }
        }
        
        return null;
    }

    /**
     * 验证编解码器状态
     */
    private void validateCodecs() {
        int validCount = 0;
        int invalidCount = 0;

        for (var entry : codecRegistry.entrySet()) {
            Protocol protocol = entry.getKey();
            MessageCodec codec = entry.getValue();
            
            try {
                // 检查编解码器是否仍然支持该协议
                if (codec.supports(protocol)) {
                    validCount++;
                    log.debug("[DefaultMessageCodecManager] 编解码器有效: {} -> {}", 
                            protocol.type(), codec.getClass().getSimpleName());
                } else {
                    invalidCount++;
                    log.warn("[DefaultMessageCodecManager] 编解码器无效: {} -> {}", 
                            protocol.type(), codec.getClass().getSimpleName());
                }
            } catch (Exception e) {
                invalidCount++;
                log.error("[DefaultMessageCodecManager] 编解码器验证异常: {} -> {}", 
                        protocol.type(), codec.getClass().getSimpleName(), e);
            }
        }

        log.info("[DefaultMessageCodecManager] 编解码器验证完成，有效: {}, 无效: {}", validCount, invalidCount);
    }



    // ========== 状态查询方法 ==========

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isRunning() {
        return running;
    }

    public int getCodecCount() {
        return codecRegistry.size();
    }

    @Override
    public String toString() {
        return String.format("DefaultMessageCodecManager{initialized=%s, running=%s, codecs=%d}", 
                initialized, running, codecRegistry.size());
    }
} 