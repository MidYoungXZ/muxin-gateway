package com.muxin.gateway.refactory.connect;

import com.muxin.gateway.refactory.Protocol;
import com.muxin.gateway.refactory.message.Message;
import com.muxin.gateway.refactory.node.EndpointAddress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * HTTP连接实现
 *
 * @author muxin
 */
public class HttpConnection implements Connection {

    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private final String connectionId;
    private final Protocol protocol;
    private final EndpointAddress localAddress;
    private final EndpointAddress remoteAddress;
    private final Map<String, Object> attributes;
    private final CopyOnWriteArrayList<ConnectionListener> listeners;

    private volatile ConnectionStatus status;
    private volatile long lastActiveTime;

    public HttpConnection(Protocol protocol, EndpointAddress localAddress, EndpointAddress remoteAddress) {
        this.connectionId = "http-conn-" + ID_GENERATOR.incrementAndGet();
        this.protocol = protocol;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.attributes = new ConcurrentHashMap<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.status = ConnectionStatus.CONNECTING;
        this.lastActiveTime = System.currentTimeMillis();

        // 模拟连接建立
        this.status = ConnectionStatus.CONNECTED;
    }

    @Override
    public String getConnectionId() {
        return connectionId;
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public EndpointAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public EndpointAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public ConnectionStatus getStatus() {
        return status;
    }

    @Override
    public CompletableFuture<Message> send(Message message) {
        updateLastActiveTime();

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟HTTP请求发送
                System.out.println("[HTTP_CONNECTION] 发送消息: " + message.getMessageId() +
                    " 到 " + remoteAddress.toUri());

                // 通知监听器
                notifyListeners(listener -> listener.onMessageSent(this, message));

                // 模拟响应
                return message.createResponse();
            } catch (Exception e) {
                setStatus(ConnectionStatus.ERROR);
                throw new RuntimeException("发送消息失败", e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            setStatus(ConnectionStatus.DISCONNECTING);

            try {
                // 执行连接关闭逻辑
                System.out.println("[HTTP_CONNECTION] 关闭连接: " + connectionId);

                // 通知监听器
                notifyListeners(listener -> listener.onConnectionClosed(this));

                setStatus(ConnectionStatus.DISCONNECTED);
            } catch (Exception e) {
                setStatus(ConnectionStatus.ERROR);
                System.err.println("关闭连接时发生错误: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean isActive() {
        return status == ConnectionStatus.CONNECTED;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return new ConcurrentHashMap<>(attributes);
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (key != null) {
            if (value != null) {
                attributes.put(key, value);
            } else {
                attributes.remove(key);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }

        if (type.isInstance(value)) {
            return (T) value;
        }

        throw new ClassCastException("无法将属性 " + key + " 转换为类型 " + type.getName());
    }

    /**
     * 设置连接状态
     */
    private void setStatus(ConnectionStatus newStatus) {
        ConnectionStatus oldStatus = this.status;
        this.status = newStatus;

        if (oldStatus != newStatus) {
            notifyListeners(listener -> listener.onStatusChanged(this, oldStatus, newStatus));
        }
    }

    /**
     * 更新最后活跃时间
     */
    private void updateLastActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 获取最后活跃时间
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    // Repository 接口实现
    @Override
    public ConnectionListener save(ConnectionListener entity) {
        if (entity != null) {
            listeners.add(entity);
            return entity;
        }
        return null;
    }

    @Override
    public void removeByUniqueCode(String uniqueCode) {
        listeners.removeIf(listener -> uniqueCode.equals(listener.id()));
    }

    @Override
    public ConnectionListener findByUniqueCode(String uniqueCode) {
        return listeners.stream()
                .filter(listener -> uniqueCode.equals(listener.id()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ConnectionListener> findAll() {
        return List.copyOf(listeners);
    }

    @Override
    public String toString() {
        return "HttpConnection{" +
                "connectionId='" + connectionId + '\'' +
                ", protocol=" + protocol +
                ", localAddress=" + localAddress +
                ", remoteAddress=" + remoteAddress +
                ", status=" + status +
                ", lastActiveTime=" + lastActiveTime +
                '}';
    }
}