package com.muxin.gateway.core.plus.route;

import com.muxin.gateway.core.plus.route.filter.Filter;
import com.muxin.gateway.core.plus.route.filter.FilterType;
import com.muxin.gateway.core.plus.route.loadbalance.LoadBalanceStrategy;
import com.muxin.gateway.core.plus.route.predicate.PathPredicate;
import com.muxin.gateway.core.plus.route.predicate.Predicate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
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

    private final List<Filter> preFilters;
    private final List<Filter> postFilters;
    private final PathPredicate pathPredicate;
    private final int stripPrefixCount;

    private DefaultRoute(String id, String name, String description, int order, boolean enabled,
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

        this.preFilters = initFilters(FilterType.PRE);
        this.postFilters = initFilters(FilterType.POST);
        this.pathPredicate = findPathPredicate();
        this.stripPrefixCount = this.pathPredicate != null ? this.pathPredicate.getStripPrefixCount() : 0;

        log.debug("创建路由: {} (策略: {}, preFilters: {}, postFilters: {})",
                id, loadBalanceStrategy.getStrategyName(), preFilters.size(), postFilters.size());
    }

    public static Builder builder() {
        return new Builder();
    }

    private List<Filter> initFilters(FilterType type) {
        return filters.stream()
                .filter(f -> f.getType() == type && f.isEnabled())
                .sorted(Comparator.comparingInt(Filter::getOrder))
                .toList();
    }

    private PathPredicate findPathPredicate() {
        for (Predicate p : predicates) {
            if (p instanceof PathPredicate) {
                return (PathPredicate) p;
            }
        }
        return null;
    }

    public boolean hasStripPrefix() {
        return stripPrefixCount > 0;
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

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private int order;
        private boolean enabled;
        private List<Predicate> predicates;
        private List<Filter> filters;
        private RouteService service;
        private LoadBalanceStrategy loadBalanceStrategy;
        private Map<String, Object> metadata;
        private TimeoutConfig timeoutConfig;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder predicates(List<Predicate> predicates) {
            this.predicates = predicates;
            return this;
        }

        public Builder filters(List<Filter> filters) {
            this.filters = filters;
            return this;
        }

        public Builder service(RouteService service) {
            this.service = service;
            return this;
        }

        public Builder loadBalanceStrategy(LoadBalanceStrategy loadBalanceStrategy) {
            this.loadBalanceStrategy = loadBalanceStrategy;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder timeoutConfig(TimeoutConfig timeoutConfig) {
            this.timeoutConfig = timeoutConfig;
            return this;
        }

        public DefaultRoute build() {
            return new DefaultRoute(id, name, description, order, enabled,
                    predicates, filters, service, loadBalanceStrategy, metadata, timeoutConfig);
        }
    }
}