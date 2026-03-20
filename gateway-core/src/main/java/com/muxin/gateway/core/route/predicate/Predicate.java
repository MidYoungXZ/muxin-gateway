package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.HttpServerExchange;

import java.util.Map;

/**
 * HTTP断言接口
 * 简化版本：只支持HTTP协议
 *
 * @author muxin
 * @version 2.0.0
 * @since 1.0.0
 */
public interface Predicate {

    boolean test(HttpServerExchange exchange);

    String getType();

    String getName();

    Map<String, Object> getConfig();

    default Predicate and(Predicate other) {
        Predicate self = this;
        return new Predicate() {
            @Override
            public boolean test(HttpServerExchange exchange) {
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
            public Map<String, Object> getConfig() {
                return self.getConfig();
            }
        };
    }

    default Predicate or(Predicate other) {
        Predicate self = this;
        return new Predicate() {
            @Override
            public boolean test(HttpServerExchange exchange) {
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
            public Map<String, Object> getConfig() {
                return self.getConfig();
            }
        };
    }

    default Predicate negate() {
        Predicate self = this;
        return new Predicate() {
            @Override
            public boolean test(HttpServerExchange exchange) {
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
            public Map<String, Object> getConfig() {
                return self.getConfig();
            }
        };
    }
}
