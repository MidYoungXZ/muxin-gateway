package com.muxin.gateway.core.plus.route.predicate;

import com.muxin.gateway.core.plus.message.Message;
import com.muxin.gateway.core.plus.message.Protocol;
import com.muxin.gateway.core.plus.message.ServerExchange;

import java.util.Map;
import java.util.Set;

/**
 * 通用断言接口 - 支持多协议
 *
 * @author muxin
 */
public interface Predicate {

    /**
     * 测试请求是否匹配
     */
    boolean test(ServerExchange<? extends Message, ? extends Message> exchange);

    /**
     * 断言类型
     */
    String getType();

    /**
     * 断言名称
     */
    String getName();

    /**
     * 支持的协议类型
     */
    Set<Protocol> getSupportedProtocols();

    /**
     * 断言配置
     */
    Map<String, Object> getConfig();

    /**
     * AND组合
     */
    default Predicate and(Predicate other) {
        Predicate self = this;
        return new Predicate() {
            @Override
            public boolean test(ServerExchange<? extends Message, ? extends Message> exchange) {
                return self.test(exchange) && other.test(exchange);
            }

            @Override
            public String getType() {
                return "AND";
            }

            @Override
            public String getName() {
                return self.getName() + " AND " + other.getName();
            }

            @Override
            public Set<Protocol> getSupportedProtocols() {
                return self.getSupportedProtocols();
            }

            @Override
            public Map<String, Object> getConfig() {
                return self.getConfig();
            }
        };
    }

    /**
     * OR组合
     */
    default Predicate or(Predicate other) {
        Predicate self = this;
        return new Predicate() {
            @Override
            public boolean test(ServerExchange<? extends Message, ? extends Message> exchange) {
                return self.test(exchange) || other.test(exchange);
            }

            @Override
            public String getType() {
                return "OR";
            }

            @Override
            public String getName() {
                return self.getName() + " OR " + other.getName();
            }

            @Override
            public Set<Protocol> getSupportedProtocols() {
                return self.getSupportedProtocols();
            }

            @Override
            public Map<String, Object> getConfig() {
                return self.getConfig();
            }
        };
    }

    /**
     * NOT取反
     */
    default Predicate negate() {
        Predicate self = this;
        return new Predicate() {
            @Override
            public boolean test(ServerExchange<? extends Message, ? extends Message> exchange) {
                return !self.test(exchange);
            }

            @Override
            public String getType() {
                return "NOT";
            }

            @Override
            public String getName() {
                return "NOT " + self.getName();
            }

            @Override
            public Set<Protocol> getSupportedProtocols() {
                return self.getSupportedProtocols();
            }

            @Override
            public Map<String, Object> getConfig() {
                return self.getConfig();
            }
        };
    }
} 