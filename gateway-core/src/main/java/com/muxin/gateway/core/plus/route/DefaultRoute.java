package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.predicate.Predicate;
import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import lombok.extern.slf4j.Slf4j;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Data
@Builder
public class DefaultRoute implements Route {

    private final String id;
    private final String name;
    private final String description;
    private final int order;
    private final boolean enabled;
    private final List<Predicate> predicates;
    private final List<Filter> filters;
    private final RouteService service;
    private final LoadBalanceStrategy loadBalanceStrategy;
    private final Map<String, Object> metadata;
    private final TimeoutConfig timeoutConfig;

    public DefaultRoute(String id, String name, String description, int order, boolean enabled,
                        List<Predicate> predicates, List<Filter> filters,
                        RouteService service, LoadBalanceStrategy loadBalanceStrategy,
                        Map<String, Object> metadata, TimeoutConfig timeoutConfig) {
        this.id = Objects.requireNonNull(id, "路由ID不能为空");
        this.name = Objects.requireNonNull(name, "路由名称不能为空");
        this.description = description;
        this.order = order;
        this.enabled = enabled;
        this.predicates = predicates != null ? predicates : Collections.emptyList();
        this.filters = filters != null ? filters : Collections.emptyList();
        this.service = Objects.requireNonNull(service, "目标服务不能为空");
        this.loadBalanceStrategy = Objects.requireNonNull(loadBalanceStrategy, "负载均衡策略不能为空");
        this.metadata = metadata != null ? metadata : Collections.emptyMap();
        this.timeoutConfig = timeoutConfig;

        log.debug("创建路由: {} (策略: {})", id, loadBalanceStrategy.getStrategyName());
    }

    @Override
    public void validate() {
        Objects.requireNonNull(id, "路由ID不能为空");
        Objects.requireNonNull(name, "路由名称不能为空");
        Objects.requireNonNull(service, "目标服务不能为空");
        Objects.requireNonNull(loadBalanceStrategy, "负载均衡策略不能为空");
        if (predicates == null || predicates.isEmpty()) {
            throw new IllegalArgumentException("断言列表不能为空");
        }
    }

    @Override
    public boolean matches(RequestContext context) {
        if (!enabled) {
            return false;
        }
        if (context == null || context.exchange() == null) {
            return false;
        }
        for (Predicate predicate : predicates) {
            if (!predicate.test(context.exchange())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Long getConnectionTimeout() {
        return timeoutConfig != null ? timeoutConfig.getConnection() : TimeoutConfig.DEFAULT_CONNECTION;
    }

    @Override
    public Long getRequestTimeout() {
        return timeoutConfig != null ? timeoutConfig.getRequest() : TimeoutConfig.DEFAULT_REQUEST;
    }

    @Override
    public Long getTotalTimeout() {
        return timeoutConfig != null ? timeoutConfig.getTotal() : TimeoutConfig.DEFAULT_TOTAL;
    }

    @Override
    public Long getReadTimeout() {
        return timeoutConfig != null ? timeoutConfig.getRead() : TimeoutConfig.DEFAULT_READ;
    }

    @Override
    public Long getWriteTimeout() {
        return timeoutConfig != null ? timeoutConfig.getWrite() : TimeoutConfig.DEFAULT_WRITE;
    }

    public boolean isValid() {
        return id != null && !id.trim().isEmpty()
                && service != null
                && loadBalanceStrategy != null
                && !predicates.isEmpty();
    }
}
